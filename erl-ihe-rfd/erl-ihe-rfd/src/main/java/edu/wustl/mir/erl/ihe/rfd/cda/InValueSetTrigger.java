/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Trigger for CodedDataElement. Checks if code matches any of the values in the
 * passed {@link ValueSet ValueSet(s)}. By default, both the code (Concept Code)
 * and the codeSystem (Code system OID) must match.
 */
public class InValueSetTrigger implements Trigger, Serializable {
	private static final long serialVersionUID = 1L;

	protected static Logger log = Util.getLog();

	protected List<ValueSet> valuesets = new ArrayList<>();

	protected boolean matchOID = true;

	/**
	 * Create InValueSetTrigger, passing one or more ValueSets to search.
	 * 
	 * @param valsets
	 *            to search.
	 */
	public InValueSetTrigger(ValueSet... valsets) {
		for (ValueSet vs : valsets)
			valuesets.add(vs);
	}

	/**
	 * Add additional ValueSets to search.
	 * 
	 * @param valsets
	 *            to add
	 * @return this InValueSetTrigger instance, for method chaining.
	 */
	public InValueSetTrigger addValueSets(ValueSet... valsets) {
		for (ValueSet vs : valsets)
			valuesets.add(vs);
		return this;
	}

	/**
	 * Set match Code System OID parameter. Default is true.
	 * 
	 * @param b
	 *            new value for parameter
	 * @return this InValueSetTrigger instance, for method chaining.
	 */
	public InValueSetTrigger setMatchOID(boolean b) {
		matchOID = b;
		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.wustl.mir.erl.ihe.rfd.cda.Trigger#trigger(edu.wustl.mir.erl.ihe.rfd.
	 * cda.DataElement)
	 */
	@Override
	public boolean trigger(DataElement dataElement, StringBuilder msg) {
		CodedDataElement cde = null;
		if (dataElement instanceof CodedDataElement)
			cde = (CodedDataElement) dataElement;
		else if (dataElement instanceof ProcedureDataElement)
			cde = ((ProcedureDataElement) dataElement).getCode();
		else {
			msg.append(" InValueSetTrigger used on data element without coded value: " + dataElement.parName);
			return false;
		}
		String code = cde.getCode();
		String oid = cde.getCodeSystem();
		msg.append(" code " + code);
		if (matchOID == true)
			msg.append(" OID " + oid);
		for (ValueSet valueSet : valuesets) {
			if (matchOID == true) {
				if (valueSet.isCodeInValueSet(code, oid)) {
					msg.append(" match found in " + valueSet.getName());
					return true;
				}
			} else if (valueSet.isCodeInValueSet(code)) {
				msg.append(" match found in " + valueSet.getName());
				return true;
			}
			msg.append(" checked " + valueSet.getName());
		}
		msg.append(" no match found.");
		return false;
	}

}
