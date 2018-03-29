/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.util.TS;
import edu.wustl.mir.erl.ihe.util.XmlUtil;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class IntervalDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   /**
    * high date/datetime value. The low date/datetime value is stored in value
    */
   protected String highValue = null;
   /**
    * Type of date/datetime value we are looking for. Currently, only
    * {@link TS#DATE} and {@link TS#DATETIME} are supported.
    */
   protected TS type = null;

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    * @param type the {@link #type} value
    */
   public IntervalDataElement(XPath xpath, String parName, String element,
      String description, String xpathExpression, TS type) {
      super(xpath, parName, element, description, xpathExpression);
      if (type == TS.FULLDATE) {
         log.warn("TS.FULLDATE not supported, using TS.DATE");
         type = TS.DATE;
      }
      if (type == TS.FULLDATETIME) {
         log.warn("TS.FULLDATETIME not supported, using TS.DATETIME");
         type = TS.DATETIME;
      }
      this.type = type;
   }
   
   /**
    * Copy constructor
    * @param other IdDataElement
    */
   public IntervalDataElement(IntervalDataElement other) {
      super();
      move (other, this);
      type = other.type;
   }

   /**
    * @return the {@link #highValue} value.
    */
   public String getHighValue() {
      return highValue;
   }

   /**
    * @param highValue the {@link #highValue} to set
    */
   public void setHighValue(String highValue) {
      this.highValue = highValue;
   }

   /**
    * @return the {@link #type} value.
    */
   public TS getType() {
      return type;
   }
   
   @Override
   public IntervalDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public IntervalDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }

   @Override
   public void loadValue(Element element2) throws Exception {
      value = null;
      highValue = null;
      Node attr = null;
      StringBuilder msg =
         new StringBuilder("Load " + type.getName() + " Interval: ")
            .append(parName + nl);
      Node node = (Node) evaluate(element2, xpathExpression,  XPathConstants.NODE);
      
      // Must be effectiveTime Element
      if (node == null || node.getNodeType() != Node.ELEMENT_NODE ||
          node.getLocalName().equalsIgnoreCase("effectiveTime") == false) {
         msg.append(": effectiveTime element not found");
         log.debug(msg.toString());
         return;
      }
      // ----------------------- low child element
      low: {
         Element[] low = XmlUtil.getFirstLevelChildElementsByName(node, "low");
         if (low.length != 1) {
            msg.append("low Element not found" + nl);
            break low;
         }
         // ---------------------------- pull attributes of node
         NamedNodeMap map = low[0].getAttributes();
         if (map == null) {
            msg.append("low Element had no attributes" +  nl);
            break low;
         } 
         // ---------------- pull value attribute
         attr = map.getNamedItem("value");
         if (attr != null) {
            String av = attr.getNodeValue();
            if (type == TS.DATE) av = av.substring(0, 8);
            value = av;
            msg.append(" low value: ").append(value);
         } else {
            msg.append(" low Element had no value attribute" + nl);
         }
      } // EO low block
      
      // ----------------------- high child element
      high: {
         Element[] high = XmlUtil.getFirstLevelChildElementsByName(node, "high");
         if (high.length != 1) {
            msg.append("high Element not found" + nl);
            break high;
         }
         // ---------------------------- pull attributes of node
         NamedNodeMap map = high[0].getAttributes();
         if (map == null) {
            msg.append("low Element had no attributes" +  nl);
            break high;
         } 
         // ---------------- pull value attribute
         attr = map.getNamedItem("value");
         if (attr != null) {
            String av = attr.getNodeValue();
            if (type == TS.DATE) av = av.substring(0, 8);
            highValue = av;
            msg.append(" high value: ").append(highValue);
         } else {
            msg.append(" high Element had no value attribute" + nl);
         }
      } // EO low block
      
      log.debug(msg);
   } // EO load value 

   @Override
   public void loadWSLog(WSLog wsLog) {      
      // ---------------------------------- load name value pairs
      if (wsLog == null) return;
      if (value == null && highValue == null) return;
      String prefix = "prepopData " + element;
      StringBuilder val = new StringBuilder();
      if (value != null)
         val.append("low ").append(value).append(" ");
      if (highValue != null)
         val.append("high ").append(highValue);
      LoadNameValuePairs.addPair(wsLog, prefix, val,
         LoadNameValuePairs.SOAP_REQUEST);      
      
   } 

} // EO IntervalDataType Class
