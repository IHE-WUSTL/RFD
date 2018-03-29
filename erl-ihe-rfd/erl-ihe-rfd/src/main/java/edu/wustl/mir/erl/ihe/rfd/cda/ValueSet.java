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
 *******************************************************************************/
/**
 * 
 */
package edu.wustl.mir.erl.ihe.rfd.cda;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;

/**
 * Encapsulates an encoded value set.
 */
public class ValueSet implements Serializable {
   private static final long serialVersionUID = 1L;

   private static Map <String, ValueSet> valueSetsByCode = new HashMap <>();
   private static Map <String, ValueSet> valueSetsByName = new HashMap <>();
   private static boolean valueSetsLoaded = false;
   /** initialize ValueSets */
   public static boolean init() { return valueSetsLoaded; }
   private static Logger log = Util.getLog();
   
   static {
      try {
         ValueSet.loadValueSets();
      } catch (Exception e) {
         Util.exit(Util.getEM(e));
      }
   }

   /**
    * Unique vsCode for ValueSet. For example PHVS_Chlamydia_NCHS.
    */
   private String vsCode;
   /**
    * The name of the ValueSet. For example, 'Chlamydia (NCHS) '.
    */
   private String name;
   /**
    * OID for the ValueSet For example '1.3.6.1.4.1.19376.1.7.3.1.1.13.8.93'.
    */
   private String oid;
   /**
    * User readable description of ValueSet.
    */
   private String description;
   /**
    * The version of the ValueSet, if any. This could be a release number or a
    * release date. Reference only.
    */
   private String version;
   /**
    * {@link java.util.Map Map} of Code instances for this ValueSet. Key is
    * {@link Code#code vsCode}
    */
   private Map <String, Code> codes;

   /**
    * @param vsCode the {@link #vsCode}. Must be non-blank and unique.
    * @param name the {@link #name}. Must be non-blank and unique.
    * @param oid the {@link #oid}
    * @param description the {@link #description}
    * @param version the {@link #version}
    * @throws Exception if vsCode or name are invalid or duplicate another
    * ValueSet.
    */
   public ValueSet(String vsCode, String name, String oid, String description, String version) throws Exception {
      if (StringUtils.isBlank(vsCode)) throw new Exception("ValueSet vsCode must be non-blank");
      ValueSet ovs = valueSetsByCode.get(vsCode);
      if (ovs != null)
         throw new Exception("tried to add duplicate ValueSet vsCode for [" + ovs.vsCode + " " + ovs.getName() + "].");
      if (StringUtils.isBlank(name)) throw new Exception("ValueSet name must be non-blank");
      ovs = valueSetsByName.get(name);
      if (ovs != null)
         throw new Exception("tried to add duplicate ValueSet name for [" + ovs.vsCode + " " + ovs.getName() + "].");
      this.vsCode = vsCode;
      this.name = name;
      this.oid = oid;
      this.description = description;
      this.version = version;
      log.trace("Add Value Set " + vsCode + " " + name);
      codes = new HashMap <>();
      valueSetsByCode.put(this.vsCode, this);
      valueSetsByName.put(this.name, this);
   }

   /**
    * Convenience constructor, leaves {@link #oid}, {@link #description} and
    * {@link #version} blank.
    * 
    * @param vsCode the {@link #vsCode}. Must be non-blank and unique.
    * @param name the {@link #name}. Must be non-blank and unique.
    * @throws Exception if vsCode or name are invalid or duplicate another
    * ValueSet.
    */
   public ValueSet(String vsCode, String name) throws Exception {
      this(vsCode, name, "", "", "");
   }

   /**
    * Encapsulates a single code for the enclosing ValueSet.
    */
   public class Code {
      /**
       * The code used to represent the value. For example, "2576002".
       */
      private String code;
      /**
       * The short human readable value for the code. For example, "Trachoma".
       */
      private String displayName;
      /**
       * The codeSystemOID, that is, the oid of the underlying code set this
       * code comes from. For example, for SNOMED-CT, "2.16.840.1.113883.6.96".
       */
      private String codeSystemOID;

      /**
       * Constructor for vsCode items.
       * 
       * @param code the {@link #code} value.
       * @param displayName the {@link #displayName} value.
       * @param codeSystemOID the {@link #codeSystemOID} value.
       */
      private Code(String code, String displayName, String codeSystemOID) {
         this.code = code;
         this.displayName = displayName;
         this.codeSystemOID = codeSystemOID;
         log.trace("  [" + code + "]  " + displayName);
      }

      /**
       * @return the {@link #vsCode} value.
       */
      public String getCode() {
         return code;
      }

