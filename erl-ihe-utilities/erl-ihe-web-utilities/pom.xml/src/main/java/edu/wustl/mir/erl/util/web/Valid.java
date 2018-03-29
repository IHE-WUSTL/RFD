/*
 * Copyright (c) 2015 Washington University in St. Louis All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. The License is available at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Initial author: Ralph Moulton / MIR WUSM IHE Development
 * Project moultonr@mir.wustl.edu
 */
package edu.wustl.mir.erl.util.web;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Used to provide validation for screen input values. An instance keeps track
 * of whether an error has been found, provides common standard validation
 * methods and allows entry of results of more specific validations.
 * <p>The following example code illustrates an action method for a case where the
 * application user has entered name, IP address and port number; the
 * application needs to validate that a server name and a valid IP address were
 * entered, and that the port is one of a array of valid ports. If any errors
 * occur, the method returns null, which returns to the same web page, showing
 * the error messages. Otherwise processing continues:</p>
 * 
 * <pre>
 *    public String checkServiceParameters() {
 *       Valid v = new Valid();
 *       v.NB("serverName", serverName);
 *       v.Ip("ipAddress", ipAddress, true);
 *       if (Util.isOneOf(port, validPortsArray) == false)
 *          v.error("port", "invalid port");
 *       if (v.isErrors) return null;
 *       ...
 * </pre>
 */
public class Valid implements Serializable {
   static final long serialVersionUID = 1L;

   private boolean errors = false;

   /**
    * Constructor, sets initial error status, "No errors (yet)".
    */
   public Valid() {}

   /**
    * Add error message to current web page.
    * 
    * @param componentId on web page of component to add error to.
    * @param errorMessage to add.
    */
   public void error(String componentId, String errorMessage) {
      FacesUtil.addErrorMessage(componentId, errorMessage);
      errors = true;
   }

   /**
    * Add global error message to current web page.
    * 
    * @param errorMessage to add.
    */
   public void error(String errorMessage) {
      FacesUtil.addErrorMessage(errorMessage);
      errors = true;
   }

   /**
    * Validate that property value is not null, empty, or just whitespace. Add
    * error message to web page if validation fails.
    * 
    * @param componentId on web page of component to be validated.
    * @param componentValue to be validated.
    */
   public void NB(String componentId, String componentValue) {
      if (StringUtils.isBlank(componentValue)) {
         error(componentId, " Can't be null, empty, or just whitespace");
      }
   }

   /**
    * Validate that property is a valid port number (1-65535). Add error message
    * to web page if validation fails.
    * 
    * @param componentId on web page of port number to be validated.
    * @param portNumber to validate.
    * @param requiredEntry boolean, Is entry required? If false, a value of zero
    * will also be accepted.
    */
   public void Port(String componentId, int portNumber, boolean requiredEntry) {
      if (!requiredEntry && portNumber == 0) return;
      if (portNumber < 1 || portNumber > 65535) {
         error(componentId, "Invalid port number");
      }
   }

   /**
    * Validate that property represents a valid {@link java.net.URL URL}. Add
    * error message to web page if validation fails.
    * 
    * @param componentId on web page of URL to validate.
    * @param url String url to validate.
    * @param requiredEntry boolean, Is entry required? If false, an empty value will
    * also be accepted.
    */
   @SuppressWarnings("unused")
   public void URL(String componentId, String url, boolean requiredEntry) {
      if (!requiredEntry && StringUtils.isBlank(url)) return;
      try {
         new URL(url);
      } catch (MalformedURLException ex) {
         error(componentId, "Invalid URL");
      }
   }

   /**
    * Validate that the property represents a valid ipv4 address in dot
    * notation. Add error message to web page if validation fails. Optionally
    * normalizes ip format.
    * 
    * @param componentId on web page of ipv4 address to validate.
    * @param ipAddress String ipv4 address to validate.
    * @param requiredEntry boolean, Is entry required? If false, an empty value
    * will also be accepted.
    * @param normalizeFormat boolean Should ip dot notation be normalized? If
    * true, and the ip address is valid, it will be reformatted so that there
    * are no leading zeros on the tuples.
    */
   public void Ip(String componentId, String ipAddress, boolean requiredEntry, boolean normalizeFormat) {
      if (!requiredEntry && StringUtils.isBlank(ipAddress)) return;
      try {
         String formattedIpAddress = Util.validateFormatIpv4(ipAddress);
         if (normalizeFormat) ipAddress = formattedIpAddress;
      } catch (Exception e) {
         error(componentId, "Not valid IP V4 address: " + e.getMessage());
      }
   }

   /**
    * Validate that the property represents a valid DICOM AE Title (see DICOM
    * PS3.5-2011 section 6.2) Add error message to web page if validation fails.
    * @param componentId on web page of AE title to validate.
    * @param aeTitle String AE Title to validate
    * @param requiredEntry  boolean, Is entry required? If false, an empty value
    * will also be accepted.
    */
   public void AeTitle(String componentId, String aeTitle, boolean requiredEntry) {
      if (!requiredEntry && StringUtils.isBlank(aeTitle)) return;
      if (!StringUtils.trimToEmpty(aeTitle).matches("\\w{1,16}"))
         error(componentId, "Invalid AE Title");
   }

   /**
    * Validate that the property represents a valid Email address
    * @param componentId  on web page of Email address to validate.
    * @param emailAddress to validate
    * @param requiredEntry  boolean, Is entry required? If false, an empty value
    * will also be accepted.
    */
   public void Email(String componentId, String emailAddress, boolean requiredEntry) {
      if (!requiredEntry && StringUtils.isBlank(emailAddress)) return;
      if (!EmailValidator.getInstance().isValid(StringUtils.trimToEmpty(emailAddress))) {
         error(componentId, "Invalid Email Address");
      }
   }

   /**
    * @return boolean, true if errors have been recorded, otherwise false.
    */
   public boolean isErrors() {
      return errors;
   }

} // EO Class Valid
