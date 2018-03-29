/**
 * 
 */
package edu.wustl.mir.erl.IHETools.RFD.view;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ComponentSystemEvent;

import org.apache.log4j.Logger;
import org.javatuples.LabelValue;
import org.primefaces.event.FileUploadEvent;

import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;
import edu.wustl.mir.erl.ihe.ws.client.ClientTestRMIClient;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.util.web.FacesUtil;
import edu.wustl.mir.erl.util.web.Valid;

/**
 * View bean specific to FormReceiverCDAExporter tests
 */
@ManagedBean
@ViewScoped
public class FormReceiverCDAExporterTests
   implements Serializable, WSProperties {
   private static final long serialVersionUID = 1L;

   private static ApplicationBean applicationBean =
      ApplicationBean.getInstance();
   private static Logger log = ApplicationBean.getLog();

   private SessionBean sessionBean;

   /**
    * Zero argument constructor.
    */
   public FormReceiverCDAExporterTests() {
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
   private String formId = "";

   /**
    * Message which will be sent.
    */
   private String msg;

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
    * @return {@link #formId}
    */
   public String getFormID() {
      return formId;
   }

   /**
    * Sets {@link #formId} to passed value.
    * 
    * @param string new value
    */
   public void setFormID(String string) {
      formId = string;
   }

   /**
    * @return the {@link #msg} value.
    */
   public String getMsg() {
      return msg;
   }

   ClientTestRMIClient clientTestRMIClient = null;

   private List <LabelValue <String, Object>>
      runTest(List <LabelValue <String, Object>> pars) throws Exception {
      if (clientTestRMIClient == null) {
         int port = Util.getProperties().getInteger("RMI[@port]",
            DEFAULT_RMI_REGISTRY_PORT);
         clientTestRMIClient = new ClientTestRMIClient(port);
      }
      return clientTestRMIClient.runTest(pars);
   }

   private void msgSetup(String msgIn) {
      msg = new Plug(msgIn).set("formId", formId).get();
   }

   // **********************************************************
   // BFDR_E_1_10000_LDS_A
   // **********************************************************
   /**
    * Initialize for this test
    * 
    * @param event ignored
    */
   public void initBFDR_E_1_10000_LDS_A(ComponentSystemEvent event) {
      if (initialized) return;
      Util.invoked(log);
      initialized = true;
      setFormID("BFDR_E_1_10000_LDS_A");
      msgSetup(
         edu.wustl.mir.erl.ihe.rfd.tests.FRCE.BFDR_E_1_10000_LDS_A.FormFiller
            .getRequest());
   }

   /**
    * Submit Form Request Command Button for this test clicked.
    * 
    * @param event ignored
    */
   @SuppressWarnings("cast")
   public void submitBFDR_E_1_10000_LDS_A(ActionEvent event) {
      Util.invoked(log);
      Valid v = new Valid();
      v.URL("testDataForm:fmurl", serverURL, true);
      v.NB("testDataForm:formId", formId);
      if (v.isErrors()) return;
      List <LabelValue <String, Object>> pars = new ArrayList <>();
      List <LabelValue <String, Object>> returns = new ArrayList <>();
      pars.add(new LabelValue <>(LABEL_TEST_STEP,
         (Object) "FRCE.BFDR_E_1_10000_LDS_A/1"));
      pars.add(new LabelValue <>(LABEL_WSSERVER_URL, (Object) serverURL));
      pars.add(new LabelValue <>(LABEL_FORM_ID, (Object) formId));
      try {
         returns = runTest(pars);
      } catch (Exception e) {
         v.error(e.getMessage());
         return;
      }
      try {
         v.error((String) Util.getValueForLabel(LABEL_ERROR, returns));
      } catch (Exception e) {}
      return;
   } // EO submitBFDR_E_1_10000_LDS_I method

   /**
    * Handle CDA Document upload for BFDR_E_1_10000_LDS_A test
    * 
    * @param event {@link FileUploadEvent} instance
    */
   public void uploadBFDR_E_1_10000_LDS_A(FileUploadEvent event) {
      byte[] bytes = event.getFile().getContents();
      Util.invoked(log);
      FacesMessage message = new FacesMessage("Succesful",
         event.getFile().getFileName() + " is uploaded.");
      FacesContext.getCurrentInstance().addMessage(null, message);
      edu.wustl.mir.erl.ihe.rfd.tests.FRCE.BFDR_E_1_10000_LDS_A.CDAInspector cdaInspector =
         new edu.wustl.mir.erl.ihe.rfd.tests.FRCE.BFDR_E_1_10000_LDS_A.CDAInspector();
      String errMsg = cdaInspector.inspectCDADocument(formId, bytes);
      if (errMsg != null) 
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(errMsg));
      WSLog wsLog = cdaInspector.getWSLog();
      if (wsLog != null) 
         applicationBean.receiveWSLogRMI(wsLog);
   }
   

} // EO FormReceiverCDAExporterTests class
