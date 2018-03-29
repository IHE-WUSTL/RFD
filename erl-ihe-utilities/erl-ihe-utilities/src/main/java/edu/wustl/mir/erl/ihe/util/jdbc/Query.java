package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.text.StrLookup;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection;

/**
 * Utility class, handles SQL queries for applications in a RDBMS type
 * independent manner. General use:
 * <ol>
 * <li>A SQL query, consisting of a SQL statement in a String, or multiple SQL
 * statements in a String[], is passed to a new Query instance using one of its
 * constructors.</li>
 * <li>The query may be modified using the append or add methods.</li>
 * <li>The query may have zero or more substitutable parameters of the form
 * ${parameterName}, in which case values for these parameters are passed to
 * the Query instance using one of the set methods.</li>
 * <li>When the query is complete is is executed using one of the dbQuery or
 * dbUpdate methods.</li></ol>
 * <p><b>Note:</b> Many methods in this class return the Query instance to allow
 * method chaining.</p>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class Query implements Serializable {
   private static final long serialVersionUID = 1L;

   private ArrayList <StringBuilder> sql = new ArrayList <StringBuilder>();
   private String lastCommand = null;
   private ArrayList <String> lastSql = new ArrayList <String>();
   private Map <String, String> vars = new HashMap <String, String>();
   private Map <String, String> nulls = new HashMap <String, String>();

   private static String lstCommand = null;
   private static ArrayList <String> lstSql = new ArrayList <String>();
   private static Logger log = Util.getLog();
   
   /**
    * Sets logger for use by this query instance. By default the SYSTEM log is
    * used.
    * @param logger {@link Logger} to use for this instance of Query
    */
   public void setLogger(Logger logger) {
      log = logger;
   }

   /**
    * Default constructor. Creates Query with single empty SQL Query String
    */
   public Query() {
      sql.add(new StringBuilder(""));
   }

   /**
    * Creates query with single SQL query string using passed query string as
    * starting point.
    * 
    * @param obj - {@link java.lang.Object Object} Starting SQL Query string is
    * the .toString() value of this Object.
    */
   public Query(Object obj) {
      sql.add(new StringBuilder(obj.toString()));
   }

   /**
    * Creates query with one SQL statement made from the {@link #toString()}
    * method of each of the passed objects in the array.
    * 
    * @param objs Object array used to build query.
    */
   public Query(Object[] objs) {
      for (Object o : objs) {
         sql.add(new StringBuilder(o.toString()));
      }
   }

   /**
    * Creates query, loading query string from an "sql" Enum in a data table
    * class. These Enums have a instance method:
    * 
    * <pre>
    *    String[] getQuery()
    * </pre>
    * 
    * which is used to retrieve the query strings. A sample invocation:
    * 
    * <pre>
    * Query query = new Query(WSLog.sql.insert);
    * </pre>
    * 
    * @param e instance of an "sql" Enum.
    * @throws Exception on error, for example, if an Enum which is not a "sql"
    * Enum, that is, does not have the "getQuery" method, is used as the
    * argument.
    */
   public Query(Enum <?> e) throws Exception {
      String[] strs = (String[]) e.getClass().getMethod("getQuery").invoke(e);
      for (String str : strs) {
         sql.add(new StringBuilder(str));
      }
   }

   /**
    * Appends the passed string to the SQL Query string (the first/0 string if
    * this query has multiple query strings.
    * 
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    * Object is appended to the Query.
    * @return a reference to this Query object.
    */
   public Query append(Object obj) {
      sql.get(0).append(obj.toString());
      return this;
   }

   /**
    * Appends the passed string to the SQL Query string indicated by index,
    * numbered 0 - size-1.
    * 
    * @param index number of sql line to append to.
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    * Object is appended to the Query.
    * @return a reference to this Query object.
    * @throws Exception on index out of bounds.
    */
   public Query append(int index, Object obj) throws Exception {
      if (index < 0 || index >= sql.size())
         throw new Exception("append to non-existent Query line " + index);
      sql.get(index).append(obj.toString());
      return this;
   }

   /**
    * Adds the value of the passed object to the query as the next query line.
    * 
    * @param obj {@link java.lang.Object Object} the .toString() value of this
    * Object is added as the next query line
    * @return a reference to this Query object.
    */
   public Query add(Object obj) {
      sql.add(new StringBuilder(obj.toString()));
      return this;
   }

   /**
    * Adds the values of each object in the passed array to the query as the
    * next query lines.
    * 
    * @param objs {@link java.lang.Object Object} array. the .toString() values
    * of these Objects are added as the next query lines
    * @return a reference to this Query object.
    */
   public Query add(Object[] objs) {
      for (Object o : objs)
         sql.add(new StringBuilder(o.toString()));
      return this;
   }

   /**
    * Sets a parameter value in the SQL Query string. Parameters must be of the
    * form ${parameter name}, and are handled by a default
    * {@link org.apache.commons.lang.text.StrSubstitutor StrSubstitutor}.
    * <p><b>Note:</b> Parameter substitutions are not actually processed until the
    * Query is submitted. If the same parameter is set multiple times, the last
    * value will be the effective one.
    * <p><b>Note:</b> Handles null values on inserts. If the parameter is of the
    * form '${parameter name}' in the SQL (indicating a string value), set will
    * replace the entire expression, including the quotes, with NULL.</p>
    * 
    * @param key {@link java.lang.Object Object} the .toString() value of this
    * Object is the parameter name.
    * @param value {@link java.lang.Object Object} the .toString() value of this
    * Object is the value which replaces the parameter entry. This value will be
    * escaped for SQL before use.
    * @return a reference to this Query object.
    */
   public Query set(Object key, Object value) {
      if (value == null) {
         nulls.put(key.toString(), "NULL");
         vars.put(key.toString(), "NULL");
      } else {
         vars
            .put(key.toString(), StringEscapeUtils.escapeSql(value.toString()));
      }
      return this;
   }

   /**
    * <p>Convenience method, sets all parameters in the current query which have
    * matching properties in the passed instance to their values in that
    * instance. Equivalent to repeated calls to .set(parName, parValue).
    * <b>Note:</b> see {@link DBHelper} for documentation on setting up java
    * beans to use this method.
    * </p>
    * 
    * @param instance of a Java Bean corresponding to a database table. Class
    * must have either a public instance of DBHelper for that class, for
    * example:
    * 
    * <pre>
    * public static DBHelper helper = new DBHelper(WSLog.class);
    * </pre>
    * 
    * or a public method which returns the DBHelper for the class, for example:
    * 
    * <pre>
    * private static DBHelper helper = new DBHelper(WSLog.class);
    * 
    * public static DBHelper getHelper() {
    *    return helper;
    * }
    * </pre>
    * @return this instance of Query, for method chaining.
    */
   public Query setAll(DBTable instance) {

      DBHelper<?> helper = instance.getDBHelper();

      // Get all unique parameters in query
      Set <String> pars = new HashSet <>();
      for (StringBuilder str : sql) {
         for (String s : StringUtils.substringsBetween(str.toString(), "${",
            "}")) {
            pars.add(s);
         }
      }

      // find all pars which are properties of instance
      for (String par : pars) {
         // Must have property, with getter, not ignored for SQL
         DBHelper<?>.Property property = helper.getProperty(par);
         if (property == null) continue;
         Method getter = property.getGetter();
         if (getter == null) continue;
         if (property.isIgnoreSQL()) continue;
         try {
            Object ret = getter.invoke(instance);
            set(par, ret);
         } catch (Exception e) {
            log.warn("DBHelper property get error - " + e.getMessage());
         }
      }
      return this;
   }

   /**
    * Executes the query on the passed Connection. This call is for queries
    * which return a ResultSet. Uses {@link JDBC#dbQuery(Connection, Object)}
    * 
    * @param c open {@link Connection} instance for database.
    * @return {@link java.sql.ResultSet ResultSet}
    * @throws Exception on error or if query has multiple lines.
    */
   public ResultSet dbQuery(Connection c) throws Exception {
      lstCommand = lastCommand = "dbQuery(" + c.getDbName() + ")";
      if (sql.size() > 1)
         throw new Exception("Multi-line query; Use dbQueries");
      return JDBC.dbQuery(c, prepQuery(0));
   }

   /**
    * Executes the query on the passed Connection. This call is for queries
    * which have multiple SQL statements returning ResultSets. Uses
    * {@link JDBC#dbQuery(Connection, Object)}
    * 
    * @param c open {@link Connection} instance for database.
    * @return {@link java.sql.ResultSet ResultSet}
    * @throws Exception on error or if query has multiple lines.
    */
   public ResultSet[] dbQueries(Connection c) throws Exception {
      ResultSet[] ret = new ResultSet[sql.size()];
      for (int i = 0; i < sql.size(); i++ )
         ret[i] = JDBC.dbQuery(c, prepQuery(i));
      return ret;
   }

   /**
    * Executes the query on the passed Connection. This call is for queries
    * which return record counts or nothing. Uses
    * {@link JDBC#dbUpdate(Connection, Object)}
    * 
    * @param c open {@link Connection} instance for database.
    * @return integer result of query.
    * @throws Exception on error.
    */
   public int dbUpdate(Connection c) throws Exception {
      lstCommand = lastCommand = "dbUpdate(" + c.getDbName() + ")";
      if (sql.size() > 1)
         throw new Exception("Multi-line query; Use dbUpdates");
      return JDBC.dbUpdate(c, prepQuery(0));
   }

   /**
    * Executes the query on the passed Connection. This call is for multiple
    * line queries which return record counts or nothing. Uses
    * {@link JDBC#dbUpdate(Connection, Object)}
    * 
    * @param c open {@link Connection} instance for database.
    * @return integer arrays with results of queries.
    * @throws Exception on error.
    */
   public int[] dbUpdates(Connection c) throws Exception {
      lstCommand = lastCommand = "dbUpdates(" + c.getDbName() + ")";
      int[] ret = new int[sql.size()];
      for (int i = 0; i < sql.size(); i++ )
         ret[i] = JDBC.dbUpdate(c, prepQuery(i));
      return ret;
   }

   /**
    * Insert one record into the database, returning the primary key assigned
    * to that record. The query must be a valid sql insert which inserts one 
    * record (row).
    * @param c open {@link Connection} instance for database.
    * @return the primary key value assigned to the new row.
    * @throws Exception if SQL error occurs.
    */
   public int dbInsertOne(Connection c) throws Exception {
      lstCommand = lastCommand = "dbUpdate(" + c.getDbName() + ")";
      if (sql.size() > 1)
         throw new Exception("Multi-line query; Use dbUpdates");
      return JDBC.dbInsertOne(c, prepQuery(0));
   }

   private String prepQuery(int index) {
      String query = sql.get(index).toString();
      if (!nulls.isEmpty())
         query = StrSubstitutor.replace(query, nulls, "'${", "}'");
      if (!vars.isEmpty()) query = StrSubstitutor.replace(query, vars);
      StrLookup <String> variableResolver = new SegmentUsedLookup();
      StrSubstitutor ss =
         new StrSubstitutor(variableResolver, "<{", "}>", '\\');
      query = ss.replace(query);
      Util.getLog().info("    query = " + query);
      lastSql.add(index, query);
      lstSql.add(index, query);
      return query;
   }

   /**
    * Returns last call to Util db routines executed by this instance of Query.
    * Returns "N/A" if no routine has been executed.
    * 
    * @return string representation of command, with variables replaced.
    */
   public String lastCommand() {
      if (lastCommand == null) return "N/A";
      StringBuilder str = new StringBuilder(lastCommand).append("\n");
      for (String s : lastSql)
         str.append(s).append("\n");
      return str.toString();
   }

   /**
    * Returns last call to Util db routines executed by any instance of Query.
    * Returns "N/A" if no routine has been executed.
    * 
    * @return string representation of command, with variables replaced.
    */
   public static String LastCommand() {
      if (lstCommand == null) return "N/A";
      StringBuilder str = new StringBuilder(lstCommand).append("\n");
      for (String s : lstSql)
         str.append(s).append("\n");
      return str.toString();
   }

   /**
    * Specialized version of {@link org.apache.commons.lang3.text.StrLookup
    * StrLokup} used to remove unused segments from a query. If the segment
    * (key) contains a parameter, of the form ${parName}, that has not been
    * resolved, then that segment is assumed to not have been used, and "" is
    * returned. If not, the original segment is returned.
    */
   public class SegmentUsedLookup extends StrLookup <String> {

      @Override
      public String lookup(String key) {
         if (StringUtils.substringBetween(key, "${", "}") == null) return key;
         return null;
      }
   }

} // EO Query class
