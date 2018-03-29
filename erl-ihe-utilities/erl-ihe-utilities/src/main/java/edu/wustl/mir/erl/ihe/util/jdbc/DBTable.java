package edu.wustl.mir.erl.ihe.util.jdbc;

import java.io.Serializable;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Interface which is implemented by all classes representing database tables
 * and utilizing the erl-ihe-util db package.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public interface DBTable extends Serializable, AutoCloseable {
   /**
    * Set or reset the {@link Logger} for this instance. The default is
    * {@link Util#getLog()}, that is, the SYSTEM logger. However this may often
    * be overridden by the application.
    * 
    * @param logger Logger instance for this database record.
    */
   public void setLog(Logger logger);

   /**
    * @return String logical database name for DB containing the table modeled
    * by this class.
    */
   public String getLogicalDbName();

   /**
    * @return DBHelper instance for this database class.
    */
   public DBHelper <?> getDBHelper();

}
