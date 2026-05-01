package com.open.spring.mvc.slack;

import java.util.Map;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.open.spring.mvc.person.Person;
import com.open.spring.mvc.person.PersonJpaRepository;
import com.open.spring.mvc.person.Email.Email;

import io.github.cdimascio.dotenv.Dotenv;

@Service
public class EmailNotificationService {

    private static final Properties APPLICATION_PROPERTIES = loadApplicationProperties();

    @Autowired
    private PersonJpaRepository personRepository;

    private static Properties loadApplicationProperties() {
        Properties props = new Properties();
        try (var input = EmailNotificationService.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                props.load(input);
            }
        } catch (Exception e) {
            // fall back to env/system properties
        }
        return props;
    }

    private String resolveValue(String key, String applicationKey) {
        String value = System.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        value = System.getenv(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        try {
            Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
            value = dotenv.get(key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        } catch (Exception e) {
            // ignore and fall back to packaged properties
        }

        value = APPLICATION_PROPERTIES.getProperty(key);
        if (value != null && !value.isBlank()) {
            return value;
        }

        if (applicationKey != null && !applicationKey.isBlank()) {
            value = APPLICATION_PROPERTIES.getProperty(applicationKey);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }

        return null;
    }

    /**
     * Notify configured recipient(s) about a Slack message.
     * Priority: ENV FORWARD_EMAIL -> ENV FORWARD_PERSON_UID -> no-op
     */
    public void notifyOnSlackMessage(Map<String, String> messageData) {
        String forwardEmail = resolveValue("FORWARD_EMAIL", null);
        String forwardPersonUid = resolveValue("FORWARD_PERSON_UID", null);

        String recipient = null;
        if (forwardEmail != null && !forwardEmail.isBlank()) {
            recipient = forwardEmail;
        } else if (forwardPersonUid != null && !forwardPersonUid.isBlank()) {
            Person p = personRepository.findByUid(forwardPersonUid);
            if (p != null && p.getEmail() != null && !p.getEmail().isBlank()) {
                recipient = p.getEmail();
            }
        }

        if (recipient == null || recipient.isBlank()) {
            recipient = resolveValue("EMAIL_USERNAME", "spring.mail.username");
        }

        if (recipient == null) {
            // Nothing configured; don't create new systems or spam logs
            return;
        }

        // Build a concise subject and body
        String subject = "Open Coding Society: new Slack message";
        StringBuilder body = new StringBuilder();
        body.append("A new Slack event was received:\n\n");
        for (Map.Entry<String, String> e : messageData.entrySet()) {
            body.append(e.getKey()).append(": ").append(e.getValue()).append("\n");
        }

        // Use existing Email utility to send the notification
        try {
            Email.sendEmail(recipient, subject, body.toString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
