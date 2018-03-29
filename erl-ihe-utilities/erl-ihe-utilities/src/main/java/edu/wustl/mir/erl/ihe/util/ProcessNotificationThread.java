package edu.wustl.mir.erl.ihe.util;

/**
 * Class extends {@link NotificationThread} for the purpose of monitoring
 * a subprocess for completion.
 * <p>Usage:</p>
 * <ol>
 * <li>Create and start the subprocess you want to monitor, for example:<pre>
 * ProcessBuilder builder = new ProcessBuilder(cmdList);
 * Process process = builder.start();</pre></li>
 * <li>Create new ProcessNotificationThread, passing the running {@link Process} instance:<pre>
 * ProcessNotificationThread pnt = new ProcessNotificationThread(process);</pre></li>
 * <li>Add listener classes and instances. See {@link NotificationThread} for
 * details.</li>
 * <li>Run the ProcessNotificationThread instance in a separate thread, for
 * example:<pre>
 * Util.getExec().exec(pnt);</pre></li>
 * <li>The {@link ThreadListener#threadComplete(Runnable)} method in each
 * listener will be called when the process completes.</li>
 * </ol>
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project
 *
 */
public class ProcessNotificationThread extends NotificationThread {
   
   private Process process = null;
   
   /**
    * @param prs Process which is to be monitored for completion.
    */
   public ProcessNotificationThread(Process prs) {
      process = prs;
   }

   /* (non-Javadoc)
    * @see edu.wustl.mir.erl.ihe.util.NotificationThread#doWork()
    */
   @Override
   public void doRun() {
      try {
         process.waitFor();
      } catch (Exception e) {
         Util.getLog().warn("ProcessNotificationThread error " + e.getMessage());
      }

   }

}
