package edu.wustl.mir.erl.ihe.ws.db;

import java.io.Serializable;

import edu.wustl.mir.erl.ihe.util.jdbc.Queries;
import edu.wustl.mir.erl.ihe.util.jdbc.RDBMS;

/**
 * <p>Specific {@link Queries} for the {@link WSMsg} data table class in the 
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
public class WSMsgPostgres extends Queries implements Serializable {
	private static final long serialVersionUID = 1L;
	
	static {
		 queries.put("create", new String[] {
		         "CREATE SEQUENCE seq_wsmsg_id START 1;",

		         "CREATE TABLE wsmsg (" +
		            "id INT PRIMARY KEY DEFAULT NEXTVAL ('seq_wsmsg_id'), " +
		        		 
		            "wslog_id    INT NOT NULL, " +
		            "msg_type    VARCHAR(16) NOT NULL, " +
		            "description VARCHAR(256) NOT NULL, " +
		            "log_time    TIMESTAMP NOT NULL, " +
		            "message     TEXT NOT NULL); ",
		            
		            "GRANT ALL ON wsmsg TO GROUP public;"
		      });
		 
		 queries.put("insert", new String[] {
		         "INSERT INTO wsmsg VALUES(NEXTVAL('seq_wsmsg_id'), " +
		         "${wslogId}, '${msgType}' '${description}', " +
		         "'${connOpenTime}', '{message}');"
		      });
		 
		 queries.put("wslogId", new String[] {
				 "SELECT * FROM wsmsg WHERE wslog_id = ${wslogId};"
	      	  });
	}

} // EO WSMsgPostgres
