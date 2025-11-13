# Product Requirements Document: InvoiceMe Deployment to AWS & Vercel

**Version:** 1.0
**Date:** 2025-11-12
**Status:** Draft
**Owner:** Deployment Team

---

## Executive Summary

Deploy the InvoiceMe AI-Assisted Invoicing System to production with:
- **Backend:** Spring Boot API on AWS EC2
- **Frontend:** Next.js app on Vercel
- **Database:** PostgreSQL on AWS RDS
- **Environment:** Production-ready demo infrastructure

---

## 1. Project Overview

### 1.1 Current Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CURRENT (Local)                       │
├─────────────────────────────────────────────────────────────┤
│  Frontend (Next.js)  →  Backend (Spring Boot)  →  Database  │
│  localhost:3000         localhost:8080             :5432     │
└─────────────────────────────────────────────────────────────┘
```

### 1.2 Target Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                        TARGET (Production)                        │
├──────────────────────────────────────────────────────────────────┤
│                                                                   │
│   ┌─────────────┐         ┌──────────────┐      ┌────────────┐ │
│   │   Vercel    │   →     │   AWS EC2    │  →   │  AWS RDS   │ │
│   │  (Frontend) │         │  (Backend)   │      │ PostgreSQL │ │
│   │  Next.js    │         │ Spring Boot  │      │            │ │
│   └─────────────┘         └──────────────┘      └────────────┘ │
│   invoiceme.vercel.app    api.yourdomain.com    (private VPC) │
│                                                                   │
│   ┌─────────────────────────────────────────────────────────┐  │
│   │            AWS Systems Manager Parameter Store           │  │
│   │         (Secrets: OpenAI API, JWT, DB Credentials)       │  │
│   └─────────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
```

### 1.3 Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| Backend Runtime | Java (Eclipse Temurin) | 21 |
| Backend Framework | Spring Boot | 3.2.0 |
| Frontend Framework | Next.js | 14.0.3 |
| Frontend Runtime | Node.js (Bun) | Latest |
| Database | PostgreSQL | 15+ |
| Container | Docker | Latest |
| AI Service | OpenAI API | gpt-4o-mini |

---

## 2. Deployment Requirements

### 2.1 AWS Infrastructure (Backend + Database)

#### 2.1.1 AWS RDS PostgreSQL Database

**Instance Specifications:**
- **Type:** `db.t3.micro` (Free Tier eligible for 12 months)
- **Engine:** PostgreSQL 15.x
- **Storage:** 20 GB GP3 (General Purpose SSD)
- **Multi-AZ:** No (demo purposes, single AZ)
- **Public Access:** No (private within VPC)
- **Backup Retention:** 7 days
- **Auto Minor Version Upgrade:** Yes

**Configuration:**
```yaml
Database Name: invoiceme
Master Username: invoiceadmin
Master Password: <generated securely>
Port: 5432
Parameter Group: default.postgres15
VPC: invoiceme-vpc
Security Group: rds-sg (allow 5432 from EC2 only)
```

**Required Database Setup:**
- Run Flyway migrations automatically on first backend startup
- Initial schema includes:
  - Users, Customers, Invoices, Payments tables
  - Required indexes for performance
  - Default demo user credentials

#### 2.1.2 AWS EC2 Instance (Backend)

**Instance Specifications:**
- **Type:** `t2.micro` (Free Tier eligible, 1 vCPU, 1 GB RAM)
- **Alternative:** `t3.small` (2 vCPU, 2 GB RAM) for better performance
- **AMI:** Amazon Linux 2023 or Ubuntu 22.04 LTS
- **Storage:** 20 GB GP3 EBS volume
- **Region:** us-east-1 (or closest to your location)

**Required Software:**
- Docker & Docker Compose
- AWS CloudWatch Agent (for logs)
- Optional: Amazon SSM Agent (for secure SSH alternative)

**Security Group Rules:**
```yaml
Inbound:
  - Port 80 (HTTP): 0.0.0.0/0
  - Port 443 (HTTPS): 0.0.0.0/0
  - Port 22 (SSH): Your-IP/32 (restrict to your IP)
  - Port 8080 (App): Optional for direct access

Outbound:
  - All traffic: 0.0.0.0/0 (for package downloads, external APIs)
```

