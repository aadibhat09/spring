package com.open.spring.mvc.person.Email;


// Java program to send email 
  
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;


//dot env for email username/password
import io.github.cdimascio.dotenv.Dotenv;
  
public class Email  
{ 

   private static final Properties APPLICATION_PROPERTIES = loadApplicationProperties();

   private static Properties loadApplicationProperties() {
      Properties props = new Properties();
      try (InputStream input = Email.class.getClassLoader().getResourceAsStream("application.properties")) {
         if (input != null) {
            props.load(input);
         }
      } catch (IOException e) {
         // Fall back to env/system properties if classpath properties cannot be loaded.
      }
      return props;
   }

   private static String resolveCredential(String key, String applicationKey) {
      String value = System.getProperty(key);
      if (value != null && !value.isBlank()) {
         return value;
      }

      value = System.getenv(key);
      if (value != null && !value.isBlank()) {
         return value;
      }

      try {
         final Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
         value = dotenv.get(key);
         if (value != null && !value.isBlank()) {
            return value;
         }
      } catch (Exception e) {
         // Ignore and fall back to packaged properties.
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
  
   public static void sendEmail(String recipient, String subject, Multipart multipart){
      // email ID of Recipient. 
  
      // email ID of  Sender. 
      String sender = resolveCredential("EMAIL_USERNAME", "spring.mail.username");
      String password = resolveCredential("EMAIL_PASSWORD", "spring.mail.password");
      String smtpHost = resolveCredential("EMAIL_SMTP_HOST", "spring.mail.host");
      String smtpPort = resolveCredential("EMAIL_SMTP_PORT", "spring.mail.port");

      if (sender == null || password == null) {
         throw new IllegalStateException("Email credentials are not configured. Set EMAIL_USERNAME and EMAIL_PASSWORD or spring.mail.username/password.");
      }
  
      // Getting system properties 
      Properties properties = System.getProperties(); 
  
      // Setting up mail server 
      properties.put("mail.smtp.auth", "true");
      properties.put("mail.smtp.starttls.enable", "true");
      properties.put("mail.smtp.host", smtpHost != null && !smtpHost.isBlank() ? smtpHost : "smtp.gmail.com");
      properties.put("mail.smtp.port", smtpPort != null && !smtpPort.isBlank() ? smtpPort : "587");
      properties.put("mail.smtp.ssl.protocols", "TLSv1.2");
  
      // creating session object to get properties 
      Session session = Session.getDefaultInstance(properties,new Authenticator() {
        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(sender,password); // email and password, see this for app passwords https://support.google.com/accounts/answer/185833?visit_id=638748419667916449-2613033234&p=InvalidSecondFactor&rd=1
        }
    }); 
  
      try 
      { 
         // MimeMessage object. 
         MimeMessage message = new MimeMessage(session); 
  
         // Set From Field: adding senders email to from field. 
         message.setFrom(new InternetAddress(sender)); 
  
         // Set To Field: adding recipient's email to from field. 
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient)); 
  
         // Set Subject: subject of the email 
         message.setSubject(subject); 
  
         // SetContent: content (Multipart) of the email
         message.setContent(multipart);

  
         // Send email. 
         Transport.send(message); 
         System.out.println("Mail successfully sent"); 
      } 
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendEmail(String recipient, String subject, String content){

      try{
         MimeMultipart emailContent = new MimeMultipart();
         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<p>"+content+"</p>","text/html");

         emailContent.addBodyPart(body1);

         sendEmail(recipient, subject, emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendPasswordResetEmail(String recipient,String code){

      try{
         MimeMultipart emailContent = new MimeMultipart();

         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<h1>To reset your password use the following code:</h1>","text/html");
         MimeBodyPart body2 = new MimeBodyPart();
         body2.setContent("<code style=\"background-color: lightblue; font-size: 50px; border-radius: 15px;\">"+code+"</code>","text/html");

         emailContent.addBodyPart(body1);
         emailContent.addBodyPart(body2);

         sendEmail(recipient, "Password Reset", emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }

   public static void sendVerificationEmail(String recipient,String code){

      try{
         MimeMultipart emailContent = new MimeMultipart();

         MimeBodyPart body1 = new MimeBodyPart();
         body1.setContent("<h1>Thank you for signing up for DNHS Computer Science. Use the following code to verify your email:</h1>","text/html");
         MimeBodyPart body2 = new MimeBodyPart();
         body2.setContent("<code style=\"background-color: lightblue; font-size: 50px; border-radius: 15px;\">"+code+"</code>","text/html");

         emailContent.addBodyPart(body1);
         emailContent.addBodyPart(body2);

         sendEmail(recipient, "Email Verification", emailContent);
      }
      catch (MessagingException mex)  
      { 
         mex.printStackTrace(); 
      } 
   }
} 
