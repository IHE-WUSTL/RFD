package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.List;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;

/**
 * Helper class designed to help automate a number of actions related to java
 * beans, especially those used to model SQL DB tables, including:
 * <ul>
 * <li>Implementation of inner {@link java.util.Comparator Comparator} classes
 * used to sort arrays of bean instances for processing, for example to sort
 * tables in web interfaces.</li>
 * <li>Simplification of process of plugging java properties into SQL statements
 * using the ${parameterName} construct.</li>
 * <li>automate retrieval of properties from {@link java.sql.ResultSet
 * ResultSets}.</li>
 * </ul>
 * <b>Using DBHelper:</b>
 * <p>
 * (The sample usage shown here is taken from WSLog.java.)
 * </p>
 * <p>
 * A static instance of DBHelper for the type of the bean is inserted in the
 * bean as a property:
 * </p>
 * 
 * <pre>
 * private static DBHelper &lt;WSLog&gt; helper = new DBHelper &lt;&gt;();
 * </pre>
 * 
 * DBHelper will scan the bean on class load, and generate a table of properties
 * which use standard java getters and setters (including the isProperty for for
 * booleans). Properties whose return type implements the
 * {@link java.lang.Comparable Comparable} interface, will be considered as
 * Comparable properties. The {@link edu.wustl.mir.erl.ihe.util.jdbc.Helper
 * Helper} annotation can be used to modify how properties are processed.
 * <p>
 * Once the static DBHelper instance has been added to the java bean, along with
 * any appropriate Helper annotations, the following features are available:
 * </p>
 * <p>
 * <b>A simple inner comparable class can be added to the java bean to allow
 * sorting of arrays of bean instances on any comparable property:</b>
 * </p>
 * 
 * <pre>
 * // ********************************************************
 * // Comparator inner class
 * // ********************************************************
 * public static class Comp implements Comparator &lt;WSLog&gt; {
 * 
 *    private String property;
 *    private boolean ascending;
 * 
 *    public Comp(String property, boolean ascending) {
 *       this.property = property;
 *       this.ascending = ascending;
 *    }
 * 
 *    &#064;Override
 *    public int compare(WSLog one, WSLog two) {
 *       if (StringUtils.isBlank(property)) return 0;
 *       return helper.compare(one, two, property, ascending);
 *    }
 * } // EO Comparator inner class
 * </pre>
 * 
 * <b>Convenience methods in {@link Query} can be used to simplify and reduce
 * the possibility of coding errors when coding database queries:</b>
 * <ul>
 * <li>{@link Query#setAll(DBTable)}</li>
 * </ul>
 * <b>Convenience methods in DBHelper can be used to simplify and reduce the
 * possibility of coding errors when loading the values of
 * {@link java.sql.ResultSet ResultSet} instances into instances of database
 * java beans:</b>
 * <ul>
 * <li>{@link #loadNextRow(ResultSet)}</li>
 * <li>{@link #loadRows(ResultSet)}</li>
 * </ul>
 * 
 * @param <T> Type of the database class for this instance.
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class DBHelper <T> implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   private static String thisClassName = DBHelper.class.getCanonicalName();
   private static String thisPackageName = thisClassName.substring(0,
      thisClassName.lastIndexOf("."));

   private transient Logger log = Util.getLog();
   private Class <?> cls;
   private String clsName;

   private ResultSetHandler <T> nextRowHandler = null;
   private ResultSetHandler <List <T>> allHandler = null;

   /**
    * map of bean property names and Property objects;
    */
   private SortedMap <String, Property> properties = new TreeMap <>();

   /**
    * Creates an instance of DBHelper for passed database class type, which
    * should be placed as a static property in the class. For example:
    * 
    * <pre>
    * private static DBHelper &lt;WSLog&gt; helper = new DBHelper &lt;&gt;();
    * </pre>
    * 
    * @param t type of java bean database class helped for this instance.
    */
   public DBHelper(Class <T> t) {

      nextRowHandler = new BeanHandler <T>(t);
      allHandler = new BeanListHandler <T>(t);

      try {
         cls = t;
         clsName = t.getName();
         // scan methods looking for getters
         Method[] methods = cls.getMethods();
         for (Method method : methods) {
            String name = method.getName();

            Helper annotation = method.getAnnotation(Helper.class);
            String annotationPropertyName = "";
            String annotationCompareUsing = "";
            boolean ignoreComp = false;
            boolean ignoreResultSet = false;
            boolean ignoreSQL = false;
            if (annotation != null) {
               annotationPropertyName = annotation.propertyName();
               annotationCompareUsing = annotation.compareUsing();
               ignoreComp = annotation.ignoreComp();
               ignoreResultSet = annotation.ignoreResultSet();
               ignoreSQL = annotation.ignoreSQL();
            }

            // get return type
            Class <?> returnType = method.getReturnType();

            // get parameter types
            Class <?>[] parameters = method.getParameterTypes();

            TYPE methodNameFormatType = TYPE.NEITHER;

            // --------------------------- Find property name (if we can)
            String propertyName = null;
            do {
               // Property name is from @Compare propertyName value
               if (annotation != null && annotation.propertyName().length() > 0) {
                  propertyName = annotation.propertyName();
                  break;
               }

               // Property name from getPropertyName
               if (name.matches("^get[A-Z].*")) {
                  propertyName =
                     name.substring(3, 4).toLowerCase() + name.substring(4);
                  methodNameFormatType = TYPE.GETTER;
                  break;
               }

               // Property name from isPropertyName
               if (name.matches("^is[A-Z].*")
                  && (returnType.equals(boolean.class) || returnType
                     .equals(Boolean.class))) {
                  propertyName =
                     name.substring(2, 3).toLowerCase() + name.substring(3);
                  methodNameFormatType = TYPE.GETTER;
                  break;
               }

               if (name.matches("^set[A-Z].*")) {
                  propertyName =
                     name.substring(3, 4).toLowerCase() + name.substring(4);
                  methodNameFormatType = TYPE.SETTER;
                  break;
               }
            } while (false);
            // No valid property name means this is not a property.
            if (propertyName == null) continue;

            // Is this a valid getter or setter?
            TYPE methodType = TYPE.NEITHER;
            switch (methodNameFormatType) {
               case NEITHER:
                  if (!returnType.equals(Void.TYPE) && parameters.length == 0)
                     methodType = TYPE.GETTER;
                  if (returnType.equals(Void.TYPE) && parameters.length == 1)
                     methodType = TYPE.SETTER;
                  break;
               case GETTER:
                  if (!returnType.equals(Void.TYPE) && parameters.length == 0)
                     methodType = TYPE.GETTER;
                  break;
               case SETTER:
                  if (returnType.equals(Void.TYPE) && parameters.length == 1)
                     methodType = TYPE.SETTER;
                  break;
               default:
                  Util.exit("Unknown instance of DBHelper.Type encountered");
            }
            // ----------- not a valid method type
            if (methodType == TYPE.NEITHER) continue;
            // -------------------------- skip getters which are ignored
            if (methodType == TYPE.GETTER && ignoreComp && ignoreResultSet)
               continue;
            // -------------------- skip setters which are ignored
            if (methodType == TYPE.SETTER && ignoreSQL) continue;

            if (!annotationPropertyName.isEmpty())
               propertyName = annotationPropertyName;

            // ----------------------------- Load or create Property
            String propertyname = propertyName.toLowerCase();
            Property property = null;
            if (properties.containsKey(propertyname)) property =
               properties.get(propertyname);
            else {
               property = new Property();
               property.name = propertyName;
            }

            /*
             * Helper requires that distinct property name must not compare
             * equal case insensitive.
             */
            if (!propertyName.equals(property.name)
               && propertyName.equalsIgnoreCase(property.name)) { throw new Exception(
               "property name: " + propertyName + " Ignore case it duplicates "
                  + property.name); }

            if (ignoreComp) property.ignoreComp = ignoreComp;
            if (ignoreResultSet) property.ignoreResultSet = ignoreResultSet;
            if (ignoreSQL) property.ignoreSQL = ignoreSQL;

            if (methodType == TYPE.GETTER) {
               if (property.getter != null) { throw new Exception(
                  "Duplicate getter for property " + propertyName); }
               property.getter = method;
               property.type = returnType;
            } else {
               if (property.setter != null) { throw new Exception(
                  "Duplicate setter for property " + propertyName); }
               property.type = parameters[0];
               property.setter = method;
               continue;
            }

            // ------ Find comparator method (if we can)
            Method compareMethod = null;
            // This loop looks for method in @Helper annotation, puts it in
            // compareMethod if found
            do {
               // --------- No @Helper or no compareUsing attribute
               if (annotationCompareUsing.isEmpty()) break;
               String cuName = annotationCompareUsing;

               /*
                * Get name of compareUsing method from name in annotation. The
                * name format must be: methodName, in which case the method is
                * assumed to be in this class. #methodName, in which case the
                * method is assumed to be in the Util class in this package.
                * ClassName#methodName, in which case the class is assumed to be
                * in this package. CanonicalClassName#method name.
                * 
                * The method must have the signature int methodName(Type, Type),
                * where the parameters are the type of the property. The method
                * must compare its two arguments for order, Returning a negative
                * integer, zero, or a positive integer as the first argument is
                * less than, equal to, or greater than the second. Invalid
                * formats will be logged as fatal errors.
                */
               if (!cuName.contains("#")) cuName = thisClassName + "#" + cuName;
               else if (cuName.startsWith("#")) cuName =
                  thisPackageName + ".Util" + cuName;
               else if (!cuName.contains("."))
                  cuName = thisPackageName + "." + cuName;

               // split into Canonical class name and method name.
               String[] part = cuName.split("[#]");
               try {
                  Class <?> cuCls = Class.forName(part[0]);
                  Method cuMethod =
                     cuCls.getMethod(part[1], returnType, returnType);
                  Class <?> ret = cuMethod.getReturnType();
                  if (!ret.equals(int.class))
                     throw new Exception("not int return type "
                        + ret.getSimpleName());
                  compareMethod = cuMethod;
               } catch (Exception e) {
                  String em =
                     "Invalid compareUsing method in Helper annotation:" + nl
                        + "   method name " + part[1] + nl + "   in class    "
                        + cls.getCanonicalName() + nl + "   on method   "
                        + name + nl + "   error:      " + e.getMessage();
                  log.error(em);
                  Util.exit(1);
               }
            } while (false);
            /*
             * If compareMethod is null, the property must implement Comparable,
             * or be a primitive type, or it will be ignored for Comparisons.
             */
            property.compareMethod = compareMethod;

            properties.put(propertyname, property);

         } // EO scan methods

         Logger elog = Logger.getLogger(ENV_LOG_NAME);
         if (elog.isInfoEnabled()) {
            StringBuffer str =
               new StringBuffer("DBCompare setup complete for "
                  + cls.getSimpleName() + nl + nl + "Property list:");
            Collection <Property> props = properties.values();
            for (Property p : props) {
               str.append(nl).append("  ").append(p.toString());
            }
            elog.info(str);
         }
      } catch (Exception e) {
         log.warn("DBLink constructor error: " + e.getMessage());
         Util.exit(1);
      }
   }

   /**
    * Standard compare method for use in Comparators in database beans using
    * DBLink.
    * 
    * @param one first object of this bean class
    * @param two second object of this bean class
    * @param propertyName camelCase property name to be compared.
    * @param ascending boolean <b>true</b> for ascending order, <b>false</b> for
    * descending
    * @return int Returns a negative integer, zero, or a positive integer as the
    * first argument is less than, equal to, or greater than the second, in
    * cases where ascending is <b>true</b>. Reverse if ascending is
    * <b>false</b>.
    */
   public int compare(Object one, Object two, String propertyName,
      boolean ascending) {
      /*
       * MOD to use DBLink with other types, you may need to expand the
       * comparability methods. This basic routine handles all types which
       * implement the {@link java.lang.Comparable} interface, a couple of types
       * which have relevant toString() methods, and one special compare method
       * for IP addresses. The primitive types are handled by autoboxing and
       * unboxing. If you wish to add other types to this list, you can:<ul>
       * <li> Implement the Comparable interface in your type (if it is
       * yours).</li> <li> Add the type to the switch below along with the other
       * types which have relevant, that is, orderable, toString() methods.</li>
       * <li> Add a new case section to the switch with appropriate code.</li>
       * <li> Make a new compare method like {@link Util#compareIp} and
       * reference it by name using a {@Link} annotation.</li></ul>
       */
      try {
         // ------------------------- Validate bean objects
         if (one == null) throw new Exception("first object is null");
         if (!one.getClass().equals(cls))
            throw new Exception("first object not " + clsName);
         if (two == null) throw new Exception("second object is null");
         if (!two.getClass().equals(cls))
            throw new Exception("second object not " + clsName);
         // --------------------- Pull property information
         if (!properties.containsKey(propertyName.toLowerCase()))
            throw new Exception(clsName + " has no property " + propertyName);
         Property p = properties.get(propertyName.toLowerCase());
         if (p.getter == null) throw new Exception(" property has no getter");
         // --------------- load properties values for bean objects
         Object pone = p.getter.invoke(one);
         Object ptwo = p.getter.invoke(two);
         if (!ascending) {
            Object t = one;
            one = two;
            two = t;
         }
         // ------------------- using override compare method
         if (p.compareMethod != null) { return (int) p.compareMethod.invoke(
            null, pone, ptwo); }
         // ------------------ types with Comparable interface
         try {
            if (Comparable.class.isAssignableFrom(p.getClass())) {
               Method com = p.type.getMethod("compareTo", p.type);
               return (int) com.invoke(pone, ptwo);
            }
         } catch (Exception ee) {};
         // ----------------------- other types
         String typeName = p.type.getName();
         switch (typeName) {
         // Primitive types
            case "byte":
               return Byte.compare((byte) one, (byte) two);
            case "short":
               return Short.compare((short) one, (short) two);
            case "long":
               return Long.compare((long) one, (long) two);
            case "float":
               return Float.compare((float) one, (float) two);
            case "double":
               return Double.compare((double) one, (double) two);
            case "char":
               return Character.compare((char) one, (char) two);
            case "boolean":
               return Boolean.compare((boolean) one, (boolean) two);
            case "int":
               return Integer.compare((int) one, (int) two);
               // MOD add other types with relevant toString() methods here.
            case "java.lang.StringBuffer":
            case "org.apache.commons.lang3.text.StrBuilder":
               return one.toString().compareTo(two.toString());
            default:
               throw new Exception("no compare method for type " + typeName);
         }

      } catch (Exception e) {
         log.error("DBLink compare method error: " + e.getMessage());
         Util.exit(1);
      }
      return 0;
   }

   /**
    * Simple class used to contain data items for a single property of the Class
    * passed in the constructor.
    * 
    * @see DBHelper#compare(Object, Object, String, boolean)
    */
   public class Property {
      /** camelCase name of property */
      private String name = null;
      /** getter method for property ("is" method for boolean) */
      private Method getter = null;
      /** setter method for property. */
      private Method setter = null;
      /** type of property (based on return type of getter) */
      private Class <?> type = null;
      /** compare method (if null, use standard for type */
      private Method compareMethod = null;
      /** ignore this property in compare processing. */
      private boolean ignoreComp = false;
      /** ignore this property in ResultSet processing. */
      private boolean ignoreResultSet = false;
      /** ignore this property in SQL processing */
      private boolean ignoreSQL = false;

      @Override
      public String toString() {
         StringBuilder str =
            new StringBuilder(name
               + " getter="
               + (getter == null ? "none" : StringUtils.trimToEmpty(getter
                  .getName()))
               + " setter="
               + (setter == null ? "none" : StringUtils.trimToEmpty(setter
                  .getName())) + " type=" + type.getSimpleName()
               + " compare with="
               + (compareMethod == null ? "default" : compareMethod.getName()));
         boolean firstIgnore = true;
         if (ignoreComp) {
            if (firstIgnore) {
               str.append(" ignore for ");
               firstIgnore = false;
            } else str.append(", ");
            str.append("Compare");
         }
         if (ignoreSQL) {
            if (firstIgnore) {
               str.append(" ignore for ");
               firstIgnore = false;
            } else str.append(", ");
            str.append("SQL");
         }
         if (ignoreResultSet) {
            if (firstIgnore) {
               str.append(" ignore for ");
               firstIgnore = false;
            } else str.append(", ");
            str.append("ResultSets");
         }
         return str.toString();
      }

      /**
       * @return {@link #name}
       */
      public String getName() {
         return name;
      }

      /**
       * @return {@link #getter} method.
       */
      public Method getGetter() {
         return getter;
      }

      /**
       * @return {@link #getSetter()} method
       */
      public Method getSetter() {
         return setter;
      }

      /**
       * @return {@link #type} of property
       */
      public Class <?> getType() {
         return type;
      }

      /**
       * @return {@link #compareMethod} for property, or null to use standard
       * method for this type.
       */
      public Method getCompareMethod() {
         return compareMethod;
      }

      /**
       * @return {@link #ignoreComp}
       */
      public boolean isIgnoreComp() {
         return ignoreComp;
      }

      /**
       * @return {@link #ignoreResultSet}
       */
      public boolean isIgnoreResultSet() {
         return ignoreResultSet;
      }

      /**
       * @return {@link #ignoreSQL}
       */
      public boolean isIgnoreSQL() {
         return ignoreSQL;
      }
   } // EO Class Property

   /**
    * @param propertyName the name of a property in the database class
    * @return {@link Property} instance for this property, or <code>null</code>
    * if the database class has no property with than name.
    */
   public Property getProperty(String propertyName) {
      return properties.get(propertyName.toLowerCase());
   }

   /**
    * Generates a new instance of T, loading all properties which have matching
    * columns in the next row of the passed ResultSet. If the ResultSet has not
    * been repositioned after the query, this will return an instance of T for
    * the first row of the ResultSet. If this method is invoked repeatedly, it
    * will return instances for successive rows of the ResultSet.
    * 
    * @param resultSet Passed result set
    * @return new instance of T, with matching properties loaded, or null if
    * there is no next row.
    * @throws Exception on SQL error
    */
   public T loadNextRow(ResultSet resultSet) throws Exception {
      return nextRowHandler.handle(resultSet);
   }

   /**
    * Generates a List of new instances of T, one for each row of the passed
    * result set after its current position, loading all properties which have a
    * matching column in the ResultSet. If the ResultSet has not been
    * repositioned after the query, this will return an instance of T for every
    * row of the ResultSet.
    * 
    * @param resultSet Passed result set
    * @return a List of instances of T, empty if there are no rows.
    * @throws Exception on SQL error
    */
   public List <T> loadRows(ResultSet resultSet) throws Exception {
      return allHandler.handle(resultSet);
   }

   /*
    * Special case comparator methods. Must have signature int method(T, T)
    * where T is the java type of the property being compared. Each method
    * should have a public string constant name which can be used in the
    * 
    * @Compare annotation to avoid typing errors.
    */

   /**
    * {@link Helper#compareUsing()} value used to denote properties that are
    * IPV4 addresses in dot notation for comparison purposes. For example:
    * 
    * <pre>
    * &#064;Helper(compareUsing = DBHelper.COMPARE_IP)
    * public String getClientIp() {
    *    return clientIp;
    * }
    * </pre>
    */
   public static final String COMPARE_IP = "compareIp";

   /**
    * Comparator for ipv4 address strings in dot notation.
    * 
    * @param one ipv4 address
    * @param two ipv4 address
    * @return int comparison value.
    */
   public static int compareIp(String one, String two) {
      Long lone, ltwo;
      Scanner s;
      s = new Scanner(one);
      s.useDelimiter("\\.");
      lone =
         (s.nextLong() << 24) + (s.nextLong() << 16) + (s.nextLong() << 8)
            + s.nextLong();
      s.close();
      s = new Scanner(two);
      s.useDelimiter("\\.");
      ltwo =
         (s.nextLong() << 24) + (s.nextLong() << 16) + (s.nextLong() << 8)
            + s.nextLong();
      s.close();
      return lone.compareTo(ltwo);
   }

   private enum TYPE {
      GETTER, SETTER, NEITHER
   }

} // EO DBHelper class
