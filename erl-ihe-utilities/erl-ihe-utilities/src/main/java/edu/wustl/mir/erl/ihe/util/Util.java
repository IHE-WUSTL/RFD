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
package edu.wustl.mir.erl.ihe.util;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.tools.ant.DirectoryScanner;
import org.javatuples.LabelValue;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * General utility methods for IHE Connectathon tools software.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
@SuppressWarnings("restriction")
public class Util implements Serializable, UtilProperties {
   private static final long serialVersionUID = 1L;

   /**
    * The run profile id for the application, for example, "dev" for a
    * development system, "na" for "North American Connectathon", and so on. The
    * default is an empty string, meaning "no special profile". <b>Note:</b> The
    * profile may only contain Upper and lower case letters and decimal digits.
    */
   private static String profile = "";
   /**
    * The application instance name. The default value is passed to
    * {@link #initializeCommandLine} for command line applications, and to
    * initializeWebApp in WebUtil for web applications.
    */
   private static String applicationName = null;
   /**
    * Is this application web based? That is, running in an application server
    * such as Tomcat. This is set to <code>true</code> by the initializeWebApp
    * method in WebUtil, and <code>false</code> by the
    * {@link #initializeCommandLine(String, String[], Object[])
    * initializeCommandLine} method.
    */
   private static Boolean webApplication = null;
   /**
    * The run directory, that is, a directory containing properties files and
    * other program configuration data. For command line applications, the
    * default run directory is the current user directory at startup, that is,
    * the value of the system property "user.dir".
    * For web applications the default run directory is a resource, that is, a
    * directory in the /WEB-INF/classes, named {@link #applicationName}.
    */
   private static Path runDirectory = null;

   /**
    * The physical file name of the Apache Log4J properties file in the
    * {@link #runDirectory}.
    */

   private static String log4jPropertiesFileName = null;

   /**
    * The physical file name to get access to the runDirectory dedicated to
    * formManager inside RFD
    */

   private static Path formManagerRunDirectory = null;

   /**
    * The physical file name of the
    * {@link org.apache.commons.configuration.XMLConfiguration XMLConfiguration}
    * file in the {@link #runDirectory}.
    */
   /**
    * The log directory, that is, the directory where log files generated by the
    * application will be placed when logging to files is specified in the log4j
    * properties file. This directory must exist and have read, write, create
    * and delete permissions. The default is "logs" in the runDirectory.
    * <b>NOTE:</b> Used only if {@link FileAppender} or
    * {@link RollingFileAppender} are used for logging.
    */
   protected static Path logDirectory = null;

   /**
    * The file name of the xml properties file. This file must exist in the
    * {@link Util#runDirectory runDirectory} and be readable. The default is
    * {@link Util#applicationName applicationName}.{@link Util#profile profile}
    * .xml if it exists, or {@link Util#applicationName applicationName}.xml.
    * 
    * @see XMLConfiguration
    */
   private static String xmlPropertiesFileName = null;
   private static XMLConfiguration properties = null;
   /**
    * A flag indicating whether or not the application should terminate if an
    * internal error occurs. Internal errors are understood to be programming
    * errors, for example a divide by zero or attempting to use a database
    * connection that has already been closed, as opposed to an operational
    * error, for example an I/O error or an authentication failure. Defaults to
    * <i>true</i>, but can be set by adding the attribute:
    * 
    * <pre>
    * abortApplicationOnInternalError = &quot;false&quot;.
    * </pre>
    * 
    * to the &lt;Util> element in the applications properties file.
    * 
    * @see Util#exit(int)
    * @see Util#exit(String)
    */
   private static boolean abortApplicationOnInternalError = true;
   private static Logger log = null;
   private static Executor exec = Executors.newCachedThreadPool();
   private static String externalHostName = null;
   private static String externalHostIp = null;   

   /*
    * Set up temporary logging to console out. This will be replaced with the
    * logging in the appropriate configuration file.
    */
   static {
      PropertyConfigurator.configure(IOUtils
         .toInputStream("log4j.rootLogger=WARN, SYSTEM" + nl
            + "log4j.appender.SYSTEM=org.apache.log4j.ConsoleAppender" + nl
            + "log4j.appender.SYSTEM.layout=org.apache.log4j.PatternLayout"
            + nl
            + "log4j.appender.SYSTEM.layout.ConversionPattern=%d{dd HH:mm:ss} "
            + "STARTUP %5p: %m%n" + nl));
      log = Logger.getLogger(DEFAULT_LOG_NAME);

      System.setProperty("file.encoding", UTF_8);
   }

   /**
    * @return {@link Util#profile profile}
    */
   public static String getProfile() {
      return profile;
   }

   /**
    * @return the instance application name, which was passed to Util at program
    * startup when {@link #initializeCommandLine(String, String[], Object[])} or
    * the initializeWebApp method in WebUtil was invoked. Usually the program
    * name, like "SyslogBrowser".
    */
   public static String getApplicationName() {
      return applicationName;
   }

   /**
    * Set the {@link #webApplication} indicator.
    * 
    * @param isThisAWebApplication boolean <code>true</code> if the application
    * is running in an application server such as Tomcat. <code>false</code>
    * otherwise.
    */
   public static void setWebApplication(boolean isThisAWebApplication) {
      webApplication = isThisAWebApplication;
   }

   /**
    * @return {@link #webApplication}
    */
   public static boolean isWebApplication() {
      return webApplication;
   }

   /**
    * @return {@link org.apache.log4j.Logger Logger} for the "system" log
    * defined in the runDirectory/log4j.properties file. This is the system log,
    * intended to hold standard error, warning, and debugging messages. If other
    * loggers are defined for the application, they should be retrieved using
    * one of the org.apache.log4j.Logger#getLogger(parameter) methods.
    */
   public static Logger getLog() {
      return log;
   }

   /**
    * @return {@link org.apache.commons.configuration.XMLConfiguration
    * XMLConfiguration} properties object defined in
    * runDirectory/applicationName.xml. This is loaded during initialization.
    */
   public static XMLConfiguration getProperties() {
      return properties;
   }

   /**
    * @return {@link java.nio.file.Path Path} object for the application
    * runDirectory, which is the 'home' directory for the application. It
    * contains the applicationname.xml properties file, the log4j.properties
    * file, certificates, and other configuration data for the application which
    * is not stored in a Data Base.
    */
   public static Path getRunDirectoryPath() {
      return runDirectory;
   }

   /**
    * @return {@link java.nio.file.Path Path} object for the application
    * {@link #logDirectory}
    */
   public static Path getLogDirectoryPath() {
      return logDirectory;
   }

   /**
    * Override default log directory path
    * 
    * @param p new log directory path
    */
   public static void setLogDirectoryPath(Path p) {
      logDirectory = p;
   }

