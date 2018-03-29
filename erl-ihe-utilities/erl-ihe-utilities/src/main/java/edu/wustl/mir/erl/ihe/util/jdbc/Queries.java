package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Base class for classes implementing SQL query and query segment strings for
 * the specific RDBMS type being used on a particular installation. The purpose
 * of this class and its subclasses is to allow an application to work with
 * logical queries, insulated from the specific SQL involved.
 * <ul>
 * <li>While reading this description and implementation, the classes WSLog and
 * WSLogPostgres in the erl-ihe-ws package would serve as useful examples to
 * follow.</li>
 * <li>A "query" may contain more than one SQL statement; The number of SQL
 * statements may be different for different RDBMS systems.</li>
 * <li>Developers are encouraged to use standard SQL whenever reasonable.</li>
 * </ul>
 * <p>
 * On application startup, {@link JDBC} will load a particular subclass of
 * Queries for each database table class, based on the RDBMS that has been set
 * up for the system. For example a WSServer application, wanting to use the
 * WSLog database class, and which was configured to use the PostgreSQL RDBMS,
 * would load the WSLogPostgres class. This subclass contains a set of named
 * queries, for example a query named "insert" which inserts a single row into
 * the database, and <b>possibly</b> also a set of named query segments, all
 * specifically written for the PostgreSQL dialect. Other Query subclasses, for 
 * the same table but a different RDBMS, for example WSLogMySQL, would have 
 * different queries, but use the same names, matching each query to its use
 * inside the application.
 * </p>
 * <p>Both queries and query segments may have embedded parameters for which
 * specific values are substituted prior to their execution. Query segments
 * differ from queries in that they represent only a portion of a query, needing
 * to be concatenated with other segments and information to create a complete
 * query.
 * </p>
 * When the Query subclass is loaded, it loads its named queries into
 * {@link #queries} and its named query segments into {@link #segments}. They 
 * can then be retrieved by name from the application.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class Queries implements Serializable {
   private static final long serialVersionUID = 1L;
   /**
    * Map of named queries for a particular RDBMS type, for example, "Postgres".
    */
   protected static Map <String, String[]> queries = new HashMap <>();
   /**
    * Map of named query segments for a particular RDBMS type, for example,
    * "Postgres".
    */
   protected static Map<String, String> segments = new HashMap<>();

   /**
    * Load {@link Queries query} matching the passed query name. Used to load
    * queries into "sql" Enums in database classes.
    * 
    * @param name of query. Should be the {@link Enum#name()} value.
    * @return String[] containing query.
    * <p>This method will terminate the application using {@link Util#exit(String)}
    * if no query matching name is found.</p>
    * @see #getQuery(String)
    */
   public String[] loadQuery(String name) {
      if (queries == null) return null;
      if (!queries.containsKey(name))
         Util.exit("Missing query: " + name);
      return queries.get(name);
   }

   /**
    * Get {@link Queries query} matching passed name. Can be used to retrieve
    * any query stored by name, including those which match the
    * {@link java.lang.Enum#name() Enum.name()} value.
    * @param name String name of query to get.
    * @return the String[] query matching the passed name, or <i>null</i> if no
    * query matches the name.
    * <p>This method will not terminate or throw an Exception. It is the callers
    * responsibility to validate the return is not <i>null</i>.</p>
    * @see #loadQuery(String)
    */
   public String[] getQuery(String name) {
      return queries.get(name);
   }

   /**
    * Get query {@link #segments segment} matching passed name.
    * @param name String name of segment to get.
    * @return the String query matching the passed name, or <i>null</i> if no
    * query matches the name.
    * <p>This method will not terminate or throw an Exception. It is the callers
    * responsibility to validate the return is not <i>null</i>.</p>
    */
   public String getSegment(String name) {
      return segments.get(name);
   }
}
