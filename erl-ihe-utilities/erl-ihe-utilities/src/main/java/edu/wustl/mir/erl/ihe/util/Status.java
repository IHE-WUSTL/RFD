/*******************************************************************************
 * Copyright (c) 2014  Washington University in St. Louis
 *  All rights reserved. This program and the accompanying 
 *  materials are made available under the terms of the
 *  Apache License, Version 2.0 (the "License");  you may not 
 *  use this file except in compliance with the License.
 * The License is available at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, 
 *  software  distributed under the License is distributed on 
 *  an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS  
 *  OF ANY KIND, either express or implied. See the License 
 *  for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Contributors:
 *    Initial author: Ralph Moulton / MIR WUSM IHE Development Project 
 *    moultonr@mir.wustl.edu
 *******************************************************************************/
package edu.wustl.mir.erl.ihe.util;

import org.apache.log4j.Level;

/**
 * Interface which must be implemented by all Status enums, which allows
 * multiple separate Status enums to be used simultaneously in an application.
 * In addition to these methods, status enums must implement a static method
 * which takes as its only argument the name of one of its members and returns
 * the enum instance for that member cast to the Status interface. For example:
 * 
 * <pre>
 * public static Status getStatus(String name) {
 *    return Enum.valueOf(RFDStatus.class, name);
 * }
 * </pre>
 * 
 * In addition, enums implementing Status must be "registered" with
 * {@link StatusHelper#addCodeSet}. This can be done by the
 * application during startup, or using a static block in the enum itself, for
 * example:
 * 
 * <pre>
 * static {
 *    StatusHelper.addCodeSet(RFDStatus.class);
 * }
 * </pre>
 */
public interface Status {

   /**
    * @return the name of the validation phase for this status code.
    */
   public String getPhaseMsg();
   
   /**
    * @return the status message for this status code
    */
   public String getStatusMsg();

   /**
    * @return phase name and status message;
    */
   public String getMsg();

   /**
    * @return the {@link StatusUpdateType} of this status.
    */
   public StatusUpdateType getUpdateType();

   /**
    * @return a String encoding the status for storage in a database. The
    *         String is of the form CODESETNAME,STATUSCODENAME|
    */
   public String encode();

   /**
    * Implementations may set specific log levels for each status, or return the
    * same level for all statuses. When a status is recorded, a log message at
    * this level will be recorded in the wslog. If the method returns null, no
    * log message will be generated.
    * 
    * @return the log4j log Level for this status.
    */
   public Level getLogLevel();
   
   /**
    * Implementations may conceptualize tests for which a particular status code
    * encapsulates a result for that test. If so, this method would return the
    * appropriate {@link edu.wustl.mir.erl.ihe.util.Result Result} value 
    * indicating the result for the test or test step. If the implementation 
    * does not use this feature, it should return Result.NA for all instances.
    * @return appropriate Result code for this Status instance.
    */
   public Result getResult();
   
   /**
    * Status codes may be grouped by StatusType, for example to organize them
    * in a display
    * @return the StatusType for this code.
    */
   public StatusType getStatusType();

   /**
    * @return the set code for this Phase/Status pair.
    *         see {@link StatusHelper} for details.
    */
   public String getSetCode();
   
} // EO Status interface
