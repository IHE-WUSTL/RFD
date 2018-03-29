/**
 * 
 */
package edu.wustl.mir.erl.ihe.util;

import java.io.Serializable;

/**
 * Simple String, String pair
 */
public class LabelValue implements Serializable {
   private static final long serialVersionUID = 1L;
   
   private String label = "";
   private String value = "";
   
   /**
    * New instance with empty strings
    */
   public LabelValue() { }
   
   /**
    * New instance with passed strings
    * @param label String initial label value
    * @param value String initial value
    */
   public LabelValue(String label, String value) {
      this.label = label;
      this.value = value;
   }

   /**
    * @return the {@link #label} value.
    */
   public String getLabel() {
      return label;
   }

   /**
    * @param label the {@link #label} to set
    */
   public void setLabel(String label) {
      this.label = label;
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
   
   public String toString() {
      return label + ":" + value;
   }

}
