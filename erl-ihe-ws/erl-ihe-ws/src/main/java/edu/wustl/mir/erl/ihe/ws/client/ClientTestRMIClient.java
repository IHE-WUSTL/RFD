/**
 * 
 */
package edu.wustl.mir.erl.ihe.ws.client;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

import org.apache.log4j.Logger;
import org.javatuples.LabelValue;

import edu.wustl.mir.erl.ihe.util.Util;
import edu.wustl.mir.erl.ihe.ws.WSProperties;

/**
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public class ClientTestRMIClient implements Serializable, WSProperties {
   private static final long serialVersionUID = 1L;

   private ClientTestRMIRemoteInterface stub = null;
   private Logger log = null;
   private String description = "Run Client Test RMI Interface";

   /**
    * @param port RMI registry port
    * @throws Exception on error
    */
   public ClientTestRMIClient(int port) throws Exception {
      Util.invoked();
      log = Logger.getLogger("rmi." + WSCLIENT_RMI_SHORT_NAME);
      description =
         "ClientTestRMIClient: port=" + port + " name "
            + WSCLIENT_RMI_SHORT_NAME + " ";
      try {
         Registry registry = LocateRegistry.getRegistry(port);
         stub = (ClientTestRMIRemoteInterface) registry
               .lookup(WSCLIENT_RMI_LONG_NAME);
         log.info(description + " connection established.");
      } catch (Exception e) {
         String em = description + " " + e.getClass().getName() + " " + Util.getEM(e);
         log.error(em);
         throw new Exception(em);
      }
   } // EO Constructor
   
   /**
    * Invokes runTest RMI method
    * @param pars List of LabelValue pairs 
    * @return List of LabelValue pairs 
    * @throws Exception on error
    */
   
   public List <LabelValue <String, Object>> runTest (
      List <LabelValue <String, Object>> pars) throws Exception {
      try {
         List <LabelValue <String, Object>> returnPairs = 
                  stub.runTest(pars);
         log.trace(description + " invoked runTest");
         return returnPairs;
      } catch (Exception e) { 
         log.error(description + "error invoking runTest " + Util.getEM(e));
         throw e;
      }
   }
} // EO ClientTestRMIClient class
