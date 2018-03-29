/*******************************************************************************
 * Copyright (c) 2015 Washington University in St. Louis All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. The License is available at:
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. Contributors:
 * Initial author: Ralph Moulton / MIR WUSM IHE Development Project
 * moultonr@mir.wustl.edu
 ******************************************************************************/
package edu.wustl.mir.erl.ihe.ws.handlers;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.db.MessageType;

/**
 * Singleton class handles storage of SOAP messages to file system. Use:
 * <ul>
 * <li>Set up
 * 
 * <pre>
 * {@code <WSTests>
 *     <StoreSOAPMessages on="true" rootMessageDirectoryName="messages"/> }
 * </pre>
 * 
 * in properties file as appropriate.</li>
 * <li>Invoke {@link #initStoreSOAPMessages()} on program startup.</li>
 * <li>Invoke {@link #storeMessage(String, String, MessageType)} for the first
 * message in each transaction, for example:
 * 
 * <pre>
 * {@code Path messageDirectoryPath =
 *       StoreSOAPMessages.storeMessage(&quot;FMS&quot;, msg, MessageType.SOAP_IN);
 * }
 * </pre></li>
 * <li>Invoke {@link #storeMessage(Path, String, MessageType)} for the other
 * (usually just the SOAP response) message in the transaction, for example:
 * 
 * <pre>
 * {@code StoreSOAPMessages.storeMessage(messageDirectoryPath, msg, 
 *    MessageType.SOAP_OUT);}
 * </pre></li>
 * 
 * </ul>
 * Messages will be stored in a directory tree under the
 * {@link #rootMessageDirectoryPath} of the form:
 * 
 * <pre>
 * {@code
 * subDir/yyyy-MM-dd-HH-mm-ss-SSS/file-name}
 * </pre>
 * 
 * where the subDirectory should be representative of the client or server
 * simulator, the directory name is based on a time hack, and the specific file
 * name is based on the message type. For example:
 * 
 * <pre>
 * {@code
 * FMS/2015-03-12-14-24-11-015/soap_request.bin}
 * </pre>
 * 
 */
public class StoreSOAPMessages implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   /**
    * format for the directory used to store messages for a specific SOAP
    * request/response in the file system. The current time when the request is
    * stored is used to determine the directory name.
    */
   private static final SimpleDateFormat MSG_DIR_FORMAT = new SimpleDateFormat(
      "yyyy-MM-dd-HH-mm-ss-SSS");
   /**
    * boolean, has the store SOAP messages facility been initialized?
    */
   private static boolean initDone = false;
   /**
    * The root path of all directories holding SOAP messages. Determined by the
    * value of WSTests.StoreSOAPMessages@rootMessageDirectoryName in the
    * properties file. By default, it is the "messages" directory in the
    * {@link Util#getRunDirectoryPath() runDirectory}.
    */
   private static Path rootMessageDirectoryPath = null;
   private static Logger log = Logger.getLogger("StoreSOAPMessages");

   /**
    * Initializes the SOAP Messages storage facility, which stores copies of the
    * inbound and outbound SOAP messages in the file system. Looks in
    * {@code <WSTests>} element of properties for {@code <StoreSOAPMessages>}
    * element with attributes:
    * <ul>
    * <li><b>on</b> - boolean is facility to be used? Default "false".</li>
    * <li><b>rootMessageDirectoryName</b> - root directory for stored SOAP
    * messages. Relative paths are assumed to be in the
    * {@link Util#getRunDirectoryPath() run directory}. Default is "messages".</li>
    * </ul>
    * 
    * @throws Exception on error:
    * <ul>
    * <li>Attempt to invoke method more than once.</li>
    * <li>Needed elements not in properties file.</li>
    * <li>Root directory did not exist and could not be created.</li>
    * <li>Root directory already exists but does not have needed permissions.</li>
    * </ul>
    */
   public static void initStoreSOAPMessages() throws Exception {
      Util.invoked(log);
      if (initDone)
         throw new Exception("attempt to re-initialize StoreSOAPMessages");
      initDone = true;
      HierarchicalConfiguration storeSOAPMessagesProperties =
         Util.getProperties().configurationAt("WSTests.StoreSOAPMessages");
      if (storeSOAPMessagesProperties.getBoolean("[@on]", false) == false) {
         log.info("StoreSOAPMessages not enabled");
         return;
      }
      String rmd =
         storeSOAPMessagesProperties.getString("[@rootMessageDirectoryName]",
            "messages");
      Path rmdp = Util.getRunDirectoryPath().resolve(rmd);
      File rmdf = rmdp.toFile();
      if (!rmdf.exists()) rmdf.mkdirs();
      Util.isValidPfn("SOAP Messages Storage root directory", rmdp,
         PfnType.DIRECTORY, "rx");
      rootMessageDirectoryPath = rmdp;
      log.info("StoreSOAPMessages initialized, using " + rmdp);
   } // EO initStoreSOAPMessages method

   /**
    * Is the StoreSOAPMessages functionality in use?
    * 
    * @return boolean true if StoreSOAPMessages is being used, false otherwise.
    */
   public static boolean isON() {
      return rootMessageDirectoryPath != null;
   }

   /**
    * static method to store the first SOAP message for a particular transaction
    * to the file system. Must have invoked {@link #initStoreSOAPMessages()}
    * first.
    * 
    * @param subDirName The name of the sub directory under the
    * {@link #rootMessageDirectoryPath} for this message, intented to indicate
    * the simulator involved. For example, "FM" for "Form Manager", or "FR-1"
    * for "Form Receiver 1".
    * @param msg the message being stored.
    * @param messageType the {@link MessageType} for the message bing stored.
    * Intended to be {@link MessageType#SOAP_IN SOAP_IN} or
    * {@link MessageType#SOAP_IN SOAP_OUT}, but other types will also work.
    * messageFileName will be used as the file name for the
    * message
    * @return {@link Path} of the directory containing the message. Use this
    * path in invocations of {@link #storeMessage(Path, String, MessageType)} to
    * store other messages relating to the same transaction.
    * @throws Exception on errors, for example could not create file or
    * directory.
    */
   public static Path storeMessage(String subDirName, String msg,
      MessageType messageType) throws Exception {
      if (StringUtils.isEmpty(subDirName))
         throw new Exception("Missing/invalid sub directory name");
      String dirName = MSG_DIR_FORMAT.format(new Date());
      Path mp = rootMessageDirectoryPath.resolve(subDirName + fs + dirName);
      storeMessage(mp, msg, messageType);
      return mp;
   }

   /**
    * static method to store subsequent SOAP message(s) for a particular
    * transaction; use {@link #storeMessage(String, String, MessageType)} for
    * the first message.
    * 
    * @param dirName Complete {@link Path} of the directory created to store
    * messages relating to this transaction by {@link #storeMessage(String, String, MessageType)}
    * @param msg the message being stored.
    * @param messageType  the {@link MessageType} for the message bing stored.
    * Intended to be {@link MessageType#SOAP_IN SOAP_IN} or
    * {@link MessageType#SOAP_IN SOAP_OUT}, but other types will also work.
    * messageFileName will be used as the file name for the
    * message
    * @throws IOException on error writing file.
    */
   public static void storeMessage(Path dirName, String msg,
      MessageType messageType) throws IOException {
      File pf = dirName.resolve(messageType.getMessageFileName()).toFile();
      FileUtils.writeStringToFile(pf, msg, CHAR_SET_UTF_8);
      log.debug("SOAP Message stored: " + pf.toString());
   }

} // EO StoreSOAPMessages class