**Elastic IP:**
- Allocate and associate an Elastic IP for consistent DNS

#### 2.1.3 AWS Systems Manager Parameter Store

**Parameters to Store:**
```
/invoiceme/prod/DATABASE_URL          → jdbc:postgresql://[rds-endpoint]:5432/invoiceme
/invoiceme/prod/DATABASE_USERNAME     → invoiceadmin
/invoiceme/prod/DATABASE_PASSWORD     → [secure-password]
/invoiceme/prod/OPENAI_API_KEY        → sk-proj-...
/invoiceme/prod/OPENAI_MODEL          → gpt-4o-mini
/invoiceme/prod/JWT_SECRET            → [256-bit secure random string]
```

**Access:**
- EC2 instance must have IAM role with `ssm:GetParameter` permission
- Parameters encrypted with default AWS KMS key

### 2.2 Vercel Deployment (Frontend)

#### 2.2.1 Project Configuration

**Build Settings:**
```yaml
Framework Preset: Next.js
Build Command: bun run build
Output Directory: .next
Install Command: bun install
Node Version: 18.x
```

**Root Directory:**
- Set to `frontend/` (monorepo structure)

**Environment Variables:**
```
NEXT_PUBLIC_API_URL=https://[your-ec2-elastic-ip-or-domain]/api
```

#### 2.2.2 Deployment Configuration

- **Git Integration:** Connect to GitHub repository
- **Production Branch:** `main`
- **Auto-deploy on Push:** Enabled
- **Preview Deployments:** Enabled for PRs
- **Custom Domain:** (Optional) `invoiceme.yourdomain.com`

---

## 3. Implementation Plan

### 3.1 Phase 1: AWS Infrastructure Setup

#### Step 1.1: Create VPC and Networking
```bash
1. Create VPC: invoiceme-vpc (10.0.0.0/16)
2. Create Subnets:
   - Public: 10.0.1.0/24 (for EC2)
   - Private: 10.0.2.0/24, 10.0.3.0/24 (for RDS, multi-AZ)
3. Create Internet Gateway
4. Configure Route Tables
5. Create NAT Gateway (optional, for private subnet internet access)
```

#### Step 1.2: Launch RDS PostgreSQL
```bash
1. Navigate to AWS RDS Console
2. Create Database:
   - Engine: PostgreSQL 15.x
   - Template: Free tier
   - DB instance: db.t3.micro
   - DB name: invoiceme
   - Master username: invoiceadmin
   - Auto-generate password (save securely!)
3. Configure:
   - VPC: invoiceme-vpc
   - Subnet group: Create new with private subnets
   - Public access: No
   - Security group: Create 'rds-sg' (port 5432 from EC2 SG)
4. Wait for creation (~10 minutes)
5. Note endpoint: invoiceme.xxxxx.us-east-1.rds.amazonaws.com
```

#### Step 1.3: Store Secrets in Parameter Store
```bash
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_URL" \
  --value "jdbc:postgresql://[RDS-ENDPOINT]:5432/invoiceme" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_USERNAME" \
  --value "invoiceadmin" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_PASSWORD" \
  --value "[YOUR-RDS-PASSWORD]" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_API_KEY" \
  --value "[YOUR-OPENAI-KEY]" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/invoiceme/prod/JWT_SECRET" \
  --value "$(openssl rand -base64 48)" \
  --type "SecureString"

aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_MODEL" \
  --value "gpt-4o-mini" \
  --type "String"
```

#### Step 1.4: Create IAM Role for EC2
```json
{
  "RoleName": "invoiceme-ec2-role",
  "Policies": [
    {
      "PolicyName": "SSMParameterAccess",
      "PolicyDocument": {
        "Version": "2012-10-17",
        "Statement": [
          {
            "Effect": "Allow",
            "Action": [
              "ssm:GetParameter",
              "ssm:GetParameters",
              "ssm:GetParametersByPath"
            ],
            "Resource": "arn:aws:ssm:*:*:parameter/invoiceme/prod/*"
          },
          {
            "Effect": "Allow",
            "Action": [
              "kms:Decrypt"
            ],
            "Resource": "*"
          }
        ]
      }
    }
  ]
}
```