   /**
    * @return {@link #log4jPropertiesFileName}
    */
   public static String getLog4jPropertiesFileName() {
      return log4jPropertiesFileName;
   }

   /**
    * @return {@link #xmlPropertiesFileName}
    */
   public static String getXmlPropertiesFileName() {
      return xmlPropertiesFileName;
   }

   /**
    * @return {@link java.util.concurrent.Executor Executor} object for the
    * application. Currently this is a Cached Thread Pool.
    */
   public static Executor getExec() {
      return exec;
   }

   /**
    * @return String the fully qualified domain name for the server the
    * application is running on. This defaults to the
    * {@link InetAddress#getCanonicalHostName() canonical host name} of the
    * local host, but can be set using the Util[@externalHostname] attribute in
    * the applicationName.xml file.
    */
   public static String getExternalHostName() {
      return externalHostName;
   }

   /**
    * @return String the IP address for the server the application is running
    * on, in text format. This defaults to the
    * {@link InetAddress#getHostAddress() host address} of the local host, but
    * can be set using the Util[@externalHostIp] attribute in the
    * application.xml file.
    */
   public static String getExternalHostIp() {
      return externalHostIp;
   }

   /**
    * @return Path to the FormManagerRunDirectory
    */
   public static Path getFormManagerRunDirectory() {
      return formManagerRunDirectory;
   }

   /**
    * enum encapsulates settings for default parameters. The properties have the
    * same names as corresponding properties of
    * {@link org.apache.commons.cli.Option Option}.
    * <ul>
    * <li><b>opt</b> - Short name of the option.</li>
    * <li><b>longOpt</b> - Long multi-character name of the option.</li>
    * <li><b>hasArg</b> - Is an argument is required after this option?</li>
    * <li><b>description</b> - Self-documenting description</li>
    * </ul>
    * The instances are:
    * <ul>
    * <li><b>HELP</b> - Print help message and exit (command line only).</li>
    * <li><b>RUNDIRECTORY</b> - {@link Util#runDirectory runDirectory}</li>
    * <li><b>APPLICATIONNAME</b> - {@link Util#applicationName applicationName}</li>
    * <li><b>PROFILE</b> - {@link Util#profile profile}</li>
    * <li><b>LOG4JPROPERTIES</b> - {@link Util#log4jPropertiesFileName log4jPropertiesFileName}</li>
    * <li><b>LOGDIRECTORY</b> - {@link Util#logDirectory logDirectory}</li>
    * <li><b>XMLPROPERTIES</b> - {@link Util#xmlPropertiesFileName xmlPropertiesFileName}</li>
    * </ul>
    */
   public enum KEYS {
      /**
       * Print help message and exit (command line only).
       */
      HELP("h", "help", false, "help message and exit"),
      /**
       * Override value for {@link Util#runDirectory runDirectory}
       */
      RUNDIRECTORY("r", "runDirectory", true, "instance run Directory"),
         /**
          * Override value for {@link Util#applicationName applicationName}
          */
         APPLICATIONNAME("a", "applicationName", true,
            "instance applicationName"),
         /**
          * Override value for {@link Util#profile profile}
          */
         PROFILE("p", "profile", true, "run profile"),
         /**
          * Override value for
          */
         LOG4JPROPERTIES("l", "log4j.Properties", true,
            "log4j properties file name"),
         /**
          * Override value for {@link Util#logDirectory logDirectory}
          */
         LOGDIRECTORY("d", "logDirectory", true, "log directory"),
         /**
          * Override value for {@link Util#xmlPropertiesFileName
          * xmlPropertiesFileName}
          */
         XMLPROPERTIES("x", "xmlProperties", true, "XML properties file name");

      private String opt;
      private String longOpt;
      private boolean hasArg;
      private String description;

      KEYS(String o, String lo, boolean a, String desc) {
         opt = o;
         longOpt = lo;
         hasArg = a;
         description = desc;
      }

      /**
       * @return The {@link Option#getOpt() option name} for this KEY instance.
       * This or the long option may used in command line initializations.
       */
      public String getOpt() {
         return opt;
      }

      /**
       * @return The {@link Option#getLongOpt() long option name} for this KEY
       * instance. This or the option name may be used in command line
       * initializations. This is the value for the env-entry-name element in
       * WebUtil.
       */
      public String getLongOpt() {
         return longOpt;
      }

      /**
       * @return The {@link Option#hasArg() has argument flag} for this KEY
       * type. True if the KEY is to have an argument, false otherwise.
       */
      public boolean getHasArg() {
         return hasArg;
      }

      /**
       * @return The {@link Option#getDescription() description} for this KEY
       * type.
       */
      public String getDescription() {
         return description;
      }

      /**
       * @param longOption String representing a long option name.
       * @return true if this is the long option name of one of the instances of
       * the KEY enum, false otherwise.
       */
      public static boolean oneOf(String longOption) {
         for (KEYS key : KEYS.values()) {
            if (longOption.equals(key.getLongOpt())) return true;
         }
         return false;
      }
   }

