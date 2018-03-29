package edu.wustl.mir.erl.ihe.util.jdbc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DB Helper annotation. Used along with {@link DBHelper} to automate the
 * implementation of inner {@link java.util.Comparator Comparator} classes,
 * to facilitate plugging java properties into SQL statements using the 
 * ${parameterName} construct, and to automate retrieval of properties from 
 * {@link java.sql.ResultSet ResultSets}.
 * DBHelper uses {@link java.lang.reflect Reflection} to scan the getters of a
 * class and generate a list of properties which can be compared or used in
 * automated plugging, using a set of reasonable assumptions:
 * <ul>
 * <li>Standard getPropertyName / isPropertyName method naming is being used.</li>
 * <li>Valid comparisons can be made by looking at the return type of the
 * getter method and:
 * <ul>
 * <li>Using its compareTo method if it implements comparable.</li>
 * <li>Using boxing and unboxing on primitive types.</li>
 * <li>Comparing the .toString() values on some relevant types.</li>
 * </ul>
 * </li>
 * <li>All methods which meet the above requirements, and only those methods,
 * are to be compared or used in automated plugging.</li>
 * </ul>
 * The Helper annotation can be put on methods in the class to override those 
 * assumptions when needed, in the following ways:
 * <ul>
 * <li><b>@Helper(ignoreComp=true)</b> will tell DBHelper to ignore the method
 * for comparisons, even if it fits the assumptions.</li>
 * <li><b>@Helper(ignoreSQL=true)</b> will cause the method to be ignored
 * during automated plugging.</li>
 * <li><b>@Helper(ignoreResultSet=true)</b> will cause the method to be </li>
 * <li><b>@Helper(propertyName="pName")</b> will tell DBHelper to use the
 * method as a getter for a property "pName", when the standard getter naming
 * convention is not used.</li>
 * <li><b>@Helper(compareUsing="mName")</b> will tell DBHelper to use the 
 * method "mName" to compare the properties. See {@link #compareUsing()} for
 * details.</li>
 * </ul>
 * <b>NOTE:</b> In order to automate transfer of data between Bean Properties
 * and SQL statements ResultSets, the following naming conventions must be
 * followed:
 * <ul>
 * <li>The property names used in the bean must be unique when compared on a
 * case insensitive basis.</li>
 * <li>Java property names and SQL column names must be "matched" in that,
 * when underscores ("_") are removed from the SQL column name, it must match
 * its corresponding property name when compared case insensitively.</li>
 * </ul>
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
@Target (ElementType.METHOD)
@Retention (RetentionPolicy.RUNTIME)
public @interface Helper {

   /**
    * override property name. If absent the standard property name for this
    * property will be used, for example, a method getThisName or isThisName
    * would have a property name of thisName. Duplicate property names in a
    * class will generate a fatal error.
    */
   public String propertyName() default "";

   /**
    * override comparison method. If absent, the standard comparator for this
    * type of property will be used. Only considered on getters which are not
    * ignored. The format of the method string must be:
    * <ul>
    * <li><b>methodName</b>, in which case the method is assumed to be in
    * {@link edu.wustl.mir.erl.ihe.util.jdbc.DBHelper DBCompare}.</li>
    * <li><b>#methodName</b>, in which case the method is assumed to be in
    * {@link edu.wustl.mir.erl.ihe.util.Util Util}.</li>
    * <li><b>ClassName#methodName</b>, in which case the class is assumed to be
    * in {@link edu.wustl.mir.erl.ihe.util.jdbc the jdbc package}.</li>
    * <li><b>CanonicalClassName#method</b> name.</li>
    * </ul>
    * <p>The method must have the signature static int methodName(Type, Type),
    * where the two parameters are the type of the property. The method must
    * compare its two arguments for order. Returns a negative integer, zero, or
    * a positive integer as the first argument is less than, equal to, or
    * greater than the second.</p>
    * <p>For example, @Helpwe(compareUsing="DBCompare.COMPARE_IP") can be used to
    * compare two IPv4 address in String dot notation, using the method
    * {@link DBHelper#compareIp(String, String)}.</p>
    */
   public String compareUsing() default "";

   /**
    * ignore this method for comparison purposes. If true, the property will not
    * be a valid compare option. Default is false.
    */
   public boolean ignoreComp() default false;

   /**
    * ignore this method for SQL mapping purposes. Default is false.
    */
   public boolean ignoreSQL() default false;
   /**
    * ignore this method for ResultSet processing. Default is false.
    */
   public boolean ignoreResultSet() default false;

}
