package hu.tilos.radio.backend.email;


import com.microtripit.mandrillapp.lutung.MandrillApi;
import com.microtripit.mandrillapp.lutung.view.MandrillMessage;
import com.microtripit.mandrillapp.lutung.view.MandrillMessageStatus;
import hu.tilos.radio.backend.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmailSender {

    private static Logger LOG = LoggerFactory.getLogger(EmailSender.class);

    @Inject
    @Configuration(name = "mandrill.key")
    private String key;

    @Inject
    @Configuration(name = "mail.mock")
    private String mock;

    public EmailSender() {
    }

    public void send(Email email) {
        try {
            MandrillApi api = new MandrillApi(key);
            MandrillMessage message = new MandrillMessage();
            message.setSubject(email.getSubject());
            message.setText(email.getBody());

            MandrillMessage.Recipient recipient = new MandrillMessage.Recipient();
            recipient.setEmail(email.getTo());

            List<MandrillMessage.Recipient> recipients = new ArrayList<>();
            recipients.add(recipient);
            message.setTo(recipients);

            message.setFromName("Tilos üzenőfal");
            message.setFromEmail("noreply@tilos.hu");

            Map<String, String> headers = new HashMap<>();
            headers.put("Reply-To", email.getFrom());

            message.setHeaders(headers);

            message.setPreserveRecipients(false);
            if (!mock.equals("true")) {
                LOG.debug("Sending message over mandrill to " + message.getTo());
                MandrillMessageStatus[] statuses = api.messages().send(message, false);
                for (MandrillMessageStatus status : statuses) {
                    if (!"sent".equals(status.getStatus())) {
                        throw new RuntimeException("Can't send the email: " + status.getRejectReason());
                    }
                    LOG.debug(status.getStatus());
                    LOG.debug(status.getRejectReason());
                }
            } else {
                LOG.debug("Ignoring mail send due to mail.mock parameter");
                LOG.debug("mail.to=" + message.getTo());
                LOG.debug("mail.body=" + email.getBody());

            }
        } catch (Exception e) {
            throw new RuntimeException("Can't send email", e);
        }

    }
}
