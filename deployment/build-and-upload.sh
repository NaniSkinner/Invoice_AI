#!/bin/bash
# Script to build Docker image and upload to EC2
# Run this script from your local machine

set -e

echo "======================================"
echo "InvoiceMe - Build and Upload"
echo "======================================"
echo ""

# Get EC2 details
read -p "Enter EC2 Elastic IP or hostname: " EC2_HOST
if [ -z "$EC2_HOST" ]; then
    echo "Error: EC2 host is required"
    exit 1
fi

read -p "Enter path to SSH key (e.g., ~/.ssh/invoiceme-key.pem): " SSH_KEY
if [ -z "$SSH_KEY" ]; then
    echo "Error: SSH key path is required"
    exit 1
fi

if [ ! -f "$SSH_KEY" ]; then
    echo "Error: SSH key not found at $SSH_KEY"
    exit 1
fi

# Ensure correct permissions on SSH key
chmod 400 "$SSH_KEY"

echo ""
echo "Building Docker image..."
cd "$(dirname "$0")/.."
docker build -t invoiceme-backend:latest .

echo ""
echo "Saving Docker image to tar.gz..."
docker save invoiceme-backend:latest | gzip > invoiceme-backend.tar.gz

echo ""
echo "Image size: $(du -h invoiceme-backend.tar.gz | cut -f1)"
echo ""
echo "Uploading to EC2..."
scp -i "$SSH_KEY" invoiceme-backend.tar.gz ubuntu@$EC2_HOST:/home/ubuntu/invoiceme/

echo ""
echo "Deploying on EC2..."
ssh -i "$SSH_KEY" ubuntu@$EC2_HOST << 'ENDSSH'
cd /home/ubuntu/invoiceme
echo "Loading Docker image..."
docker load < invoiceme-backend.tar.gz
echo "Deploying application..."
./deploy.sh
ENDSSH

echo ""
echo "======================================"
echo "Deployment Complete!"
echo "======================================"
echo ""
echo "Your backend is now running at:"
echo "  http://$EC2_HOST/api"
echo ""
echo "Health check:"
echo "  curl http://$EC2_HOST/actuator/health"
echo ""
echo "View logs:"
echo "  ssh -i $SSH_KEY ubuntu@$EC2_HOST 'cd /home/ubuntu/invoiceme && docker-compose logs -f'"
echo ""
