package edu.wustl.mir.erl.util.web;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import edu.wustl.mir.erl.ihe.util.Util;

/**
 * Simple class to handle an email session using the JavaMail API.
 * <h3>Example of Use</h3>
 * 1.Determine the properties of the email session.
 * @see <a href="http://www.oracle.com/technetwork/java/javamail/index.html">
 * JavaMail Overview</a>
 * @see <a href="http://www.oracle.com/technetwork/java/javamail-1-149769.pdf">
 * JavaMail API Design Specification (JSR 919) online PDF</a>
 * @see <a href="https://jcp.org/aboutJava/communityprocess/mrel/jsr919/index.html">
 * JavaMail API Design Specification (JSR 919) download PDF</a>
 * @see <a href="https://javamail.java.net/nonav/docs/api/">JavaMail javaDoc</a>
 * @see <a href="https://javamail.java.net/nonav/docs/api/javax/mail/internet/package-summary.html">
 * </a>
 * 
 */
public class Email implements Serializable {
   private static final long serialVersionUID = 1L;

   private static Logger log = Util.getLog();

   Properties emailConfig;
   Session session;

   /**
    * Default constructor initializes email session using configuration
    * parameters defined as attributes of the {@code <Email>} element of the
    * application XML configuration file, using the same names as defined in
    * the <a
    * href="https://javamail.java.net/docs/api/overview-summary.html">Javax
    * Mail</a> specifications.
    */
   public Email() {
      initialize(Util.getProperties().configurationAt("Email"));
   }

   /**
    * Default constructor initializes email session using configuration
    * parameters defined as attributes of the passed element of an XML
    * configuration file, using the same names as defined in
    * the <a
    * href="https://javamail.java.net/docs/api/overview-summary.html">Javax
    * Mail</a> specifications.
    * @param configuration passed configuration data
    */
   public Email(HierarchicalConfiguration configuration) {
      initialize(configuration);
   }
   
   /**
    * @param to recipient email
    * @param subject email subject line
    * @param body email text
    * @throws Exception on error
    */
   public void send(String to, String subject, String body)
           throws Exception {
      MimeMessage email = new MimeMessage(session);
      email.addRecipient(RecipientType.TO, new InternetAddress(to));
      email.setSubject(subject);
      email.setContent(body, "text/html");
      Transport.send(email);
   }

   /**
    * Sends without throwing any Exceptions
    * @param to recipient email
    * @param subject email subject line
    * @param body email text
    * @return true if exception occurred, otherwise false
    */
   public boolean sendSilent(String to, String subject, String body) {
      try {
         send(to, subject, body);
         return false;
      } catch (Exception e) {
         StringBuilder em = new StringBuilder("Error sending email:\n to ");
         em.append(to).append(" - ").append(e.getMessage());
         Util.getLog().warn(em.toString());
         return true;
      }
   }


   /**
    * @param emails recipient emails
    * @param subject email subject line
    * @param body email text
    * @param files for each mime body part
    * @throws Exception on error
    */
   public void sendToEmails(Collection<String> emails, String subject, String
           body, Collection<String> files) throws Exception {
      Message msg = new MimeMessage(session);
      for (String em : emails)
         msg.addRecipient(RecipientType.TO, new InternetAddress(em));
      msg.setSubject(subject);

      Multipart multipart = new MimeMultipart();

      BodyPart mbp = new MimeBodyPart();
      mbp.setText(body);
      multipart.addBodyPart(mbp);

      for (String f : files) {
         mbp = new MimeBodyPart();
         DataSource source = new FileDataSource(f);
         mbp.setDataHandler(new DataHandler(source));
         mbp.setFileName(StringUtils.substringAfterLast(f, "/"));
         multipart.addBodyPart(mbp);
      }
      msg.setContent(multipart);
      Transport.send(msg);
   }

   /**
    * @param emails recipient emails
    * @param subject email subject line
    * @param body email text
    * @param files for each mime body part
    * @return true if exception occurred, otherwise false
    */
   public boolean sendToEmailsSilent(Collection<String> emails, String subject, String body, Collection<String> files) {
      try {
         sendToEmails(emails, subject, body, files);
         return false;
      } catch (Exception e) {
         Util.getLog().warn("sendToEmailSilent: " + e.getMessage());
         return true;
      }
   }


   private void initialize(HierarchicalConfiguration email) {
      log.trace("Email.initialize(conf) called.");
      emailConfig = new Properties();
      List<ConfigurationNode> nodes = email.getRootNode().getAttributes();
      for (ConfigurationNode node: nodes) {
         String name = node.getName();
         String value = (String) node.getValue();
         if (name.startsWith("mail.")) {
            emailConfig.setProperty(name, (String) node.getValue());
            log.trace("Email setProperty(" + name + "=" + value + ")");
         }
      }
      session = Session.getDefaultInstance(emailConfig);
   }
   
   /**
    * Sets {@link org.apache.log4j.Logger log} to use for logging e-mails.
    * Default is the system log.
    * @param logger log to use.
    */
   public void setLog(Logger logger) {
      log = logger;
   }
} // EO Email class