   /**
    * <p>Handles initialization for programs invoked from the command line, shell
    * script or ant task.</p>
    * <p>This method recognizes these command line parameters, all of which are
    * optional:</p>
    * <ul>
    * <li>-h or -help, which causes the program to print a standard command
    * line help message to standard out and terminate.</li>
    * <li>-r or -runDirectory {@link #runDirectory}. For command line
    * applications, the default is the current user directory, that is, the
    * value of the Java system property "user.dir".</li>
    * <li>-a or -applicationName {@link #applicationName}. The default
    * applicationName is passed from the calling class.</li>
    * <li>-p or -profile The {@link Util#profile profile}. The default value is
    * no profile.</li>
    * <li>-l or -log4jProperties The {@link #log4jPropertiesFileName} The
    * default is log4j.{@link Util#profile profile}.properties if it exists, or
    * log4j.properties.</li>
    * <li>-d or -logDirectory The {@link #logDirectory}.</li>
    * <li>-x or -xmlProperties The {@link #xmlPropertiesFileName}.</li>
    * </ul>
    * Command line parameters are parsed using the <a href=
    * "http://pubs.opengroup.org/onlinepubs/009695399/basedefs/xbd_chap12.html"
    * >POSIX</a> standard.
    * <p>
    * Additional command line parameters required by the application may be
    * passed as an Object[] in the addOns parameter, whose entries must be
    * instances of {@link org.apache.commons.cli.Option Option} or
    * {@link org.apache.commons.cli.OptionGroup OptionGroup}. Add on parameters
    * must not conflict with the built in parameters; since all the built in
    * parameters have lower case short option codes, an easy way to avoid
    * conflict would be to use upper case short option codes for add on
    * parameters. If no additional parameters are required, the value of addOns
    * should be null. After processing Command line parameters, initializes the
    * application by invoking {@link Util#initialize(String, Map) initialize}.</p>
    * 
    * @param defaultApplicationName of the program, for example, WSServer.
    * @param args Argument vector from main method.
    * @param addOns Array of Option and/or OptionGroup instances describing
    * additional command line arguments, null if no add ons.
    * @return Map&lt;String, String%rt; containing long option name and value of any
    * addOns which appeared on the command line. Map may be empty, but will
    * never be null.
    */
   public static Map <String, String> initializeCommandLine(
      String defaultApplicationName, String[] args, Object[] addOns) {
      setWebApplication(false);
      Map <String, String> pars = new HashMap <>();
      Map <String, String> addOnPars = new HashMap <>();
      try {

         CommandLineParser parser = new PosixParser();
         Options opts = new Options();
         for (KEYS k : KEYS.values()) {
            opts.addOption(k.getOpt(), k.getLongOpt(), k.getHasArg(),
               k.getDescription());
         }

         if (addOns != null) {
            for (Object o : addOns) {
               if (o instanceof Option) opts.addOption((Option) o);
               if (o instanceof OptionGroup)
                  opts.addOptionGroup((OptionGroup) o);
            }
         }
         // -------------------------------- parse command line arguments
         CommandLine line = parser.parse(opts, args);

         // ----------------------------------- Process help parameters
         if (line.hasOption(KEYS.HELP.getLongOpt())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Util.applicationName + " ", opts);
            System.exit(0);
         }

         // ---------------------------- Process parameters
         for (Option o : line.getOptions()) {

            // ------------------------------ parameters from Util
            if (KEYS.oneOf(o.getLongOpt())) {
               pars.put(o.getLongOpt(), o.getValue());
               continue;
            }

            // ------------------------ parameters from application
            addOnPars.put(o.getLongOpt(), o.getValue());
         }

         // ----------------- default runDirectory is current user.dir
         if (!pars.containsKey(KEYS.RUNDIRECTORY.getLongOpt())) {
            pars.put(KEYS.RUNDIRECTORY.getLongOpt(),
               System.getProperty("user.dir"));
         }

         // Exception here means logging not initialized.
      } catch (Exception e) {
         System.err.println("Util.initializeCommandLine() - " + e.getMessage());
         System.exit(1);
      }

