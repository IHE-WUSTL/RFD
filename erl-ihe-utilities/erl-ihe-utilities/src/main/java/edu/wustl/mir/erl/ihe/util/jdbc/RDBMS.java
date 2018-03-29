package edu.wustl.mir.erl.ihe.util.jdbc;

import java.lang.reflect.Constructor;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Enum declares the RDBMS systems currently supported by the {@link 
 * edu.wustl.mir.erl.ihe.util.jdbc jdbc} package. At the beginning
 * of an application using this package, the program must call {@link 
 * JDBC#setSupportedDatabases(RDBMS...)}, passing the Database values for the
 * databases <b>the application supports</b>. {@link JDBC} will not initialize
 * any database not on the supported list.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public enum RDBMS {
   /**
    * PostgreSQL RDBMS.<ul>
    * <li>Will use {@link DBUtilPostgres}.</li>
    * <li>When loading database classes, will look for RDBMS specific classes
    * with the "Postgres" extension. For example, for a DB class WSLog, it will
    * look for a class WSLogPostgres with postgres specific SQL information.</li>
    * <li>Uses "org.postgresql.Driver" as the JDBC driver class. Developer is
    * responsible for including an appropriate JDBC jar compatible with their
    * Postgres installation.</li> </ul>
    */
   POSTGRESQL("org.postgresql.Driver", DBUtilPostgres.class);

   /**
    * The RDBMS type name for this instance, for example "Postgres" for the
    * PostgreSQL RDBMS, or "MySQL" for the MySQL RDBMS.<ul>
    * <li>In this package, there will be a class named DBUtil<i>dbTypeName</i>
    * for each Database, which will implement the {@link DBUtil} interface for
    * that database type. For example {@link DBUtilPostgres}.</li></ul>
    */
   private final String dbTypeName;
   
   /**
    * The canonical class name of the JDBC Driver class used for this database.
    * This will be matched to the JDBC Driver class specified for each database
    * initialized by {@link JDBC} to determine that the database is supported.
    */
   private final String driverClassName;
   /**
    * The constructor used to build {@link DBUtil} object for this DB 
    * implementation.
    */
   private Constructor<? extends DBUtil> constructor = null;

   RDBMS(String dcn, Class<? extends DBUtil> dbUtilClass) {
      dbTypeName = dbUtilClass.getName().split("DBUtil")[1];
      this.driverClassName = dcn;
      try {
         constructor = dbUtilClass.getConstructor(String.class, String.class);
      } catch (Exception e) {
         Util.getLog().error(dbUtilClass.getName() + 
            " has no public (String, String) constructor.");
         System.exit(1);
      }
   }

   /**
    * @return The {@link #dbTypeName} for this RDBMS type.
    */
   public String getDbTypeName() {
      return dbTypeName;
   }

   /**
    * @return The {@link #driverClassName} for this RDBMS type.
    */
   public String getDriverClassName() {
      return driverClassName;
   }
   
   /** 
    * Lookup RDBMS value by driverClassName
    * @param driverClassName canonical name of JDBC driver class for database.
    * @return RDBMS value for this driver class
    * @throws Exception if there is no enum value for this driver class, which
    * means that the RDBMS type is not supported by the 
    * {@link edu.wustl.mir.erl.ihe.util.jdbc jdbc} package.
    */
   public static RDBMS getRDBMSForDriverClassName(String driverClassName)
      throws Exception {
      RDBMS[] rdbmss = RDBMS.values();
      for (RDBMS database : rdbmss) {
         if (database.getDriverClassName().equals(driverClassName)) 
            return database;
      }
      throw new Exception("Database: " + driverClassName + 
         " not supported by edu.wustl.mir.erl.util.jdbc package.");
   }
   
   /**
    * Generates an instance of the DB Utility class for a particular database
    * server of this RDBMS type, for example {@link DBUtilPostgres} and returns
    * it as an instance of the {@link DBUtil} interface.
    * 
    * @param url JDBC connection string for the <b>administrative</b> database,
    * for example, "jdbc:postgresql://dbserver003.widget.com:4500/postgres".
    * @param pw administrative password for this database, for example, "admin".
    * @return an instance of the DBUtility class, in this example
    * {@link DBUtilPostgres}, cast to {@link DBUtil}.
    * @throws Exception on SQL error.
    */
   public DBUtil getDBUtil(String url, String pw) throws Exception {
      return constructor.newInstance(url, pw);
   }
}