#### Step 1.5: Launch EC2 Instance
```bash
1. Navigate to EC2 Console
2. Launch Instance:
   - Name: invoiceme-backend
   - AMI: Ubuntu 22.04 LTS
   - Instance type: t2.micro (or t3.small)
   - Key pair: Create or select existing
   - VPC: invoiceme-vpc
   - Subnet: Public subnet
   - Auto-assign public IP: Yes
   - IAM role: invoiceme-ec2-role
   - Security group: Create 'backend-sg' with rules above
   - Storage: 20 GB GP3
3. Allocate Elastic IP and associate with instance
4. Note Elastic IP address
```

### 3.2 Phase 2: Backend Deployment

#### Step 2.1: Prepare EC2 Instance
```bash
# SSH into EC2
ssh -i your-key.pem ubuntu@[ELASTIC-IP]

# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
sudo apt install -y docker.io docker-compose
sudo systemctl enable docker
sudo systemctl start docker
sudo usermod -aG docker ubuntu

# Install AWS CLI
sudo apt install -y awscli

# Install CloudWatch Agent (optional)
wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
sudo dpkg -i amazon-cloudwatch-agent.deb

# Logout and login again for docker group
exit
```

#### Step 2.2: Create Deployment Script on EC2
```bash
# Create app directory
mkdir -p /home/ubuntu/invoiceme
cd /home/ubuntu/invoiceme

# Create docker-compose.yml
cat > docker-compose.yml << 'EOF'
version: '3.8'

services:
  backend:
    image: invoiceme-backend:latest
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
    restart: unless-stopped
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
EOF

# Create environment file loader
cat > load-env.sh << 'EOF'
#!/bin/bash
export DATABASE_URL=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_URL" --with-decryption --query "Parameter.Value" --output text --region us-east-1)
export DATABASE_USERNAME=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_USERNAME" --with-decryption --query "Parameter.Value" --output text --region us-east-1)
export DATABASE_PASSWORD=$(aws ssm get-parameter --name "/invoiceme/prod/DATABASE_PASSWORD" --with-decryption --query "Parameter.Value" --output text --region us-east-1)
export OPENAI_API_KEY=$(aws ssm get-parameter --name "/invoiceme/prod/OPENAI_API_KEY" --with-decryption --query "Parameter.Value" --output text --region us-east-1)
export OPENAI_MODEL=$(aws ssm get-parameter --name "/invoiceme/prod/OPENAI_MODEL" --query "Parameter.Value" --output text --region us-east-1)
export JWT_SECRET=$(aws ssm get-parameter --name "/invoiceme/prod/JWT_SECRET" --with-decryption --query "Parameter.Value" --output text --region us-east-1)
EOF

chmod +x load-env.sh

# Create deploy script
cat > deploy.sh << 'EOF'
#!/bin/bash
set -e

echo "Loading environment variables from Parameter Store..."
source ./load-env.sh

echo "Pulling latest Docker image..."
# For initial deployment, we'll build locally
# Later, you can pull from ECR or Docker Hub

echo "Stopping existing container..."
docker-compose down || true

echo "Starting new container..."
docker-compose up -d

echo "Waiting for health check..."
sleep 30

echo "Checking application status..."
docker-compose ps
docker-compose logs --tail=50

echo "Deployment complete!"
EOF

chmod +x deploy.sh
```

#### Step 2.3: Build and Deploy Backend
```bash
# Option A: Build Docker image locally on your machine and transfer

# On your local machine:
cd /Users/nanis/dev/Gauntlet/Invoice_AI
docker build -t invoiceme-backend:latest .
docker save invoiceme-backend:latest | gzip > invoiceme-backend.tar.gz
scp -i your-key.pem invoiceme-backend.tar.gz ubuntu@[ELASTIC-IP]:/home/ubuntu/invoiceme/

# On EC2:
cd /home/ubuntu/invoiceme
docker load < invoiceme-backend.tar.gz
./deploy.sh

# Option B: Build directly on EC2 (requires git clone)
git clone https://github.com/your-repo/Invoice_AI.git
cd Invoice_AI
docker build -t invoiceme-backend:latest .
cd /home/ubuntu/invoiceme
./deploy.sh
```

