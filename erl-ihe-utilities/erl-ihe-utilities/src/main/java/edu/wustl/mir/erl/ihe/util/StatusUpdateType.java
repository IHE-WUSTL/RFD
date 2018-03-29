package edu.wustl.mir.erl.ihe.util;

/**
 * Status update types: Indicate action to take when more than one 
 * enum value with the same phase value is stored.<ul>
 * <li>ADD - Add the new Status, retaining the previous one(s).</li>
 * <li>UPDATE - Replace the previous Status with the new one.</li></ul>
 * A StatusUpdateType value is stored with each ZZZPhase value.
 * @author rmoult01
 *
 */
public enum StatusUpdateType {
   /**
    * Add the new Status, retaining any previous statuses.
    */
   ADD,
   /**
    * Replace the previous Status with the new one.
    */
   UPDATE
}