      /**
       * @param code the {@link #code} to set
       */
      public void setCode(String code) {
         this.code = code;
      }

      /**
       * @return the {@link #displayName} value.
       */
      public String getDisplay() {
         return displayName;
      }

      /**
       * @param displayName the {@link #displayName} to set
       */
      public void setDisplay(String displayName) {
         this.displayName = displayName;
      }

      /**
       * @return the {@link #codeSystemOID} value.
       */
      public String getCodeSystemOID() {
         return codeSystemOID;
      }

      /**
       * @param codeSystemOID the {@link #codeSystemOID} to set
       */
      public void setCodeSystemOID(String codeSystemOID) {
         this.codeSystemOID = codeSystemOID;
      }

   } // EO Code class

   // ***************************************************************
   // Getters and Setters for ValueSet
   // ***************************************************************

   /**
    * @return the {@link #vsCode} value.
    */
   public String getCode() {
      return vsCode;
   }

   /**
    * @param code the {@link #vsCode} to set
    */
   public void setCode(String code) {
      this.vsCode = code;
   }

   /**
    * @return the {@link #name} value.
    */
   public String getName() {
      return name;
   }

   /**
    * @param name the {@link #name} to set
    */
   public void setName(String name) {
      this.name = name;
   }

   /**
    * @return the {@link #oid} value.
    */
   public String getOID() {
      return oid;
   }

