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
}
