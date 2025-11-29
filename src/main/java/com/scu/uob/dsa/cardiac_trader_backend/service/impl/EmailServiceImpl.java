package com.scu.uob.dsa.cardiac_trader_backend.service.impl;

import com.scu.uob.dsa.cardiac_trader_backend.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Email service implementation using Thymeleaf templates
 * For production, configure SMTP settings in application.properties
 * For development, logs emails to console
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.enabled:false}")
    private Boolean emailEnabled;

    @Value("${app.email.from:noreply@cardiactrader.com}")
    private String fromEmail;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(String to, String username, String verificationToken) {
        String subject = "Verify your Cardiac Trader account";
        String verificationLink = frontendUrl + "/verify-email?token=" + verificationToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("verificationLink", verificationLink);
        context.setVariable("frontendUrl", frontendUrl);

        sendHtmlEmail(to, subject, "email/verification-email", context);
    }

    @Override
    public void sendPasswordResetEmail(String to, String username, String resetToken) {
        String subject = "Reset your Cardiac Trader password";
        String resetLink = frontendUrl + "/reset-password?token=" + resetToken;

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("resetLink", resetLink);
        context.setVariable("frontendUrl", frontendUrl);

        sendHtmlEmail(to, subject, "email/password-reset-email", context);
    }

    @Override
    public void sendOtpEmail(String to, String username, String otp) {
        String subject = "Your Cardiac Trader OTP Code";

        Context context = new Context();
        context.setVariable("username", username);
        context.setVariable("otp", otp);
        context.setVariable("frontendUrl", frontendUrl);

        sendHtmlEmail(to, subject, "email/otp-email", context);
    }

    /**
     * Send HTML email using Thymeleaf template
     */
    private void sendHtmlEmail(String to, String subject, String templateName, Context context) {
        if (emailEnabled) {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(fromEmail != null ? fromEmail : "noreply@cardiactrader.com");
                helper.setTo(to);
                helper.setSubject(subject != null ? subject : "Cardiac Trader");

                // Render Thymeleaf template to HTML
                String htmlContent = templateEngine.process(templateName, context);
                if (htmlContent == null || htmlContent.isEmpty()) {
                    logger.error("Template rendered empty content for: {}", templateName);
                    return;
                }
                helper.setText(htmlContent, true); // true = HTML content

                mailSender.send(message);
                logger.info("Email sent successfully to: {}", to);
            } catch (MessagingException e) {
                logger.error("Failed to send email to: {}", to, e);
                // In development, log the email content
                logEmailContent(to, subject, templateName, context);
            }
        } else {
            // In development mode, log email content instead of sending
            logger.info("Email sending is disabled. Email content logged below:");
            logEmailContent(to, subject, templateName, context);
        }
    }

    /**
     * Log email content for development/debugging
     */
    private void logEmailContent(String to, String subject, String templateName, Context context) {
        try {
            String htmlContent = templateEngine.process(templateName, context);
            if (htmlContent == null) {
                htmlContent = "[Template rendered null]";
            }
            logger.info("==========================================");
            logger.info("EMAIL (NOT SENT - Development Mode)");
            logger.info("To: {}", to);
            logger.info("Subject: {}", subject);
            logger.info("Template: {}", templateName);
            logger.info("HTML Body:\n{}", htmlContent);
            logger.info("==========================================");
        } catch (Exception e) {
            logger.error("Failed to render email template: {}", templateName, e);
        }
    }
}
