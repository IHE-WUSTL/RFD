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
package edu.wustl.mir.erl.ihe.ws.handlers;

import javax.xml.ws.handler.MessageContext;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.db.TransactionType;

/**
 * Base class for handlers. Logs invocations for methods which are not
 * overridden by the specific handler subclass to the system log.
 * 
 * @param <T> Type of Message Context used in the subclass.
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class BaseHandler <T extends MessageContext> implements WSProperties {
   protected TransactionType transactionType = null;
   protected String handlerName = null;

   private static Logger syslog = Util.getLog();

   /**
    * The handleFault method is invoked for fault message processing. Refer to
    * the description of the handler framework in the JAX-WS specification for
    * full details. 
    * 
    * @param messageContext the message context
    * @return boolean Should handler fault processing continue for the
    * current message?
    */
   public boolean handleFault(T messageContext) {
      syslog.trace("Handler handleFault() called for " + handlerName);
      return true;
   }

   /**
    * Called at the conclusion of a message exchange pattern just prior to the
    * JAX-WS runtime dispatching a message, fault or exception. Refer to the
    * description of the handler framework in the JAX-WS specification for full
    * details.
    * 
    * @param messageContext the Message Context
    */
   public void close(MessageContext messageContext) {
      syslog.trace("Handler close() called for " + handlerName);
   }

   /**
    * Set handler name for logging purposes. This should be called by the
    * subclass constructor.
    * @param type Transaction Type processed by this handler.
    * @param name short human readable name for the specific handler.
    */
   public void setHandlerTypeName(TransactionType type, String name) {
      transactionType = type;
      handlerName = name;
   }

   protected static final String NO_MAP =
      "Message Context has no mapping for ";

}
