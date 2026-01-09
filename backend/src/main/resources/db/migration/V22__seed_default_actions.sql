-- Seed default actions into actions table
-- These actions are pre-registered and available for use in workflows

-- API Call Action
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'api-call-action-standard',
    'API Call Action',
    'api-call',
    NULL,
    'Make HTTP request to external API',
    '{
        "url": "",
        "method": "GET",
        "headers": {},
        "body": {},
        "timeout": 5000,
        "retry": {
            "maxAttempts": 3,
            "backoffStrategy": "exponential"
        },
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "api-call",
        "color": "#22c55e",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Publish Event Action (Kafka)
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'publish-event-action-kafka',
    'Publish Kafka Event',
    'publish-event',
    NULL,
    'Publish message to Kafka topic',
    '{
        "kafka": {
            "brokers": ["localhost:9092"],
            "topic": "",
            "key": null,
            "headers": {}
        },
        "message": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "publish-event",
        "color": "#22c55e",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Function Action
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'function-action-standard',
    'Function Action',
    'function',
    NULL,
    'Define simple calculation logic',
    '{
        "expression": "",
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "function",
        "color": "#22c55e",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Custom Actions
-- Send Email
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-email-action',
    'Send Email',
    'custom-action',
    'send-email',
    'Send email notification',
    '{
        "recipient": "",
        "subject": "",
        "body": "",
        "attachments": [],
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "email",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send SMS
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-sms-action',
    'Send SMS',
    'custom-action',
    'send-sms',
    'Send SMS notification',
    '{
        "recipient": "",
        "message": "",
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "sms",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send Push Notification
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-push-action',
    'Send Push Notification',
    'custom-action',
    'send-push',
    'Send push notification',
    '{
        "recipient": "",
        "title": "",
        "body": "",
        "data": {},
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "push",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send In-App Notification
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-in-app-action',
    'Send In-App Notification',
    'custom-action',
    'send-in-app',
    'Send in-app notification',
    '{
        "recipient": "",
        "title": "",
        "message": "",
        "type": "info",
        "actionUrl": null,
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "in-app",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send Slack Message
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-slack-action',
    'Send Slack Message',
    'custom-action',
    'send-slack',
    'Send Slack message',
    '{
        "channel": "",
        "message": "",
        "blocks": [],
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "slack",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send Discord Message
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-discord-action',
    'Send Discord Message',
    'custom-action',
    'send-discord',
    'Send Discord message',
    '{
        "channel": "",
        "message": "",
        "embeds": [],
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "discord",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send Teams Message
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-teams-action',
    'Send Teams Message',
    'custom-action',
    'send-teams',
    'Send Teams message',
    '{
        "webhookUrl": "",
        "message": "",
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "teams",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Send Webhook
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'send-webhook-action',
    'Send Webhook',
    'custom-action',
    'send-webhook',
    'Send webhook notification',
    '{
        "url": "",
        "method": "POST",
        "headers": {},
        "body": {},
        "authentication": {
            "type": "none"
        },
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "webhook",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

-- Wait for Events
INSERT INTO actions (id, name, type, action_type, description, config_template, metadata, version, enabled, created_at, updated_at)
VALUES (
    'wait-events-action',
    'Wait for Events',
    'custom-action',
    'wait-events',
    'Wait for multiple async events and aggregate results',
    '{
        "events": [],
        "aggregationStrategy": "all",
        "timeout": 30000,
        "inputSchema": {},
        "outputSchema": {}
    }'::jsonb,
    '{
        "icon": "wait-events",
        "color": "#3b82f6",
        "version": "1.0.0"
    }'::jsonb,
    '1.0.0',
    true,
    NOW(),
    NOW()
) ON CONFLICT (id) DO NOTHING;

