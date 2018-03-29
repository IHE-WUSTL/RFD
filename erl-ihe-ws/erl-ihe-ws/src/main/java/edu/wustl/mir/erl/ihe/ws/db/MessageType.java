package edu.wustl.mir.erl.ihe.ws.db;

import edu.wustl.mir.erl.ihe.ws.handlers.StoreSOAPMessages;

/**
 * Enum used to categorize {@link edu.wustl.mir.erl.ihe.ws.db.WSMsg
 * WSMsg}s for filtering, sorting and pretty printing.
 * 
 * @author Ralph Moulton / MIR WUSTL IHE Development Project 
 */
public enum MessageType {
   /**
    * Message type is not known, although all messages are unicode strings of
    * some time, that is, there are no binary messages.
    */
   UNKNOWN,
   /**
    * Message is text which does not qualify for any of the more specific
    * message types, for example {@link MessageType#XML XML}. An example of
    * this type of message would be a parsing results report.
    */
   TEXT,
   /**
    * HTTP 1.1 message
    */
   HTTP,
   /**
    * HTTP 1.1 Request message Body
    */
   HTTP_IN_BODY,
   /**
    * HTTP 1.1 Response message Body
    */
   HTTP_OUT_BODY,
   /**
    * XML 1.0 message
    */
   XML,
   /**
    * SOAP 1.2 request message.
    */
   SOAP_IN ("soap_request.bin"),
   /**
    * SOAP 1.2 requestForm prepopData contents
    */
   SOAP_PREPOP ("soap_prepop.bin"),
   /**
    * SOAP 1.2 response message.
    */
   SOAP_OUT ("soap_response.bin"),
   /**
    * WSDL web service message, without any binding wrappers, that is SOAP.
    */
   WSDL_MSG;
   
   /**
    * Default constructor, uses name for file name.
    */
   private MessageType() {
      messageFileName = this.name() + ".bin";
   }
   /**
    * Constructor with provided file name.
    * @param mfn file name to use for this message type.
    */
   private MessageType(String mfn) {
      messageFileName = mfn;
   }
   /**
    * File name used when storing messages of this type using the {@link 
    * StoreSOAPMessages} facility. For example, {@link #SOAP_IN} messages will
    * be stored with the name "soap_request.bin".
    */
   private String messageFileName;
   
   /**
    * @return message file name for this message type, using when storing
    * messages to files.
    */
   public String getMessageFileName() {
      return messageFileName;
   }
}
