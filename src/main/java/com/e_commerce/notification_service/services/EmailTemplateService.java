package com.e_commerce.notification_service.services;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailTemplateService {

    @Autowired
    private TemplateEngine templateEngine;

    @Autowired
    private ResourceLoader resourceLoader;

    // List of available templates
    private static final List<String> AVAILABLE_TEMPLATES = Arrays.asList(
            "password-reset-email",
            "welcome-email",
            "account-locked-email",
            "email-verification-email");

    public EmailTemplateService(TemplateEngine templateEngine, ResourceLoader resourceLoader) {
        this.templateEngine = templateEngine;
        this.resourceLoader = resourceLoader;
    }

    /**
     * Check if template exists
     */
    public boolean templateExists(String templateName) {
        if (!AVAILABLE_TEMPLATES.contains(templateName)) {
            return false;
        }

        try {
            String templatePath = "classpath:templates/" + templateName + ".html";
            Resource resource = resourceLoader.getResource(templatePath);
            return resource.exists();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Get list of available templates
     */
    public List<String> getAvailableTemplates() {
        return AVAILABLE_TEMPLATES;
    }

    /**
     * Render template to string for preview
     */
    public String renderTemplatePreview(String templateName, Map<String, Object> variables) {
        if (!templateExists(templateName)) {
            throw new IllegalArgumentException("Template not found: " + templateName);
        }

        try {
            Context context = new Context();
            context.setVariable("currentYear", java.time.LocalDateTime.now().getYear());
            context.setVariable("appName", "Your App Name");
            context.setVariable("supportEmail", "support@yourapp.com");

            if (variables != null) {
                variables.forEach(context::setVariable);
            }

            return templateEngine.process(templateName, context);
        } catch (Exception e) {
            throw new RuntimeException("Failed to render template: " + e.getMessage(), e);
        }
    }

    /**
     * Validate template variables
     */
    public boolean validateTemplateVariables(String templateName, Map<String, Object> variables) {
        // Basic validation - in production, you might want more sophisticated
        // validation
        if (variables == null) {
            return true;
        }

        // Check for common required variables based on template
        switch (templateName) {
            case "password-reset-email":
                return variables.containsKey("userName") &&
                        variables.containsKey("resetUrl") &&
                        variables.containsKey("token");
            case "welcome-email":
                return variables.containsKey("userName");
            case "account-locked-email":
                return variables.containsKey("userName") &&
                        variables.containsKey("unlockUrl");
            case "email-verification-email":
                return variables.containsKey("userName") &&
                        variables.containsKey("verificationUrl");
            default:
                return true;
        }
    }

    /**
     * Get template variable requirements
     */
    public Map<String, String> getTemplateRequirements(String templateName) {
        return switch (templateName) {
            case "password-reset-email" -> Map.of(
                    "userName", "string",
                    "resetUrl", "string",
                    "token", "string");
            case "welcome-email" -> Map.of(
                    "userName", "string");
            case "account-locked-email" -> Map.of(
                    "userName", "string",
                    "unlockUrl", "string");
            case "email-verification-email" -> Map.of(
                    "userName", "string",
                    "verificationUrl", "string");
            default -> Map.of();
        };
    }
}