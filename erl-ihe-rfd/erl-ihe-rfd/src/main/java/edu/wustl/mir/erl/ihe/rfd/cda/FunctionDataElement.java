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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.xml.xpath.XPath;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import edu.wustl.mir.erl.ihe.util.Plug;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;

/**
 * Subclass of DataElement to handle function calculations. The value property
 * contains the return value of the function. Does not have its own
 * xpathExpression.
 */
public class FunctionDataElement extends DataElement {
   private static final long serialVersionUID = 1L;

   /**
    * function replacement mappings for this function.
    */
   private Map<String, String> maps = new LinkedHashMap<>();
   
   /**
    * DataElements serving as variables for this function.
    */
   List<Variable> variables = new ArrayList<>();

   /**
    * String, giving the javascript function that determines the value of the
    * DataElement, including substitution parameters in the form ${parameter} to
    * be filled in from the variables
    */
   protected String function;

   /**
    * The default return value if the function fails. 
    */
   protected String defaultValue = "Function failed";

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    */
   public FunctionDataElement(XPath xpath, String parName, String element, String description) {
      super(xpath, parName, element, description, "");
   }

   /**
    * @param xpath The {@link XPath} api to use with this data element.
    * @param parName the {@link #parName} value.
    * @param element the {@link #element} value
    * @param description the {@link #description} value
    * @param function javascript function to calculate value
    */
   public FunctionDataElement(XPath xpath, String parName, String element, String description, String function) {
      super(xpath, parName, element, description, "");
      this.function =  normalizeFunction(function);
   }

   /**
    * @return the {@link #function} value.
    */
   public String getFunction() {
      return function;
   }

   /**
    * @param function the {@link #function} to set
    */
   public void setFunction(String function) {
      this.function = normalizeFunction(function);
   }

   /**
    * @return the {@link #defaultValue} value.
    */
   public String getDefaultValue() {
      return defaultValue;
   }

   /**
    * @param defaultValue the {@link #defaultValue} to set
    */
   public void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
   }
   
   @Override
   public FunctionDataElement loadSnippet(String key) {
      super.loadSnippet(key);
      return this;
   }

   @Override
   public FunctionDataElement setSnippet(String snippet) {
      super.setSnippet(snippet);
      return this;
   }
   
   
   
   /**
    * routine for Function Data Elements adds insert of contained data elements
    * in string.
    */
   @Override
   public String getResolvedSnippet() {
      String ws = snippet;
      if (ws.contains("[VARIABLES]")) {
         StringBuilder des = new StringBuilder();
         for(Variable va : variables) {
            des.append(va.getDataElement().getResolvedSnippet()).append(nl);
         }
         ws = StringUtils.replace(ws, "[VARIABLES]", des.toString());
      }
      if (ws.contains("[VARIABLEXPATHS]")) {
         StringBuilder xps = new StringBuilder();
         for (Variable va : variables) {
            DataElement de = va.getDataElement();
            String xp = snippets.get("VARIABLE_XPATH");
            xp = new Plug(xp)
                 .set("Suffix", de.getParNameSuffix())
                 .set("XpathExpression", StringUtils.replace(de.getXpathExpression(), "nl:", ""))
                 .get();
            xps.append(xp);
         }
         ws = StringUtils.replace(ws, "[VARIABLEXPATHS]", xps.toString());
      }
      return StringUtils.replace(ws, "[PN]", parName);
   }


   /**
    * Replacement pairs for {@link #normalizeFunction(String)} method. Left
    * string is what is replaced, right string is replacement.
    */
   private static String[][] replacements =
      new String[][] { 
         { "IF", "if" }, 
         { "AND", "&&" }, 
         { "OR", "||" }, 
         { "AND", "&&" }, 
         { "NOT", "!" }, 
         { "SHALL", "" }, 
         { ",", "" }, 
         { "THEN", "{" }, 
         { "ELSE", "} else {" } 
         };


   @Override
   public void loadValue(Element element2) throws Exception {

      String func = function;
      log.trace("Original = " + func);
      
      // ------------------------- resolve function mappings
      Iterator <Entry <String, String>> itr = maps.entrySet().iterator();
      while (itr.hasNext()) {
         Entry<String, String> map = itr.next();
         func = StringUtils.replace(func, map.getKey(),map.getValue());
      }
      log.trace("Function maps = " + func);
      
      // ------------------------------- load local variables
      for (Variable variable : variables) {
         DataElement dataElement = variable.getDataElement();
         if (dataElement.isLoadFromDocument() == false)
            DataElement.downLoadValue(dataElement, element2);
      }
      
      // ----------------------------- resolve maps in variables
      for (Variable variable : variables) {
         Map<String, String> mappings = variable.getMappings();
         Iterator <Entry <String, String>> iterator = mappings.entrySet().iterator();
         while (iterator.hasNext()) {
            Entry<String, String> mapping = iterator.next();
            func = StringUtils.replace(func, mapping.getKey(),mapping.getValue());
         }
      }
      log.trace("Variable maps = " + func);
      
      // ----------------------------------------- standard substitutions
      for (int i = 0; i < replacements.length; i++ )
         func = StringUtils.replace(func, replacements[i][0], replacements[i][1]);

      log.trace("Replacements = " + func);
      
      // --------------------------------------------------- Plug in values
     DataElement[] dataElements = new DataElement[variables.size()];
     for (int i=0; i < variables.size(); i++)
        dataElements[i] = variables.get(i).getDataElement();
     func = new CDAPlug(func).setDataElement(dataElements).get();
     log.trace("Parameter maps = " + func);
      
      value = defaultValue;
      try {
         ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
         value = (String) engine.eval(func);
      } catch (Exception e) {
         log.warn("Error evaluating function " + parName + nl + "function: " + function + nl + "script  : " + func + nl
            + "default : " + ((value == null) ? "null" : value) + nl + Util.getEM(e));
      }
      log.trace("Result = " + value);
} // EO load value 

