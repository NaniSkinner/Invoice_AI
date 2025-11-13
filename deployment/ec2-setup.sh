#!/bin/bash
# Script to set up EC2 instance for InvoiceMe backend
# Run this script on your EC2 instance after launching it

set -e

echo "======================================"
echo "InvoiceMe - EC2 Instance Setup"
echo "======================================"
echo ""

# Update system
echo "Step 1: Updating system packages..."
sudo apt update && sudo apt upgrade -y

# Install Docker
echo ""
echo "Step 2: Installing Docker..."
sudo apt install -y docker.io docker-compose
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# Install AWS CLI
echo ""
echo "Step 3: Installing AWS CLI..."
sudo apt install -y awscli unzip
aws --version

# Install CloudWatch Agent (optional)
echo ""
echo "Step 4: Installing CloudWatch Agent..."
wget -q https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb
rm amazon-cloudwatch-agent.deb

# Install Nginx
echo ""
echo "Step 5: Installing Nginx..."
sudo apt install -y nginx

# Create application directory
echo ""
echo "Step 6: Creating application directory..."
mkdir -p /home/ubuntu/invoiceme/logs
cd /home/ubuntu/invoiceme

# Create docker-compose.yml
echo ""
echo "Step 7: Creating docker-compose.yml..."
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  backend:
    image: invoiceme-backend:latest
    container_name: invoiceme-backend
    ports:
      - "8080:8080"
    environment:
      - DATABASE_URL=${DATABASE_URL}
      - DATABASE_USERNAME=${DATABASE_USERNAME}
      - DATABASE_PASSWORD=${DATABASE_PASSWORD}
      - OPENAI_API_KEY=${OPENAI_API_KEY}
      - OPENAI_MODEL=${OPENAI_MODEL}
      - JWT_SECRET=${JWT_SECRET}
      - SERVER_PORT=8080
      - CORS_ORIGINS=${CORS_ORIGINS}
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
EOF

# Create environment loader script
echo ""
echo "Step 8: Creating environment loader script..."
cat > load-env.sh << 'EOF'
#!/bin/bash
# Load environment variables from AWS Parameter Store

AWS_REGION=${AWS_REGION:-us-east-1}

echo "Loading environment variables from Parameter Store..."

export DATABASE_URL=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_URL" --with-decryption --query "Parameter.Value" --output text --region $AWS_REGION)
export DATABASE_USERNAME=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_USERNAME" --with-decryption --query "Parameter.Value" --output text --region $AWS_REGION)
export DATABASE_PASSWORD=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_PASSWORD" --with-decryption --query "Parameter.Value" --output text --region $AWS_REGION)
export OPENAI_API_KEY=$(aws ssm get-parameter --name "/invoiceme/prod/OPENAI_API_KEY" --with-decryption --query "Parameter.Value" --output text --region $AWS_REGION)
export OPENAI_MODEL=$(aws ssm get-parameter --name "/invoiceme/prod/OPENAI_MODEL" --query "Parameter.Value" --output text --region $AWS_REGION)
export JWT_SECRET=$(aws ssm get-parameter --name "/invoiceme/prod/JWT_SECRET" --with-decryption --query "Parameter.Value" --output text --region $AWS_REGION)

# Set CORS_ORIGINS (update this after Vercel deployment)
export CORS_ORIGINS=${CORS_ORIGINS:-http://localhost:3000}

echo "Environment variables loaded successfully!"
EOF

chmod +x load-env.sh

# Create deployment script
echo ""
echo "Step 9: Creating deployment script..."
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "======================================"
echo "InvoiceMe - Backend Deployment"
echo "======================================"
echo ""

echo "Step 1: Loading environment variables from Parameter Store..."
source ./load-env.sh

echo ""
echo "Step 2: Stopping existing container..."
docker-compose down || true

echo ""
echo "Step 3: Starting new container..."
docker-compose up -d

echo ""
echo "Step 4: Waiting for application to start..."
sleep 30

echo ""
echo "Step 5: Checking application status..."
docker-compose ps
echo ""
docker-compose logs --tail=30

echo ""
echo "Step 6: Testing health endpoint..."
sleep 10
if curl -f http://localhost:8080/actuator/health; then
    echo ""
    echo "======================================"
    echo "SUCCESS! Backend is running."
    echo "======================================"
else
    echo ""
    echo "WARNING: Health check failed. Check logs:"
    echo "  docker-compose logs -f"
fi
EOF

chmod +x deploy.sh

# Configure Nginx
echo ""
echo "Step 10: Configuring Nginx reverse proxy..."
sudo tee /etc/nginx/sites-available/invoiceme > /dev/null << 'EOF'
server {
    listen 80;
    server_name _;

    # Increase timeouts for AI requests
    proxy_connect_timeout 300;
    proxy_send_timeout 300;
    proxy_read_timeout 300;

    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
        access_log off;
    }

    # Root path for health check
    location / {
        return 200 "InvoiceMe API is running. Use /api endpoint.\n";
        add_header Content-Type text/plain;
    }
}
EOF

sudo ln -sf /etc/nginx/sites-available/invoiceme /etc/nginx/sites-enabled/
sudo rm -f /etc/nginx/sites-enabled/default
sudo nginx -t
sudo systemctl restart nginx

# Create systemd service
echo ""
echo "Step 11: Creating systemd service..."
sudo tee /etc/systemd/system/invoiceme.service > /dev/null << 'EOF'
[Unit]
Description=InvoiceMe Backend Service
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/invoiceme
ExecStart=/home/ubuntu/invoiceme/deploy.sh
ExecStop=/usr/bin/docker-compose -f /home/ubuntu/invoiceme/docker-compose.yml down
User=ubuntu
Environment="AWS_REGION=us-east-1"

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable invoiceme

echo ""
echo "======================================"
echo "EC2 Setup Complete!"
echo "======================================"
echo ""
echo "Next steps:"
echo "1. Log out and log back in for docker group to take effect:"
echo "   exit"
echo ""
echo "2. Upload Docker image to EC2:"
echo "   scp -i your-key.pem invoiceme-backend.tar.gz ubuntu@[ELASTIC-IP]:/home/ubuntu/invoiceme/"
echo ""
echo "3. Load and deploy the image:"
echo "   docker load < invoiceme-backend.tar.gz"
echo "   ./deploy.sh"
echo ""
echo "4. Check status:"
echo "   docker-compose ps"
echo "   docker-compose logs -f"
echo "   curl http://localhost:8080/actuator/health"
echo ""
