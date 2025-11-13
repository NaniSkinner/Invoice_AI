# InvoiceMe Deployment Checklist

Use this checklist to track your deployment progress.

## Pre-Deployment Preparation

- [ ] AWS account created and verified
- [ ] AWS CLI installed (`aws --version`)
- [ ] AWS credentials configured (`aws configure`)
- [ ] OpenAI API key obtained
- [ ] Docker installed locally (`docker --version`)
- [ ] Vercel account created
- [ ] GitHub repository ready (if using Git integration)

---

## Phase 1: AWS Infrastructure (2 hours)

### VPC and Networking
- [ ] VPC created (`invoiceme-vpc` - 10.0.0.0/16)
- [ ] Public subnet created (10.0.1.0/24)
- [ ] Private subnet 1 created (10.0.2.0/24)
- [ ] Private subnet 2 created (10.0.3.0/24)
- [ ] Internet Gateway created and attached
- [ ] Route table updated for public subnet

### RDS PostgreSQL Database
- [ ] RDS instance launched (db.t3.micro, PostgreSQL 15)
- [ ] Database name: `invoiceme`
- [ ] Master username: `invoiceadmin`
- [ ] Master password saved securely
- [ ] DB subnet group created with private subnets
- [ ] RDS security group created (`invoiceme-rds-sg`)
- [ ] Public access: disabled
- [ ] RDS endpoint noted and saved
- [ ] Database creation completed (~10 min wait)

### IAM Role for EC2
- [ ] IAM policy created (`InvoiceMeParameterStoreAccess`)
- [ ] IAM role created (`invoiceme-ec2-role`)
- [ ] Policy attached to role
- [ ] Role allows SSM Parameter Store access

### EC2 Instance
- [ ] EC2 instance launched (t2.micro, Ubuntu 22.04)
- [ ] Instance name: `invoiceme-backend`
- [ ] Key pair created and downloaded (`invoiceme-key.pem`)
- [ ] VPC: `invoiceme-vpc` selected
- [ ] Public subnet selected
- [ ] Auto-assign public IP: enabled
- [ ] Security group created (`invoiceme-backend-sg`)
- [ ] Security group rules configured:
  - [ ] SSH (22) from My IP
  - [ ] HTTP (80) from Anywhere
  - [ ] HTTPS (443) from Anywhere
  - [ ] TCP (8080) from Anywhere (optional)
- [ ] IAM role attached (`invoiceme-ec2-role`)
- [ ] Elastic IP allocated
- [ ] Elastic IP associated with instance
- [ ] Elastic IP address saved: `___________________`

### Security Group Updates
- [ ] RDS security group updated to allow EC2 SG on port 5432
- [ ] Verified connectivity: EC2 → RDS

### Parameter Store
- [ ] Script executed: `./scripts/setup-parameter-store.sh`
- [ ] Parameters stored:
  - [ ] `/invoiceme/prod/DATABASE_URL`
  - [ ] `/invoiceme/prod/DATABASE_USERNAME`
  - [ ] `/invoiceme/prod/DATABASE_PASSWORD`
  - [ ] `/invoiceme/prod/OPENAI_API_KEY`
  - [ ] `/invoiceme/prod/OPENAI_MODEL`
  - [ ] `/invoiceme/prod/JWT_SECRET`
- [ ] Parameters verified with `aws ssm get-parameters-by-path`

---

## Phase 2: Backend Deployment (45 minutes)

### EC2 Setup
- [ ] SSH key permissions set: `chmod 400 invoiceme-key.pem`
- [ ] Uploaded setup script: `scp ec2-setup.sh ubuntu@[IP]:/home/ubuntu/`
- [ ] Connected to EC2: `ssh -i invoiceme-key.pem ubuntu@[IP]`
- [ ] Executed setup script: `./ec2-setup.sh`
- [ ] System packages updated
- [ ] Docker installed and running
- [ ] AWS CLI installed
- [ ] CloudWatch agent installed
- [ ] Nginx installed and configured
- [ ] Application directory created at `/home/ubuntu/invoiceme`
- [ ] Deployment scripts created (docker-compose.yml, load-env.sh, deploy.sh)
- [ ] Systemd service created
- [ ] Logged out and back in (docker group)

### Backend Build and Deploy
- [ ] Script executed: `./build-and-upload.sh`
- [ ] Docker image built successfully
- [ ] Image uploaded to EC2
- [ ] Image loaded on EC2
- [ ] Application deployed with `deploy.sh`
- [ ] Container running: `docker-compose ps`
- [ ] Logs checked: `docker-compose logs`

### Backend Verification
- [ ] Health check returns 200: `curl http://[IP]/actuator/health`
- [ ] API responds: `curl http://[IP]/api/auth/login -X POST -H "Content-Type: application/json" -d '{"username":"demo","password":"password"}'`
- [ ] JWT token received in response
- [ ] No database connection errors in logs
- [ ] Flyway migrations completed successfully
- [ ] Nginx reverse proxy working

---

## Phase 3: Frontend Deployment (30 minutes)

### Vercel Setup
- [ ] Vercel CLI installed: `npm install -g vercel`
- [ ] Logged in: `vercel login`