#### Step 2.4: Configure Nginx Reverse Proxy (Optional but Recommended)
```bash
# Install Nginx
sudo apt install -y nginx

# Configure Nginx
sudo tee /etc/nginx/sites-available/invoiceme << 'EOF'
server {
    listen 80;
    server_name [YOUR-ELASTIC-IP-OR-DOMAIN];

    location /api {
        proxy_pass http://localhost:8080/api;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /actuator/health {
        proxy_pass http://localhost:8080/actuator/health;
    }
}
EOF

# Enable site
sudo ln -s /etc/nginx/sites-available/invoiceme /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl restart nginx
```

#### Step 2.5: Set Up Systemd Service (Auto-restart)
```bash
sudo tee /etc/systemd/system/invoiceme.service << 'EOF'
[Unit]
Description=InvoiceMe Backend Service
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/home/ubuntu/invoiceme
ExecStart=/home/ubuntu/invoiceme/deploy.sh
ExecStop=/usr/bin/docker-compose down
User=ubuntu

[Install]
WantedBy=multi-user.target
EOF

sudo systemctl daemon-reload
sudo systemctl enable invoiceme
sudo systemctl start invoiceme
```

### 3.3 Phase 3: Frontend Deployment to Vercel

#### Step 3.1: Prepare Repository
```bash
# Ensure .gitignore includes:
cat >> .gitignore << 'EOF'
.env
.env.local
.env*.local
EOF

# Commit and push
git add .
git commit -m "Prepare for Vercel deployment"
git push origin main
```

#### Step 3.2: Deploy to Vercel via CLI
```bash
# Install Vercel CLI
npm i -g vercel

# Login
vercel login

# Deploy from frontend directory
cd frontend
vercel

# Follow prompts:
# - Set up and deploy: Yes
# - Which scope: Your account
# - Link to existing project: No
# - Project name: invoiceme-frontend
# - Directory: ./ (current directory)
# - Override settings: No

# Set environment variable
vercel env add NEXT_PUBLIC_API_URL production
# Enter: http://[YOUR-ELASTIC-IP]/api

# Deploy to production
vercel --prod
```

#### Step 3.3: Deploy via Vercel Dashboard (Alternative)
```
1. Go to https://vercel.com/dashboard
2. Click "Add New" → "Project"
3. Import from GitHub (connect repo if needed)
4. Select your repository: Invoice_AI
5. Configure:
   - Framework Preset: Next.js
   - Root Directory: frontend
   - Build Command: bun run build (or npm run build)
   - Output Directory: .next
   - Install Command: bun install (or npm install)
6. Add Environment Variables:
   - NEXT_PUBLIC_API_URL = http://[YOUR-ELASTIC-IP]/api
7. Click "Deploy"
8. Wait 2-3 minutes for deployment
9. Access your app at: https://invoiceme-frontend-xxx.vercel.app
```

### 3.4 Phase 4: Testing & Verification

#### Test Checklist:
```
□ Backend Health Check:
  curl http://[ELASTIC-IP]:8080/actuator/health
  Expected: {"status":"UP"}

□ Database Connection:
  - Check backend logs for successful Flyway migration
  - Verify tables created in RDS

□ API Endpoints:
  curl http://[ELASTIC-IP]/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"demo","password":"password"}'
  Expected: JWT token response

□ Frontend Access:
  - Open https://[vercel-url].vercel.app
  - Verify login page loads
  - Test login with demo/password
  - Verify API calls work (check browser network tab)

□ CORS Configuration:
  - Ensure backend allows requests from Vercel domain
  - Check browser console for CORS errors

□ AI Chat Feature:
  - Test chat functionality
  - Verify OpenAI API calls work

□ End-to-End Flow:
  - Create customer
  - Create invoice
  - Record payment
  - Check dashboard analytics
```

---

## 4. Configuration Files

### 4.1 Backend Configuration Updates

