package com.shutterflow.infrastructure.mail;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.shutterflow.core.common.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final SendGrid sendGrid;

    @Value("${sendgrid.from-email}")
    private String fromEmail;

    /**
     * Dispatches transactional emails asynchronously via the SendGrid gateway.
     */
    @Async
    public void sendHtmlEmail(String toEmail, String subject, String htmlContent) {
        Email from = new Email(fromEmail);
        Email to = new Email(toEmail);
        Content content = new Content("text/html", htmlContent);
        Mail mail = new Mail(from, subject, to, content);

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("Successfully dispatched SendGrid transactional mail to: {}", toEmail);
            } else {
                log.error("SendGrid responded with error code: {}. Body: {}", response.getStatusCode(), response.getBody());
                throw new AppException("Transactional email gateway failure", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            log.error("Failed to connect to SendGrid mail servers to dispatch email. Error: {}", e.getMessage());
            throw new AppException("Failed to deliver outbound email notification", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Dispatches a magic link email to the user.
     */
    @Async
    public void sendMagicLinkEmail(String toEmail, String magicLink) {
        String subject = "Your ShutterFlow Magic Login Link";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Login to ShutterFlow</h2>
                <p>Click the secure link below to log in instantly. No password required.</p>
                <div style="margin: 30px 0;">
                    <a href="%s" style="background-color: #000000; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">Log in instantly</a>
                </div>
                <p style="color: #666; font-size: 14px;">This link will expire in 15 minutes and can only be used once.</p>
                <p style="color: #666; font-size: 14px;">If you didn't request this link, you can safely ignore this email.</p>
            </div>
            """.formatted(magicLink);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Dispatches a password reset email to the user.
     */
    @Async
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        String subject = "ShutterFlow Password Reset";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Reset your Password</h2>
                <p>We received a request to reset your ShutterFlow password.</p>
                <div style="margin: 30px 0;">
                    <a href="%s" style="background-color: #000000; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">Reset Password</a>
                </div>
                <p style="color: #666; font-size: 14px;">This link will expire in 15 minutes and can only be used once.</p>
                <p style="color: #666; font-size: 14px;">If you didn't request a password reset, you can safely ignore this email.</p>
            </div>
            """.formatted(resetLink);
        
        sendHtmlEmail(toEmail, subject, htmlContent);
    }

    /**
     * Dispatches a studio team invitation email.
     */
    @Async
    public void sendInvitationEmail(String toEmail, String inviteLink) {
        String subject = "You've been invited to join a ShutterFlow Studio!";
        String htmlContent = """
            <div style="font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto;">
                <h2>Join your Studio team on ShutterFlow</h2>
                <p>You have been invited to join your studio's team spaces on ShutterFlow as a Photographer.</p>
                <div style="margin: 30px 0;">
                    <a href="%s" style="background-color: #10b981; color: #ffffff; padding: 12px 24px; text-decoration: none; border-radius: 4px; font-weight: bold;">Accept & Join Team</a>
                </div>
                <p style="color: #666; font-size: 14px;">This invitation will expire in 48 hours.</p>
                <p style="color: #666; font-size: 14px;">If you didn't expect this, you can safely ignore this email.</p>
            </div>
            """.formatted(inviteLink);

        sendHtmlEmail(toEmail, subject, htmlContent);
    }
}
