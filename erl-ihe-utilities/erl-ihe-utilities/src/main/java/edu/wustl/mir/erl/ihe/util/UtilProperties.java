/*
 * Copyright (c) 2014  Washington University in St. Louis
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
package edu.wustl.mir.erl.ihe.util;

import java.nio.charset.Charset;
import java.text.SimpleDateFormat;

/**
 * Constants for erl-ihe-util and dependent projects.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public interface UtilProperties {
	/**
	 * The system newline character. Usually '\n'
	 */
	public static final String nl = System.getProperty("line.separator");

	/**
	 * The system file separator character, used to separate directories in a
	 * path. In Windows '\'; in Linux '/'.
	 */
	public static final String fs = System.getProperty("file.separator");
	/**
	 * The system path separator character, used to separate elements in a 
	 * list of file paths, for example in a PATH environment variable or in a
	 * java command line -cp parameter value. In Windows ";", in Linux ":".
	 */
	public static final String ps = System.getProperty("path.separator");
	/**
	 * The default {@link org.apache.log4j.Logger Logger} name. This is the name
	 * of the Logger which will be returned by {@link Util#getLog()}.
	 */
	public static final String DEFAULT_LOG_NAME = "system";
	/**
	 * The environment {@link org.apache.log4j.Logger Logger} name. The
	 * environment log is used to log:
	 * <ul>
	 * <li>Java Classpath entries on program start.</li>
	 * <li>System environment entries on program start.</li>
	 * <li>DB class properties on class load.</li>
	 * </ul>
	 */
	public static final String ENV_LOG_NAME = "environment";
	/**
	 * Canonical name string for UTF-8 character set.
	 */
	public static final String UTF_8 = "UTF-8";
	/**
	 * Canonical name of the default character set (UTF-8).
	 */
	public static final Charset CHAR_SET_UTF_8 = Charset.forName(UTF_8);

	/**
	 * SimpleDateFormat for RFC 3339 format, that is, yyyy-MM-dd'T'HH:mm:ss.SSSZ
	 */
	public static final SimpleDateFormat RFC3339_TIMESTAMP_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/**
	 * SimpleDateFormat for short form log: dd HH:mm:ss.SSSZ
	 */
	public static final SimpleDateFormat LOG_TIMESTAMP_FORMAT = new SimpleDateFormat("dd HH:mm:ss.SSS");

	/**
	 * Default port number for Java RMI Registry
	 */
	public static final Integer DEFAULT_RMI_REGISTRY_PORT = 1099;
	/**
	 * sort order, boolean true is ASCENDING
	 */
	public static final boolean ASCENDING = true;
	/**
	 * sort order, boolean true is DESCENDING
	 */
	public static final boolean DESCENDING = false;

} // EO Util Properties interface
