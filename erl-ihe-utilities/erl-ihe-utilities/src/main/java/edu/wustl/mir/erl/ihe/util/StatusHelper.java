/*
 * Copyright (c) 2014 Washington University in St. Louis All rights reserved.
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
 */
package edu.wustl.mir.erl.ihe.util;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

/**
 * Encapsulates the Status enums used to provide log status information for the
 * various validation modules in use, and handles encoding and decoding status
 * information for the RDBMS.
 * <p>
 * Validation modules may contain one or more enums which implement the
 * {@link Status} interface, by convention named ZZZStatus. Each enum contains a
 * 'setCode' string which uniquely identifies this enum in the application. Each
 * instance of the enum denotes a particular validation status. User readable
 * Phase descriptions and Status messages are included in the enums, which
 * together describe the status.</p>
 * <p>
 * When new ZZZStatus values are added to the log, they are converted to
 * instances of this class.</p>
 * <p>
 * To store status codes in the RDBMS, they are converted to a String of the
 * form set code,Status enum name. Multiple status values are stored as a single
 * text string, with "|" delimiters. When status codes are retrieved from the
 * RDBMS, the string is converted into an array of objects of this class.</p>
 * <p>
 * For an example of a Status enum, see RFDStatus in the erl-ihe-rfd project.</p>
 */
public class StatusHelper implements Serializable {
   private static final long serialVersionUID = 1L;

   private static Logger log = Util.getLog();

   /**
    * Map of CodeSet instances for Phase-Status enum pairs in use. Key is the
    * setCode of the enum.
    */
   private static Map <String, Method> statusSets = new HashMap <>();

   private static Status decode(String encodedStatus) {
      Status status = null;
      String str = StringUtils.removeEnd(encodedStatus, "|");
      try {
         String strs[] = str.split(",");
         if (strs.length != 2)
            throw new Exception("not of form xxx,statusName");
         String sCode = strs[0];
         String sName = strs[1];
         if (!statusSets.containsKey(sCode))
            throw new Exception("unknown status code type " + sCode);
         Method getStatus = statusSets.get(sCode);
         status = (Status) getStatus.invoke(null, sName);
      } catch (Exception e) {
         log.warn("StatusHelper.decode: error decoding: " + encodedStatus + " "
            + e.getMessage());
      }
      return status;
   }

   /**
    * Converts a String containing one or more encoded status codes to a List of
    * Status instances. This is usually done when querying a database for
    * statuses which were previously encoded. The String is composed of
    * sequences of the form SETCODE,STATUSCODENAME| with one sequence per status
    * instance. {@link #exportToDb(List)} is used to encode statuses as a
    * String.
    * 
    * @param dbValue The String of encoded Status values.
    * @return The List of decoded Status instances.
    */
   public static List <Status> importFromDb(String dbValue) {
      List <Status> statuses = new ArrayList <>();
      String statusStrings[] = dbValue.split("|");
      for (String statusString : statusStrings) {
         statuses.add(StatusHelper.decode(statusString));
      }
      return statuses;
   }

   /**
    * Converts the passed List of Status instances to a single String for
    * storage in an RDBMS table. The String is composed of sequences of the form
    * SETCODE,STATUSCODENAME| with one sequence per status instance.
    * {@link #importFromDb(String)} is used to reverse the process when querying
    * the database.
    * 
    * @param statuses List of Status instances to encode.
    * @return String encoding of the passed statuses
    */
   public static String exportToDb(List <Status> statuses) {
      StringBuilder str = new StringBuilder();
      for (Status status : statuses) {
         str.append(status.encode());
      }
      return str.toString();
   }

   /**
    * Sorts a {@link java.util.List List} of {@link Status} instances by their
    * {@link StatusType} ordinal. Within an ordinal, the original order of the
    * Status instances is retained.
    * 
    * @param unsorted the unsorted List
    * @param ascending boolean indicating the order of the sort. If true, the
    * {@link Status} instances are sorted in increasing order of the ordinal; 
    * If false, they are sorted in descending order.
    * 
    * @return sorted list.
    */
   public static List <Status> sortByStatusType(List <Status> unsorted, boolean ascending) {
      boolean repeat;
      Status[] work = unsorted.toArray(new Status[0]);
      do {
         repeat = false;
         for (int i = 0; i < work.length - 1; i++ ) {
            if (comp(work[i].getStatusType().ordinal(), 
                     work[i + 1].getStatusType().ordinal(), 
                     ascending)) {
               Status hold = work[i];
               work[i] = work[i + 1];
               work[i + 1] = hold;
               repeat = true;
            }
         }
      } while (repeat);
      List <Status> sorted = new ArrayList <Status>();
      for (Status s : work)
         sorted.add(s);
      return sorted;
   }
   private static boolean comp(Integer i, Integer j, boolean ascending) {
      if (i == j) return false;
      if (i > j) return ascending;
      return !ascending;
   }

   /**
    * Adds a Status code set to those being handled by {@link StatusHelper} for
    * this application. Before being added, the passed class will be validated:
    * <ul>
    * <li>It must be an {@link java.lang.Enum enum}.</li>
    * <li>The class must have a set code, unique for the application.</li>
    * <li>It must implement a static method which takes as its only argument
    * the name of one of its members and returns the enum instance for that
    * member cast to the Status interface.</li>
    * </ul>
    * 
    * @param setCode The setCode string for this enum. Must not contain white
    * space, commas (",") or vertical bars ("|"),
    * @param cls The {@link Class} of the Status enum for this set, which must
    * implement the {@link Status} interface.
    */
   public static void addCodeSet(String setCode, Class <? extends Status> cls) {
      Method method = null;
      try {

         if (!cls.isEnum())
            throw new Exception(cls.getName() + " is not an Enum");

         try {
            method = cls.getMethod("getStatus", String.class);
            if (!method.getReturnType().equals(Status.class))
               throw new Exception("return type not Status");
            if (!Modifier.isStatic(method.getModifiers()))
               throw new Exception("not a static method");
         } catch (Exception e1) {
            throw new Exception("getStatus method: " + e1.getMessage());
         }

         if (setCode == null || setCode.length() == 0
            || StringUtils.containsWhitespace(setCode)
            || StringUtils.containsAny(setCode, ",|"))
            throw new Exception("invalid set code in: "
               + cls.getCanonicalName());

         if (statusSets.containsKey(setCode)) {
            Class <?> existingCls = statusSets.get(setCode).getClass();
            if (!existingCls.equals(cls)) { throw new Exception(
               "More than one status class for code " + setCode + " "
                  + existingCls.getCanonicalName() + " and "
                  + cls.getCanonicalName()); }
            log.warn("status class code " + setCode
               + " registered more than once.");
            return;
         }
         statusSets.put(setCode, method);
      } catch (Exception e) {
         Util.exit("StatusHelper.addCodeSet error: " + e.getMessage());
      }
   } // addCodeSet method

} // EO Status class name
