#!/bin/bash

# Start webhook test service
cd "$(dirname "$0")"

echo "Starting Webhook Test Service..."
echo "Port: ${PORT:-3002}"
echo ""

npm start

