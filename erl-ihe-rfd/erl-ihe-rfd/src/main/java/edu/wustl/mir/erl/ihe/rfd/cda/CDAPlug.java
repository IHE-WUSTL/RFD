/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

import edu.wustl.mir.erl.ihe.util.Plug;

/**
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class CDAPlug extends Plug {

   private static final long serialVersionUID = 1L;

   /**
    * Builds new Plug instance from passed String
    * 
    * @param string String with ${parName} parameters for plugging.
    */
   public CDAPlug(String string) {
      super(string);
   }

   /**
    * Add <u>common</u> parameters for one or more DataElement or
    * CodedDataElement instances to a Plug instance. Currently, the Element,
    * Description, and Value properties are added for DataElements. For
    * CodedDataElements, the code, displayName, codeSystem, and codeSystemName
    * values are added. The parameter name is composed by concatenating the
    * parName and the property name camelcase. For example, "ageValue". Any
    * parameters which do not appear in the Plug string will simply be ignored.
    * 
    * @param dataElements DataElement instances to add.
    * @return the Plug instance, allowing chaining.
    */
   public Plug setDataElement(DataElement... dataElements) {
      for (DataElement de : dataElements) {
         String parName = de.getParName();
         this.set(parName + "Element", de.getElement());
         this.set(parName + "Description", de.getDescription());
         this.set(parName + "Value", de.getValue());
         
         if (de instanceof CodedDataElement) {
            CodedDataElement sde = (CodedDataElement) de;
            this.set(parName + "Code", sde.getCode());
            this.set(parName + "DisplayName", sde.getDisplayName());
            this.set(parName + "CodeSystem", sde.getCodeSystem());
            this.set(parName + "CodeSystemName", sde.getCodeSystemName());
            continue;
         }
         if (de instanceof ValueDataElement) {
            ValueDataElement sde = (ValueDataElement) de;
            this.set(parName + "Value", sde.getValue());
            sde.plug(this);
            continue;
         }
         if (de instanceof IdDataElement) {
            IdDataElement sde = (IdDataElement) de;
            this.set(parName + "Root", sde.getValue());
            this.set(parName + "Extension", sde.getExtension());
            continue;
         }
         if (de instanceof IntervalDataElement) {
            IntervalDataElement sde = (IntervalDataElement) de;
            this.set(parName + "LowValue", sde.getValue());
            this.set(parName + "HighValue", sde.getHighValue());
            continue;
         }
         if (de instanceof PersonNameDataElement) {
            PersonNameDataElement sde = (PersonNameDataElement) de;
            this.set(parName + "Prefix", sde.getPrefix());
            this.set(parName + "Given",  sde.getGiven());
            this.set(parName + "Family", sde.getFamily());
            this.set(parName + "Suffix", sde.getSuffix());
            continue;
         }
         if (de instanceof AddrDataElement) {
            AddrDataElement sde = (AddrDataElement) de;
            this.set(parName + "StreetAddressLine", sde.getStreetAddressLine());
            this.set(parName + "City",  sde.getCity());
            this.set(parName + "State", sde.getState());
            this.set(parName + "PostalCode", sde.getPostalCode());
            this.set(parName + "Country", sde.getCountry());
            continue;
         }
         if (de instanceof CODDataElement) {
            CODDataElement sde = (CODDataElement) de;
            this.set(parName + "CauseOfDeath", sde.getCauseOfDeath());
            this.set(parName + "OnsetToDeathInterval", sde.getOnsetToDeathInterval());
            continue;
         }
         if (de instanceof SequenceDataElement) {
            @SuppressWarnings("rawtypes")
            SequenceDataElement sde = (SequenceDataElement) de;
            this.set(parName + "Count", sde.getCount());
            this.setDataElement(sde.getDataElements());
            sde.getTrigger().plug(this);
            continue;
         }
         if (de instanceof FunctionDataElement) {
            FunctionDataElement sde = (FunctionDataElement) de;
            this.set(parName + "returnValue", sde.getValue());
            this.set(parName + "Function", sde.getFunction());
            continue;
         }
         if (de instanceof ProcedureDataElement) {
            ProcedureDataElement sde = (ProcedureDataElement) de;
            this.set(parName + "ClassCode", sde.getClassCode());
            this.set(parName + "MoodCode", sde.getMoodCode());
            this.setDataElement(sde.getId());
            this.setDataElement(sde.getCode());
            this.setDataElement(sde.getMethodCode());
            this.setDataElement(sde.getObservationValue());
            this.set(parName + "Reference", sde.getReference());
            this.set(parName + "StatusCode", sde.getStatusCode());
            this.setDataElement(sde.getInterval());
         }
      }
      return this;
   }
}