@Override
public void loadWSLog(WSLog wsLog) {  
   for (Variable variable : variables) {
      DataElement dataElement = variable.getDataElement();
      if (dataElement.loadFromDocument == false)
         DataElement.downLoadWSLog(dataElement, wsLog);
   }
}

   /**
    * Replacement pairs for {@link #normalizeFunction(String)} method. Left
    * string is what is replaced, right string is replacement.
    */
   private static String[][] normalizations =
      new String[][] { 
         { "( (", "((" }, 
         { ") )", "))" }, 
         { "ValueSet (", "ValueSet(" } 
         };

   /**
    * {@link Util#normalize(String) normalizes} input function string, then runs
    * a series of additional {@link #normalizations}, getting rid of spaces
    * between parentheses of the same type and the space between "ValueSet" and
    * a following opening parenthesis. Run when function string is stored.
    * 
    * @param functionString raw function string.
    * @return "normalized" function string.
    */
   public static String normalizeFunction(String functionString) {
      functionString = Util.normalize(functionString);
      for (int i = 0; i < normalizations.length; i++ )
         functionString = StringUtils.replace(functionString, normalizations[i][0], normalizations[i][1]);
      return functionString;
   }
   
   /**
    * Add variable instance to this Function
    * @param variable {@link Variable} to add
    * @return this function instance, for method chaining.
    */
   public FunctionDataElement addVariable(Variable variable) {
      variables.add(variable);
      return this;
   }
   
   /**
    * Add new function mapping
    * 
    * @param from String to map from, for example:
    * 
    * <pre>
    * "THEN 'CHAM' SHALL = 'Y' ELSE 'N'"</pre>
    * 
    * @param to String to map to, for example:
    * 
    * <pre>
    * "{'Y'} else {'N'}"</pre>
    * <ul>
    * <li>As many of these mappings as needed can be added, but there can
    * only be one mapping for a given string.</li>
    * <li>If mappings with duplicate <b>from</b> strings are entered, the
    * second mapping will replace the first.</li>
    * <li>These mappings are applied BEFORE other replacements, in the
    * order they added to the variable.</li>
    * <li>As seen in the example, ${} parameters are processed after
    * mappings, so they may be included in <b>to</b> strings.</li>
    * </ul>
    * @return this functionDataElement instance, for method chaining.
    */
   public FunctionDataElement addMapping(String from, String to) {
      from = FunctionDataElement.normalizeFunction(from);
      to = FunctionDataElement.normalizeFunction(to);
      maps.put(from, to);
      return this;
   }

} // EO FunctionDataElement class
