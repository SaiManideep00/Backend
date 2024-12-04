package com.messagingservice.deliveryservice.service;

import lombok.extern.slf4j.Slf4j;

import javax.mail.*;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Properties;
@Slf4j
public class EmailSender {
    private static final String EMAIL_USERNAME = "projectone.alerts@gmail.com";
    private static final String EMAIL_PASSWORD = "twpxwylorcsadzuw";

    public static void sendHtmlEmail(List<String> recipients, String subject, String[] values) throws MessagingException {
        // Load the HTML template from file
        String htmlBody = loadTemplate("MailTemplate.html");
        String[] placeholders = {"{eventName}", "{providerName}", "{criteria1}"};
        // Replace placeholders with values
        for (int i = 0; i < placeholders.length; i++) {
            htmlBody = htmlBody.replace(placeholders[i], values[i]);
        }

        // Send the email
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                log.info("Sent to password authentication");
                return new PasswordAuthentication(EMAIL_USERNAME, EMAIL_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(EMAIL_USERNAME));

            InternetAddress[] recipientAddresses = new InternetAddress[recipients.size()];
            for (int i = 0; i < recipients.size(); i++) {
                recipientAddresses[i] = new InternetAddress(recipients.get(i));
            }
            message.setRecipients(Message.RecipientType.TO, recipientAddresses);
            message.setSubject(subject);
            message.setContent(htmlBody, "text/html");

            Transport.send(message);

        } catch (AddressException e) {
            log.error("Address Exception");

            throw new RuntimeException(e);
        } catch (MessagingException e) {
            log.error("Message Exception");
            throw new RuntimeException(e);
        }
    }

    private static String loadTemplate(String templateFile) {
        try {
            InputStream inputStream = EmailSender.class.getClassLoader().getResourceAsStream("templates/" + templateFile);
            byte[] encoded = inputStream.readAllBytes();
            return new String(encoded, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}

