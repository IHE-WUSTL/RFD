/*******************************************************************************
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
 *******************************************************************************/
package edu.wustl.mir.erl.ihe.util;

/**
 * enum used to note result of test or evaluation.
 */
public enum Result {

   /**
    * Test/Evaluation passed
    */
   PASS("passed"),
   /**
    * Test/Evaluation failed
    */
   FAIL("failed"),
   /**
    * Test/Evaluation result not known or indeterminate at this point
    */
   UNKNOWN("not known"),
   /**
    * Test/Evaluation result not applicable to this status.
    */
   NA("not applicable");

   private Result(String str) {
      string = str;
   }

   private String string;

   /**
    * Convenience method used to "collect" an overall test result, in situations
    * where a number of test steps must be passed for the overall test to pass.
    * Usage:
    * 
    * <pre>
    * {@code
    * Result one = result of first test step
    * Result overall = one.overall(null);
    * Result two = result of second test step.
    * overall = two.overall(overall);
    * }
    * </pre>
    * at the end of the sequence, overall will be PASS if none of the individual
    * test steps resulted in FAIL
    * 
    * @param overall Result value for the overall test.
    * @return Result FAIL is this test failed, otherwise the current value of
    * the overall parameter.
    */
   public Result overall(Result overall) {
      if (overall == null) overall = Result.PASS;
      if (this.equals(Result.FAIL)) overall = Result.FAIL;
      return overall;
   }

   @Override
   public String toString() {
      return string;
   }

}
