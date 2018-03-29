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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.javatuples.Pair;

/**
 * <p>Singleton shutdown hook. It will destroy {@link Process processes} and
 * objects implementing {@link java.io.Closeable Closeable} passed to it. It
 * registers itself as a Shutdown Hook in its constructor, so all that is
 * to pass the Processes and Closeable objects.
 * </p>
 * <p>Processes and Closeable objects are terminated in the order they are passed
 * to the ShutDown instance.
 * </p>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public class ShutDown extends Thread implements Serializable {
   private static final long serialVersionUID = 1L;

   private static List <Pair <String, Object>> thingsToKill;
   private static Logger log;
   @SuppressWarnings("unused")
   private static ShutDown shutDown;
   
   static {
      shutDown = new ShutDown();
      thingsToKill = new ArrayList<>();
      log = Util.getLog();
   }

   /**
    * Creates ShutDown instance with a blank name and registers it with the
    * {@link Runtime} as a Shutdown Hook.
    */
   private ShutDown() {
      Runtime.getRuntime().addShutdownHook(this);
   }

   /**
    * Set {@link org.apache.log4j.Logger log} to be used by this instance of
    * ShutDown. Default is the {@link Util#getLog() system log}.
    * 
    * @param log Logger to use.
    */
   public static void setLog(Logger log) {
      ShutDown.log = log;
   }

   /**
    * Add a {@link Process} to be destroyed when the JVM is shutdown to the
    * list. Processes and Closeable objects will be shutdown in the order they
    * are added.
    * 
    * @param processDescription Description of process
    * @param process to be shut down.
    */
   public static void addProcess(String processDescription, Process process) {
      thingsToKill.add(new Pair <String, Object>(processDescription,
         process));
      log.info(processDescription + " process added to Shutdown list");
   }

   /**
    * Add an object implementing {@link java.io.Closeable Closeable} to be
    * closed by invoking its {@link java.io.Closeable#close() close} method when
    * the JVM is shutdown to the list. Processes and Closeable objects will be
    * shutdown in the order they are added.
    * 
    * @param closeableDescription Description of Thread
    * @param closeable to be shut down.
    */
   public static void addCloseable(String closeableDescription, Closeable closeable) {
      thingsToKill.add(new Pair <String, Object>(closeableDescription,
         closeable));
      log.info(closeableDescription + " thread added to Shutdown list");
   }

   @Override
   public void run() {
      System.err.println("Shutdown hook called.");
      for (Pair <String, Object> pair : thingsToKill) {
         String n = pair.getValue0();
         Object o = pair.getValue1();
         // --------------------------------------- Processes
         if (o instanceof Process) {
            Process process = (Process) o;
            process.destroy();
            System.err.println("Process " + n + " destroyed.");
         }
         // -------------------------------- Closeable objects
         if (o instanceof Closeable) {
            Closeable closeable = (Closeable) o;
            try {
               closeable.close();
               System.err.println("Closeable " + n + " closed.");
            } catch (IOException ioe) {
               System.err.println("IOException while closing " + n + " "
                  + ioe.getMessage());
            }
         }
      }
      System.err.println("Shutdown hook exiting.");
   } // EO run()
}
