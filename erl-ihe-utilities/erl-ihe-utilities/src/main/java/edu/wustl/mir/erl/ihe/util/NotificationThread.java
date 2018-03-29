package edu.wustl.mir.erl.ihe.util;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Abstract class implements the {@link Runnable} interface, used to notify
 * listeners when a Runnable thread has completed.
 * <p>Usage:</p>
 * <ol>
 * <li>Write your Runnable class as an extension of this class.</li>
 * <li>Implement the {@link #doRun()} method as you would normally implement
 * the {@link Runnable#run()} method. </li>
 * <li>Instantiate your subclass, performing whatever tasks you need to do 
 * before starting the thread.</li>
 * <li>Add any listener objects you wish to have notified when the thread 
 * completes, using {@link #addListener(ThreadListener)}. These object must
 * implement the {@link ThreadListener} interface, and will have their 
 * {@link ThreadListener#threadComplete(Runnable)} method invoked when the 
 * thread completes.</li>
 * <li>Because Java 7 does not support static methods in an interface, there is
 * an {@link #addListenerClass(Class)} method to add classes with static 
 * threadComplete methods to the notification list. </li>
 * <li>When all listeners have been added, create a new Thread from the 
 * instance and run it, for example using:<pre>
 * Util.getExec().exec(myInstance);</pre></li>
 * </ol>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 */
public abstract class NotificationThread implements Runnable {
   /**
    * An abstract function that children must implement. This function is where
    * all work - typically placed in the run of runnable - should be placed.
    */
   public abstract void doRun();

   /**
    * Our list of listeners to be notified upon thread completion.
    */
   private java.util.List <ThreadListener> listeners = Collections
      .synchronizedList(new ArrayList <ThreadListener>());

   private java.util.List <Method> listenMethods = Collections
      .synchronizedList(new ArrayList <Method>());

   protected static Class <?> notifyingClass;

   /**
    * Adds a listener to this object.
    * 
    * @param listener Adds a new listener to this object.
    */
   public void addListener(ThreadListener listener) {
      listeners.add(listener);
   }

   /**
    * Adds a class to the list of those to be notified. The notification method
    * must be:
    * <ul>
    * <li>Named threadComplete.</li>
    * <li>Have one argument, the instance which is being listened to. The
    * argument must be a subclass of NotificationThread.</li>
    * <li>Be public, static, and void.</li>
    * </ul>
    * <b>Note:</b> Errors in the type of class passed are coding errors, and
    * will be treated as fatal.
    * 
    * @param cls Class to be notified by calling its static threadComplete
    * method.
    */
   public void addListenerClass(Class <?> cls) {
      String prefix = "NotificationThread.addListenerClass() error: ";
      try {
         prefix += "threadcomplete method ";
         Method[] mtds = cls.getMethods();
         for (Method mtd : mtds) {
            /*
             * Scan methods looking for correct method name.
             */
            if (!mtd.getName().equals("threadComplete")) continue;
            /*
             * There could be several methods with the correct name but with
             * different signatures, so if the signature doesn't match, we
             * continue to scan for another method.
             */
            Class <?>[] params = mtd.getParameterTypes();
            if (params.length != 1) continue;
            if (!NotificationThread.class.isAssignableFrom(params[0]))
               continue;
            /*
             * only one method in the class could have the correct signature, so
             * we don't loop any more; The remainder of the tests throw
             * exceptions.
             */
            if (!mtd.getReturnType().equals(Void.TYPE))
               throw new Exception("not void return type");
            int mods = mtd.getModifiers();
            if (!Modifier.isPublic(mods)) throw new Exception("not public");
            if (!Modifier.isStatic(mods)) throw new Exception("not static");
            listenMethods.add(mtd);
            return;
         }
         throw new Exception("with correct signature not found.");
      } catch (Exception e) {
         Util.exit(prefix + " " + e.getMessage());
      }
   }

   /**
    * Removes a particular listener from this object, or does nothing if the
    * listener is not registered.
    * 
    * @param listener The listener to remove.
    */
   public void removeListener(ThreadListener listener) {
      listeners.remove(listener);
   }

   /**
    * Notifies all listeners that the thread has completed.
    */
   private final void notifyListeners() {
      synchronized (listeners) {
         for (ThreadListener listener : listeners) {
            listener.threadComplete(this);
         }
         for (Method mtd : listenMethods) {
            try {
               mtd.invoke(null, this);
            } catch (Exception e) {
               Util.getLog()
                  .error(
                     "NotificationThread.notifyListeners error: "
                        + e.getMessage());
            }
         }
      }
   }

   /**
    * Implementation of the Runnable interface. This function first calls
    * doRun(), then notifies all listeners of completion.
    */
   @Override
   public void run() {
      doRun();
      notifyListeners();
   }
}
