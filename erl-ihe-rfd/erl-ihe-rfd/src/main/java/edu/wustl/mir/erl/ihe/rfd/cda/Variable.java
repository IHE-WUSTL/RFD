
package edu.wustl.mir.erl.ihe.rfd.cda;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Encapsulates function variable
 */
public class Variable  {
   private Map<String, String> mappings = new LinkedHashMap<>();
   private DataElement dataElement;
   
   /**
    * Create new instance
    * @param dataElement DataElement or subclass for this variable
    */
   public Variable(DataElement dataElement) {
      this.dataElement = dataElement;
   }

   /**
    * @return the {@link #mappings} value.
    */
   public Map <String, String> getMappings() {
      return mappings;
   }

   /**
    * @return the {@link #dataElement} value.
    */
   public DataElement getDataElement() {
      return dataElement;
   }
   
   /**
    * Add new variable mapping to this variable
    * 
    * @param from String to map from, for example:
    * 
    * <pre>
    * "$ProblemCode  CONTAINS ValueSet (Chlamydia (NCHS))"</pre>
    * 
    * @param to String to map to, for example:
    * 
    * <pre>
    * "${cham$ProblemCodeCount} > 0"</pre>
    * <ul>
    * <li>As many of these mappings as needed can be added, but there can
    * only be one mapping for a given string.</li>
    * <li>If mappings with duplicate <b>from</b> strings are entered, the
    * second mapping will replace the first.</li>
    * <li>These mappings are applied BEFORE standard replacements, in the
    * order they added to the variable.</li>
    * <li>Variables are processed in the order they are added to the function
    * data element.</li>
    * <li>As seen in the example, ${} parameters are processed after
    * mappings, so they may be included in <b>to</b> strings.</li>
    * </ul>
    * @return this variable instance, for method chaining.
    */
   public Variable addMapping(String from, String to) {
      from = FunctionDataElement.normalizeFunction(from);
      to = FunctionDataElement.normalizeFunction(to);
      mappings.put(from, to);
      return this;
   }
}