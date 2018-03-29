package edu.wustl.mir.erl.ihe.util.jdbc;

/**
 * Interface defining a set of standard database actions (for example, dropUser,
 * createDabase) to be implemented for particular DBMS systems (for example, 
 * {@link DBUtilPostgres}).
 * <p><b>Note: </b>In addition to the methods in this interface, implementing 
 * classes must have a method with this signature:</p>
 * <pre>{@code
 *       public static DBUtil getDBUtil(String url, String user, String pw);
 * }</pre>
 * which serves as factory.
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public interface DBUtil {
   
   /**
    * @return the name of the root (master) database. For example, "mysql" for
    * MySQL, "postgres" for postgresql
    */
   public String getRootDatabaseName();
   
   /**
    * Determines if the passed user exists in the DBMS.
    * @param userName String name of the user to check for.
    * @return boolean true if user exists, false otherwise.
    * @throws Exception if the DBMS server encounters an error.
    */
   public boolean isUser(String userName) throws Exception;
   
    /**
    * Creates database user IAW with passed data.
    * @param userName String user name
    * @param password String user password. If null, empty, or whitespace, user
    * will have no password.
    * @param createDB boolean should user have power to create databases.
    * @throws Exception on error; will have been logged.
    */
   public void createUser(String userName, String password,
         boolean createDB) throws Exception;
   
   /**
    * Drops named user from DB.
    * @param userName String user name to drop
    * @throws Exception on error or if user name is invalid.
    */
   public void dropUser(String userName) throws Exception;
   
   /**
    * Determines if the named database exists in the DBMS.
    * @param dbName String name of the database to check for.
    * @return boolean true if database exists, false otherwise.
    * @throws Exception  if the DBMS server encounters an error.
    */
   public boolean databaseExists(String dbName) throws Exception;
   
   /**
    * Creates database with passed name.
    * @param dbName Name of database to create
    * @param owner db user who is the owner of the database. In some RDBMS
    * systems, this parameter will be ignored.
    * @throws Exception if the DBMS server encounters an error.
    */
   public void createDatabase(String dbName, String owner) throws Exception;
   
   /**
    * Drops (deletes) passed database name (must not be in use)
    * @param dbName database name to drop.
    * @throws Exception if the DBMS server encounters an error.
    */
   public void dropDatabase(String dbName) throws Exception;
}
