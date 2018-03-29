/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

/**
 * Trigger function interface, used to 'fire' actions.
 */
public interface Trigger {
   /**
    * Does the passed value 'trigger' the action.
    * @param dataElement test value
    * @param msg StringBuilder with results message, status for this function 
    * will be appended.
    * @return boolean true if the trigger has been fired, false otherwise.
    */
   public boolean trigger(DataElement dataElement, StringBuilder msg);
   
   /**
    * Sets up additional plug elements from the Trigger function, if any. By
    * default, returns with no action. This method only needs to be implemented
    * in triggers which actually have additional plug elements.
    * @param plug {@link CDAPlug} instance to add plug elements to.
    */
   public default void plug(CDAPlug plug) {
      return;
   }
}
