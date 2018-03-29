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
package edu.wustl.mir.erl.IHETools.RFD.view;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;
import org.primefaces.push.EventBusFactory;

import edu.wustl.mir.erl.ihe.util.ShutDown;
import edu.wustl.mir.erl.ihe.util.ThreadListener;
import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.util.Util.PfnType;
import edu.wustl.mir.erl.ihe.util.UtilProperties;
import edu.wustl.mir.erl.ihe.ws.db.WSLog;
import edu.wustl.mir.erl.ihe.ws.db.rmi.WSLogRMIReceiverInterface;
import edu.wustl.mir.erl.ihe.ws.db.rmi.WSLogRMIServer;
import edu.wustl.mir.erl.ihe.ws.server.WSServer;
import edu.wustl.mir.erl.util.web.WebUtil;

/**
 * Application scoped bean for RFD web application.
 */
@ManagedBean(eager = true)
@ApplicationScoped
public class ApplicationBean implements Serializable, WSLogRMIReceiverInterface, ThreadListener, UtilProperties {
	private static final long serialVersionUID = 1L;

	private static ApplicationBean instance = null;
	private static final String APPLICATION_NAME = "rfdWebapp";
	XMLConfiguration properties;

	/**
	 * Valid profiles for this application
	 */
	public enum Profiles {
		/**
		 * DEV, development
		 */
		DEV, /**
				 * PRODUCTION, normal production
				 */
		PRODUCTION
	}

	private static Logger log = null;
	private static Process formManagerProcess = null;
	private boolean formManagerRunning = true;
	private static List<WSLog> transactions = null;

	private static List<SessionBean> sessionBeans = new CopyOnWriteArrayList<>();

	/**
	 * Add passed SessionBean to the list. Sound be called on creation.
	 * 
	 * @param sb
	 *            SessionBean instance.
	 */
	public static void addSessionBean(SessionBean sb) {
		sessionBeans.add(sb);
	}

	/**
	 * Remove passed SessionBean from the list. Sound be called on destruction.
	 * 
	 * @param sb
	 *            SessionBean instance.
	 */
	public static void removeSessionBean(SessionBean sb) {
		sessionBeans.remove(sb);
	}

	/**
	 * Force page refresh on all current sessions.
	 */
	public static void refreshAllSessionBeans() {
		for (SessionBean sb : sessionBeans)
			sb.refreshScreen();
	}

	@Override
	public void receiveWSLogRMI(WSLog wsLog) {
      if (wsLog.getId() == null || wsLog.getId() <= 0) 
         wsLog.setId(WSLogRMIServer.getNextId());
		transactions.add(wsLog);
		EventBusFactory.getDefault().eventBus().publish("/wsLogReceiveEvent", "WSLog RMI msg received");
	}

