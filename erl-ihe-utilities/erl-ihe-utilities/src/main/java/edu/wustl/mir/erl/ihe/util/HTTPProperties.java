/*
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
 */
package edu.wustl.mir.erl.ihe.util;

/**
 * Constants used in HTTP processing.
 */
@SuppressWarnings("javadoc")
public interface HTTPProperties {
   /**
    * HTTP Request
    * Methods
    */
   public enum METHOD {
      
      // @formatter:off
      OPTIONS, 
      GET, 
      HEAD, 
      POST, 
      PUT, 
      DELETE, 
      TRACE, 
      CONNECT, 
      EXTENSION_METHOD;
      // @formatter:on

      /**
       * Returns the enum instance corresponding to the passed HTTP Request
       * Method token. IAW <a
       * href="http://tools.ietf.org/html/rfc2616#section-5.1.1">RFC 2616</a>,
       * the tokens are case-sensitive, that is, they are expected to be in all
       * capitals.
       * 
       * @param method String HTTP Request Method token.
       * @return METHOD instances for this token. Returns EXTENSION_METHOD if
       * the passed String does not match any of the other methods. Never
       * returns null.
       */
      public static METHOD getMethod(String method) {
         METHOD[] methods = METHOD.values();
         for (METHOD m : methods) {
            if (method.equals(m.name())) return m;
         }
         return EXTENSION_METHOD;
      }
   } // EO METHOD enum

   /**
    * <a href="http://tools.ietf.org/html/rfc2616#section-6.1.1">HTTP Response
    * Statuses</a>, including Status Code and Reason Phrase.<p>
    * <b>Note:</b> Only codes that have come up in programming are entered; Add
    * codes as needed. <b>Do not program as if this enum will not expand.</b></p>
    */
   public enum STATUS {
      
      // @formatter:off
      OK(200, "OK"), 
      NOT_FOUND(404, "Not Found"), 
      METHOD_NOT_ALLOWED(405, "Method Not Allowed"), 
      SERVER_ERROR(500, "Internal Server Error"),
      NOT_IMPLEMENTED(501, "Not Implemented");
      // @formatter:on

      /**
       * Three digit integer status code, per RFC 2616 section <a
       * href="http://tools.ietf.org/html/rfc2616#section-6.1.1">6.1.1</a> and
       * <a href="http://tools.ietf.org/html/rfc2616#section-10">10</a>.
       */
      public int statusCode;
      public String reasonPhrase;

      STATUS(int statusCode, String reasonPhrase) {
         this.statusCode = statusCode;
         this.reasonPhrase = reasonPhrase;
      }
   } // EO STATUS enum

   /**
    * <a href="http://en.wikipedia.org/wiki/Internet_media_type">Internet Media
    * Types</a> used in programming, along with their <a
    * href="http://www.iana.org/assignments/media-types/media-types.xhtml">IANA</a>
    * type/subtype strings. <p> <b>Note:</b> Only types that have 
    * come up in programming are entered; Add as needed. <b>Do not program as if 
    * this enum will not expand.</b></p>
    */
   public enum MediaType {
      
      APPLICATION_XML ("Application/xml"),
      TEXT_PLAIN      ("Text/plain");
      
      public String typeSubtype;
      
      MediaType(String string) {
         typeSubtype = string;
      }
   }  // EO MediaType enum

}
