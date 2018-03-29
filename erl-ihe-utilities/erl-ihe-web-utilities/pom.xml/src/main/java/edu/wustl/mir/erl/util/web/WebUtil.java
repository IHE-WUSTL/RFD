/*
 * Copyright (c) 2015  Washington University in St. Louis
 *  All rights reserved. This program and the accompanying 
 *  materials are made available under the terms of the
 *  Apache License, Version 2.0 (the "License");  you may not 
 *  use this file except in compliance with the License.
 * The License is available at:
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing, 
 *  software  distributed under the License is distributed on 
 *  an "AS IS" BASIS,  WITHOUT WARRANTIES OR CONDITIONS  
 *  OF ANY KIND, either express or implied. See the License 
 *  for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  Contributors:
 *    Initial author: Ralph Moulton / MIR WUSM IHE Development Project 
 *    moultonr@mir.wustl.edu
 */
package edu.wustl.mir.erl.util.web;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.KEYS;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;

/**
 * A breakout from edu.wustl.mir.erl.ihe.util.Util so that the erl-ihe-util jar
 * can be used without web dependencies.
 * 
 * @author rmoult01
 */
public class WebUtil implements Serializable {
   private static final long serialVersionUID = 1L;

   /**
    * Handles initialization for web applications. To use Util, these must have
    * an {@link Util#getApplicationName applicationName}, the default value of
    * which is passed as the first parameter of this method, and a
    * {@link Util#getRunDirectoryPath runDirectory}. Parameters can be passed to
    * the application from the web.xml file using {@code <env-entry>} elements,
    * for example:
    * 
    * <pre>
    * {@code
    *  <env-entry>
    *     <env-entry-name>profile</env-entry-name>
    *     <env-entry-type>java.lang.String</env-entry-type>
    *     <env-entry-value>eu</env-entry-value>
    * </env-entry>}
    * </pre>
    * 
    * The following environment entries will be looked for by this method:
    * <ul>
    * <li><b>applicationName:</b> The {@link Util#getApplicationName applicationName},
    * which would override the default applicationName. The default
    * applicationName is passed from the calling class.</li>
    * <li><b>runDirectory:</b> The name of a directory which would override the
    * default run directory. For web applications, the default runDirectory name
    * is the {@link Util#getApplicationName() applicationName}. The run Directory
    * is a Directory in the loadable resource area, that is, the directory tree
    * rooted at WEB-INF/classes which has the runDirectory name.</li>
    * <li><b>profile:</b> The {@link Util#getProfile profile}.</li>
    * <li><b>log4jProperties:</b> The {@link Util#getLog4jPropertiesFileName
    * log4jPropertiesFileName} The default is log4j.{@link Util#getProfile
    * profile}.properties if it exists, or log4j.properties, in the
    * runDirectory.</li>
    * <li><b>logDirectory:</b> The {@link Util#getLogDirectoryPath logDirectory} name.
    * The default is logs in the runDirectory. A relative path will be resolved
    * in relation to the runDirectory.</li>
    * <li><b>xmlProperties:</b> The {@link Util#getXmlPropertiesFileName
    * xmlPropertiesFileName}. The default is {@link Util#getApplicationName
    * applicationName}. {@link Util#getProfile profile}.xml if it exists, or
    * {@link Util#getApplicationName applicationName}.xml.</li>
    * </ul>
    * <p>Additional environment entries, if any, used by the application can be
    * passed in the options parameter. After processing environment variables,
    * the application is initialized using the
    * {@link Util#initialize(String, Map) initialize} method.</p>
    * 
    * @param appName of the program, for example, CDAContentEvaluation. This
    * value can be overridden using the applicationName environment entry.
    * @param addOnPar One parameter with the name for each additional
    * environment entry used by the application.
    * @return A map with all the default and add on parameter names as keys
    * along with their corresponding values. Default values are also included in
    * the map for those parameters which were not overridden.
    */
   public static Map <String, String> initializeWebApp(String appName,
      String... addOnPar) {
      Map <String, String> pars = new HashMap <>();
      Map <String, String> addOnPars = new HashMap <>();
      
      try {
      Util.setWebApplication(true);
      // ------------------------------ process Util parameters
      
      for (KEYS key : KEYS.values()) {
         if (key.equals(KEYS.HELP)) continue;
         String keyStr = key.getLongOpt();
         String value = FacesUtil.getContextString(keyStr, null);
         if (value != null) pars.put(keyStr, value);
      }
      for (String keyStr : addOnPar) {
         String value = FacesUtil.getContextString(keyStr, null);
         if (value != null) pars.put(keyStr, value);
      }
      
      // default run directory is resource directory with name applicationName.
      String runDirName = appName;
      if (pars.containsKey(KEYS.APPLICATIONNAME.getLongOpt()))
         runDirName = pars.get(KEYS.APPLICATIONNAME.getLongOpt());
      if (pars.containsKey(KEYS.RUNDIRECTORY.getLongOpt())) 
         runDirName = pars.get(KEYS.RUNDIRECTORY.getLongOpt());
      List<Path> lp = Util.find(null, runDirName, PfnType.DIRECTORY, false);
      if (lp.isEmpty())
         throw new Exception("run directory " + runDirName + " not found.");
      if (lp.size() > 1)
         throw new Exception(lp.size() + " directories with name " + runDirName + " found.");
      pars.put(KEYS.RUNDIRECTORY.getLongOpt(), lp.get(0).toString());
      
      // Exception here means logging not initialized.
      } catch (Exception e) {
         System.err.println("WebUtil.initializeWebApp() - " + e.getMessage());
         System.exit(1);
      }

      Util.initialize(appName, pars);
      return addOnPars;
      
   } // EO initializeWebApp method

} // EO WebUtil class
