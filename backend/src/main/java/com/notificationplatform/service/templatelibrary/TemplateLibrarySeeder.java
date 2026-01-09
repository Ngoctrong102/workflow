package com.notificationplatform.service.templatelibrary;

import com.notificationplatform.entity.Template;
import com.notificationplatform.repository.TemplateRepository;


import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
/**
 * Seeds pre-built templates into the template library
 */
@Slf4j
@Component
@Order(1)
public class TemplateLibrarySeeder implements CommandLineRunner {

    private final TemplateRepository templateRepository;

    public TemplateLibrarySeeder(TemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Override
    public void run(String... args) {
        // Only seed if no library templates exist
        long libraryTemplateCount = templateRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getIsLibrary()))
                .count();

        if (libraryTemplateCount > 0) {
            log.info("Library templates already exist, skipping seed");
            return;
        }

        log.info("Seeding pre-built templates...");

        List<Template> templates = createPreBuiltTemplates();
        templateRepository.saveAll(templates);

        log.info("Seeded {} pre-built templates", templates.size());
    }

    private List<Template> createPreBuiltTemplates() {
        List<Template> templates = new ArrayList<>();

        // Welcome Email Templates
        templates.add(createWelcomeEmailTemplate());
        templates.add(createWelcomeEmailWithDiscountTemplate());

        // Password Reset Templates
        templates.add(createPasswordResetEmailTemplate());
        templates.add(createPasswordResetSmsTemplate());

        // Order Confirmation Templates
        templates.add(createOrderConfirmationEmailTemplate());
        templates.add(createOrderShippedEmailTemplate());

        // Newsletter Templates
        templates.add(createNewsletterTemplate());
        templates.add(createPromotionalEmailTemplate());

        // Alert Templates
        templates.add(createSystemAlertEmailTemplate());
        templates.add(createSecurityAlertEmailTemplate());

        // Notification Templates
        templates.add(createPushNotificationTemplate());
        templates.add(createSmsNotificationTemplate());

        return templates;
    }

    private Template createWelcomeEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Welcome Email");
        template.setDescription("Welcome new users to your platform");
        template.setChannel("email");
        template.setCategory("onboarding");
        template.setLibraryCategory("Welcome Emails");
        template.setSubject("Welcome {{user.name}}!");
        template.setBody("""
                <html>
                <body>
                    <h1>Welcome {{user.name}}!</h1>
                    <p>Thank you for joining us. We're excited to have you on board.</p>
                    <p>Your account has been created with email: {{user.email}}</p>
                    <p>Get started by exploring our features.</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("welcome", "onboarding", "email"));
        return template;
    }

    private Template createWelcomeEmailWithDiscountTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Welcome Email with Discount");
        template.setDescription("Welcome email with special discount code");
        template.setChannel("email");
        template.setCategory("onboarding");
        template.setLibraryCategory("Welcome Emails");
        template.setSubject("Welcome {{user.name}}! Here's your {{discount.percent}}% off");
        template.setBody("""
                <html>
                <body>
                    <h1>Welcome {{user.name}}!</h1>
                    <p>Thank you for joining us. As a welcome gift, use code <strong>{{discount.code}}</strong> for {{discount.percent}}% off your first purchase.</p>
                    <p>This offer expires on {{discount.expiresAt}}.</p>
                    <p>Happy shopping!</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("welcome", "onboarding", "discount", "email"));
        return template;
    }

    private Template createPasswordResetEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Password Reset Email");
        template.setDescription("Password reset email with reset link");
        template.setChannel("email");
        template.setCategory("security");
        template.setLibraryCategory("Password Reset");
        template.setSubject("Reset Your Password");
        template.setBody("""
                <html>
                <body>
                    <h1>Password Reset Request</h1>
                    <p>Hello {{user.name}},</p>
                    <p>We received a request to reset your password. Click the link below to reset it:</p>
                    <p><a href="{{reset.url}}">Reset Password</a></p>
                    <p>This link will expire in {{reset.expiresIn}} minutes.</p>
                    <p>If you didn't request this, please ignore this email.</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("password", "reset", "security", "email"));
        return template;
    }

    private Template createPasswordResetSmsTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Password Reset SMS");
        template.setDescription("Password reset SMS with code");
        template.setChannel("sms");
        template.setCategory("security");
        template.setLibraryCategory("Password Reset");
        template.setBody("Your password reset code is {{reset.code}}. Valid for {{reset.expiresIn}} minutes. Do not share this code.");
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("password", "reset", "security", "sms"));
        return template;
    }

    private Template createOrderConfirmationEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Order Confirmation Email");
        template.setDescription("E-commerce order confirmation email");
        template.setChannel("email");
        template.setCategory("ecommerce");
        template.setLibraryCategory("Order Confirmation");
        template.setSubject("Order Confirmation #{{order.id}}");
        template.setBody("""
                <html>
                <body>
                    <h1>Order Confirmation</h1>
                    <p>Thank you for your order, {{user.name}}!</p>
                    <p><strong>Order #{{order.id}}</strong></p>
                    <p>Order Date: {{order.date}}</p>
                    <p>Total Amount: {{order.total}}</p>
                    <h2>Items:</h2>
                    <ul>
                        {{#each order.items}}
                        <li>{{name}} - {{quantity}} x {{price}}</li>
                        {{/each}}
                    </ul>
                    <p>We'll send you a shipping confirmation once your order ships.</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("order", "confirmation", "ecommerce", "email"));
        return template;
    }

    private Template createOrderShippedEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Order Shipped Email");
        template.setDescription("Order shipped notification with tracking");
        template.setChannel("email");
        template.setCategory("ecommerce");
        template.setLibraryCategory("Order Confirmation");
        template.setSubject("Your Order #{{order.id}} Has Shipped!");
        template.setBody("""
                <html>
                <body>
                    <h1>Your Order Has Shipped!</h1>
                    <p>Great news, {{user.name}}! Your order #{{order.id}} has been shipped.</p>
                    <p><strong>Tracking Number:</strong> {{shipment.trackingNumber}}</p>
                    <p><a href="{{shipment.trackingUrl}}">Track Your Package</a></p>
                    <p>Expected delivery: {{shipment.estimatedDelivery}}</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("order", "shipped", "tracking", "ecommerce", "email"));
        return template;
    }

    private Template createNewsletterTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Newsletter Template");
        template.setDescription("Monthly newsletter template");
        template.setChannel("email");
        template.setCategory("marketing");
        template.setLibraryCategory("Newsletter");
        template.setSubject("{{newsletter.title}} - {{newsletter.month}} {{newsletter.year}}");
        template.setBody("""
                <html>
                <body>
                    <h1>{{newsletter.title}}</h1>
                    <p>Hello {{user.name}},</p>
                    <p>{{newsletter.intro}}</p>
                    <h2>Featured Content</h2>
                    <p>{{newsletter.content}}</p>
                    <p><a href="{{newsletter.link}}">Read More</a></p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("newsletter", "marketing", "email"));
        return template;
    }

    private Template createPromotionalEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Promotional Email");
        template.setDescription("Promotional email with CTA");
        template.setChannel("email");
        template.setCategory("marketing");
        template.setLibraryCategory("Newsletter");
        template.setSubject("{{promo.title}} - Limited Time Offer!");
        template.setBody("""
                <html>
                <body>
                    <h1>{{promo.title}}</h1>
                    <p>Hello {{user.name}},</p>
                    <p>{{promo.description}}</p>
                    <p><strong>Special Offer:</strong> {{promo.offer}}</p>
                    <p><a href="{{promo.ctaUrl}}">{{promo.ctaText}}</a></p>
                    <p>Offer expires: {{promo.expiresAt}}</p>
                    <p>Best regards,<br>The Team</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("promotional", "marketing", "cta", "email"));
        return template;
    }

    private Template createSystemAlertEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("System Alert Email");
        template.setDescription("System alert notification email");
        template.setChannel("email");
        template.setCategory("alerts");
        template.setLibraryCategory("Alerts");
        template.setSubject("System Alert: {{alert.title}}");
        template.setBody("""
                <html>
                <body>
                    <h1>System Alert</h1>
                    <p><strong>{{alert.title}}</strong></p>
                    <p>{{alert.message}}</p>
                    <p>Severity: {{alert.severity}}</p>
                    <p>Time: {{alert.timestamp}}</p>
                    <p>Please review and take appropriate action.</p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("alert", "system", "notification", "email"));
        return template;
    }

    private Template createSecurityAlertEmailTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Security Alert Email");
        template.setDescription("Security alert notification email");
        template.setChannel("email");
        template.setCategory("security");
        template.setLibraryCategory("Alerts");
        template.setSubject("Security Alert: {{alert.type}}");
        template.setBody("""
                <html>
                <body>
                    <h1>Security Alert</h1>
                    <p>Hello {{user.name}},</p>
                    <p>We detected {{alert.type}} on your account.</p>
                    <p><strong>Details:</strong> {{alert.details}}</p>
                    <p>Time: {{alert.timestamp}}</p>
                    <p>If this wasn't you, please secure your account immediately.</p>
                    <p><a href="{{security.url}}">Secure Account</a></p>
                </body>
                </html>
                """);
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("security", "alert", "notification", "email"));
        return template;
    }

    private Template createPushNotificationTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("Push Notification");
        template.setDescription("Generic push notification template");
        template.setChannel("push");
        template.setCategory("notifications");
        template.setLibraryCategory("Alerts");
        template.setBody("{{notification.title}} - {{notification.message}}");
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("push", "notification", "mobile"));
        return template;
    }

    private Template createSmsNotificationTemplate() {
        Template template = new Template();
        template.setId(UUID.randomUUID().toString());
        template.setName("SMS Notification");
        template.setDescription("Generic SMS notification template");
        template.setChannel("sms");
        template.setCategory("notifications");
        template.setLibraryCategory("Alerts");
        template.setBody("{{notification.message}}. Reply STOP to unsubscribe.");
        template.setStatus("active");
        template.setVersion(1);
        template.setIsLibrary(true);
        template.setIsPublic(true);
        template.setInstallCount(0);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setTags(Arrays.asList("sms", "notification", "mobile"));
        return template;
    }
}