### Deployment
- [ ] Changed to frontend directory: `cd frontend`
- [ ] Initiated deployment: `vercel`
- [ ] Project created/linked
- [ ] Environment variable added: `NEXT_PUBLIC_API_URL`
- [ ] Environment variable value: `http://[YOUR-ELASTIC-IP]/api`
- [ ] Production deployment: `vercel --prod`
- [ ] Vercel URL noted: `_______________________________`

### CORS Configuration
- [ ] SSH'd back into EC2
- [ ] Updated CORS_ORIGINS in `/home/ubuntu/invoiceme/load-env.sh`
- [ ] Added Vercel URL to CORS origins
- [ ] Redeployed backend: `./deploy.sh`
- [ ] Verified no CORS errors in browser console

---

## Phase 4: Testing (30 minutes)

### Frontend Access
- [ ] Opened Vercel URL in browser
- [ ] Login page loads without errors
- [ ] No console errors (check browser DevTools)

### Authentication
- [ ] Logged in with demo/password
- [ ] Received JWT token
- [ ] Redirected to dashboard

### Core Functionality
- [ ] Dashboard displays correctly
- [ ] Analytics/charts render
- [ ] Navigation works

### Customer Management
- [ ] Create new customer
- [ ] View customer list
- [ ] Update customer
- [ ] Delete customer

### Invoice Management
- [ ] Create new invoice
- [ ] View invoice list
- [ ] Invoice details display
- [ ] Update invoice
- [ ] Cancel invoice

### Payment Management
- [ ] Record payment
- [ ] View payment history
- [ ] Payment status updates correctly

### AI Chat Feature
- [ ] Chat interface accessible
- [ ] Send message to AI
- [ ] Receive response from AI
- [ ] Chat history persists
- [ ] Natural language queries work

### Error Handling
- [ ] 404 pages display correctly
- [ ] API errors show appropriate messages
- [ ] Network errors handled gracefully

### Browser Compatibility
- [ ] Tested in Chrome
- [ ] Tested in Firefox/Safari (optional)
- [ ] Mobile responsive (optional)

---

## Post-Deployment (Optional)

### Custom Domain
- [ ] Domain purchased/configured
- [ ] DNS pointed to Elastic IP
- [ ] SSL certificate obtained (Let's Encrypt)
- [ ] HTTPS configured
- [ ] Vercel custom domain added
- [ ] Updated NEXT_PUBLIC_API_URL to HTTPS

### Monitoring
- [ ] CloudWatch logs configured
- [ ] AWS Budget alerts set
- [ ] Vercel analytics reviewed
- [ ] Uptime monitoring configured (optional)

### Backup and Recovery
- [ ] RDS automated backups verified
- [ ] Manual snapshot created
- [ ] Backup restoration tested
- [ ] Disaster recovery plan documented

### CI/CD (Optional)
- [ ] GitHub Actions workflow created
- [ ] GitHub secrets configured
- [ ] Automated deployment tested
- [ ] Rollback procedure tested

### Documentation
- [ ] API endpoints documented
- [ ] Deployment process documented
- [ ] Troubleshooting guide reviewed
- [ ] Team onboarding materials prepared

---

## Final Verification Checklist

### Performance
- [ ] Page load time < 3 seconds
- [ ] API response time < 500ms
- [ ] Database query time < 100ms
- [ ] No memory leaks (check over 24 hours)

### Security
- [ ] RDS not publicly accessible
- [ ] SSH restricted to specific IP
- [ ] Secrets not in source code
- [ ] CORS properly configured
- [ ] HTTPS enabled (if custom domain)
- [ ] Security group rules follow least privilege

### Cost Optimization
- [ ] Free Tier resources confirmed
- [ ] AWS Budget alert configured
- [ ] Unnecessary resources removed
- [ ] Cost estimate reviewed (~$0 year 1, ~$25-35/mo after)

### Operational Readiness
- [ ] Health check endpoint working
- [ ] Logs accessible and readable
- [ ] Restart procedure documented
- [ ] Update procedure documented
- [ ] Support contacts documented

---

## Sign-off

| Role | Name | Date | Signature |
|------|------|------|-----------|
| Developer | | | |
| DevOps | | | |
| Product Owner | | | |

---

## Important Information

**Save these details securely:**

- AWS Account ID: `___________________`
- AWS Region: `___________________`
- RDS Endpoint: `___________________`
- DB Username: `___________________`
- DB Password: `___________________` (use password manager!)
- EC2 Elastic IP: `___________________`
- EC2 Key Pair: `___________________` (path to .pem file)
- Vercel URL: `___________________`
- OpenAI API Key: `___________________`
- Custom Domain (if any): `___________________`

---

## Next Steps After Deployment

1. Share demo URL with stakeholders
2. Monitor logs for first 24 hours
3. Schedule regular backups verification
4. Plan for custom domain setup (optional)
5. Set up monitoring and alerts
6. Create runbook for common operations
7. Document any issues encountered

---

**Deployment Date**: ___________________
**Deployed By**: ___________________
**Status**: ⬜ In Progress | ⬜ Completed | ⬜ Issues Found

**Notes**:
_________________________________________________________________
_________________________________________________________________
_________________________________________________________________
