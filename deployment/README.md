# InvoiceMe Deployment Resources

This directory contains all scripts and documentation needed to deploy InvoiceMe to AWS and Vercel.

## Directory Structure

```
deployment/
├── README.md                      # This file
├── DEPLOYMENT_GUIDE.md           # Step-by-step deployment guide
├── ec2-setup.sh                  # Script to set up EC2 instance
├── build-and-upload.sh           # Script to build and deploy backend
└── scripts/
    └── setup-parameter-store.sh  # Script to configure AWS Parameter Store
```

## Quick Start

Follow the steps in [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for detailed instructions.

### TL;DR

1. **AWS Infrastructure** (via AWS Console):
   - Create VPC and subnets
   - Launch RDS PostgreSQL
   - Create IAM role for EC2
   - Launch EC2 instance with Elastic IP

2. **Store Secrets**:
   ```bash
   cd scripts
   ./setup-parameter-store.sh
   ```

3. **Set Up EC2**:
   ```bash
   scp -i your-key.pem ec2-setup.sh ubuntu@[ELASTIC-IP]:/home/ubuntu/
   ssh -i your-key.pem ubuntu@[ELASTIC-IP]
   ./ec2-setup.sh
   exit
   ```

4. **Deploy Backend**:
   ```bash
   ./build-and-upload.sh
   ```

5. **Deploy Frontend**:
   ```bash
   cd ../frontend
   vercel --prod
   ```

6. **Update CORS** and test!

## Files Description

### ec2-setup.sh
Automates EC2 instance configuration:
- Installs Docker, AWS CLI, Nginx
- Creates application directory
- Sets up docker-compose.yml
- Configures Nginx reverse proxy
- Creates systemd service for auto-restart

**Usage**:
```bash
# On EC2 instance
chmod +x ec2-setup.sh
./ec2-setup.sh
```

### build-and-upload.sh
Builds and deploys your backend:
- Builds Docker image locally
- Uploads to EC2
- Deploys application
- Runs health checks

**Usage**:
```bash
# From local machine
chmod +x build-and-upload.sh
./build-and-upload.sh
```

### scripts/setup-parameter-store.sh
Configures AWS Systems Manager Parameter Store with secrets:
- Database credentials
- OpenAI API key
- JWT secret

**Usage**:
```bash
cd scripts
chmod +x setup-parameter-store.sh
./setup-parameter-store.sh
```

## Prerequisites

- AWS Account with admin access
- AWS CLI configured (`aws configure`)
- Docker installed locally
- OpenAI API key
- SSH key pair for EC2
- Vercel account

## Architecture Overview

```
┌─────────────┐         ┌──────────────┐      ┌────────────┐
│   Vercel    │   →     │   AWS EC2    │  →   │  AWS RDS   │
│  (Frontend) │         │  (Backend)   │      │ PostgreSQL │
│  Next.js    │         │ Spring Boot  │      │            │
└─────────────┘         └──────────────┘      └────────────┘
                              ↓
                    ┌─────────────────────┐
                    │ Parameter Store     │
                    │ (Secrets)           │
                    └─────────────────────┘
```

## Support

See [DEPLOYMENT_GUIDE.md](DEPLOYMENT_GUIDE.md) for troubleshooting and detailed instructions.

## Estimated Deployment Time

- AWS infrastructure setup: ~1 hour
- Backend deployment: ~30 minutes
- Frontend deployment: ~15 minutes
- Testing and verification: ~30 minutes

**Total**: 2-3 hours
