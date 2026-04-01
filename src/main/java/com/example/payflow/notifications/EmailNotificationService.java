package com.example.payflow.notifications;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailNotificationService {

    private final JavaMailSender mailSender;

    @Value("${payflow.notifications.from:basitmohammed300@gmail.com}")
    private String fromAddress;

    @Async("taskExecutor")
    public CompletableFuture<Void> sendPaymentAuthorized(String to, UUID paymentId, BigDecimal amount, String currency) {
        return send(
                to,
                "Payment authorised - " + paymentId,
                """
                <html>
                    <body>
                        <h2>Payment authorised</h2>
                        <p>Your payment has been authorised successfully.</p>
                        <p><strong>Payment ID:</strong> %s</p>
                        <p><strong>Amount:</strong> %s %s</p>
                    </body>
                </html>
                """.formatted(paymentId, amount, currency)
        );
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> sendPaymentDeclined(String to, UUID paymentId, String reason) {
        return send(
                to,
                "Payment declined - " + paymentId,
                """
                <html>
                    <body>
                        <h2>Payment declined</h2>
                        <p>Your payment could not be authorised.</p>
                        <p><strong>Payment ID:</strong> %s</p>
                        <p><strong>Reason:</strong> %s</p>
                    </body>
                </html>
                """.formatted(paymentId, reason)
        );
    }

    private CompletableFuture<Void> send(String to, String subject, String body) {
        try {
            sendEmail(to, subject, body);
            log.info("Notification email sent to {}", to);
            return CompletableFuture.completedFuture(null);
        } catch (Exception e) {
            log.error("Failed to send notification email to {}: {}", to, e.getMessage(), e);
            return CompletableFuture.failedFuture(new CompletionException(e));
        }
    }

    private void sendEmail(String to, String subject, String body) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        helper.setText(body, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setFrom(fromAddress);
        mailSender.send(message);
    }
}
