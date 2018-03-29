package edu.wustl.mir.erl.ihe.ws.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.jdbc.DBHelper;
import edu.wustl.mir.erl.ihe.util.jdbc.DBTable;
import edu.wustl.mir.erl.ihe.util.jdbc.Helper;
import edu.wustl.mir.erl.ihe.util.jdbc.JDBC;
import edu.wustl.mir.erl.ihe.util.jdbc.Queries;

/**
 * Java bean for WS transaction logging table. One row is made in the message
 * logging table for each transaction sent by a a user to a WS Endpoints.
 * Includes:
 * <ul>
 * <li>Object representation of table row.</li>
 * <li>List of {@link WSMsg} instances logged for this transaction.</li>
 * <li>sql inner Enum containing queries for this table.</li>
 * <li>Constructor and methods to build objects from JDBC
 * {@link java.sql.ResultSet ResultSet}</li>
 * <li>Insert method.</li>
 * <li>Comparator for sorting arrays of instances</li>
 * <li>toString method useful for inserting object in log.</li>
 * </ul>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class WSMsg implements DBTable, WSLogProperties {
   private static final long serialVersionUID = 1L;

   @Override
   public String getLogicalDbName() {
      return LOGICAL_DB_NAME;
   }

   @SuppressWarnings("unused")
   private transient Logger log = Util.getLog();

   @Override
   public void setLog(Logger logger) {
      log = logger;
   }

   private static DBHelper <WSMsg> helper = new DBHelper <WSMsg>(WSMsg.class);

   @Override
   public DBHelper <?> getDBHelper() {
      return helper;
   }

   private static Queries queries = JDBC.getQueries(WSMsg.class);

   // ********************************************************
   // Persisted properties. (Object representation of table)
   // ********************************************************

   /**
    * Primary unique key for this WSMsg row. DBA
    */
   private Integer id = null;
   /**
    * id of {@link WSLog} record to which this message is tied. Foreign key.
    */
   private Integer wslogId = null;
   /**
    * {@link MessageType} Message type for this logged message.
    */
   private MessageType msgType = null;
   /**
    * A short human readable description of this message
    */
   private String description = null;
   /**
    * Date and time this message was logged. Defaults to the system time when
    * the constructor was invoked.
    */
   private Date logTime = new Date();
   /**
    * Text of message being logged, corresponding with the message type.
    */
   private String message = null;

   // ********************************************************
   // Non-persisted (transient) properties
   // ********************************************************

   /**
    * When dealing with an array of instances in a table, used to indicate
    * whether this instance is selected
    */
   private boolean selected = false;

   /**
    * enum of SQL queries and updates for this table.
    */
   public enum sql {
      /**
       * Create WSMsg table in database.
       */
      create(),
      /**
       * insert a new WSMsg row in database table.
       */
      insert(),
      /**
       * select all WSMsg rows for a specific {@link WSLog}.
       */
      wslogId;

      /**
       * @return String[] containing the query for this sql instance.
       */
      public String[] getQuery() {
         try {
            return WSMsg.query(this.name());
         } catch (Exception e) {
            Util.exit("Query did not exists for WSMsg.sql " + this.name());
            return null;
         }
      }

   } // EO sql Enum

   /**
    * Returns query for passed name.
    * 
    * @param name String name of query
    * @return String[] query, one SQL statement per String in array.
    * @throws Exception if no such query.
    */
   public static String[] query(String name) throws Exception {
      String[] query = queries.getQuery(name);
      if (query == null)
         throw new Exception("WSMsg error. No such query " + name);
      return query;
   }

   /**
    * No argument constructor creates instance with default (mostly, empty)
    * values, no ID. Use this when creating a new instance.
    */
   public WSMsg() {}

   /**
    * Constructor used to create instance when populating data is available.
    * 
    * @param messageType {@link MessageType} of message, for example, XML.
    * @param messageDescription short human readable description of message, for
    * example, "SOAP request body".
    * @param messageString Actual message.
    */
   public WSMsg(MessageType messageType, String messageDescription,
      String messageString) {
      this.msgType = messageType;
      this.description = messageDescription;
      this.message = messageString;
   }

   /**
    * Constructor builds instance from the next" row of the passed ResultSet.
    * 
    * @param result ResultSet. Must be positioned at row for which instance is
    * desired. Use this when retrieving an instance from the database.
    * @throws SQLException on error.
    */
   public WSMsg(ResultSet result) throws SQLException {
      id = result.getInt("id");
      msgType = Enum.valueOf(MessageType.class, result.getString("msg_type"));
      description = result.getString("description");
      logTime = result.getTimestamp("log_time");
      message = result.getString("message");
   }

   /**
    * Inserts this instance into the table. Gets and closes its own
    * {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection Connection}.
    * 
    * @return integer the primary key (DBA) for the new WSMsg row.
    * @throws Exception on SQL error.
    */
   public int insert() throws Exception {
      try (JDBC.Connection conn = JDBC.getConnection()) {
         return insert(conn);
      }
   }

   /**
    * Inserts this instance into the table. Uses the passed
    * {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection Connection},
    * leaving to be closed by the caller.
    * 
    * @param conn an existing, open
    * {@link edu.wustl.mir.erl.ihe.util.jdbc.JDBC.Connection Connection}
    * @return int id assigned by the database to the new row.
    * @throws Exception on SQL error.
    */
   public int insert(JDBC.Connection conn) throws Exception {
      // TODO implement this method
      return 0;
   }

   // ********************************************************
   // Standard getters and setters
   // ********************************************************
   /**
    * @return {@link #id}
    */
   public Integer getId() {
      return id;
   }

   /**
    * set {@link #id} value
    * 
    * @param id value to set
    */
   public void setId(Integer id) {
      this.id = id;
   }

   /**
    * @return current {@link #wslogId} value
    */
   public Integer getWslogId() {
      return wslogId;
   }

   /**
    * set {@link #wslogId} value
    * 
    * @param wslogId new value
    */
   public void setWslogId(Integer wslogId) {
      this.wslogId = wslogId;
   }

   /**
    * @return current {@link #msgType} value
    */
   public MessageType getMsgType() {
      return msgType;
   }

   /**
    * set {@link #msgType} value
    * 
    * @param msgType new value
    */
   public void setMsgType(MessageType msgType) {
      this.msgType = msgType;
   }

   /**
    * @return current {@link #description} value
    */
   public String getDescription() {
      return description;
   }

   /**
    * set {@link #description} value
    * 
    * @param description new value
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * @return current {@link #logTime} value
    */
   public Date getLogTime() {
      return logTime;
   }

   /**
    * set {@link #logTime} value
    * 
    * @param logTime new value
    */
   public void setLogTime(Date logTime) {
      this.logTime = logTime;
   }

   /**
    * @return current {@link #message} value
    */
   @Helper(ignoreComp = true)
   public String getMessage() {
      return message;
   }

   /**
    * set {@link #message} value
    * 
    * @param message new value
    */
   public void setMessage(String message) {
      this.message = message;
   }
   
   /**
    * Returns message, formatting for "pretty print" if it is XML or SOAP, 
    * otherwise returning raw message.
    * @return message, "pretty" if possible.
    */
   public String getPretty() {
      switch (msgType) {
         case SOAP_IN:
         case SOAP_PREPOP:
         case SOAP_OUT:
            return Util.prettyPrintSOAP(message);
         case WSDL_MSG:
         case XML:
            return Util.prettyPrintXML(message);
         case HTTP_IN_BODY:
         case HTTP_OUT_BODY:
         default:
            return message;
      }
   }

   /**
    * @return is {@link #selected} true?
    */
   @Helper(ignoreComp = true)
   public boolean isSelected() {
      return selected;
   }

   /**
    * set {@link #selected} value
    * 
    * @param selected new value
    */
   public void setSelected(boolean selected) {
      this.selected = selected;
   }

   /**
    * Standard inner {@link Comparator} class for WSMsg using {@link DBHelper}
    */
   public static class Comp implements Comparator <WSMsg> {

      private String property;
      private boolean ascending;

      /**
       * @param property name of {@link WSMsg} property to sort on.
       * @param ascending boolean ascending=true, descending=false.
       */
      public Comp(String property, boolean ascending) {
         this.property = property;
         this.ascending = ascending;
      }

      @Override
      public int compare(WSMsg one, WSMsg two) {
         if (StringUtils.isBlank(property)) return 0;
         return helper.compare(one, two, property, ascending);
      }
   } // EO Comparator inner class

   static List <WSMsg> loadRows(ResultSet resultSet) throws Exception {
      return helper.loadRows(resultSet);
   }

   @Override
   public void close() throws Exception {
      // TODO Auto-generated method stub

   }

   /**
    * @return string with message type, description, and log time.
    */
   public String getLead() {
      return msgType.name() + " " + description + " "
         + Util.getRFC3339TimeStamp(logTime);
   }

} // EO WSLogMessage class