**Required Changes to [application.properties](backend/src/main/resources/application.properties):**

```properties
# CORS Configuration (add this)
cors.allowed-origins=${CORS_ORIGINS:https://your-vercel-domain.vercel.app}

# Actuator Health Endpoint (add this)
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=when-authorized
```

**Create [SecurityConfig.java](backend/src/main/java/com/invoiceme/config/SecurityConfig.java) CORS update:**

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();

    // Read from environment or default
    String allowedOrigins = environment.getProperty("cors.allowed-origins",
        "https://your-vercel-domain.vercel.app");
    configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
}
```

### 4.2 Frontend Configuration

**Update [next.config.js](frontend/next.config.js):**

```javascript
/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: true,
  output: 'standalone', // Optimized for Vercel

  // API rewrites (optional, for cleaner URLs)
  async rewrites() {
    return [
      {
        source: '/api/:path*',
        destination: `${process.env.NEXT_PUBLIC_API_URL}/:path*`,
      },
    ]
  },
}

module.exports = nextConfig
```

---

## 5. CI/CD Pipeline (Optional Enhancement)

### 5.1 GitHub Actions for Backend

**Create [.github/workflows/deploy-backend.yml](../.github/workflows/deploy-backend.yml):**

```yaml
name: Deploy Backend to EC2

on:
  push:
    branches: [main]
    paths:
      - 'backend/**'
      - 'Dockerfile'
      - '.github/workflows/deploy-backend.yml'

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Build Docker Image
        run: docker build -t invoiceme-backend:latest .

      - name: Save Docker Image
        run: docker save invoiceme-backend:latest | gzip > invoiceme-backend.tar.gz

      - name: Copy to EC2
        uses: appleboy/scp-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ubuntu
          key: ${{ secrets.EC2_SSH_KEY }}
          source: invoiceme-backend.tar.gz
          target: /home/ubuntu/invoiceme/

      - name: Deploy on EC2
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.EC2_HOST }}
          username: ubuntu
          key: ${{ secrets.EC2_SSH_KEY }}
          script: |
            cd /home/ubuntu/invoiceme
            docker load < invoiceme-backend.tar.gz
            ./deploy.sh
```

**Required GitHub Secrets:**
- `EC2_HOST`: Your Elastic IP
- `EC2_SSH_KEY`: Your private SSH key

### 5.2 Vercel Auto-Deploy

Vercel automatically deploys on push to `main` branch. No additional setup needed.

---

## 6. Monitoring & Maintenance

### 6.1 Application Monitoring

**CloudWatch Logs:**
```bash
# Configure CloudWatch agent on EC2
sudo tee /opt/aws/amazon-cloudwatch-agent/etc/config.json << 'EOF'
{
  "logs": {
    "logs_collected": {
      "files": {
        "collect_list": [
          {
            "file_path": "/home/ubuntu/invoiceme/logs/*.log",
            "log_group_name": "/aws/ec2/invoiceme/backend",
            "log_stream_name": "{instance_id}"
          }
        ]
      }
    }
  }
}
EOF

sudo /opt/aws/amazon-cloudwatch-agent/bin/amazon-cloudwatch-agent-ctl \
  -a fetch-config \
  -m ec2 \
  -s \
  -c file:/opt/aws/amazon-cloudwatch-agent/etc/config.json
