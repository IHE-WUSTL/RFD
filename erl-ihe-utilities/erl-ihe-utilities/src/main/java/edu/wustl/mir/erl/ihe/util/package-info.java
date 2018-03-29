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
 * <p>Provides reusable static utility methods, general utility classes,
 * interfaces, annotations and properties used in ERL IHE software.
 * </p>
 * <h3>Util and Properties</h3>
 * <ul>
 * <li>{@link edu.wustl.mir.erl.ihe.util.Util Util} encapsulates a number of
 * static utility methods along with general information about the running
 * program using the singleton pattern. It provides a standardized foundation
 * for both command line and web applications, including:
 * <ul>
 * <li>Access to resources via a run directory.</li>
 * <li>Startup processing with parameter processing.</li>
 * <li>Profile support</li>
 * <li>Logging</li>
 * <li>Static utility methods not available in other libraries.</li>
 * </ul>
 * </li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.XmlUtil XmlUtil} static utility 
 * methods related to XML processing.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.UtilProperties UtilProperties} is an
 * interface containing a number of generally applicable constants.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.HTTPProperties HTTPProperties} is an
 * interface containing a number of generally applicable constants for HTTP
 * processing.</li>
 * </ul>
 * <h3>Log Appenders</h3>
 * <ul>
 * <li>{@link edu.wustl.mir.erl.ihe.util.RollingFileAppender
 * RollingFileAppender} is a modified version of
 * {@link org.apache.log4j.DailyRollingFileAppender DailyRollingFileAppender}
 * which adds options to allow rolled over log files to be compressed and to set
 * how long rolled over logs will be retained.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.FileAppender FileAppender} is a clone
 * of {@link org.apache.log4j.FileAppender log4j FileAppender}, moved to this
 * package to simplify the build of RollingFileAppender, which subclasses it.</li>
 * </ul>
 * <h3>Validation status support classes</h3> <p>Provide a foundation for
 * processing enum based status codes which combine a Phase code to indicate the
 * current validation phase along with a status code indicating the status
 * (valid or otherwise) of validation at that phase, for example, validation of
 * an http request message might reveal that the Method code on the request line
 * was not valid for the operation being requested, corresponding to a
 * HTTPStatues.REQUEST_INV_METHOD. </p>
 * The validation status support classes and interfaces are:
 * <ul>
 * <li>The {@link edu.wustl.mir.erl.ihe.util.StatusHelper StatusHelper} class,
 * which encapsulates all the Status enums in use in a particular application.
 * Each Status enum is registered with StatusHelper using the
 * {@link edu.wustl.mir.erl.ihe.util.StatusHelper#addCodeSet(String, Class)
 * addCodeSet} method, usually as part of a static initialization block in the
 * Status enum. StatusHelper then keeps track of all the code sets in use and
 * handles their storage and retrieval.</li>
 * <li>The interface {@link edu.wustl.mir.erl.ihe.util.Status Status},
 * implemented by all status enums, which allows them to be processed and stored
 * along with other status types.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.StatusUpdateType StatusUpdateType},
 * which indicates whether a new status for this phase should replace the
 * previous one or be added as an additional status.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.StatusType StatusType}, used to assign
 * types to Status enums, mostly for sorting purposes.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.Result Result}, used to categorize
 * status codes as to whether they indicate the corresponding test was passed.</li>
 * </ul>
 * <h3>Process notification support classes</h3> Provide support for monitoring
 * subprocesses for completion. Includes:
 * <ul>
 * <li>{@link edu.wustl.mir.erl.ihe.util.NotificationThread NotificationThread}
 * , used as a base class for classed to be monitored.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.ProcessNotificationThread 
 * ProcessNotificationThread} a further extension of NotificationThread used to
 * monitor subprocesses from completion.</li>
 * <li>{@link edu.wustl.mir.erl.ihe.util.ThreadListener ThreadListener}
 * interface implemented by classes which are to act as listeners.</li>
 * </ul>
 */
package edu.wustl.mir.erl.ihe.util;