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
package edu.wustl.mir.erl.ihe.rfd.cda;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathVariableResolver;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.javatuples.Triplet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import edu.wustl.mir.erl.ihe.rfd.LoadNameValuePairs;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;

/**
 * Encapsulates a single form data element and its mapping to a CDADocument
 * document.
 */
public class DataElement implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;
   
   protected static Map<String, String> snippets;
   
   protected String snippet;

   protected Logger log = Util.getLog();

   /**
    * The {@link XPath} api to use with this data element. All XPath strings
    * used in this DataElement are compiled against this.
    */
   protected XPath xpath;

   /**
    * Data map for XPath variable information used in this DataElement (if any).
    * <ul>
    * <li>Value0 is the variable to be replaced, for example "idOfTheChild".
    * This would appear in the XPath statement with a leading $, for example,
    * "$idOfTheChild".</li>
    * <li>Value1 String is the label extension for the item to be substituted.
    * For example, if the DataElement parName is "myVariable", and it is a
    * CodedDataElement, using "Code" for this value would result in the
    * "myVariableCode" value being substituted for "$idOfTheChild".</li>
    * <li>Value2 DataElement is the DataElement used for the variable.</li>
    * </ul>
    */
   protected List <Triplet <String, String, DataElement>> xpathVariables = new ArrayList <>();
   private boolean loadXpathVariables = false;

   /**
    * html parameter name
    */
   protected String parName;
   /**
    * Human readable short name of data element. Used as label for individual
    * fields or as column header.
    */
   protected String element;
   /**
    * Human readable description of data element.
    */
   protected String description;
   /**
    * XPath expression for this data element, relative to the Node which is
    * passed to it. If empty, the Node passed to the DataElement is used
    * directly.
    */
   protected String xpathExpression;
   /**
    * Value of data element. For the CodedDataElement subclass, this is the
    * value of the code attribute.
    */
   protected String value = null;

   /**
    * boolean, should this DataElement be processed when generating values to
    * substitute for form parameters in {@link CDAPlug}? By default this is
    * true, but it can be set false using {@link #setPlugInForm(boolean)} or
    * {@link #plugOff()}. False for variables which are not important for the
    * form.
    */
   protected boolean plugInForm = true;

   /**
    * boolean, should this DataElement be loaded when pulling data from the CDA
    * document. By default this is true, but it can be set false using
    * {@link #setLoadFromDocument(boolean)} or {@link #loadOff()}. False for
    * variables which are loaded during the calculation of their containing
    * function. 
    */
   protected boolean loadFromDocument = true;

   /**
    * @param xpath The {@link #xpath} value.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param xpathExpression the {@link #xpathExpression} value
    */
   public DataElement(XPath xpath, String parName, String element, String description, String xpathExpression) {
      this.xpath = xpath;
      this.parName = parName;
      this.element = element;
      this.description = description;
      this.xpathExpression = StringUtils.trimToEmpty(xpathExpression);
      snippet = snippets.get(this.getClass().getSimpleName().toUpperCase());
   }

   protected DataElement() {
      xpath = null;
      parName = null;
      element = null;
      description = null;
      xpathExpression = "";
   }
   
   /**
    * Copy constructor
    * @param other DataElement or subclass
    */
   public DataElement(DataElement other) {
      this();
      move(other, this);
   }

   /**
    * @return the {@link #log} value.
    */
      Logger getLog() {
      return log;
   }

   /**
    * @param log the {@link #log} to set
    */
      void setLog(Logger log) {
      this.log = log;
   }

   /**
    * @return the {@link #xpathVariables} value.
    */
      List <Triplet <String, String, DataElement>> getXpathVariables() {
      return xpathVariables;
   }

   /**
    * @return the {@link #parName} value.
    */
   public String getParName() {
      return parName;
   }
   
   /**
    * @return "suffix" of parameter name, from $. For example, if parName is
    * "indl$ProcedureCode" returns "$ProcedureCode". If no $ in parName, returns
    * parName
    */
   public String getParNameSuffix() {
      if (parName.contains("$")) 
         return "$" + StringUtils.substringAfter(parName, "$");
      return parName;
   }

   /**
    * @param parName the {@link #parName} to set
    */
   public void setParName(String parName) {
      this.parName = parName;
   }

   /**
    * @return the {@link #element} value.
    */
   public String getElement() {
      return element;
   }

   /**
    * @param element the {@link #element} to set
    */
   public void setElement(String element) {
      this.element = element;
   }

   /**
    * @return the {@link #description} value.
    */
   public String getDescription() {
      return description;
   }

   /**
    * @param description the {@link #description} to set
    */
   public void setDescription(String description) {
      this.description = description;
   }

   /**
    * @return the {@link #xpathExpression} value.
    */
   public String getXpathExpression() {
      return xpathExpression;
   }

   /**
    * @param xpathExpression the {@link #xpathExpression} to set
    */
   public void setXpathExpression(String xpathExpression) {
      this.xpathExpression = xpathExpression;
   }

   /**
    * @return the {@link #value} value.
    */
   public String getValue() {
      return value;
   }

   /**
    * @param value the {@link #value} to set
    */
   public void setValue(String value) {
      this.value = value;
   }

   /**
    * @return the {@link #xpath} value.
    */
   public XPath getXPath() {
      return xpath;
   }

   /**
    * @param xpath the {@link #xpath} to set
    */
      void setXpath(XPath xpath) {
      this.xpath = xpath;
   }

   /**
    * @return the {@link #plugInForm} value.
    */
   public boolean isPlugInForm() {
      return plugInForm;
   }

   /**
    * @param plugInForm the {@link #plugInForm} to set
    */
   public void setPlugInForm(boolean plugInForm) {
      this.plugInForm = plugInForm;
   }

   /**
    * Turns off {@link #plugInForm}.
    * 
    * @return this DataElement, for method chaining.
    */
   public DataElement plugOff() {
      plugInForm = false;
      return this;
   }

   /**
    * @return the {@link #loadFromDocument} value.
    */
   public boolean isLoadFromDocument() {
      return loadFromDocument;
   }

   /**
    * @param loadFromDocument the {@link #loadFromDocument} to set
    */
   public void setLoadFromDocument(boolean loadFromDocument) {
      this.loadFromDocument = loadFromDocument;
   }

   /**
    * Turns off {@link #loadFromDocument}.
    * 
    * @return this DataElement, for method chaining
    */
   public DataElement loadOff() {
      loadFromDocument = false;
      return this;
   }
   /**
    * @return the {@link #snippet} value.
    */
   public String getSnippet() {
      return snippet;
   }
   
   /**
    * @return the snippet for this data element, with the "[PN]" parameters
    * resolved. This will work for simple data element types. It should be
    * Overridden for complex types.
    */
   public String getResolvedSnippet() {
      return StringUtils.replace(snippet, "[PN]", parName);
   }

   /**
    * @param snippet the {@link #snippet} to set. <b>Note: </b> This method
    * sets the snippet directly to the passed value. Use {@link #loadSnippet}
    * to load a snippet from the Snippet.html file.
    * @return this DataElement, for method chaining.
    */
   public DataElement  setSnippet(String snippet) {
      this.snippet = snippet;
      return this;
   }

   /**
    * @param key of the snippet to load from the Snippet.html file.
    * Use {@link #setSnippet} to load a snippet string directly.
    * @return this DataElement, for method chaining.
    */
   public DataElement  loadSnippet(String key) {
      this.snippet = snippets.get(key);
      return this;
   }

   /**
    * Add XPath variable to DataElement.
    * 
    * @param toReplace String in function to replace, for example
    * "idOfTheChild".
    * @param labelExtension variable label extension for item replacing, for
    * example "Code" for "parNameCode".
    * @param dataElement to use for replacement.
    * @return this DataElement, for method chaining
    */
   public DataElement addXpathVariable(String toReplace, String labelExtension, DataElement dataElement) {
      xpathVariables.add(new Triplet <String, String, DataElement>(toReplace, labelExtension, dataElement));
      log.trace(parName + " add variable " + toReplace + " -> " + dataElement.getParName() + labelExtension);
      loadXpathVariables = true;
      return this;
   }

   
   /**
    * Loads value for this DataElement from the passed {@link Element}. 
    * 
    * @param element2 document to search
    * @throws Exception on error.
    */
   public void loadValue(Element element2) throws Exception {
      String val = (String) evaluate(element2, xpathExpression, XPathConstants.STRING);
      log.debug("Load: " + parName + " [" + val + "]");
      value = val;
   }
   
   /**
    * Loads values for this DataElement to the passed {@link WSLog} instance.
    * <b>Note: </b>Must run AFTER {@link #loadValue(Element)}.
    * @param wsLog to load.
    */
   public void loadWSLog(WSLog wsLog) {
      LoadNameValuePairs.addPair(wsLog, "prepopData " + element, value, LoadNameValuePairs.SOAP_REQUEST);
   }

   /**
    * XPath variables
    */
   private Map <String, Object> vars = new HashMap <>();

   /**
    * Encapsulates XPath evaluation, including:
    * <ul>
    * <li>Attaches {@link XPathVariableResolver} if needed.</li>
    * <li>Compiles the expression string</>
    * <li>Evaluates the node using the expression</li>
    * </ul>
    * If the xpExperssion is blank, returns node;
    * 
    * @param node to evaluate.
    * @param xpExpression String XPath expression.
    * @param returnType QName, must be one of the valid return types in
    * {@link XPathConstants}.
    * @return The Object that is the result of evaluating the expression and
    * converting the result to returnType.
    */
   protected Object evaluate(Node node, String xpExpression, QName returnType){
      if (StringUtils.isBlank(xpExpression)) return node;
      try {
      xpathResolver();
      XPathExpression expr = xpath.compile(xpExpression);
      return expr.evaluate(node, returnType);
      } catch (Exception e) {
         log.warn("DataElement#evaluate error " + xpExpression + " " + e.getMessage());
         return null;
      }
   }

   /**
    * Attaches {@link XPathVariableResolver} to {@link XPath}, using
    * {@link #vars}
    */
   protected void xpathResolver() {
      if (loadXpathVariables) {
         for (Triplet <String, String, DataElement> xpv : xpathVariables) {
            DataElement de = xpv.getValue2();
            String in = "${" + de.getParName() + xpv.getValue1() + "}";
            String v = new CDAPlug(in).setDataElement(de).get();
            vars.put(xpv.getValue0(), v);
         }
         xpath.setXPathVariableResolver(new XPathVariableResolver() {
            @Override
            public Object resolveVariable(QName name) {
               return vars.get(name.getLocalPart());
            }
         });
         loadXpathVariables = false;
      }
   }

   /**
    * Method moves {@link DataElement} properties from one instance of a
    * DataElement or subclass to another.
    * <ul>
    * <li>Does not move {@link #value}, instead sets it to null.</li>
    * <li>Creates a new {@link #xpath} using the existing
    * {@link NamespaceContext}.</li>
    * <li>data elements referenced in the two instances refer to the same
    * instances, but all Collection and Container instances are new.</li>
    * 
    * @param in source DataElement or subclass.
    * @param out destination DataElement or subclass.
    */
   public static <T extends DataElement> void move(T in, T out) {

      // Generate new XPath, passing NamespaceContext
      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath outXpath = xpathFactory.newXPath();
      XPath inXpath = in.getXPath();
      NamespaceContext nsc = inXpath.getNamespaceContext();
      if (nsc != null) outXpath.setNamespaceContext(nsc);

      out.setXpath(outXpath);

      out.setLog(in.getLog());

      // xpathVariables, using new List & list elements
      List <Triplet <String, String, DataElement>> xpathVariables = in.getXpathVariables();
      for (Triplet <String, String, DataElement> xpathVariable : xpathVariables) {
         out.addXpathVariable(xpathVariable.getValue0(), xpathVariable.getValue1(), xpathVariable.getValue2());
      }

      out.setParName(in.getParName());
      out.setElement(in.getElement());
      out.setDescription(in.getDescription());
      out.setXpathExpression(in.getXpathExpression());
      out.setValue(null);
      out.setPlugInForm(in.isPlugInForm());
      out.setLoadFromDocument(in.isLoadFromDocument());
      out.setSnippet(in.getSnippet());

   }
   
   /**
    * Determines actual type of passed DataElement or subclass and performs
    * the appropriate loadValue method
    * @param dataElement or subclass
    * @param element node
    * @throws Exception on error.
    */
   public static void downLoadValue(DataElement dataElement, Element element) throws Exception {
      if (dataElement instanceof CodedDataElement) {
         CodedDataElement de = (CodedDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof ValueDataElement) {
         ValueDataElement de = (ValueDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof IdDataElement) {
         IdDataElement de = (IdDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof IntervalDataElement) {
         IntervalDataElement de = (IntervalDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof PersonNameDataElement) {
         PersonNameDataElement de = (PersonNameDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof AddrDataElement) {
         AddrDataElement de = (AddrDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof CODDataElement) {
         CODDataElement de = (CODDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof SequenceDataElement) {
         SequenceDataElement <?> de = (SequenceDataElement <?>) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof FunctionDataElement) {
         FunctionDataElement de = (FunctionDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      if (dataElement instanceof ProcedureDataElement) {
         ProcedureDataElement de = (ProcedureDataElement) dataElement;
         de.loadValue(element);
         return;
      }
      dataElement.loadValue(element);
   }
   
   /**
    * Determines actual type of passed DataElement or subclass and performs
    * the appropriate loadValue method
    * @param dataElement or subclass
    * @param wsLog WSLog
    */
   public static void downLoadWSLog(DataElement dataElement, WSLog wsLog) {
      if (dataElement instanceof CodedDataElement) {
         CodedDataElement de = (CodedDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof ValueDataElement) {
         ValueDataElement de = (ValueDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof IdDataElement) {
         IdDataElement de = (IdDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof IntervalDataElement) {
         IntervalDataElement de = (IntervalDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof PersonNameDataElement) {
         PersonNameDataElement de = (PersonNameDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof AddrDataElement) {
         AddrDataElement de = (AddrDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof CODDataElement) {
         CODDataElement de = (CODDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof SequenceDataElement) {
         SequenceDataElement <?> de = (SequenceDataElement <?>) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof FunctionDataElement) {
         FunctionDataElement de = (FunctionDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      if (dataElement instanceof ProcedureDataElement) {
         ProcedureDataElement de = (ProcedureDataElement) dataElement;
         de.loadWSLog(wsLog);
         return;
      }
      dataElement.loadWSLog(wsLog);
   }

   /**
    * Adds node sequence data to parName and element of dataElement. Override in
    * more complex elements
    * @param nodeSequence to append.
    */
   public void sequence(String nodeSequence) {
      parName += nodeSequence;
      element += nodeSequence;
   }
   
   static {
      snippets = new HashMap<>();
      Pattern keyPattern = Pattern.compile("^\\s*[A-Z|0-9|_|\\-]+\\s*$");
      
      try (InputStream in = DataElement.class.getResourceAsStream("Snippets.html");
           BufferedReader br = new BufferedReader(new InputStreamReader(in))) {
         Logger log = WSServer.getLog();
         String key = null, newKey = null, line = null;
         StringBuilder snippet = new StringBuilder();
         while ((line = br.readLine()) != null) {
            line = StringUtils.substringBefore(line, "//");
            if (StringUtils.isBlank(line)) continue;
            if (keyPattern.matcher(line).matches()) newKey = line.trim();
            else newKey = null;
            if (newKey != null) {
               if (key != null) {
                  snippets.put(key, snippet.toString());
                  snippet = new StringBuilder();
                  log.trace(key + " snipped loaded");
               }
               key = newKey;
               continue;
            }
            if (key != null) snippet.append(line).append(nl);
         } // EO while
         if (key != null) {
            snippets.put(key, snippet.toString());
            log.trace(key + " snipped loaded");
         }
         log.info("Snippet File loaded");
      } catch (Exception e) {
         Util.exit("snippet loading: " + Util.getEM(e));
      }
   } // EO static block
   
   /**
    * Copy class for DataElement
    */
   public static class Copy implements DataElementCopy <DataElement> {

      /* (non-Javadoc)
       * @see edu.wustl.mir.erl.ihe.rfd.cda.DataElementCopy#copy(edu.wustl.mir.erl.ihe.rfd.cda.DataElement)
       */
      @Override
      public DataElement copy(DataElement dataElement) {
         return new DataElement(dataElement);
      }

      
   }

} // EO DataElement class