	/**
	 * Constructor for the Application Bean. The Constructor here starts up both
	 * the user's server to run the web interface as well as a Java process of
	 * the WSServer.
	 */
	public ApplicationBean() {

		/*
		 * If dbgPrs is true, the subprocess will be run set up for remote
		 * debugging on socket 8000, and suspend until the debugger connects.
		 */
		boolean dbgPrs = false;

		try {
			WebUtil.initializeWebApp(APPLICATION_NAME);
			log = Util.getLog();
			properties = Util.getProperties();

			log.info("Initializing ApplicationBean");

			if (properties.getBoolean("RMI[@on]", false)) {
				transactions = new CopyOnWriteArrayList<>();
				Integer port = properties.getInteger("RMI[@port]", DEFAULT_RMI_REGISTRY_PORT);
				WSLogRMIServer.initialize(port);
				WSLogRMIServer.registerListener(this);
			}

			instance = this;

			List<Path> lp = Util.find(null, "**/erl-ihe-rfd/runDirectory", PfnType.DIRECTORY, false);
			if (lp.isEmpty())
				throw new Exception("runDirectory not found");
			if (lp.size() > 1)
				throw new Exception(lp.size() + " runDirectories found");
			Path rfdServerRunDirectory = lp.get(0);
			log.trace("rfdServerRunDirectory " + rfdServerRunDirectory);

			// Build command to exec.
			List<String> cmd = new ArrayList<>();
			cmd.add("java");
			/*
			 * The first entry in the classpath allows the subprocess to find
			 * its java classes in the web application library directory. The
			 * second entry allows the services to localize by pointing to the
			 * .wsdl file.
			 */
			cmd.add("-cp");
			// In Linux: "../../../lib/*:../"
			cmd.add(".." + fs + ".." + fs + ".." + fs + "lib" + fs + "*" + ps + ".." + fs);
			if (dbgPrs)
				cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,address=8000");
			cmd.add(WSServer.class.getCanonicalName());
			cmd.add("-p");
			cmd.add(Util.getProfile());
			cmd.add("-a");
			cmd.add("RFDServers");
			cmd.add("-rmi"); // Use RMI if set up in properties file.

			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(rfdServerRunDirectory.toFile());
			pb.inheritIO();

			// log command
			if (log.isInfoEnabled()) {
				List<String> c = pb.command();
				StringBuilder strb = new StringBuilder("Subprocess = ");
				for (String s : c) {
					strb.append(s).append(" ");
				}
				log.info(strb);
			}

			if (dbgPrs)
				log.warn("RFDServers subprocess will suspend for debug");
			else
				log.info("RFDServer sub process will run normally");

			formManagerProcess = pb.start();
			ShutDown.addProcess("RFDServers", formManagerProcess);

			log.info("ApplicationBean initialized");
		} catch (Exception e) {
			Util.exit("Could not instatiate ApplicationBean " + Util.getEM(e));
		}
	} // EO ApplicationBean constructor

	/******************************************************************/
	/********************** Getters And Setters ***********************/

	/******************************************************************/
	/**
	 * Gets application logger
	 * 
	 * @return Logger
	 */
	public static Logger getLog() {
		return log;
	}

	/**
	 * @return a short string indicating the status of the form manager process
	 *         launched by this web application.
	 *         <ul>
	 *         <li>Not found - process does not exist, it is null. Probably a
	 *         programming error.</li>
	 *         <li>Terminated - process has terminated. Most likely indicates a
	 *         problem with the form manager process.</li>
	 *         <li>Running</li>
	 *         </ul>
	 */
	public String getFormManagerStatus() {
		if (formManagerRunning)
			return "Running";
		return ("not running");
	}

	/**
	 * @return the ApplicationBean instance for this program.
	 */
	public static ApplicationBean getInstance() {
		return instance;
	}

	/**
	 * Returns a COPY of the {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog}
	 * transactions in applicationBean.
	 * 
	 * @return {@link java.util.List List} of transactions, set to exact size.
	 *         If there are no transactions, a zero length list will be
	 *         returned, never null.
	 */
	public List<WSLog> getTransactions() {
		List<WSLog> b = new ArrayList<>();
		for (WSLog w : transactions) {
			b.add(w);
		}
		return b;
	}

	/**
	 * Remove passed {@link edu.wustl.mir.erl.ihe.ws.db.WSLog WSLog} transaction
	 * from transaction list. <b>Note:</b> The passed WSLog is a copy of an
	 * instance in the transactions list. The comparison is made by comparing
	 * the {@link WSLog#getId() id} values of the two objects.
	 * 
	 * @param wsLog
	 *            WSLog to remove.
	 * @return boolean true if passed wsLog was found and removed, false
	 *         otherwise.
	 */
	public boolean removeTransaction(WSLog wsLog) {
		for (int i = 0; i < transactions.size(); i++) {
			WSLog w = transactions.get(i);
			if (w.getId() == wsLog.getId()) {
				transactions.remove(w);
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.wustl.mir.erl.ihe.util.ThreadListener#threadComplete(java.lang.
	 * Runnable )
	 */
	@Override
	public void threadComplete(Runnable runner) {
		int status = -1;
		if (formManagerProcess != null)
			status = formManagerProcess.exitValue();
		log.error("FormManagerSimulator Process terminated, status = " + status);
		formManagerRunning = false;
	}

}