   /**
    * @param oid {@link #oid} to set
    */
   public void setOID(String oid) {
      this.oid = oid;
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
    * @return the {@link #version} value.
    */
   public String getVersion() {
      return version;
   }

   /**
    * @param version the {@link #version} to set
    */
   public void setVersion(String version) {
      this.version = version;
   }

   /**
    * @param code the {@link Code#code} value. Must be non-blank and unique for
    * this ValueSet.
    * @param displayName the {@link Code#displayName} value. Must be non-blank
    * and should be unique for this ValueSet.
    * @param codeSystemOID the {@link Code#codeSystemOID} value.
    * @throws Exception if vsCode or displayName values are not valid.
    */
   public void addCode(String code, String displayName, String codeSystemOID) throws Exception {
      if (StringUtils.isBlank(code)) throw new Exception("code value must be non-blank");
      if (codes.containsKey(code)) throw new Exception("tried to add duplicate vsCode [" + code + "].");
      if (StringUtils.isBlank(displayName))
         throw new Exception("displayName value must be non-blank: vsCode [" + code + "].");
      if (StringUtils.isBlank(codeSystemOID))
         throw new Exception("codeSystemOID value must be non-blank: vsCode [" + code + "].");
      codes.put(code, new Code(code, displayName, codeSystemOID));
   }

   /**
    * Does this ValueSet contain an entry matching the passed code and
    * codeSystemOID?
    * 
    * @param code {@link Code#code} to match.
    * @param codeSystemOID {@link Code#codeSystemOID} to match.
    * @return boolean true if an entry for this code exists, false otherwise.
    */
   public boolean isCodeInValueSet(String code, String codeSystemOID) {
      Code cd = codes.get(code);
      if (cd == null) return false;
      return cd.getCodeSystemOID().equalsIgnoreCase(codeSystemOID);
   }

   /**
    * Does this ValueSet contain an entry matching the passed code. (Does not
    * check Code System OID).
    * 
    * @param code {@link Code#code} to match.
    * @return boolean true if an entry for this code exists, false otherwise.
    */
   public boolean isCodeInValueSet(String code) {
      Code cd = codes.get(code);
      if (cd == null) return false;
      return true;
   }
   

   /**
    * Fetch Code instance for passed code
    * 
    * @param code {@link Code#code} value
    * @return {@link Code} instance for this code, never null
    * @throws Exception if no matching code exists.
    */
   public Code getCode(String code) throws Exception {
      Code c = codes.get(code);
      if (c == null) throw new Exception("No vsCode [" + code + "] in ValueSet [" + code + "]");
      return c;
   }

   /**
    * Check for ValueSet instance with given vsCode.
    * 
    * @param vsCode {@link #vsCode} to check.
    * @return boolean true if ValueSet with this vsCode exists.
    */
   public static boolean isValueSetForCode(String vsCode) {
      return valueSetsByCode.containsKey(vsCode);
   }

   /**
    * Fetch ValueSet for passed {@link #vsCode}.
    * 
    * @param vsCode {@link #vsCode} for desired ValueSet
    * @return ValueSet, never null
    * @throws Exception if no matching ValueSet exists.
    */
   public static ValueSet getValueSetForCode(String vsCode) throws Exception {
      ValueSet valueSet = valueSetsByCode.get(vsCode);
      if (valueSet == null) throw new Exception("No such ValueSet: vsCode [" + vsCode + "]");
      return valueSet;
   }

   /**
    * Check for ValueSet instance with given name.
    * 
    * @param name {@link #name} to check.
    * @return boolean true if ValueSet with this vsCode exists.
    */
   public static boolean isValueSetForName(String name) {
      return valueSetsByName.containsKey(name);
   }

   /**
    * Fetch ValueSet for passed {@link #name}.
    * 
    * @param name {@link #name} for desired ValueSet
    * @return ValueSet, never null
    * @throws Exception if no matching ValueSet exists.
    */
   public static ValueSet getValueSetForName(String name) throws Exception {
      ValueSet valueSet = valueSetsByName.get(name);
      if (valueSet == null) throw new Exception("No such ValueSet: name [" + name + "]");
      return valueSet;
   }

   /**
    * static method to load ValueSets from .xlsx spreadsheets, as indicated in
    * properties file. For example:
    * 
    * <pre>
    * {@code
    * <valueSets>
    *    <WorkBook name="RFDValueSets.xlsx" sheetNames="Value Set Members"/>
    *</valueSets>
    * }
    * </pre>
    * <ol>
    * <li>There may be as many {@code <WorkBook>} elements within the {@code 
    * <ValueSets>} element as desired.</li>
    * <li>The name attribute gives the WorkBook file path. Relative paths are
    * resolved relative to the {@link Util#getRunDirectoryPath() runDirectory}.
    * </li>
    * <li>The sheetNames attributed contains a comma delimited list of sheet
    * names in the WorkBook which are to be processed. If the value of sheetName
    * is "All", all of the sheets in the WorkBook will be processed.</li>
    * <li>Each processed sheet should begin with a header row, including the
    * following column headers denoting the columns containing data which will
    * be loaded:<ul>
    * <li>Value Set Code</li>
    * <li>Value Set Name</li>
    * <li>Concept Code</li>
    * <li>Concept Name</li>
    * <li>Code System OID</li></ul>
    * other columns may be included, but will be ignored. The data values
    * corresponding to each header must be in that column for the entire sheet.</li>
    * </ol>
    * 
    * @throws Exception on error:
    * <ul>
    * <li>Invalid WorkBook name, file, permissions, or file not found.</li>
    * <li>WorkBook sheetNames empty or invalid</li>
    * </ul>
    */
   private static void loadValueSets() throws Exception {
      if (valueSetsLoaded == true) return;
      valueSetsLoaded = true;
      ValueSet valueSet = null;
      Logger log = Util.getLog();
      // Pull Spreadsheet elements from Configuration ValueSets element
      List <HierarchicalConfiguration> spreadsheets = Util.getProperties().configurationsAt("ValueSets.WorkBook");
      // Iterate over Spreadsheet elements
      for (HierarchicalConfiguration spreadsheet : spreadsheets) {
         String xlsxName = spreadsheet.getString("[@name]", "ValueSets.xlsx");
         // Validate WorkBook pfn relative to runDirectory
         Path xlsxPath = Util.getRunDirectoryPath().resolve(xlsxName);
         Util.isValidPfn("ValueSet spreadsheet file", xlsxPath, PfnType.FILE, "r");
         // What sheets are being processed in this workbook?
         String s = spreadsheet.getString("[@sheetNames]");
         if (StringUtils.isBlank(s)) throw new Exception("ValueSets " + xlsxName + " null Sheet attribute");
         String[] sheets = s.split(",");
         if (sheets.length == 0) throw new Exception("ValueSets " + xlsxName + " no Sheets specified");
         // Open workbook
         XSSFWorkbook wb = new XSSFWorkbook(xlsxPath.toFile());
         // Iterate sheets
         sheet: for (int i = 0; i < wb.getNumberOfSheets(); i++ ) {
            XSSFSheet sheet = wb.getSheetAt(i);
            // Process this sheet if in sheetNames or sheetNames is "all"
            if (((sheets.length == 1 && sheets[0].equalsIgnoreCase("all"))
               || Util.isOneOf(sheet.getSheetName(), sheets)) == false) continue;
            log.info("loading " + xlsxName + " " + sheet.getSheetName());
            // Iterate rows in sheet
            Iterator <Row> rows = sheet.rowIterator();
            // Set up state 0
            int state = 0;
            int valueSetCodeCol = -1, valueSetNameCol = -1, conceptCodeCol = -1, 
            conceptNameCol = -1, codeSystemOIDCol = -1;
            String currentCodeSystemOID = "";
            while (rows.hasNext()) {
               Row row = rows.next();
               if (row.getPhysicalNumberOfCells() == 0) continue;
               if (isRowEmpty(row)) continue;
               /*
                * State 0. At beginning of spreadsheet, looking for column
                * header row. Locate required columns by matching headers.
                */
               if (state == 0) {
                  for (short c = row.getFirstCellNum(); c < row.getLastCellNum(); c++ ) {
                     Cell cell = row.getCell(c);
                     if (cell == null) continue;
                     if (cell.getCellType() != Cell.CELL_TYPE_STRING) continue;
                     switch (cell.getStringCellValue().trim()) {
                        case "Value Set Code":
                           valueSetCodeCol = c;
                           break;
                        case "Value Set Name":
                           valueSetNameCol = c;
                           break;
                        case "Concept Code":
                           conceptCodeCol = c;
                           break;
                        case "Concept Name":
                           conceptNameCol = c;
                           break;
                        case "Code System OID":
                           codeSystemOIDCol = c;
                           break;
                        default:
                     }
                  }
                  /* If any required headers are missing, skip this sheet */
                  if (Util.isOneOf(-1, valueSetCodeCol, valueSetNameCol, conceptCodeCol, 
                     conceptNameCol, codeSystemOIDCol)) {
                     log.warn("one or more required columns not recognized, skipping sheet.");
                     break sheet;
                  }
                  state = 1;
                  continue;
               } // EO state 0 processing
                 // Values from designated columns are relevant to other states
               String valueSetCode = loadCell(row.getCell(valueSetCodeCol));
               String valueSetName = loadCell(row.getCell(valueSetNameCol));
               String conceptCode = loadCell(row.getCell(conceptCodeCol));
               String conceptName = loadCell(row.getCell(conceptNameCol));
               String codeSystemOID = loadCell(row.getCell(codeSystemOIDCol));
               if (StringUtils.isBlank(codeSystemOID)) codeSystemOID = currentCodeSystemOID;
               else currentCodeSystemOID = codeSystemOID;
               /*
                * state 1. Must find a "new Value Set", that is, a row with
                * non-blank Value Set code and Value Set Name. Otherwise row is
                * skipped.
                */
               if (state == 1) {
                  if (StringUtils.isBlank(valueSetName) || StringUtils.isBlank(valueSetCode)) continue;

                  try {
                     valueSet = new ValueSet(valueSetCode, valueSetName);
                     log.trace("Added Value Set = [" + valueSetCode + "] [" + valueSetName + "]");
                  } catch (Exception e) {
                     log.warn("skipping value set " + e.getMessage());
                     continue;
                  }
                  state = 2;
                  try {
                     valueSet.addCode(conceptCode, conceptName, codeSystemOID);
                  } catch (Exception e) {
                     log.warn("ValueSet code skipped " + e.getMessage());
                  }
                  continue;
               } // EO state 1 processing
               /*
                * state 2. As state 1 except that does not have to have new
                * Value Set
                */

               if (StringUtils.isNotBlank(valueSetName) && StringUtils.isNotBlank(valueSetCode)) {

                  try {
                     valueSet = new ValueSet(valueSetCode, valueSetName);
                     log.trace("Added Value Set = [" + valueSetCode + "] [" + valueSetName + "]");
                  } catch (Exception e) {
                     log.warn("skipping value set " + e.getMessage());
                     state = 1;
                     continue;
                  }
               }
               try {
                  valueSet.addCode(conceptCode, conceptName, codeSystemOID);
               } catch (Exception e) {
                  log.warn("ValueSet code skipped " + e.getMessage());
               }

            } // EO Iterate rows in sheet.
         }
         wb.close();
      } // EO iterate WorkBook elements
   } // EO loadValueSets method

   private static String loadCell(Cell cell) {
      if (cell == null) 
         return "";
      switch (cell.getCellType()) {
         case Cell.CELL_TYPE_STRING:
            return cell.getStringCellValue().trim();
         case Cell.CELL_TYPE_BOOLEAN:
            boolean b = cell.getBooleanCellValue();
            return (b) ? "TRUE" : "FALSE";
         case Cell.CELL_TYPE_NUMERIC:
            Integer d = (int) cell.getNumericCellValue();
            return d.toString();
         default:
            return "";
      }
   }
   
   /**
    * Checks to see if row is empty.
    * @param row to check
    * @return true if row is empty, false otherwise
    */
   private static boolean isRowEmpty(Row row) {
      for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
         Cell cell = row.getCell(c);
         if (cell != null && cell.getCellType() != Cell.CELL_TYPE_BLANK)
             return false;
     }
     return true;
   }

} // EO ValueSet class
