package com.oconeco.hacking

import jakarta.mail.BodyPart
import jakarta.mail.Folder
import jakarta.mail.Message
import jakarta.mail.Multipart
import jakarta.mail.Part
import jakarta.mail.Session
import jakarta.mail.Store
import jakarta.mail.internet.MimeBodyPart
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.Logger


Logger log = LogManager.getLogger(this.class.name);
log.info "Starting script: ${this.class.name}..."

final String username = "seanoc5@gmail.com";
final String password = "mwbh dchm qwgz otxg ";

Properties props = new Properties();
props.put("mail.store.protocol", "imaps");
props.put("mail.imap.ssl.trust", "*");
props.put("mail.imap.ssl.enable", "true");

try {
    Session session = Session.getInstance(props, null);
    Store store = session.getStore();
    store.connect("imap.gmail.com", username, password);

    List<String> folders = ['INBOX']
    folders.each { String fldr ->
        log.info "processing FOLDER: $fldr..."
        Folder inbox = store.getFolder(fldr);
        inbox.open(Folder.READ_ONLY);

        Message[] messages = inbox.getMessages();
        log.info "\t\tMessage count: ${messages.size()}"

        int msgNo = 0
        for (Message message : messages) {
            msgNo++
            if (message.getContentType().toLowerCase().contains("multipart")) {
                Multipart multipart = (Multipart) message.getContent();
                for (int i = 0; i < multipart.getCount(); i++) {
                    BodyPart bodyPart = multipart.getBodyPart(i);
                    log.debug "$msgNo) \t\t\t\tbody part type:(${bodyPart.description})"
                    if (bodyPart instanceof MimeBodyPart) {
                        MimeBodyPart mimeBodyPart = (MimeBodyPart) bodyPart;
                        if (mimeBodyPart.getDisposition() != null) {
                            def disposition = mimeBodyPart.getDisposition()
                            if (disposition.equalsIgnoreCase(Part.ATTACHMENT)) {
                                // This is an attachment
                                String filename = mimeBodyPart.getFileName();
                                //mimeBodyPart.saveFile("/path/to/save/" + filename);
                                log.info("\t\t$msgNo) found attachment(need to index it or save it...): " + filename);
                            } else {
                                log.info "\t\t$msgNo) found non-attachment???: ${disposition}"
                            }
                        } else {
                            log.debug "\t\t$msgNo) MimeBodyPart) bodypart disposition was null: $mimeBodyPart"
                        }
                    } else {
                        log.info "\t\t$msgNo) multipart not instance of MimeBodyPart: $bodyPart"
                    }
                }
            } else {
                log.debug "\t\tnot multipart"
            }
        }
    }

    inbox.close(false);
    store.close();
} catch (Exception e) {
    e.printStackTrace();
}

log.info "Done...?"
