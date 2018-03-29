package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Closeable;
import java.io.Serializable;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;

/**
 * Utility class designed to encapsulate JDBC processing, including
 * Initialization of JDBC databases from the application properties file, or
 * dynamically, connecting to and processing SQL against those databases, and
 * tracking and releasing resources used in that processing.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class JDBC implements Serializable, UtilProperties {
	private static final long serialVersionUID = 1L;

	private static Logger log = Util.getLog();

	/**
	 * {@link RDBMS} instances for the database systems supported by the
	 * application.
	 */
	private static Set<RDBMS> supportedRdbms = null;

	/** Set of JDBC driver classes which have been loaded. */
	private static Set<String> driverClassNames = new HashSet<String>();

	/**
	 * Default logical database name, or null for no default. If set, this will
	 * be used as the logical database name in convenience methods which do not
	 * pass a logical database name. If defaultLogicalDbName is not set,
	 * invoking any of the convenience methods will result in a fatal error. It
	 * is normally set at the beginning of the program when one of the init
	 * methods is called, but can be changed or reset to null if needed during
	 * the program.
	 */
	private static String defaultLogicalDbName = null;

	/** JDBC databases which have been initialized, mapped by their logical name */
	private static Map<String, Database> databases = new HashMap<>();

	/**
	 * Returns the {@link Database} instance for the passed logical database name
	 * @param logicalDbName logical database name
	 * @return Database instance for this database, or <code>null</code> if no
	 * database of that name has been initialized.
	 */
	public static Database getDatabase(String logicalDbName) {
		return databases.get(logicalDbName);
	}

	/**
	 * Set list of Database Management systems supported by the application.
	 * 
	 * @param rdbms
	 *            One or more {@link RDBMS} values, representing the systems
	 *            which the applications supports. The RDBMS enum only has
	 *            values for the systems the
	 *            {@link edu.wustl.mir.erl.ihe.util.jdbc jdbc} package supports.
	 */
	public static void setSupportedDatabases(RDBMS... rdbms) {
		JDBC.supportedRdbms = new HashSet<RDBMS>();
		for (RDBMS db : rdbms) {
			JDBC.supportedRdbms.add(db);
		}
	}

	/**
	 * Convenience method for {@link #init(String, List)} in cases where the
	 * database configurations are in the application properties file.
	 * 
	 * @param defaultLogDbName
	 *            {@link #defaultLogicalDbName}.
	 * @throws Exception if a database error occurs during processing.
	 */
	public static void init(String defaultLogDbName) throws Exception {
		log.trace("init(" + defaultLogDbName + ") called");
		init(defaultLogDbName,
				Util.getProperties().configurationsAt("JDBC.DataBase"));
	}

	/**
	 * Initialize JDBC to use one or more databases. This method is intended for
	 * use in cases where database properties have been set up in an XML
	 * properties file, most likely the applications properties file. When
	 * database parameters are placed in the application properties file, they
	 * are placed in a top level &lt;JDBC&gt; element as a series of &lt;DataBase&gt;
	 * elements with attributes, for example:
	 * <pre>
	 * {@code 
	 * <JDBC>
	 *    <DataBase LogicalDbname="wslog" AdministrativeDbName="postgres"
	 *              DriverClassName="org.postgresql.Driver"
	 *              ConnectionString="jdbc:postgresql://localhost/wslog" 
	 *              UserId="wslog" Password="syslog" />
	 *    <DataBase LogicalDbname="postgres"
	 *              DriverClassName="org.postgresql.Driver" 
	 *              ConnectionString="jdbc:postgresql://localhost/postgres"
	 *              UserId="postgres" Password="development" />
	 * </JDBC>
	 * }
	 * </pre>
	 * 
	 * In this example, two HierarchicalConfiguration instances would be passed,
	 * one for each of the &lt;DataBase&gt; elements.
	 * 
	 * @param defaultLogDbName {@link #defaultLogicalDbName}.
	 * @param databasesProperties a {@link List} of 
	 * {@link org.apache.commons.configuration.HierarchicalConfiguration
	 * HierarchicalConfiguration} instances, each containing one &lt;DataBase&gt;
	 * element and its attributes.
	 * @throws Exception if a database error occurs during processing.
	 */
   public static void init(String defaultLogDbName,
      List <HierarchicalConfiguration> databasesProperties) throws Exception {
      log.trace("init(" + defaultLogDbName
         + ", List<HierarchicalConfiguration>) called");
      List <Map <String, String>> dbms = new ArrayList <>();
      for (HierarchicalConfiguration db : databasesProperties) {
         Map <String, String> dbm = new HashMap <>();
         Iterator <String> i = db.getKeys();
         while (i.hasNext()) {
            String key = i.next();
            String value = db.getString(key);
            dbm.put(StringUtils.substringBetween(key, "[@", "]"), value);
         }
         dbms.add(dbm);
      } // EO for loop
      init(dbms, defaultLogDbName);
   }

	/**
	 * Initialize JDBC to use one or more databases. This method is intended for
	 * use in cases where databases are accessed dynamically during the
	 * operation of a program. In the more common case where database
	 * definitions and connections are not dynamic, it is expected that
	 * connection information will be placed in the &lt;JDBC&gt; element in the
	 * application properties file and initialized at program startup using
	 * {@link #init(String)}.
	 * <p>
	 * Database properties are provided in a {@link java.util.List List} of
	 * {@link java.util.Map Map&lt;String, String>} with the following key value
	 * pairs, which are mandatory and have no default values unless otherwise
	 * noted:
	 * </p>
	 * <ul>
	 * <li><b>LogicalDbname</b> The logical name of the database, that is, the
	 * name which will be passed in method calls to indicate which database is
	 * being referred to. For example, "wslog".</li>
	 * <li><b>PhysicalDbName</b> The physical name of the database, that is,
	 * the name used by the database server. This attribute is optional and
	 * defaults to the LogicalDbName.</li>
	 * <li><b>AdministrativeDbName</b> The logical name of the database
	 * connection which has administrative privileges for this database. This
	 * attribute is optional and has no default, that is, if it is not defined
	 * there no administrative database connection for this database. An
	 * administrative database is needed only if the application performs
	 * actions requiring administrative rights.</li>
	 * <li><b>DriverClassName</b> The canonical class name of the Java JDBC
	 * driver to be used. For example, "org.postgresql.Driver".</li>
	 * <li><b>ConnectionString</b> The jdbc connection string for the database.
	 * For example, "jdbc:postgresql://localhost/postgres".</li>
	 * <li><b>UserId</b> The user id used to access this database. For example,
	 * "appUser".</li>
	 * <li><b>Password</b> The password for the user. For example, "appPw".</li>
	 * </ul>
	 * 
	 * @param databasesProperties
	 *            zero or more database definitions
	 * @param defaultLogDbName
	 *            {@link #defaultLogicalDbName}.
	 * @throws Exception if a database error occurs during processing.
	 */
	public static void init(List<Map<String, String>> databasesProperties,
			String defaultLogDbName) throws Exception {
		log.trace("init(List<Map<String, String>> databasesProperties, " + 
			defaultLogDbName + ") called");
		// ----------------- Must set supported RDBMS before init
		if (supportedRdbms == null)
			Util.exit("supported databases not set");

		try {
			JDBC.defaultLogicalDbName = StringUtils
					.trimToNull(defaultLogDbName);
			if (JDBC.defaultLogicalDbName == null)
				log.info("JDBC.init: No default logical DB name");

			Set<String> logicalDbNames = new HashSet<>();

			if (databasesProperties == null) {
			   databasesProperties = new ArrayList<>();
				log.info("JDBC.init: No databases to initialize");
			}

			// ------------------ Pass Database elements from properties file
			for (Map<String, String> databaseProperties : databasesProperties) {

				// ------------------------------------------- Pull attributes
				String logicalDbName = StringUtils
						.trimToNull(databaseProperties.get("LogicalDbname"));
				String physicalDbName = StringUtils
						.trimToNull(databaseProperties.get("PhysicalDbname"));
				if (physicalDbName == null)
					physicalDbName = logicalDbName;
				String administrativeDbName = StringUtils
						.trimToNull(databaseProperties
								.get("AdministrativeDbName"));
				String userId = StringUtils.trimToNull(databaseProperties
						.get("UserId"));
				String password = StringUtils.trimToNull(databaseProperties
						.get("Password"));
				String driverClassName = StringUtils
						.trimToNull(databaseProperties.get("DriverClassName"));
				String connectionString = StringUtils
						.trimToNull(databaseProperties.get("ConnectionString"));

				// -------------------- Check for duplicate logical DB Name
				if (logicalDbName != null
						&& logicalDbNames.add(logicalDbName) == false) {
					log.warn("logical DB Name " + logicalDbName
							+ " multiply defined.");
					continue;
				}

				// ------------------------------ Check for missing attribute
				// values
				if (logicalDbName == null)
					throw new Exception(
							" missing/invalid logicalDbName attribute");
				StringBuilder em = new StringBuilder();
				if (physicalDbName == null)
					em.append("PhysicalDbName ");
				if (userId == null)
					em.append("UserId ");
				if (password == null)
					em.append("Password ");
				if (driverClassName == null)
					em.append("DriverClassName ");
				if (connectionString == null)
					em.append("ConnectionString ");

				if (em.length() > 0)
					throw new Exception(" for DB " + logicalDbName + ": " + em
							+ "missing or invalid");

				// ------------------ Load JDBC Driver if not already loaded
				if (!driverClassNames.contains(driverClassName)) {
					try {
						Class.forName(driverClassName).newInstance();
					} catch (Exception e) {
						throw new Exception("Couldn't load " + driverClassName
								+ " " + e.getMessage());
					}
					driverClassNames.add(driverClassName);
				}

				// ------------- is RDBMS supported by jdbc package?
				RDBMS r = RDBMS.getRDBMSForDriverClassName(driverClassName);
				if (!supportedRdbms.contains(r))
					throw new Exception("Database: " + driverClassName
							+ " not supported by " + Util.getApplicationName());

				Database database = new Database(logicalDbName, physicalDbName,
						administrativeDbName, userId, password,
						connectionString, r);

				/*
				 * If the logical database is being redefined, close any open
				 * connections the old definition may have, then replace the old
				 * definition with the new one.
				 */
				Database old = databases.get(logicalDbName);
				if (old != null)
					old.close();
				databases.put(logicalDbName, database);

			} // EO pass Database elements

			if (databases.isEmpty()) {
				log.info("JDBC.init: No databases defined.");
			}

			Iterator<String> i = databases.keySet().iterator();
			while (i.hasNext()) {
				String logicalDbName = i.next();
				Database db = databases.get(logicalDbName);
				String administrativeDbName = db.getAdministrativeDbName();
				if (administrativeDbName != null) {
					Database adb = databases.get(administrativeDbName);
					if (adb != null) {
						db.dbUtil = db.rdbms.getDBUtil(adb.connectionString,
								adb.password);
					} else {
						log.warn("Database "
								+ logicalDbName
								+ " specifies administrative database "
								+ administrativeDbName
								+ " which is not defined."
								+ nl
								+ "DBUtil methods will not be available for this db.");
					}
				}
			}

		} catch (Exception e) {
			throw new Exception("Error in JDBC.init: " + e.getMessage());
		}
	} // EO init method

	/**
	 * Convenience method for {@link #getConnection(String)} which presumes that
	 * the default logical database is to be used.
	 * @return Connection instance with a new, open {@link java.sql.Connection}
    *         to the default database.
	 * @throws Exception
	 *             If no default logical database has been defined. This is
	 *             considered to be a programming error, and is fatal.
	 */
	public static Connection getConnection() throws Exception {
		return getConnection(defaultDb());
	}

	/**
	 * Create and return a new {@link Connection} instance for a database.
	 * 
	 * @param logicalDbName
	 *            logical database name for the database
	 * @return Connection instance with a new, open {@link java.sql.Connection}
	 *         to the database.
	 * @throws Exception
	 *             if the named database has not been initialized or it was not
	 *             possible to open a connection to the database.
	 * @see #init(String, List)
	 */
	public static Connection getConnection(String logicalDbName)
			throws Exception {
		try {
			if (databases.containsKey(logicalDbName) == false)
				throw new Exception("DB " + logicalDbName + " not defined.");
			Database db = databases.get(logicalDbName);
			java.sql.Connection conn = DriverManager.getConnection(
					db.getConnectionString(), db.getUserId(), db.getPassword());
			return new Connection(db, conn);
		} catch (Exception e) {
			log.warn("Error in JDBC.getConnection: " + e.getMessage());
			throw e;
		}
	}

	private static String defaultDb() throws Exception {
		if (defaultLogicalDbName == null) {
			Util.exit("JDBC error: No default database defined");
		}
		return defaultLogicalDbName;
	}

	/**
	 * Perform SQL query on database, returning ResultSet. When finished with
	 * the ResultSet, user can invoke {@link java.sql.ResultSet#close()},
	 * {@link Connection#closeResultSet()} which will also close the Statement,
	 * or {@link Connection#close()} which will close the Connection, releasing
	 * all resources for the {@link java.sql.ResultSet ResultSet}, the
	 * {@link java.sql.Statement Statement}, and the {@link java.sql.Connection
	 * Connection}.
	 * 
	 * @param c
	 *            open {@link Connection} instance for database.
	 * @param querySQL
	 *            The SQL query in a {@link java.lang.String String} or any
	 *            class whose {@link java.lang.Object#toString() toString}
	 *            method will yield the SQL query, for example, a
	 *            {@link java.lang.StringBuilder StringBuilder}.
	 * @param resultSetType
	 *            the {@link java.sql.ResultSet ResultSet} type for this
	 *            transaction, {@link java.sql.ResultSet#TYPE_FORWARD_ONLY
	 *            TYPE_FORWARD_ONLY},
	 *            {@link java.sql.ResultSet#TYPE_SCROLL_INSENSITIVE
	 *            TYPE_SCROLL_INSENSITIVE}, or
	 *            {@link java.sql.ResultSet#TYPE_SCROLL_SENSITIVE
	 *            TYPE_SCROLL_SENSITIVE}
	 * @param resultSetConcurrency
	 *            the {@link java.sql.ResultSet ResultSet} concurrency mode for
	 *            this transaction, {@link java.sql.ResultSet#CONCUR_READ_ONLY
	 *            CONCUR_READ_ONLY} or
	 *            {@link java.sql.ResultSet#CONCUR_UPDATABLE CONCUR_UPDATABLE}
	 * @return {@link java.sql.ResultSet ResultSet}, null on error
	 * @throws Exception
	 *             on error
	 */
	public static ResultSet dbQuery(Connection c, Object querySQL,
			int resultSetType, int resultSetConcurrency) throws Exception {
		try {
			log.info(c.getDbName() + " query = " + querySQL);
			java.sql.Connection conn = c.getConnection();
			c.lastStatement = conn.createStatement(resultSetType,
					resultSetConcurrency);
			c.lastResultSet = c.lastStatement.executeQuery(querySQL.toString());
			c.lastMetaData = c.lastResultSet.getMetaData();
			return c.lastResultSet;
		} catch (Exception e) {
			StringBuilder b = new StringBuilder();
			b.append(c.getDbName());
			b.append(" query: ").append(querySQL);
			b.append("\n Error: ").append(e.getMessage());
			log.warn(b.toString());
			throw e;
		}
	}

   /**
    * Convenience method to call {@link #dbQuery(Connection, Object, int, int)},
    * assuming that the returned {@link ResultSet} should
    * be {@link ResultSet#TYPE_SCROLL_SENSITIVE TYPE_SCROLL_SENSITIVE}
    * and {@link ResultSet#CONCUR_READ_ONLY CONCUR_READ_ONLY}, which is
    * the most common type of ResultSet.
    * 
    * @param c
    *        open {@link Connection} instance for database.
    * @param querySQL The SQL query in a {@link String} or any
    *        class whose {@link #toString()} method will yield the SQL query,
    *        for example, a {@link StringBuilder}.
    * @return {@link ResultSet}
    * @throws Exception on SQL error.
    */
	public static ResultSet dbQuery(Connection c, Object querySQL)
			throws Exception {
		return dbQuery(c, querySQL, ResultSet.TYPE_SCROLL_SENSITIVE,
				ResultSet.CONCUR_READ_ONLY);
	}

	/**
	 * <p>Perform SQL update on database, returning record counts. When finished,
	 * the user can continue to use the {@link Connection} to access the
	 * database, or invoke @link Connection#close()} which will close the
	 * Connection, releasing all resources related to the Connection.
	 * </p>
	 * 
	 * @param c
	 *            open {@link Connection} instance for database.
	 * @param querySQL
	 *            The SQL query in a {@link java.lang.String String} or any
	 *            class whose {@link java.lang.Object#toString() toString}
	 *            method will yield the SQL query, for example, a
	 *            {@link java.lang.StringBuilder StringBuilder}.
	 * @return integer record count appropriate to the update.
	 * @throws Exception
	 *             on error
	 */
	public static int dbUpdate(Connection c, Object querySQL) throws Exception {
		log.debug(c.getDbName() + " query = " + querySQL);
		try (Statement update = c.getConnection().createStatement()) {
			int recordCount = update.executeUpdate(querySQL.toString());
			return recordCount;
		} catch (Exception e) {
			StringBuilder b = new StringBuilder();
			b.append(c.getDbName());
			b.append(" query: ").append(querySQL);
			b.append("\n Error: ").append(e.getMessage());
			log.error(b.toString());
			throw e;
		}
	}
	
	/**
	 * Insert one record into the database, returning the primary key assigned
	 * to that record. The query must be a valid sql insert which inserts one 
	 * record (row).
	 * @param c {@link Connection} instance for database.
	 * @param querySQL The SQL query in a {@link String} or any
	 * class whose {@link #toString()} method will 
	 * yield the SQL query, for example, a {@link StringBuilder}.
	 * @return The primary key value assigned to the created row.
	 * @throws Exception on error.
	 */
	public static int dbInsertOne(Connection c, Object querySQL) throws Exception {
		log.debug(c.getDbName() + " query = " + querySQL);
		try (Statement update = c.getConnection().createStatement()) {
			int rc = update.executeUpdate(querySQL.toString(),
					Statement.RETURN_GENERATED_KEYS);
			if (rc != 1) throw new Exception("record count " + rc);
			ResultSet rs = update.getGeneratedKeys();
			rs.next();
			return rs.getInt("id");
		} catch (Exception e) {
			StringBuilder b = new StringBuilder();
			b.append(c.getDbName());
			b.append(" query: ").append(querySQL);
			b.append("\n Error: ").append(e.getMessage());
			log.error(b.toString());
			throw e;
		}
	}

   /**
    * Generates a {@link Queries} instance for a particular database table class for the
    * selected RDBMS. For example, assuming that we had selected
    * {@link RDBMS#POSTGRESQL POSTGRESQL} as our RDBMS, the invocation:
    * <pre>
    *      Queries queries JDBC.getQueries(WSLog.class}</pre>
    * will return an instance of WSLogPostgres cast to Queries.
    * 
    * <p><b>Note:</b> If the appropriate Queries subclass is not available, this is 
    * considered to be a programming error, and is fatal.</p>
    * 
    * @param dbClass the {@link Class} of the database table. Not that this must
    * extend {@link DBTable}.
    * @return The appropriate initialized class, cast to Queries.
    */
	public static Queries getQueries(Class<? extends DBTable> dbClass) {
		String className = dbClass.getSimpleName();
		Queries queries = null;
		try {
			String logicalDbName = (String) dbClass.getField("logicalDbName").get(null);
			String name = dbClass.getCanonicalName();
			Database db = databases.get(logicalDbName);
			className = name + db.rdbms.getDbTypeName();
			queries = (Queries) Class.forName(className).newInstance();
		} catch (Exception e) {
			log.warn("No queries loaded for " + className + ": " + e.getMessage());
		}
		return queries;
	}

	/**
	 * POJO to contain information about a JDBC database. One instance of this
	 * class is created for each logical database name defined using one of the
	 * init methods. A logical database name may be re-defined by a later call
	 * to init re-using the logical database name. If this happens, any open
	 * connections to the old definition are closed, its definition is deleted,
	 * and a new definition is created. <b>NOTE:</b> If the same logical
	 * database name is defined more than one in a single call to an init
	 * method, all definitions after the first will be skipped and a warning
	 * will be logged.
	 */
	public static class Database implements Serializable, Closeable {
		private static final long serialVersionUID = 1L;

		/**
		 * The logical name of the database, that is, the name which will be
		 * passed in method calls to indicate which database is being referred
		 * to. For example, "wslog". It is often the same as the
		 * {@link #physicalDbName}
		 */
		private String logicalDbName = null;
		/**
		 * The physical database name. This is the name used to identify the
		 * database to the RDBMS. In most cases, this is the same as the
		 * {@link #logicalDbName}, but might differ, for example, in the case of
		 * multiple instances of the application using different databases on
		 * the same RDBMS server.
		 */
		private String physicalDbName = null;
		/**
		 * The logical database name of the database which has administrative
		 * rights for this database. For example, with a postgresql database,
		 * this would be the postgres user on the same RBDMS server. This only
		 * needs to be defined if the program does administrative tasks for the
		 * database, such as adding or dropping users.
		 */
		private String administrativeDbName = null;
		/**
		 * the user ID used to access the database, for example, "ralph".
		 */
		private String userId = null;
		/**
		 * the password used along with the user ID to access the database, for
		 * example, "BR-549".
		 */
		private String password = null;
		/**
		 * The jdbc connection string for the database. For example,
		 * "jdbc:postgresql://localhost/postgres".
		 */
		private String connectionString = null;

		/**
		 * An instance of a class implementing {@link DBUtil} for the database
		 */
		private DBUtil dbUtil = null;

		RDBMS rdbms = null;

		private Set<Connection> connections = Collections
				.synchronizedSet(new HashSet<Connection>());

		
		private Database(String logDbName, String physDbName,
				String adminDbName, String usrId, String passwrd,
				String connString, RDBMS dbms) {
			this.logicalDbName = logDbName;
			this.physicalDbName = physDbName;
			this.administrativeDbName = adminDbName;
			this.userId = usrId;
			this.password = passwrd;
			this.connectionString = connString;
			this.rdbms = dbms;
		}

		/**
		 * @return {@link #logicalDbName}
		 */
		public String getLogicalDbName() {
			return logicalDbName;
		}

		/**
		 * @return {@link #physicalDbName}
		 */
		public String getPhysicalDbName() {
			return physicalDbName;
		}

		/**
		 * @return {@link #administrativeDbName}
		 */
		public String getAdministrativeDbName() {
			return administrativeDbName;
		}

		/**
		 * @return {@link #userId}
		 */
		public String getUserId() {
			return userId;
		}

		/**
		 * @return {@link #password}
		 */
		public String getPassword() {
			return password;
		}

		/**
		 * @return {@link RDBMS#getDriverClassName Driver Class Name}
		 */
		public String getDriverClassName() {
			return rdbms.getDriverClassName();
		}

		/**
		 * @return {@link #connectionString}
		 */
		public String getConnectionString() {
			return connectionString;
		}

		/**
		 * @return {@link DBUtil DB Utility} instance for this database.
		 * @throws Exception if:
		 *             <ul>
		 *             <li>The database specified an administrative database,
		 *             but that database was not defined.</li>
		 *             <li>The database has no administrative database, for
		 *             example, it is an administrative database itself.</li></ul>
		 */
		public DBUtil getDBUtil() throws Exception {
			if (dbUtil == null) {
				throw new Exception("No administrative database defined for "
						+ logicalDbName);
			}
			return dbUtil;
		}

		/**
		 * @return {@link RDBMS} instance representing the type of this database.
		 */
		public RDBMS getRDBMS() {
			return rdbms;
		}

		protected boolean addConnection(Connection connection) {
			return connections.add(connection);
		}

		protected boolean removeConnection(Connection connection) {
			return connections.remove(connection);
		}

		/**
		 * A Database instance is closed only when the logical database name is
		 * being assigned to another database connection, in which case any open
		 * {@link Connection} instances are closed.
		 */
		@Override
      public void close() {
			for (Connection c : connections)
				c.close();
		}

	} // ################################## EO Database inner class

	/**
	 * POJO to contain information about a {@link java.sql.Connection
	 * Connection} An instance of this class is created for each connection. Can
	 * be created in a try-using resources block. Should be closed by invoking
	 * the {@link #close} method.
	 * 
	 * @author rmoult01
	 * 
	 */
	public static class Connection implements Serializable, Closeable {
		private static final long serialVersionUID = 1L;

		/**
		 * The {@link Database} for which this {@link Connection} was created.
		 */
		private Database database;
		/**
		 * The {@link java.sql.Connection Connection} instance for this
		 * {@link Connection}. This is opened by
		 * {@link JDBC#getConnection(String)} or one of its convenience
		 * methods and passed to to this object in its constructor.
		 */
		private java.sql.Connection conn;
		/**
		 * The most recent statement for this connection. It is closed when the
		 * result set is closed.
		 */
		private java.sql.Statement lastStatement = null;
		/**
		 * The most recent ResultSet for this Connection. This is the object
		 * returned by the most recent invocation of any of the
		 * {@link Query#dbQuery} methods, as well as being retrieved via
		 * {@link #getLastResultSet()}. The ResultSet can be closed by the
		 * calling program or by invoking {@link #closeResultSet()}, and it will
		 * be closed if needed when {@link #close()} is invoked.
		 */
		private ResultSet lastResultSet = null;
		/**
		 * The ResultSetMetaData for the last query made on this Connection. It
		 * will be cleared when the corresponding {@link java.sql.ResultSet
		 * ResultSet} is closed.
		 */
		private ResultSetMetaData lastMetaData = null;

		protected Connection(Database databse, java.sql.Connection cn) {
			this.database = databse;
			this.conn = cn;
			this.database.addConnection(this);
		}

		// --- Implements Closeable interface, allowing use in try with
		// resources
		@Override
      public void close() {
			closeResultSet();
			closeConnection();
			database.removeConnection(this);
			database = null;
		}

		/**
		 * Closes the {@link java.sql.ResultSet ResultSet} for this Connection
		 * if open. Any exceptions are swallowed.
		 */
		public void closeResultSet() {
			if (lastResultSet != null) {
				try {
					lastResultSet.close();
				} catch (Exception e) {
				}
			}
			if (lastStatement != null) {
				try {
					lastStatement.close();
				} catch (Exception e) {
				}
			}
			lastStatement = null;
			lastResultSet = null;
			lastMetaData = null;
		}

      /**
       * Closes the {@link java.sql.Connection Connection} for this
       * {@link JDBC.Connection Connection} if open. Any exceptions are
       * swallowed.
       */
		private void closeConnection() {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
				}
			}
			conn = null;
		}

		/**
		 * @return {@link #lastResultSet}
		 */
		public ResultSet getLastResultSet() {
			return lastResultSet;
		}

		protected void setLastResultSet(ResultSet resultSet) {
			lastResultSet = resultSet;
		}

		/**
		 * @return {@link #lastMetaData}
		 */
		public ResultSetMetaData getLastMetaData() {
			return lastMetaData;
		}

		private java.sql.Connection getConnection() {
			return conn;
		}

		protected String getDbName() {
			return database.getLogicalDbName();
		}

	} // EO Connection inner class

} // EO JDBC class
