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
package edu.wustl.mir.erl.ihe.rfd.tests.FRCE.BFDR_E_1_10000_LDS_A;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.BindingProvider;

import org.apache.commons.io.IOUtils;
import org.javatuples.LabelValue;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.rfd.clients.FormReceiverServiceClient;
import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.client.ClientTest;
import edu.wustl.mir.erl.ihe.ws.db.LogTransaction;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.handlers.HandlerUtil;
import ihe.iti.rfd._2007.AnyXMLContentType;
import ihe.iti.rfd._2007.RFDFormReceiverPortType;
import ihe.iti.rfd._2007.SubmitFormResponseType;

/**
 *
 */
public class FormFiller extends ClientTest {

   private static String request;
   /**
    * @return request String for this test.
    */
   public static String getRequest() {
      return request;
   }
   
   static {
      try {
         request =
            IOUtils.toString(FormFiller.class.getResource("SubmitFormRequest.xml"),
               CHAR_SET_UTF_8);
      } catch (Exception e) {
         Util.exit(Util.getEM(e));
      }
   }
   
   /**
    * Constructor should only be called from {@link ClientTest#initialize}
    */
   public FormFiller() {
      setAction(RFDFormReceiverPortType.class, "SubmitForm");
   }

   /**
    * @see edu.wustl.mir.erl.ihe.ws.client.ClientTest#runTest(java.util.List)
    */
   @Override
   @SuppressWarnings({ "cast" })
   public List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars) {
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      LogTransaction logTran = null;
      try {
         // get passed parameters
         String url = (String) Util.getValueForLabel(LABEL_WSSERVER_URL, pars);
         String formID = (String) Util.getValueForLabel(LABEL_FORM_ID, pars);
         // create form request
         AnyXMLContentType submitFormRequest = new AnyXMLContentType();
         String str = new Plug(request).set("formId", formID).get();
         Element element = XmlUtil.strToElement(str);
         submitFormRequest.getAny().add(element);

         // get port, send-receive
         FormReceiverServiceClient service = new FormReceiverServiceClient(url);
         RFDFormReceiverPortType port = service.getPort();
         logTran = HandlerUtil.initializeSOAPClientHandler((BindingProvider) port,
            (ClientTest) this, url);
         WSLog wsLog = logTran.getWsLog();
         
         LoadNameValuePairs.submitFormRequest(submitFormRequest, wsLog);
         
         SubmitFormResponseType submitFormResponse =
            port.submitForm(submitFormRequest);
         
         LoadNameValuePairs.submitFormResponse(submitFormResponse, wsLog);

         logTran.close();

      } catch (Exception e) {
         returns.add(new LabelValue <String, Object>(LABEL_ERROR, (Object) Util
            .getEM(e)));
      }
      return returns;
   } // EO runTest method

   

} // EO FormFiller class
