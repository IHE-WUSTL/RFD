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
 * */

package edu.wustl.mir.erl.ihe.util;

/**
 * TimeStamp variants
 */

public enum TS {
   
   /** Date; partial dates accepted, for example yyyymm */
   DATE ("date"),
   /** DateTime, partial values accepted */
   DATETIME ("time"),
   /** Full Date, must have complete yyyymmdd */
   FULLDATE ("full date"),
   /** Full DateTime, must have complete date and time */
   FULLDATETIME ("full time");
   
   private String name;
   
   TS(String name) {
      this.name = name;
   }
   
   /**
    * @return printable name.
    */
   public String getName() {
      return name;
   }

}
