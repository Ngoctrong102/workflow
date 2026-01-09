const express = require('express');
const cors = require('cors');
const bodyParser = require('body-parser');

const app = express();
const PORT = process.env.PORT || 3002;

// Middleware
app.use(cors());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// Request logging middleware
app.use((req, res, next) => {
  const timestamp = new Date().toISOString();
  console.log(`[${timestamp}] ${req.method} ${req.path}`);
  next();
});

/**
 * POST /webhook-test
 * Receive webhook requests and log them
 */
app.post('/webhook-test', (req, res) => {
  const timestamp = new Date().toISOString();
  
  console.log('=== Webhook Test Received ===');
  console.log(`Timestamp: ${timestamp}`);
  console.log('Headers:', JSON.stringify(req.headers, null, 2));
  console.log('Body:', JSON.stringify(req.body, null, 2));
  console.log('Query:', JSON.stringify(req.query, null, 2));
  console.log('==============================');
  
  res.status(200).json({
    status: 'received',
    timestamp: timestamp,
    message: 'Webhook received and logged successfully'
  });
});

/**
 * GET /webhook-test
 * Health check endpoint
 */
app.get('/webhook-test', (req, res) => {
  const timestamp = new Date().toISOString();
  
  res.status(200).json({
    status: 'ok',
    service: 'webhook-test',
    timestamp: timestamp
  });
});

/**
 * GET /webhook-test/health
 * Additional health check endpoint
 */
app.get('/webhook-test/health', (req, res) => {
  res.status(200).json({
    status: 'healthy',
    service: 'webhook-test',
    uptime: process.uptime(),
    timestamp: new Date().toISOString()
  });
});

/**
 * POST /webhook-test/:scenario
 * Support different test scenarios for workflow integration
 */
app.post('/webhook-test/:scenario', (req, res) => {
  const { scenario } = req.params;
  const timestamp = new Date().toISOString();
  
  console.log(`=== Webhook Test Scenario: ${scenario} ===`);
  console.log(`Timestamp: ${timestamp}`);
  console.log('Headers:', JSON.stringify(req.headers, null, 2));
  console.log('Body:', JSON.stringify(req.body, null, 2));
  console.log('==========================================');
  
  // Handle different scenarios
  let response = {
    status: 'received',
    scenario: scenario,
    timestamp: timestamp,
    message: `Webhook scenario '${scenario}' received and logged successfully`
  };
  
  // Add scenario-specific responses
  switch (scenario) {
    case 'success':
      response.data = { result: 'success', code: 200 };
      break;
    case 'error':
      response.data = { result: 'error', code: 500, message: 'Simulated error' };
      break;
    case 'delay':
      // Simulate delay
      setTimeout(() => {
        res.status(200).json(response);
      }, 2000);
      return;
    default:
      response.data = req.body;
  }
  
  res.status(200).json(response);
});

/**
 * GET /webhook-test/scenarios
 * List available test scenarios
 */
app.get('/webhook-test/scenarios', (req, res) => {
  res.status(200).json({
    scenarios: [
      {
        name: 'success',
        description: 'Returns success response',
        endpoint: 'POST /webhook-test/success'
      },
      {
        name: 'error',
        description: 'Returns error response',
        endpoint: 'POST /webhook-test/error'
      },
      {
        name: 'delay',
        description: 'Simulates delayed response (2 seconds)',
        endpoint: 'POST /webhook-test/delay'
      },
      {
        name: 'custom',
        description: 'Echo back request body',
        endpoint: 'POST /webhook-test/custom'
      }
    ]
  });
});

// Error handling middleware
app.use((err, req, res, next) => {
  console.error('Error:', err);
  res.status(500).json({
    status: 'error',
    message: err.message || 'Internal server error',
    timestamp: new Date().toISOString()
  });
});

// 404 handler
app.use((req, res) => {
  res.status(404).json({
    status: 'error',
    message: 'Endpoint not found',
    path: req.path,
    timestamp: new Date().toISOString()
  });
});

// Start server
app.listen(PORT, () => {
  console.log(`Webhook Test Service running on port ${PORT}`);
  console.log(`Health check: http://localhost:${PORT}/webhook-test/health`);
  console.log(`Available scenarios: http://localhost:${PORT}/webhook-test/scenarios`);
});

