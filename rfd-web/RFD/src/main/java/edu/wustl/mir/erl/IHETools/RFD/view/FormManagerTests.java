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
package edu.wustl.mir.erl.IHETools.RFD.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.client.ClientTestRMIClient;
import edu.wustl.mir.erl.util.web.FacesUtil;
import edu.wustl.mir.erl.util.web.Valid;

/**
 * View bean specific to FormManagerSimulator tests
 */
@ManagedBean
@ViewScoped
public class FormManagerTests implements Serializable, WSProperties {
   private static final long serialVersionUID = 1L;

   private static ApplicationBean applicationBean = ApplicationBean
      .getInstance();
   private static Logger log = ApplicationBean.getLog();

   private SessionBean sessionBean;

   /**
    * Zero argument constructor.
    */
   public FormManagerTests() {
      sessionBean = FacesUtil.getManagedBean("sessionBean");
   }

   // **********************************************************
   // Global: Properties
   // **********************************************************

   /**
    * boolean, has this bean been initialized?
    */
   private boolean initialized = false;
   /**
    * String form of the URL for the Form Manager Actor under test.
    */
   private String serverURL = "";
   /**
    * String form id to be used in RetrieveFormRequest.workflowData.formID
    * element.
    */
   private String formID = "";
   /**
    * String form of the URL returned by the Form Manager Actor under test in
    * response to a ITI-34 RetrieveFormRequest, in the
    * RetrieveFormResponse.form.URL element.
    */
   private String formURL = "";

   // **********************************************************
   // Global: Getters and Setters
   // **********************************************************

   /**
    * @return {@link #serverURL}
    */
   public String getServerURL() {
      return serverURL;
   }

   /**
    * Sets {@link #serverURL} to passed value.
    * 
    * @param string new value
    */
   public void setServerURL(String string) {
      serverURL = string;
   }

   /**
    * @return {@link #formID}
    */
   public String getFormID() {
      return formID;
   }

   /**
    * Sets {@link #formID} to passed value.
    * 
    * @param string new value
    */
   public void setFormID(String string) {
      formID = string;
   }

   /**
    * @return {@link #formURL}
    */
   public String getFormURL() {
      return formURL;
   }

   /**
    * Sets {@link #formURL} to passed value.
    * 
    * @param string new value
    */
   public void setFormURL(String string) {
      formURL = string;
   }

   /**
    * @return boolean, true if formURL should be rendered
    */
   public boolean renderFormURL() {
      return StringUtils.isNotBlank(formURL);
   }

   ClientTestRMIClient clientTestRMIClient = null;

   private List <LabelValue <String, Object>> runTest(
      List <LabelValue <String, Object>> pars) throws Exception {
      if (clientTestRMIClient == null) {
         int port =
            Util.getProperties().getInteger("RMI[@port]",
               DEFAULT_RMI_REGISTRY_PORT);
         clientTestRMIClient = new ClientTestRMIClient(port);
      }
      return clientTestRMIClient.runTest(pars);
   }

   // **********************************************************
   // RFD_0_10000 test
   // **********************************************************

   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initRFD_0_10000(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("RFD_0_10000");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitRFD_0_10000(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.RFD_0_10000/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitRFD_0_10000_FM method


   // **********************************************************
   // RFD_1_10000 test
   // **********************************************************

   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initRFD_1_10000(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("RFD_1_10000");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitRFD_1_10000(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.RFD_1_10000/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitRFD_1_10000 method

// **********************************************************
// HW_1_10000_HWS_I
// **********************************************************
   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initHW_1_10000_HWS_I(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("HW_1_10000_HWS_I");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitHW_1_10000_HWS_I(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.HW_1_10000_HWS_I/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitHW_1_10000_HWS_I method
   

// **********************************************************
// VRDR_1_10000_MS_VRDR_I
// **********************************************************
   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initVRDR_1_10000_MS_VRDR_I(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("VRDR_1_10000_MS_VRDR_I");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitVRDR_1_10000_MS_VRDR_I(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.VRDR_1_10000_MS_VRDR_I/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitVRDR_1_10000_MS_VRDR_I method
   

// **********************************************************
// VRDR_1_10000_VRDR_I
// **********************************************************
   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initVRDR_1_10000_VRDR_I(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("VRDR_1_10000_VRDR_I");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitVRDR_1_10000_VRDR_I(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.VRDR_1_10000_VRDR_I/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitVRDR_1_10000_VRDR_I method

// **********************************************************
// BFDR_E_1_10000_LDS_I
// **********************************************************
   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initBFDR_E_1_10000_LDS_I(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("BFDR_E_1_10000_LDS_I");
      setFormURL("");
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitBFDR_E_1_10000_LDS_I(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formID", formID);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP, (Object) "FM.BFDR_E_1_10000_LDS_I/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formID));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         setFormURL((String) Util.getValueForLabel(LABEL_FORM_URL, returns));
      } catch (Exception e) {
         v.error("testDataForm", e.getMessage());
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {
      }
      return;
   } // EO submitBFDR_E_1_10000_LDS_I method
   
   

} // EO FormManagerTests class
