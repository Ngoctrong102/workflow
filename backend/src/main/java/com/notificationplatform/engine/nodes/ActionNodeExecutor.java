package com.notificationplatform.engine.nodes;

import com.notificationplatform.dto.request.SendNotificationRequest;
import com.notificationplatform.dto.response.DeliveryResult;
import com.notificationplatform.engine.ExecutionContext;
import com.notificationplatform.engine.NodeExecutionResult;
import com.notificationplatform.engine.NodeExecutor;
import com.notificationplatform.service.notification.NotificationService;
import com.notificationplatform.service.channel.webhook.WebhookProvider;
import com.notificationplatform.service.template.TemplateRenderer;
import com.notificationplatform.entity.Channel;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
/**
 * Executor for action nodes (send notification)
 */
@Slf4j
@Component
public class ActionNodeExecutor implements NodeExecutor {

    private final NotificationService notificationService;
    private final WebhookProvider webhookProvider;
    private final TemplateRenderer templateRenderer;

    public ActionNodeExecutor(NotificationService notificationService,
                             WebhookProvider webhookProvider,
                             TemplateRenderer templateRenderer) {
        this.notificationService = notificationService;
        this.webhookProvider = webhookProvider;
        this.templateRenderer = templateRenderer;
    }

    @Override
    public NodeExecutionResult execute(String nodeId, Map<String, Object> nodeData, ExecutionContext context) {
        log.info("Executing action node: nodeId={}, subtype={}", nodeId, nodeData.get("subtype"));
        
        String subtype = (String) nodeData.getOrDefault("subtype", "send_email");
        Map<String, Object> output = new HashMap<>();
        
        try {
            if ("send_webhook".equals(subtype)) {
                // Send webhook action
                String url = (String) nodeData.get("url");
                String method = (String) nodeData.getOrDefault("method", "POST");
                
                Map<String, String> headers = (Map<String, String>) nodeData.get("headers");
                
                Object body = nodeData.get("body");
                
                // Get variables from context for template rendering
                // Trigger data is now accessed via _nodeOutputs.{triggerNodeId}
                Map<String, Object> variables = context.getDataForNode(nodeId);
                
                // Render body if it's a string with variables
                if (body instanceof String && variables != null && !variables.isEmpty()) {
                    body = templateRenderer.render((String) body, variables);
                }
                
                // Create a dummy channel for webhook (not actually used, just for logging)
                Channel dummyChannel = new Channel();
                dummyChannel.setType("webhook");
                
                // Send webhook
                DeliveryResult result = webhookProvider.send(dummyChannel, url, method, headers, body);
                
                output.put("action", subtype);
                output.put("status", result.isSuccess() ? "completed" : "failed");
                output.put("url", url);
                output.put("method", method);
                output.put("messageId", result.getMessageId());
                
                if (!result.isSuccess()) {
                    output.put("error", result.getError());
                }
                
                log.info("Webhook sent successfully: url={}, method={}", url, method);
            } else if (subtype.startsWith("send_")) {
                // Send notification action
                String channel = subtype.replace("send_", ""); // email, sms, push
                
                SendNotificationRequest request = buildNotificationRequest(nodeId, nodeData, context, channel);
                var notificationResponse = notificationService.sendNotification(request);
                
                output.put("action", subtype);
                output.put("status", "completed");
                output.put("notificationId", notificationResponse.getId());
                output.put("channel", channel);
                
                log.info("Notification sent successfully: notificationId={}", notificationResponse.getId());
            } else {
                log.warn("Unknown action subtype: {}", subtype);
                output.put("action", subtype);
                output.put("status", "skipped");
            }
            
            NodeExecutionResult result = new NodeExecutionResult(true, output);
            return result;
            
        } catch (Exception e) {
            log.error("Error executing action node: nodeId={}", nodeId, e);
            NodeExecutionResult result = new NodeExecutionResult(false, output);
            result.setError(e.getMessage());
            return result;
        }
    }

    private SendNotificationRequest buildNotificationRequest(String nodeId,
                                                             Map<String, Object> nodeData, 
                                                             ExecutionContext context, 
                                                             String channel) {
        SendNotificationRequest request = new SendNotificationRequest();
        request.setChannel(channel);
        
        // Get recipients from node data or context
        List<Map<String, Object>> recipientsConfig = (List<Map<String, Object>>) nodeData.get("recipients");
        
        List<SendNotificationRequest.Recipient> recipients = new ArrayList<>();
        if (recipientsConfig != null && !recipientsConfig.isEmpty()) {
            for (Map<String, Object> recipientConfig : recipientsConfig) {
                SendNotificationRequest.Recipient recipient = new SendNotificationRequest.Recipient();
                recipient.setEmail((String) recipientConfig.get("email"));
                recipient.setPhone((String) recipientConfig.get("phone"));
                recipient.setDeviceToken((String) recipientConfig.get("deviceToken"));
                recipient.setName((String) recipientConfig.get("name"));
                recipients.add(recipient);
            }
        } else {
            // Extract from context data
            recipients = extractRecipientsFromContext(context, channel);
        }
        request.setRecipients(recipients);
        
        // Get template ID or direct content
        String templateId = (String) nodeData.get("templateId");
        if (templateId != null && !templateId.isEmpty()) {
            request.setTemplateId(templateId);
        } else {
            // Use direct content
            request.setSubject((String) nodeData.get("subject"));
            request.setBody((String) nodeData.get("body"));
            request.setTitle((String) nodeData.get("title"));
        }
        
        // Get variables/data for template rendering
        Map<String, Object> data = context.getDataForNode(nodeId);
        request.setData(data);
        
        return request;
    }

    private List<SendNotificationRequest.Recipient> extractRecipientsFromContext(ExecutionContext context, String channel) {
        List<SendNotificationRequest.Recipient> recipients = new ArrayList<>();
        
        // Try to extract recipients from node outputs (trigger data is now in _nodeOutputs)
        // Check all node outputs for recipients (in case of multiple triggers)
        Map<String, Object> nodeOutputs = (Map<String, Object>) context.getNodeOutputs();
        
        // Try to find recipients in any trigger node output
        List<Map<String, Object>> recipientsList = null;
        for (Object nodeOutput : nodeOutputs.values()) {
            if (nodeOutput instanceof Map) {
                Map<String, Object> output = (Map<String, Object>) nodeOutput;
                if (output.containsKey("recipients")) {
                    List<Map<String, Object>> found = (List<Map<String, Object>>) output.get("recipients");
                    if (found != null && !found.isEmpty()) {
                        recipientsList = found;
                        break;
                    }
                }
            }
        }
        
        if (recipientsList != null) {
            for (Map<String, Object> recipientData : recipientsList) {
                SendNotificationRequest.Recipient recipient = new SendNotificationRequest.Recipient();
                recipient.setEmail((String) recipientData.get("email"));
                recipient.setPhone((String) recipientData.get("phone"));
                recipient.setDeviceToken((String) recipientData.get("deviceToken"));
                recipient.setName((String) recipientData.get("name"));
                recipients.add(recipient);
            }
        }
        
        return recipients;
    }

    @Override
    public com.notificationplatform.entity.enums.NodeType getNodeType() {
        return com.notificationplatform.entity.enums.NodeType.ACTION;
    }
}

