package edu.wustl.mir.erl.ihe.ws.db;

import java.io.Serializable;

import edu.wustl.mir.erl.ihe.util.jdbc.Queries;
import edu.wustl.mir.erl.ihe.util.jdbc.RDBMS;

/**
 * <p>Specific {@link Queries} for the {@link WSLog} data table class in the
 * {@link RDBMS#POSTGRESQL} environment.
 * </p><p>
 * <b>&#064;see Also</b> {@link Queries} for a discussion of the use of Query
 * subclasses.
 * </p>
 * <b>Notes for developers</b> When writing subclasses for other RDBMS systems:
 * <ul>
 * <li>Use standard SQL when reasonable.</li>
 * <li>Match the same names to the same processes.</li>
 * </ul>  
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 * 
 * @see <a href="http://www.postgresql.org/docs/9.3/interactive/index.html">
 * PostgreSQL 9.3 documentation</a>
 */
public class WSLogPostgres extends Queries implements Serializable {
   private static final long serialVersionUID = 1L;
         
   static { 
      
      queries.put("create", new String[] {
         "CREATE SEQUENCE seq_wslog_id START 1;",

         "CREATE TABLE wslog (" +
            "id INT PRIMARY KEY DEFAULT NEXTVAL ('seq_wslog_id'), " +

            "client_ip       VARCHAR(16) NOT NULL, " +
            "client_host_name VARCHAR(128) NOT NULL, " +
            "server_ip       VARCHAR(16) NOT NULL, " +
            "server_port     SMALLINT NOT NULL, " +
            "secure          BOOLEAN NOT NULL, " +
            "certificates    VARCHAR(256) NOT NULL, " +
            "service_name    VARCHAR(32) NOT NULL, " +
            "server_name     VARCHAR(32) NOT NULL, " +
            "conn_open_time  TIMESTAMP NOT NULL, " +
            "conn_close_time TIMESTAMP NOT NULL, " +
            "statuses_txt    TEXT NOT NULL, " +
            "error_message   TEXT NOT NULL, " +
            "error_line      SMALLINT NOT NULL, " +
            "error_column    SMALLINT NOT NULL, " +
            "error_substring TEXT NOT NULL); ",
            
            "GRANT ALL ON wslog TO GROUP public;"
      });
      
      queries.put("insert", new String[] {
         "INSERT INTO wslog VALUES(NEXTVAL('seq_wslog_id'), " +
         "'${clientIp}', '${clientHostName}', '${serverIp}', ${secure}, '${certificates}', " +
         "'${serviceName}', '${serverName}', '${connOpenTime}', " + 
         "'${connCloseTime}', '${statuses}', '{errorMessage}', " +
         "${errorLine}, ${errorColumn}, '${errorSubstring}');"

      });
      
   } // EO static block

} // EO WSLogPostgres
