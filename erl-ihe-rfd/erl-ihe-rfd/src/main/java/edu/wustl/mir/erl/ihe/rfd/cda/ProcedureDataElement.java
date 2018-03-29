/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.util.TS;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * {@code <Procedure>} DataElement
 */
public class ProcedureDataElement extends DataElement {
   private static final long serialVersionUID = 1L;
   
   /**
    *  /procedure@classCode value
    */
   protected String classCode = null;
   /**
    *  /procedure@moodCode value
    */
   protected String moodCode = null;
   /**
    *  /procedure/id (first one if more than one
    */
   protected IdDataElement id;
   /**
    *  /procedure/code coded value
    */
   protected CodedDataElement code;
   /**
    *  /procedure/methodCode coded value
    */
   protected CodedDataElement methodCode;
   /**
    *  /procedure/value value element (not always present)
    */
   protected ValueDataElement observationValue;
   /**
    *  /procedure/text/reference@value value
    */
   protected String reference = null;
   /**
    *  /procedure/statusCode@code value
    */
   protected String statusCode = null;
   /**
    *  /procedure/effectiveTime interval value
    */
   protected IntervalDataElement interval;
   
   /**
    * Create new procedure data element
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public ProcedureDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression) {
      super(xpath, parName, element, description, xpathExpression);
      
      id = new IdDataElement(xpath, parName + "-id", element + "-id",
         description + "-id", "nl:id");
      
      code = new CodedDataElement(xpath, parName + "-code", element + "-code",
         description + "-code", "nl:code");
      
      methodCode = new CodedDataElement(xpath, parName + "-methodCode", element + "-methodCode",
         description + "-methodCode", "nl:methodCode");
      
      observationValue = new ValueDataElement(xpath, parName + "-value", element + "-value",
         description + "-value", "nl:value");
      
      interval = new IntervalDataElement(xpath, parName + "-interval", 
         element + "-interval", description + "-interval", "nl:effectiveTime", 
         TS.DATETIME);
   }
   
   /**
    * Copy constructor
    * @param other ProcedureDataElement
    */
   public ProcedureDataElement (ProcedureDataElement other) {
      super();
      move(other, this);
      id = new IdDataElement(other.id);
      code = new CodedDataElement(other.code);
      methodCode = new CodedDataElement(other.methodCode);
      observationValue = new ValueDataElement(other.observationValue);
      interval = new IntervalDataElement(other.interval);
   }
   