```

**Vercel Monitoring:**
- Built-in analytics available in Vercel dashboard
- Real-time logs for each deployment
- Performance metrics and error tracking

### 6.2 Health Checks

**Backend Health Endpoint:**
```
http://[ELASTIC-IP]:8080/actuator/health
```

**Database Connection Check:**
```bash
# From EC2
psql -h [RDS-ENDPOINT] -U invoiceadmin -d invoiceme -c "SELECT version();"
```

### 6.3 Backup Strategy

**RDS Automated Backups:**
- Retention: 7 days
- Backup window: 03:00-04:00 UTC
- Manual snapshots before major changes

**Application Data:**
- Database dumps stored in S3 (optional)
```bash
pg_dump -h [RDS-ENDPOINT] -U invoiceadmin invoiceme > backup_$(date +%Y%m%d).sql
aws s3 cp backup_*.sql s3://invoiceme-backups/
```

---

## 7. Security Considerations

### 7.1 Security Checklist
```
□ RDS database not publicly accessible
□ Security groups follow principle of least privilege
□ SSH access restricted to specific IP
□ Secrets stored in Parameter Store (encrypted)
□ HTTPS enabled (SSL certificate via Let's Encrypt or AWS ACM)
□ JWT secrets are strong (256+ bits)
□ Database passwords are complex
□ No secrets in source code or environment files
□ CORS properly configured
□ Rate limiting enabled (optional)
□ SQL injection prevention (JPA/Hibernate parameterized queries)
□ XSS protection (React default escaping)
```

### 7.2 SSL/HTTPS Setup (Optional but Recommended)

**Using Let's Encrypt with Certbot:**
```bash
# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate (requires domain name)
sudo certbot --nginx -d api.yourdomain.com

# Auto-renewal
sudo systemctl enable certbot.timer
```

### 7.3 Environment Variables Security

**Never commit:**
- `.env` files
- SSH keys
- API keys
- Database credentials

**Update [.gitignore](../.gitignore):**
```
.env
.env.local
.env*.local
*.pem
*.key
```

---

## 8. Cost Estimation (Free Tier)

| Service | Configuration | Monthly Cost |
|---------|--------------|--------------|
| EC2 t2.micro | 750 hours/month (Free Tier) | $0 (first 12 months) |
| RDS db.t3.micro | 750 hours/month (Free Tier) | $0 (first 12 months) |
| EBS Storage | 20 GB | $0 (Free Tier includes 30 GB) |
| RDS Storage | 20 GB | $0 (Free Tier includes 20 GB) |
| Data Transfer | <15 GB/month | $0 (Free Tier includes 15 GB) |
| Elastic IP | Associated with running instance | $0 |
| Parameter Store | Standard params | $0 |
| Vercel | Hobby plan | $0 |
| **Total (Year 1)** | | **$0** |

**After Free Tier (12 months):**
- EC2 t2.micro: ~$8-10/month
- RDS db.t3.micro: ~$15-20/month
- Storage: ~$2-3/month
- **Total: ~$25-35/month**

**Cost Optimization Tips:**
- Stop EC2 and RDS when not in use (demo purposes)
- Use Reserved Instances for production
- Monitor CloudWatch for unusual usage

---

## 9. Troubleshooting Guide

### 9.1 Common Issues

**Issue: Backend won't start**
```bash
# Check logs
docker-compose logs -f backend

# Common causes:
# - Database connection failure
# - Missing environment variables
# - Port already in use
# - Flyway migration failure

# Solutions:
# - Verify RDS endpoint and credentials
# - Run: source ./load-env.sh && env | grep -E 'DATABASE|OPENAI|JWT'
# - Check: sudo netstat -tulpn | grep 8080
# - Manually run: docker exec -it [container] bash -c "psql -h [RDS] -U invoiceadmin"
```

**Issue: Frontend can't connect to backend**
```bash
# Check CORS settings
# Verify NEXT_PUBLIC_API_URL in Vercel dashboard
# Test API directly: curl http://[ELASTIC-IP]/api/health
# Check browser console for errors
```

**Issue: Database connection timeout**
```bash
# Verify security group allows EC2 → RDS on port 5432
# Check RDS is in same VPC as EC2
# Test connection: telnet [RDS-ENDPOINT] 5432
```

### 9.2 Rollback Procedure

**Backend Rollback:**
```bash
# Keep previous image tagged
docker tag invoiceme-backend:latest invoiceme-backend:previous
docker-compose down
docker tag invoiceme-backend:previous invoiceme-backend:latest
docker-compose up -d
```

**Frontend Rollback:**
```bash
# Via Vercel dashboard:
# 1. Go to Deployments
# 2. Find previous working deployment
# 3. Click "..." → "Promote to Production"
```

---

## 10. Success Criteria

### 10.1 Deployment Complete When:
- [ ] Backend running on EC2, accessible via HTTP
- [ ] Database hosted on RDS, migrations successful
- [ ] Frontend deployed to Vercel, loads without errors
- [ ] Frontend can communicate with backend API
- [ ] Demo user can login (demo/password)
- [ ] All CRUD operations work (Customers, Invoices, Payments)
- [ ] AI chat functionality operational
- [ ] Dashboard displays data correctly
- [ ] No CORS errors in browser console
- [ ] Health check endpoint returns 200 OK
- [ ] SSL/HTTPS configured (optional but recommended)

### 10.2 Performance Benchmarks:
- [ ] Page load time < 3 seconds
- [ ] API response time < 500ms (average)
- [ ] Database query time < 100ms (average)
- [ ] 99% uptime over 30 days

---

## 11. Post-Deployment Tasks

### 11.1 Immediate Tasks:
1. Share demo URL with stakeholders
2. Document credentials in secure location (1Password, etc.)
3. Set up monitoring alerts
4. Schedule first backup verification
5. Create runbook for common operations

### 11.2 Week 1 Tasks:
1. Monitor application logs for errors
2. Review CloudWatch metrics
3. Optimize database queries if needed
4. Set up custom domain (optional)
5. Enable HTTPS/SSL
6. Test backup and restore procedures

### 11.3 Month 1 Tasks:
1. Review AWS costs
2. Implement automated backups to S3
3. Set up CI/CD pipeline
4. Load testing
5. Security audit
6. Documentation review

---

## 12. Appendix

### 12.1 Useful Commands

**EC2 Management:**
```bash
# SSH to EC2
ssh -i your-key.pem ubuntu@[ELASTIC-IP]

# View application logs
docker-compose logs -f

# Restart application
docker-compose restart

# Check disk space
df -h

# Check memory usage
free -h

# Check running containers
docker ps
```

**RDS Management:**
```bash
# Connect to database
psql -h [RDS-ENDPOINT] -U invoiceadmin -d invoiceme

# Backup database
pg_dump -h [RDS-ENDPOINT] -U invoiceadmin invoiceme > backup.sql

# Restore database
psql -h [RDS-ENDPOINT] -U invoiceadmin invoiceme < backup.sql
```

**Parameter Store:**
```bash
# List parameters
aws ssm get-parameters-by-path --path "/invoiceme/prod" --recursive

# Update parameter
aws ssm put-parameter --name "/invoiceme/prod/OPENAI_API_KEY" \
  --value "new-key" --type "SecureString" --overwrite
```

### 12.2 Reference Links

- **AWS Documentation:**
  - [EC2 User Guide](https://docs.aws.amazon.com/ec2/)
  - [RDS User Guide](https://docs.aws.amazon.com/rds/)
  - [Systems Manager Parameter Store](https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html)

- **Vercel Documentation:**
  - [Deploy Next.js](https://vercel.com/docs/frameworks/nextjs)
  - [Environment Variables](https://vercel.com/docs/projects/environment-variables)

- **Spring Boot:**
  - [Deploying Spring Boot](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)
  - [Docker with Spring Boot](https://spring.io/guides/gs/spring-boot-docker/)

### 12.3 Contact & Support

- **AWS Support:** AWS Free Tier support via forums
- **Vercel Support:** Community support on Discord
- **Application Issues:** [Create GitHub issue]

---

## 13. Approval & Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Product Owner | | | |
| Tech Lead | | | |
| DevOps Engineer | | | |

---

**Document Version History:**

| Version | Date | Author | Changes |
|---------|------|--------|---------|
| 1.0 | 2025-11-12 | AI Assistant | Initial PRD creation |

---

## Next Steps

1. **Review this PRD** - Verify all requirements and configurations
2. **Approve AWS costs** - Confirm budget for post-free-tier usage
3. **Obtain credentials** - OpenAI API key, AWS account access
4. **Schedule deployment** - Pick a deployment window
5. **Execute Phase 1** - Set up AWS infrastructure
6. **Execute Phase 2** - Deploy backend
7. **Execute Phase 3** - Deploy frontend
8. **Execute Phase 4** - Test and verify

**Estimated Total Deployment Time:** 4-6 hours (with AWS Free Tier approval delays)