      initialize(defaultApplicationName, pars);
      return addOnPars;
   } // EO processMainArguments method

   /**
    * Initializes application, including:
    * <ul>
    * <li>Validating applicationName</li>
    * <li>Validating runDirectory</li>
    * <li>Validating the profile, if any.</li>
    * <li>Initializing log4j logging, including validating the logDirectory if
    * file logging is used.</li>
    * <li>Initializing properties.</li>
    * <li>Initializing thread Executor</li>
    * </ul>
    * <p><b>NOTE:</b> Any error will be logged or printed to stderr, and will cause
    * program to terminate.
    * </p>
    * 
    * @param defApplicationName Default name of the application, passed from
    * calling program.
    * @param pars A {@link java.util.Map Map} of those parameters which were
    * found on the command line or in the web.xml.
    */
   public static void initialize(String defApplicationName,
      Map <String, String> pars) {
      try {
         // ----------------------------------- applicationName
         applicationName = defApplicationName;
         if (pars.containsKey(KEYS.APPLICATIONNAME.getLongOpt()))
            applicationName = pars.get(KEYS.APPLICATIONNAME.getLongOpt());
         applicationName = StringUtils.trimToEmpty(applicationName);
         if (applicationName.isEmpty())
            throw new Exception("blank applicationName passed");

         // ----------- runDirectory valid directory with "rx" permissions.
         runDirectory = Paths.get(pars.get(KEYS.RUNDIRECTORY.getLongOpt()));
         isValidPfn("Run directory", runDirectory, PfnType.DIRECTORY, "rx");

         processHostOverrides(pars);

         // ------------------------------------------ profile
         if (pars.containsKey(KEYS.PROFILE.getLongOpt())) {
            profile =
               StringUtils.trimToEmpty(pars.get(KEYS.PROFILE.getLongOpt()));
            if (!profile.isEmpty() && !StringUtils.isAlphanumeric(profile))
               throw new Exception("profile: " + profile
                  + " contains characters other than letters and digits.");
         }

         // -------------- Get path for log4j properties file
         Path pth;
         do {
            // File name passed as parameter (must match)
            if (pars.containsKey(KEYS.LOG4JPROPERTIES.getLongOpt())) {
               pth =
                  runDirectory.resolve(pars.get(KEYS.LOG4JPROPERTIES
                     .getLongOpt()));
               Util.isValidPfn("log properties", pth, PfnType.FILE, "r");
               break;
            }
            // File name for specific profile, if it exists
            if (!profile.isEmpty()) {
               try {
                  pth =
                     runDirectory.resolve("log4j." + profile + ".properties");
                  log4jPropertiesFileName = pth.toString();
                  Util.isValidPfn("log properties", pth, PfnType.FILE, "r");
                  break;
               } catch (Exception e) {}
            }
            // File name is log4j.properties.
            pth = runDirectory.resolve("log4j.properties");
            Util.isValidPfn("log properties", pth, PfnType.FILE, "r");
         } while (false);

         // ------------------ Get logDirectory path.
         if (pars.containsKey(KEYS.LOGDIRECTORY.getLongOpt())) {
            logDirectory =
               runDirectory.resolve(pars.get(KEYS.LOGDIRECTORY.getLongOpt()));
         } else {
            logDirectory = runDirectory.resolve("logs");
         }
         PropertyConfigurator.configure(pth.toString());

         log = Logger.getLogger(DEFAULT_LOG_NAME);
         log.info("Util.initialize(" + applicationName + ")");
         log.info("runDirectory: " + runDirectory.toString());
         log.info("profile: " + (profile == null ? "none" : profile));
         log.info("logging initialized using " + pth.getFileName());

         Logger elog = Logger.getLogger(ENV_LOG_NAME);
         // log class path data
         if (elog.isTraceEnabled()) {
            ClassLoader cl = ClassLoader.getSystemClassLoader();
            URL[] urls = ((URLClassLoader) cl).getURLs();
            StringBuilder str = new StringBuilder("Classpath:" + nl);
            for (URL url : urls)
               str.append("   ").append(url.getFile()).append(nl);
            elog.trace(str);
         }

         // log system properties
         if (elog.isTraceEnabled()) {
            Properties sysProps = System.getProperties();
            @SuppressWarnings("unchecked")
            List <String> sl =
               (List <String>) Collections.list(sysProps.propertyNames());
            Collections.sort(sl);
            StringBuilder str = new StringBuilder("System Properties:" + nl);
            for (String s : sl) {
               str.append("   " + s + ":" + sysProps.getProperty(s) + nl);
            }
            elog.trace(str);
         }

         // log system environment
         if (elog.isTraceEnabled()) {
            Map <String, String> envProps =
               new TreeMap <String, String>(System.getenv());
            Iterator <String> si = envProps.keySet().iterator();
            StringBuilder str =
               new StringBuilder("Environment Properties:" + nl);
            while (si.hasNext()) {
               String s = si.next();
               str.append("   " + s + ":" + envProps.get(s) + nl);
            }
            elog.trace(str);
         }

         // -------------------- get path for xml properties file
         do {
            // File name passed as parameter, MUST MATCH
            if (pars.containsKey(KEYS.XMLPROPERTIES.getLongOpt())) {
               pth =
                  runDirectory.resolve(pars.get(KEYS.XMLPROPERTIES.getLongOpt()
                     + ".xml"));
               isValidPfn("properties file", pth, PfnType.FILE, "rw");
               break;
            }
            // File name for specific profile, if it exists
            if (!profile.isEmpty()) {
               pth =
                  runDirectory
                     .resolve(applicationName + "." + profile + ".xml");
               try {
                  isValidPfn("xml properties", pth, PfnType.FILE, "r");
                  break;
               } catch (Exception e) {}
            }
            pth = runDirectory.resolve(applicationName + ".xml");
            isValidPfn("xmlproperties", pth, PfnType.FILE, "r");
         } while (false);
         log.info("loading parameters from " + pth.getFileName());
         properties = new XMLConfiguration(pth.toString());
         abortApplicationOnInternalError =
            properties.getBoolean("Util[@abortApplicationOnInternalError]",
               true);

         InetAddress ina = InetAddress.getLocalHost();
         externalHostName =
            properties.getString("Util[@externalHostName]",
               ina.getCanonicalHostName());
         externalHostIp =
            properties.getString("Util[@externalHostIp]", ina.getHostAddress());
      } catch (Exception e) {
         String em = "Util.initialize() - " + e.getMessage();
         if (log != null) log.error(em);
         else System.err.println(em);
         Util.exit(1);
      }
   }

   /**
    * Returns the current system time as a date-time stamp in RFC 3339 format,
    * that is, yyyy-MM-dd'T'HH:mm:ss.SSSZ
    * 
    * @return String date-time stamp
    */
   public static String getRFC3339TimeStamp() {
      return getRFC3339TimeStamp(new Date());
   }

   /**
    * Returns the passed date as a date-time string in RFC 3339 format, that is,
    * yyyy-MM-dd'T'HH:mm:ss.SSSZ
    * 
    * @param ts date-time to represent
    * @return String date-time representation.
    */
   public static String getRFC3339TimeStamp(Date ts) {
      String a = RFC3339_TIMESTAMP_FORMAT.format(ts);
      return a.substring(0, a.length() - 2) + ":" + a.substring(a.length() - 2);
   }

   /**
    * Format passed ssn as xxx-xx-xxxx
    * 
    * @param ssn social security number as 9 character string.
    * @return formatted ssn
    */
   public static String formatSSN(String ssn) {
      if (ssn == null || ssn.length() == 0) return "";
      String s = ssn;
      while (s.length() < 9)
         s = "0" + s;
      return s.substring(0, 3) + "-" + s.substring(3, 5) + "-"
         + s.substring(5, 8);
   }

   /**
    * Determines if the two passed strings are significantly equal, that is,
    * they are not null, empty, or just whitespace, and their contents are
    * equal. Ignores case.
    * 
    * @param one first string
    * @param two second string
    * @return boolean true if strings are significantly equal, false otherwise.
    */
   public static boolean significantlyEqual(String one, String two) {
      String a = StringUtils.trimToEmpty(one);
      String b = StringUtils.trimToEmpty(two);
      if (a.length() > 0 && a.equalsIgnoreCase(b)) return true;
      return false;
   }

   /**
    * Determines if the two passed {@link java.sql.Date Date} objects are
    * significantly equal, that is, they are not null and represent the same
    * date.
    * 
    * @param one first Date
    * @param two second Date
    * @return boolean true if dates are significantly equal, false otherwise.
    */
   public static boolean
      significantlyEqual(java.sql.Date one, java.sql.Date two) {
      if (one != null && two != null && one.compareTo(two) == 0) return true;
      return false;
   }

   /**
    * Does the first int match any of the following int?
    * 
    * @param value int to match
    * @param matches possible matching ints
    * @return boolean <code>true</code> if value matches any of the subsequent
    * passed matching ints, <code>false</code> otherwise.
    */
   public static boolean isOneOf(int value, int... matches) {
      for (int match : matches) {
         if (value == match) return true;
      }
      return false;
   }

   /**
    * Does the first string match any of the following strings? <b>Not case
    * sensitive.</b>
    * 
    * @param str String to match
    * @param matches possible matching strings
    * @return boolean <code>true</code> if the first string matches any of the
    * subsequent passed Strings, <code>false</code> otherwise.
    */
   public static boolean isOneOf(String str, String... matches) {
      for (String match : matches) {
         if (str.equalsIgnoreCase(match)) return true;
      }
      return false;
   }

   /**
    * Does the first string match any of the following strings? <b>Case
    * sensitive.</b>
    * 
    * @param str String to match
    * @param matches possible matching strings
    * @return boolean <code>true</code> if the first string matches any of the
    * subsequent passed Strings, <code>false</code> otherwise.
    */
   public static boolean isExactlyOneOf(String str, String... matches) {
      for (String match : matches) {
         if (str.equals(match)) return true;
      }
      return false;
   }

   /**
    * Returns passed parameters as a single, comma delimited, string
    * 
    * @param parameters to join
    * @return String joined parameters
    */
   public static String join(String... parameters) {
      return StringUtils.join(parameters, ',');
   }

   /**
    * generates MD5 hash of password to store in DB.
    * 
    * @param pw String plain text password
    * @return String md5 hash of password
    */
   public static synchronized String hashPw(String pw) {
      return DigestUtils.md5Hex(pw);
   }

   /**
    * Validates the passed string as an ipv4 address in dot notation. If so,
    * formats it to have all four tuples with no leading zeros except for zero
    * value tuple.
    * 
    * @param in ip address to format
    * @return same ip address, formatted to remove excess zeroes
    * @throws Exception if passed string is not a valid ipv4 address in dot
    * format.
    */
   public static String validateFormatIpv4(String in) throws Exception {
      int[] tuple = { 0, 0, 0, 0 };
      int i = 0, val;
      try (Scanner s = new Scanner(in)) {
         s.useDelimiter("\\.");
         while (s.hasNext()) {
            if (i == 4) throw new Exception("Too much information");
            if (!s.hasNextInt()) throw new Exception("invalid integer found");
            val = s.nextInt();
            if (val < 0 || val > 255) throw new Exception("not 0-255");
            tuple[i] = val;
            i++ ;
         }
      } catch (Exception e) {
         log.warn(in + " not valid ipv4 address: " + e.getMessage());
         throw e;
      }
      return tuple[0] + "." + tuple[1] + "." + tuple[2] + "." + tuple[3];
   }

   /**
    * Attempts to format an XML String for pretty printing.
    * 
    * @param msg XML String to format.
    * @return String If the attempt to format the XML message for pretty print
    * succeeds, the message in pretty print format is returned.
    * <p>If the attempt fails, a log message giving some reason as to why it failed
    * will be generated, and a copy of the original message is returned, but no
    * exception is thrown.</p>
    */
   public static String prettyPrintXML(String msg) {
      String xml;
      String n = System.getProperty("line.separator");
      try {
         // ---------------------------- make sure something is there
         String m = StringUtils.stripToEmpty(msg);
         if (m.length() == 0) throw new Exception("message empty or null");
         String hdr = StringUtils.substringBefore(m, "<?xml");
         if (hdr.length() == m.length())
            throw new Exception("no XML document in message");
         xml = StringUtils.substringAfter(m, hdr);
         // -------------------------------------- String => Document
         InputSource src = new InputSource(new StringReader(xml));
         DocumentBuilderFactory dbFactory =
            DocumentBuilderFactory.newInstance();
         dbFactory.setNamespaceAware(true);
         Document doc = dbFactory.newDocumentBuilder().parse(src);
         // ------------------------------------ Pretty print format
         OutputFormat format = new OutputFormat();
         format.setMediaType("text");
         format.setLineWidth(80);
         format.setIndenting(true);
         format.setIndent(3);
         format.setEncoding(UTF_8);
         // -------------------------------------- Document => String
         StringWriter stringOut = new StringWriter();
         XMLSerializer serial = new XMLSerializer(stringOut, format);
         serial.serialize(doc);
         return hdr + n + stringOut.toString();

      } catch (Exception e) {
         log.warn("prettyPrintXML error:" + e.getMessage());
         return msg;
      }
   } // EO prettyPrintXML

   /**
    * Attempts to format a SOAP message for pretty printing.
    * 
    * @param msg SOAP msg to format.
    * @return String If the attempt to format the SOAP message for pretty print
    * succeeds, the message in pretty print format is returned.
    * <p>If the attempt fails, a log message giving some reason as to why it failed
    * will be generated, and a copy of the original message is returned, but no
    * exception is thrown.</p>
    */
   public static String prettyPrintSOAP(String msg) {
      try {
         // ---------------------------- make sure something is there
         String xml = StringUtils.stripToEmpty(msg);
         if (xml.length() == 0) throw new Exception("message empty or null");
         // -------------------------------------- String => Document
         InputSource src = new InputSource(new StringReader(xml));
         DocumentBuilderFactory dbFactory =
            DocumentBuilderFactory.newInstance();
         dbFactory.setNamespaceAware(true);
         Document doc = dbFactory.newDocumentBuilder().parse(src);
         // ------------------------------------ Pretty print format
         OutputFormat format = new OutputFormat();
         format.setMediaType("text");
         format.setLineWidth(80);
         format.setIndenting(true);
         format.setIndent(3);
         format.setEncoding(UTF_8);
         format.setOmitXMLDeclaration(true);
         // -------------------------------------- Document => String
         StringWriter stringOut = new StringWriter();
         XMLSerializer serial = new XMLSerializer(stringOut, format);
         serial.serialize(doc);
         return stringOut.toString();

      } catch (Exception e) {
         log.warn("prettyPrintXML error:" + e.getMessage());
         return nl + msg;
      }
   } // EO prettyPrintXML

   /**
    * Throw an exception if the passed object is null
    * 
    * @param obj object to be tested, any type
    * @param em error message for exception
    * @throws Exception with message if passed object is null
    */
   public static void nullException(Object obj, String em) throws Exception {
      if (obj == null) throw new Exception(em);
   }

   /**
    * Application optional exit method. If the value of
    * {@link Util#abortApplicationOnInternalError
    * abortApplicationOnInternalError} is true, the application will terminate
    * with exit status. If false, the application will log and continue.
    * 
    * @param status exit status if program terminates.
    * @see #exit(String)
    */
   public static void exit(int status) {
      if (abortApplicationOnInternalError) {
         log.error("*** System exit on internal error, status " + status
            + "***");
         System.exit(status);
      }
      log.warn("*** System continue on internal error, status " + status
         + "***");
   }

   /**
    * Application optional exit method. logs message, then, If the value of the
    * property {@link Util#abortApplicationOnInternalError
    * abortApplicationOnInternalError} is true, the application will terminate
    * with exit status 1. If false, the application will log and continue.
    * 
    * @param msg to log.
    * @see #exit(int)
    */
   public static void exit(String msg) {
      log.fatal(msg);
      exit(1);
   }

   /**
    * Returns the Simple class name, that is, without the package name, for
    * passed Object.
    * 
    * @param object instance object
    * @return String simple class name for this class.
    */
   public static String getName(Object object) {
      return object.getClass().getSimpleName();
   }

   /**
    * <p>Is the domain name equal to or a domain prefix of the Fully Qualified
    * Domain Name? For example:</p>
    * 
    * <pre>
    *    areSameDomainName("gazelle", "gazelle.zoo.stl"); and
    *    areSameDomainName("gazelle.zoo.stl", "gazelle.zoo.stl");
    * </pre>
    * 
    * will return <i>true</i>, while
    * 
    * <pre>
    *    areSameDomainName("gorilla", "gazelle.zoo.stl"); and
    *    areSameDomainName("gazel", "gazelle.zoo.stl");
    * </pre>
    * 
    * <p>will return <i>false</i>.</p>
    * 
    * @param dn String domain applicationName to test
    * @param fqdn String Fully Qualified Domain Name to test against
    * @return boolean <code>true</code> if dn is equal to the fqdn or some dot
    * (.) delimited prefix of the fqdn, <code>false</code> otherwise.
    */
   public static boolean areSameDomainName(String dn, String fqdn) {
      if (dn.length() > fqdn.length()) return false;
      if (dn.length() == fqdn.length()) return dn.equalsIgnoreCase(fqdn);
      if (fqdn.charAt(dn.length()) != '.') return false;
      return dn.equalsIgnoreCase(fqdn.substring(0, dn.length() - 1));
   }

   /**
    * Enum used to codify the distinction between a file and a directory for:
    * <ul>
    * <li>The {@link #isValidPfn(String,Path,PfnType,String) isValidPfn}
    * method, in which case the entry must be of this type.</li>
    * <li>The {@link Util#find(String, String, PfnType, Boolean) find} method,
    * in which case only entries of this type are returned.</li>
    * </ul>
    */
   public enum PfnType {
      /**
       * A directory or a link to a directory.
       */
      DIRECTORY,
      /**
       * A file or a link to a file.
       */
      FILE
   };

   /**
    * Validates that a directory or file exists and has needed permissions.
    * 
    * @param name Logical name of file/dir, for error message, for example,
    * "Message file".
    * @param path file/dir path to validate
    * @param pfnType DIRECTORY or FILE
    * @param cds String containing codes for needed permissions: r=read,
    * w=write, x=executable; for example "rw" for read-write permissions needed.
    * Case is ignored.
    * @throws Exception on error containing logical name, path, and error
    * description.
    */
   public static void isValidPfn(String name, Path path, Util.PfnType pfnType,
      String cds) throws Exception {

      String msg = name + " " + path + " ";
      String c = StringUtils.stripToEmpty(cds).toLowerCase();

      File file = path.toFile();

      if (!file.exists()) throw new Exception(msg + "not found");

      switch (pfnType) {
         case DIRECTORY:
            if (!file.isDirectory())
               throw new Exception(msg + "is not a directory");
            break;
         case FILE:
            if (!file.isFile()) throw new Exception(msg + "is not a file");
            break;
         default:
            exit("Unrecognized PfnType passed to isValidPfn method.");
      }

      // ----- return all permission errors at once
      String errs = "";
      if (c.contains("x"))
         if (!file.canExecute()) errs += "is not executable" + nl;

      if (c.contains("r")) if (!file.canRead()) errs += "is not readable" + nl;

      if (c.contains("w"))
         if (!file.canWrite()) errs += "is not writable" + nl;

      if (errs.length() > 0) throw new Exception(msg + errs);

   } // EO isValidPfn method

   /**
    * Validates that a directory or file exists and has needed permissions.
    * 
    * @param name Logical name of file/dir, for error message, for example,
    * "Message file".
    * @param file file/dir File to validate
    * @param pfnType DIRECTORY or FILE
    * @param cds String containing codes for needed permissions: r=read,
    * w=write, x=executable; for example "rw" for read-write permissions needed.
    * Case is ignored.
    * @throws Exception on error containing logical name, path, and error
    * description.
    */
   public static void isValidPfn(String name, File file, Util.PfnType pfnType,
      String cds) throws Exception {
      Util.isValidPfn(name, file.toPath(), pfnType, cds);
   }

   /**
    * <p>Converts a yes/no type string to a boolean value.</p>
    * <p>Acceptable true values are "YES", "Y", "TRUE", "T", "1", and "ON".</p>
    * <p>Acceptable false values are "NO", "N", "FALSE", "F", "0", and "OFF"</p>
    * Not case sensitive; leading and trailing white space ignored.
    * 
    * @param emHeader header string for Exception, if thrown.
    * @param value String to be evaluated.
    * @return boolean if string matches one of the acceptable values.
    * @throws Exception if no match.
    */
   public static boolean getYesNo(String emHeader, String value)
      throws Exception {
      String v = StringUtils.trimToEmpty(value);
      if (Util.isOneOf(v, "YES", "Y", "TRUE", "T", "ON", "1")) return true;
      if (Util.isOneOf(v, "NO", "N", "FALSE", "F", "OFF", "0")) return false;
      throw new Exception(emHeader + v + "not an accepted yes/no value");
   }

   /**
    * Parse parameter to an integer, no Exception on error.
    * 
    * @param value String to be parsed.
    * @return Integer value of String; null on error.
    */
   public static Integer parseIntOrNull(String value) {
      try {
         return Integer.parseInt(value);
      } catch (NumberFormatException e) {
         return null;
      }
   }

   /**
    * Gets the simple name of a method in its calling stack. For example, in the
    * code below, methodTwo will return:
    * <ul>
    * <li>"callingMethod" if "level" is 1.</li>
    * <li>"methodTwo" if "level" is 2.</li>
    * <li>"methodOne" if "level" is 3.</li>
    * </ul>
    * 
    * <pre>
    * public void methodOne() {
    *    methodTwo();
    * }
    * 
    * public void methodTwo() {
    *    return callingMethod(level);
    * }
    * </pre>
    * 
    * @param level The stack level to examine, 0 through the number of levels in
    * the stack.
    * @return the name of the calling method, or "Unknown".
    */
   public static String callingMethod(int level) {
      if (level < 0) return "Unknown";
      StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
      if (stacktrace.length < (level + 1)) return "Unknown";
      StackTraceElement e = stacktrace[level];
      return e.getMethodName();
   }

   /**
    * Gets the simple name of a class in its calling stack. For example, in the
    * code below, methodTwo will return:
    * <ul>
    * <li>"Util" if "level" is 1.</li>
    * <li>"classTwo" if "level" is 2.</li>
    * <li>"classOne" if "level" is 3.</li>
    * </ul>
    * 
    * <pre>
    *    public class classOne {
    *    ...
    *    public void methodOne() {
    *       methodTwo();
    *    }
    *    ...
    *    public class classTwo {
    *    ...
    *    public void methodTwo() {
    *       return callingMethod(level);
    *    }
    * </pre>
    * 
    * @param level The stack level to examine, 0 through the number of levels in
    * the stack.
    * @return the name of the calling class, or "Unknown".
    */
   public static String callingClass(int level) {
      if (level < 0) return "Unknown";
      StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
      if (stacktrace.length < (level + 1)) return "Unknown";
      StackTraceElement e = stacktrace[level];
      return StringUtils.substringBefore(e.getFileName(), ".");
   }

   /**
    * @return string of the form "simpleClassName.MethodName " for the method
    * which called this method.
    */
   public static String classMethod() {
      return callingClass(3) + "." + callingMethod(3) + " ";
   }

   /**
    * Logs standard class method invoked message to passed Logger. If passed
    * Logger is null, system Logger will be used.
    * 
    * @param lg logger to log message to
    */
   public static void invoked(Logger lg) {
      if (lg == null) lg = log;
      lg.trace(callingClass(3) + "." + callingMethod(3) + " invoked");
   }

   /**
    * Logs standard class method invoked message to system Logger
    */
   public static void invoked() {
      log.trace(callingClass(3) + "." + callingMethod(3) + " invoked");
   }

   private static Path rootPath = null;

   /**
    * A simple utility method for locating files and/or directories in a
    * directory tree which match some parameter settings, designed to be
    * somewhat like the Linux find command. More sophisticated directory
    * scanning can be achieved using the
    * {@link org.apache.tools.ant.DirectoryScanner Ant directory scanner}, whose
    * library is included in this project. A call to this routine is roughly
    * like executing:
    * 
    * <pre>
    * find ${baseDir} -name ${includes} -type ${type} -ls
    * </pre>
    * 
    * <b>Programmer notes:</b>
    * <ul>
    * <li>All occurrences of "/" and "\" will be replaced with the system file
    * separator character. For consistency, please use the Linux "/" style in
    * coding.</li>
    * <li>Absolute baseDir references will require system specific strings, but
    * in most cases you can stick to relative baseDir entries and avoid this
    * problem.</li>
    * <li>It is recommended that file and directory names used in applications
    * be unique regardless of case sensitivity, for example, do not use
    * "Rundirectory" and "runDirectory" in the same application. Setting the
    * caseSensitive parameter to <code>false</code> may be helpful in avoiding
    * problems when running applications on Windows.</li>
    * </ul>
    * 
    * @param baseDir A string indicating the base directory which is to be
    * searched. Relative references are relative to the "root directory" of the
    * application. The root directory of regular java applications is the
    * {@link #runDirectory}, for web applications it is the WEB-INF/classes
    * directory. The parameter may be null, in which case the default value is
    * ".";
    * @param includes a String pattern, used to find matching files and
    * directories, like the find -name pattern. For example:
    * <ul>
    * <li><b>"*.java"</b> would match all files with .java extension.</li>
    * <li><b>"log"</b> would look for files or directories named log.</li>
    * </ul>
    * <p>
    * More details on valid patterns can be found in the javadoc for the
    * {@link org.apache.tools.ant.DirectoryScanner Ant directory scanner}.
    * </p>
    * @param type An {@link PfnType} indication of whether files or directories
    * are to be considered. This can be null, in which case both files and
    * directories will be considered.
    * @param caseSensitive A Boolean indicating whether the pattern matches are
    * to be case sensitive. This can be null, in which case the default is
    * <code>true</code>, that is, the search will be case sensitive.
    * @return A {@link List} of absolute {@link Path Paths} for those files and
    * or directories which meet the criteria. The list may be empty, but it will
    * never be null.
    * @throws Exception on error. In general, these will be programming errors.
    */
   public static List <Path> find(String baseDir, String includes,
      PfnType type, Boolean caseSensitive) throws Exception {
      try {
         if (rootPath == null) {
            // Compute and validate root directory path
            Path root = null;
            if (webApplication == null)
               throw new Exception("webApplication not set");
            if (isWebApplication()) {
               root =
                  Paths.get(Util.class.getClassLoader().getResource("/")
                     .toURI());
            } else {
               if (runDirectory == null)
                  throw new Exception("runDirectory not set");
               root = runDirectory;
            }
            Util.isValidPfn("application resource root", root,
               PfnType.DIRECTORY, "rw");
            rootPath = root;
         } // compute root directory path

         DirectoryScanner ds = new DirectoryScanner();
         String bd = ".";
         if (baseDir != null) bd = baseDir;
         Path bdPath = rootPath.resolve(bd).normalize();
         ds.setBasedir(bdPath.toFile());
         if (StringUtils.isNotBlank(includes))
            ds.setIncludes(new String[] { includes });
         if (caseSensitive != null) ds.setCaseSensitive(caseSensitive);
         ds.scan();
         List <String> listStr = new ArrayList <>();
         if (type == null || type == PfnType.DIRECTORY)
            listStr.addAll(Arrays.asList(ds.getIncludedDirectories()));
         if (type == null || type == PfnType.FILE)
            listStr.addAll(Arrays.asList(ds.getIncludedFiles()));
         List <Path> listPath = new ArrayList <>();
         for (String s : listStr) {
            listPath.add(bdPath.resolve(s));
         }
         return listPath;
      } catch (Exception e) {
         log.warn("Util.find error: " + e.getMessage());
         throw e;
      }
   } // EO find method

   /**
    * Processes host based overrides in the overrides.xml file, if any. This
    * must be called after the runDirectory has been determined, and before any
    * parameters it may override are used. Functions by "put"ing the attributes
    * and values into the pars file, using the attribute name as the key and the
    * attribute value as the value. If a key is used which already exists, that
    * value will be overridden. Looks only for an element Hosts.hostname in the
    * file.
    * If override.xml does not exist, or the Hosts.hostname node does not exist,
    * the method returns silently. Other errors are assumed to be programming
    * errors and are fatal.
    * Assumes logging is not set up and logs to System.err.
    * 
    * @param parms the parameter map for the application.
    */
   private static void processHostOverrides(Map <String, String> parms) {
      try {
         Path fpath = runDirectory.resolve("overrides.xml");
         File f = fpath.toFile();
         if (!f.exists()) return;
         XMLConfiguration overrides = new XMLConfiguration(f);
         String host = InetAddress.getLocalHost().getHostName();
         List <HierarchicalConfiguration> me =
            overrides.configurationsAt("Hosts." + host);
         for (HierarchicalConfiguration hc : me) {
            Iterator <String> keys = hc.getKeys();
            while (keys.hasNext()) {
               String key = keys.next();
               String val = hc.getString(key);
               if (key.startsWith("[@") && key.endsWith("]"))
                  key = key.substring(2, key.length() - 1);
               parms.put(key, val);
               log.warn("Override " + host + " " + key + "=" + val);
            }
         }
         return;
      } catch (Exception e) {
         log.fatal("processHostOverrides error: " + e.getMessage());
         Util.exit(1);
      }
   } // EO processHostOverrides method

   /**
    * Run a command on the operating system. Uses
    * {@link java.lang.Runtime#exec(String) Runtime.exec} and will wait for the
    * command to complete.
    * <p><b>Notes:</b></p>
    * <ul>
    * <li>Designed for short commands, which will quickly return.</li>
    * <li>Assumes that return status of 0 is OK, non-zero is an error.</li>
    * <li>Programmer is responsible for OS differences.</li>
    * </ul>
    * 
    * @param cmd command to be run.
    * @return String with the standard output of the command, if it executes
    * with a return status of 0. Otherwise, throws an exception.
    * @throws Exception if the command return status is non-zero, or if any
    * exception (for example, IOException) is thrown. Note: The error is logged
    * to the system log at warn level before the Exception is thrown.
    * <p>
    * <b>OSJVM</b> This method is OS and/or jvm specific and should be tested on
    * all deployment platforms. Currently tested on:</p>
    * <ul>
    * <li>Ubuntu 14.04 LTS using SUN Java 7</li>
    * </ul>
    */
   public static List <String> runCmd(String cmd) throws Exception {
      InputStream is = null;
      try {
         Process p = Runtime.getRuntime().exec(cmd);
         int r = p.waitFor();
         if (r != 0) {
            is = p.getErrorStream();
            String err = IOUtils.toString(is, CHAR_SET_UTF_8);
            throw new Exception(" exit value " + r + nl + "err: " + err);
         }
         is = p.getInputStream();
         return IOUtils.readLines(is, CHAR_SET_UTF_8);
      } catch (Exception e) {
         String em = "runCmd(" + cmd + ") error: " + e.getMessage();
         log.warn(em);
         throw new Exception(em);
      } finally {
         if (is != null) is.close();
      }
   } // EO runCmd method.

   /**
    * @return the system process id (pid) of this process.
    * <p>
    * <b>OSJVM</b> This method is OS and/or jvm specific and should be tested on
    * all deployment platforms. Currently tested on:</p>
    * <ul>
    * <li>Ubuntu 14.04 LTS using SUN Java 7</li>
    * </ul>
    */
   public static String getPid() {
      return ManagementFactory.getRuntimeMXBean().getName().split("@")[0]
         .trim();
   }

   /**
    * @return The <u>current</u> process id of the parent process to this
    * process.
    * @throws Exception
    * <p><b>MOD</b> Need to add code for Windows systems <b>OSJVM</b> This method
    * is OS and/or jvm specific and should be tested on all deployment
    * platforms. Currently tested on:</p>
    * <ul>
    * <li>Ubuntu 14.04 LTS using SUN Java 7</li>
    * </ul>
    */
   public static String getParentPid() throws Exception {
      String pid = getPid();
      try {
         return runCmd("ps -p " + pid + " -o ppid=").get(0);
      } catch (Exception e) {
         log.warn("getParentPid error " + e.getMessage());
         throw e;
      }
   }

   /**
    * kills process associated with passed process id.
    * 
    * @param pid process id to kill
    * @throws Exception on error
    */
   public static void killPid(String pid) throws Exception {
      Util.runCmd("kill -9 " + pid);
   }

   /**
    * Return the process ids of a Java VM running a process which has ALL of the
    * passed parameters in its command line. For example, a call:
    * 
    * <pre>
    * {@code Util.getJVMPid("WSServer", "RFDServers");}
    * </pre>
    * 
    * would return a one member String[] containing "29419" if the server was
    * currently running a Java VM with this process and command line:
    * 
    * <pre>
    * {@code 29419 WSServer -p dev -a RFDServers}
    * </pre>
    * 
    * @param pars The strings to look for in the JVM command line. All must be
    * present for a match.
    * @return String[] with one element for each JVM found, containing the
    * process id of that JVM. If no JVMs are found, a zero length String[] is
    * returned.
    * @throws Exception on error, or if any pars contain single quotes.
    */
   public static List <String> getJVMPid(String... pars) throws Exception {
      List <String> pids = new ArrayList <>();
      String cmd = "jps -m";
      for (String par : pars) {
         if (par.contains("'"))
            throw new Exception("UTIL.getJVMPid, " + par + " contained quote");
         cmd += " | grep '" + par + "'";
      }
      List <String> lines = Util.runCmd(cmd);
      for (String line : lines) {
         StringTokenizer st = new StringTokenizer(line);
         if (st.hasMoreTokens()) pids.add(st.nextToken());
      }
      return pids;
   }

   /**
    * Returns a string of the form " className:lineNumber methodName " for the
    * first stack trace line from an "edu.wustl" class. If an error occurs or if
    * no "edu.wustl" class is found in the stack trace a string containing a
    * single space will be returned.
    * 
    * @param exception Exception to check.
    * @return String with error info.
    */
   public static String getErrorPoint(Exception exception) {
      String errorPoint = " ";
      if (exception == null) return errorPoint;
      StackTraceElement[] stackTrace = exception.getStackTrace();
      if (stackTrace == null) return errorPoint;
      for (StackTraceElement stackTraceElement : stackTrace) {
         String className = stackTraceElement.getClassName();
         if (className == null) continue;
         if (className.startsWith("edu.wustl") == false) continue;
         errorPoint += StringUtils.substringAfterLast(className, ".");
         Integer lineNumber = stackTraceElement.getLineNumber();
         if (lineNumber > 0) errorPoint += ":" + lineNumber;
         String methodName = stackTraceElement.getMethodName();
         if (methodName != null) errorPoint += " " + methodName;
         break;
      }
      errorPoint += " ";
      return errorPoint;
   } // EO getErrorPoint

   /**
    * Return string of form " errorMessage className:lineNumber methodName for
    * passed exception. see {@link #getErrorPoint} for details on the stack
    * trace portion of the messaged.
    * 
    * @param exception Exception to check.
    * @return String with error info.
    */
   public static String getEM(Exception exception) {
      return " " + exception.getMessage() + Util.getErrorPoint(exception);
   }
   
   /**
    * Logs the passed exception to the passed logs at warn level.
    * @param exception Exception to log
    * @param logs Loggers to log to; If none, SYSTEM log is used.
    */
   public static void logEM(Exception exception, Logger... logs) {
      if (logs.length == 0) logs = new Logger[] {Util.getLog()};
      String em = Util.getEM(exception);
      for (Logger l : logs) {
         l.warn(em);
      }
   }

   /**
    * Pause the current thread the passed number of seconds. Swallows
    * interrupts.
    * 
    * @param seconds int number of seconds to pause.
    * @param logger Logger to use, defaults to system log.
    * @param message log message (why the pause?) defaults to none.
    */
   public static void pause(int seconds, Logger logger, String message) {
      if (logger == null) logger = log;
      if (message == null) message = "";
      try {
         TimeUnit.SECONDS.sleep(seconds);
         logger.trace("Paused " + seconds + " seconds " + message);
      } catch (InterruptedException e) {
         logger.warn(Util.getEM(e));
         Thread.currentThread().interrupt();
      }
   }

   /**
    * from {@link java.util.List List} of {@link org.javatuples.LabelValue
    * LabelValues} return the value corresponding to the passed label.
    * @param label String label to match (case sensitive)
    * @param list List of LabelValues
    * @return Object value with matching label
    * @throws Exception if no match found.
    */
   public static Object getValueForLabel(String label,
      List <LabelValue <String, Object>> list) throws Exception {
      for (LabelValue <String, Object> item : list) {
         if (item.getLabel().equals(label)) return item.getValue();
      }
      throw new Exception ("No LabelValue pair matching " + label + " found");
   }
   
   /**
    * Return String[] with prefix added at beginning of each element
    * @param prefix to add
    * @param strings to prefix
    * @return same String[]
    */
   public static String[] prepend(String prefix, String[] strings) {
      for (int i = 0; i < strings.length; i++) 
         strings[i] = prefix + strings[i];
      return strings;
   }
   
   /**
    * Returns a "normalized" version of the input String, that is, all leading
    * and trailing whitespace is removed, and all interior whitespace sequences
    * are replaced with a single space. If the input string is null, an empty
    * string will be returned.
    * @param input raw string
    * @return "normalized" version of raw string.
    */
   public static String normalize(String input) {
      if (input == null) return "";
      return input.trim().replaceAll("\\s+", " ");
   }

} // EO Util class