   @Override
   public ProcedureDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public ProcedureDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }
   
   /**
    * @return the {@link #classCode} value.
    */
   public String getClassCode() {
      return classCode;
   }

   /**
    * @param classCode the {@link #classCode} to set
    */
   public void setClassCode(String classCode) {
      this.classCode = classCode;
   }

   /**
    * @return the {@link #moodCode} value.
    */
   public String getMoodCode() {
      return moodCode;
   }

   /**
    * @param moodCode the {@link #moodCode} to set
    */
   public void setMoodCode(String moodCode) {
      this.moodCode = moodCode;
   }

   /**
    * @return the {@link #id} value.
    */
   public IdDataElement getId() {
      return id;
   }

   /**
    * @param id the {@link #id} to set
    */
   public void setId(IdDataElement id) {
      this.id = id;
   }

   /**
    * @return the {@link #code} value.
    */
   public CodedDataElement getCode() {
      return code;
   }

   /**
    * @param code the {@link #code} to set
    */
   public void setCode(CodedDataElement code) {
      this.code = code;
   }

   /**
    * @return the {@link #methodCode} value.
    */
   public CodedDataElement getMethodCode() {
      return methodCode;
   }

   /**
    * @param methodCode the {@link #methodCode} to set
    */
   public void setMethodCode(CodedDataElement methodCode) {
      this.methodCode = methodCode;
   }

   /**
    * @return the {@link #observationValue} value.
    */
   public ValueDataElement getObservationValue() {
      return observationValue;
   }

   /**
    * @param observationValue the {@link #observationValue} to set
    */
   public void setObservationValue(ValueDataElement observationValue) {
      this.observationValue = observationValue;
   }

   /**
    * @return the {@link #reference} value.
    */
   public String getReference() {
      return reference;
   }

   /**
    * @param reference the {@link #reference} to set
    */
   public void setReference(String reference) {
      this.reference = reference;
   }

   /**
    * @return the {@link #statusCode} value.
    */
   public String getStatusCode() {
      return statusCode;
   }

   /**
    * @param statusCode the {@link #statusCode} to set
    */
   public void setStatusCode(String statusCode) {
      this.statusCode = statusCode;
   }

   /**
    * @return the {@link #interval} value.
    */
   public IntervalDataElement getInterval() {
      return interval;
   }

   /**
    * @param interval the {@link #interval} to set
    */
   public void setInterval(IntervalDataElement interval) {
      this.interval = interval;
   }

   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      classCode = null;
      moodCode = null;
      reference = null;
      statusCode = null;
      Node attr = null;
      // Look for Procedure node
      StringBuilder msg = new StringBuilder("Load Procedure: ").append(parName);
      Node node = (Node) evaluate(element2, xpathExpression, XPathConstants.NODE);
      if (node == null) {
         msg.append(": node not found");
         log.debug(msg.toString());
         return;
      }
      if ((node instanceof Element) == false){
         msg.append(": node is not Element");
         log.debug(msg.toString());
         return;
      }

      // ---------------------------- pull attributes of node
      NamedNodeMap map = node.getAttributes();
      if (map == null) msg.append(": node had no attributes" + nl);
      else {

         // ----------------------------------- get attribute values
         attr = map.getNamedItem("classCode");
         if (attr != null) {
            classCode = attr.getNodeValue();
            msg.append(" classCode: ").append(classCode);
         }
         attr = map.getNamedItem("moodCode");
         if (attr != null) {
            moodCode = attr.getNodeValue();
            msg.append(" moodCode: ").append(moodCode);
         }
      }
      
      id.loadValue((Element) node);
      code.loadValue((Element) node);
      methodCode.loadValue((Element) node);
      observationValue.loadValue((Element) node);
      
      reference = (String) evaluate(node, "nl:text/nl:reference/@value", XPathConstants.STRING);
      statusCode = (String) evaluate(node, "nl:statusCode/@code", XPathConstants.STRING);
      
      interval.loadValue((Element) node);

      StringBuilder val = new StringBuilder();
      if (StringUtils.isNotBlank(classCode))
            val.append(" class: ").append(classCode);
      if (StringUtils.isNotBlank(moodCode))
         val.append(" mood: ").append(moodCode);
      if (StringUtils.isNotBlank(reference))
         val.append(" reference: ").append(reference);
      if (StringUtils.isNotBlank(statusCode))
         val.append(" status: ").append(statusCode);
      String s = code.getCode() + " " + code.getDisplayName() + " " + code.getCodeSystem();
      if (StringUtils.isNotBlank(s))
         val.append("<br/>code ").append(s);
      s = methodCode.getCode() + " " + methodCode.getDisplayName() + " " + methodCode.getCodeSystem();
      if (StringUtils.isNotBlank(s))
         val.append("<br/>methodCode ").append(s);
      s = observationValue.getValue();
      if (StringUtils.isNotBlank(s))
         val.append("<br/>observation value ").append(s);
      s = id.getValue() + " extension: " + id.getExtension();
      if (id.getValue() != null)
         val.append("<br/>id: ").append(s);
      s = interval.getValue() + " " + interval.getHighValue();
      if (StringUtils.isNotBlank(s))
         val.append("<br/>start/stop times ").append(s);
      value = val.toString();
      
      log.debug(msg);
   } // EO loadValue method
   
   @Override
   public void sequence(String nodeSequence) {
      super.sequence(nodeSequence);
      code.sequence(nodeSequence);
      methodCode.sequence(nodeSequence);
      observationValue.sequence(nodeSequence);
      id.sequence(nodeSequence);
      interval.sequence(nodeSequence);
   }
   
   @Override
   public void loadWSLog(WSLog wsLog) {
      if (classCode == null) return;
      StringBuilder val = new StringBuilder(element);
      if (StringUtils.isNotBlank(classCode))
            val.append(" class ").append(classCode);
      if (StringUtils.isNotBlank(moodCode))
         val.append(" mood ").append(moodCode);
      if (StringUtils.isNotBlank(reference))
         val.append(" reference ").append(reference);
      if (StringUtils.isNotBlank(statusCode))
         val.append(" status ").append(statusCode + nl);
      String s = code.getCode() + " " + code.getDisplayName() + " " + code.getCodeSystem();
      if (StringUtils.isNotBlank(s))
         val.append("code ").append(s + nl);
      s = methodCode.getCode() + " " + methodCode.getDisplayName() + " " + methodCode.getCodeSystem();
      if (StringUtils.isNotBlank(s))
         val.append("methodCode ").append(s + nl);
      s = observationValue.getValue();
      if (StringUtils.isNotBlank(s)) 
         val.append("obs ").append(s + nl);
      s = id.getValue() + " " + id.getExtension();
      if (StringUtils.isNotBlank(s))
         val.append("id ").append(s + nl);
      s = interval.getValue() + " " + interval.getHighValue();
      if (StringUtils.isNotBlank(s))
         val.append("start/stop ").append(s);
      LoadNameValuePairs.addPair(wsLog, "prepopData " + this.element, val,
         LoadNameValuePairs.SOAP_REQUEST);
      value = val.toString();
   }
   
   /** Copy class for ProcedureDataElement */
   public static class Copy implements DataElementCopy <ProcedureDataElement> {

      @Override
      public ProcedureDataElement copy(ProcedureDataElement dataElement) {
         return new ProcedureDataElement(dataElement);
      }      
   }
      

} // EO ProcedureDataElement class
