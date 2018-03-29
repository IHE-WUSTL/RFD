package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Implementation of the {@link DBUtil} interface for the PostgreSQL RDBMS.
 * Currently running on systems with version 9.3.x postgresql.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class DBUtilPostgres implements Serializable, DBUtil {
   private static final long serialVersionUID = 1L;

   private static final String ROOT_DATABASE_NAME = "postgres";
   private static final String JDBC_DRIVER = "org.postgresql.Driver";
   private static final String ROOT_USER_ = "postgres";
   private static Logger log = Util.getLog();
   private String rootURL;
   private String rootPW;

   static {
      try {
         Class.forName(JDBC_DRIVER).newInstance();
      } catch (Exception e) {
         log.error("Error loading " + JDBC_DRIVER + ": " + e.getMessage());
         Util.exit(1);
      }
   }

   /**
    * Creates an instance for the particular Postgres database. <b>NOTE:</b> Not
    * normally called directly, but through the init methods of {@link JDBC}.
    * 
    * @param url JDBC connection string for the administrative database for this
    * server, for example "jdbc:postgresql://dbserver003.widget.com/postgres"
    * @param pw The administrative password for this database, for example
    * "admin".
    */
   public DBUtilPostgres(String url, String pw) {
      rootURL = url;
      rootPW = pw;
   }

   @Override
   public String getRootDatabaseName() {
      return ROOT_DATABASE_NAME;
   }

   @Override
   public boolean isUser(String userName) throws Exception {
      boolean exists = false;
      String un = StringUtils.trimToEmpty(userName);
      if (un.length() == 0) return false;
      String query =
         new Plug(
            "select usename from pg_user where usename = lower('${user}');")
            .set("user", un).get();
      try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
         ResultSet resultSet = stmt.executeQuery(query);
         if (resultSet.next()) exists = true;
      }
      return exists;
   }

   @Override
   public void createUser(String userName, String password, boolean createDB)
      throws Exception {
      String un = StringUtils.trimToEmpty(userName);
      String pw = StringUtils.trimToEmpty(password);
      if (un.length() == 0) throw new Exception("invalid user name");
      Plug plug = new Plug("create user ${name}").set("name", un);
      if (pw.length() != 0)
         plug.append(" password '${password}'").set("password", pw);
      if (createDB) plug.append(" CREATEDB");
      plug.append(";");
      String query = plug.get();
      DBUpdate(query);
      log.info("DB user " + un + " created.");
   }

   @Override
   public void dropUser(String userName) throws Exception {
      String un = StringUtils.trimToEmpty(userName);
      if (un.length() == 0) throw new Exception("invalid user name");
      String query = new Plug("drop user ${name}").set("name", un).get();
      DBUpdate(query);
      log.info("DB user " + un + " dropped.");
   }

   @Override
   public boolean databaseExists(String dbName) throws Exception {
      if (dbName == null) return false;
      String query =
         new Plug(
            "select datname from pg_database where datname = lower('${dbName}');")
            .set("dbName", dbName).get();
      try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
         ResultSet resultSet = stmt.executeQuery(query);
         if (resultSet.next()) return true;
      }
      return false;
   }

   @Override
   public void createDatabase(String dbName, String owner) throws Exception {
      String db = StringUtils.trimToEmpty(dbName);
      String own = StringUtils.trimToEmpty(owner);
      if (db.isEmpty())
         throw new Exception("createDatabase error: invalid dbName");
      if (own.isEmpty())
         throw new Exception("createDatabase error: invalid owner");
      String query =
         new Plug("CREATE DATABASE ${dbName} WITH OWNER ${owner}")
            .set("dbName", db).set("owner", own).get();
      DBUpdate(query);
   }

   @Override
   public void dropDatabase(String dbName) throws Exception {
      String query =
         new Plug("drop database if exists '${dbName}'").set("dbName", dbName)
            .get();
      DBUpdate(query);
   }

   private Connection getConnection() throws SQLException {
      return DriverManager.getConnection(rootURL, ROOT_USER_, rootPW);
   }

   private int DBUpdate(String query) throws Exception {
      try (Connection conn = getConnection();
         Statement stmt = conn.createStatement()) {
         return stmt.executeUpdate(query);
      }
   }

}
