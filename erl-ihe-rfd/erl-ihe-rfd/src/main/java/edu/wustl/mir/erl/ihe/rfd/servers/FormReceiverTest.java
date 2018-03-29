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
package edu.wustl.mir.erl.ihe.rfd.servers;

import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.SubmitFormResponseType;

import javax.xml.ws.soap.SOAPFaultException;

import edu.wustl.mir.erl.ihe.util.TestFailException;

/**
 * Base class for specific FormReceiver tests
 */
public class FormReceiverTest extends Test {
   private static final long serialVersionUID = 1L;

   /**
    * Default implementation of submitForm transaction. This is overriden in the
    * subclass for the particular test, if that test implements this
    * transaction. If not, this will throw the
    * {@link UnsupportedOperationException}
    * 
    * @param body {@link AnyXMLContentType}, transaction request body
    * @return {@link SubmitFormResponseType}, transaction response body
    * @throws SOAPFaultException for SOAP Faults
    * @throws UnsupportedOperationException if not implemented for this test
    * @throws TestFailException if one or more test requirements are not met.
    */
   public SubmitFormResponseType submitForm(AnyXMLContentType body)
      throws SOAPFaultException, UnsupportedOperationException,
      TestFailException {
      throw new UnsupportedOperationException();
   }

} // EO FormReceiverTest class
