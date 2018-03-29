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

/**
 * This project and package is for those utility classes which have web app
 * dependencies, including:
 * <ul>
 * <li>{@link edu.wustl.mir.erl.util.web.WebUtil WebUtil} Static utility
 * methods, like {@link edu.wustl.mir.erl.ihe.util.Util Util} but requiring web
 * dependencies. Includes the
 * {@link edu.wustl.mir.erl.util.web.WebUtil#initializeWebApp initializeWebApp},
 * analogous to {@link edu.wustl.mir.erl.ihe.util.Util#initializeCommandLine
 * Util.initializeCommandLine} to initialize web applications using the Util
 * standard foundation.</li>
 * <li>{@link edu.wustl.mir.erl.util.web.FacesUtil FacesUtil} which contains
 * static methods for functionality related to JSF.</li>
 * <li>{@link edu.wustl.mir.erl.util.web.Valid Valid} which provides a simple
 * way to validate entries on a submitted web page.</li>
 * <li>{@link edu.wustl.mir.erl.util.web.Email Email} Handles simple email
 * sessions using
 * <a href="https://javamail.java.net/docs/api/overview-summary.html">Javax
 * Mail</a></li>
 * <li>{@link edu.wustl.mir.erl.util.web.Zip Zip}</li>
 * </ul>
 * 
 * @author rmoult01
 */
package edu.wustl.mir.erl.util.web;