# InvoiceMe - Quick Deployment Guide

This guide will walk you through deploying InvoiceMe to AWS EC2 (backend) and Vercel (frontend).

## Prerequisites

- [ ] AWS Account with admin access
- [ ] AWS CLI installed and configured (`aws configure`)
- [ ] OpenAI API key
- [ ] SSH key pair for EC2 access
- [ ] Docker installed locally
- [ ] Vercel account (free tier)

---

## Phase 1: AWS Infrastructure Setup

### Step 1: Create VPC and Networking (AWS Console)

1. Go to **VPC Console** → **Create VPC**
   - Name: `invoiceme-vpc`
   - IPv4 CIDR: `10.0.0.0/16`
   - Click **Create VPC**

2. Create **Subnets**:
   - **Public Subnet** (for EC2):
     - Name: `invoiceme-public-subnet`
     - VPC: `invoiceme-vpc`
     - CIDR: `10.0.1.0/24`
     - Availability Zone: `us-east-1a`

   - **Private Subnet 1** (for RDS):
     - Name: `invoiceme-private-subnet-1`
     - VPC: `invoiceme-vpc`
     - CIDR: `10.0.2.0/24`
     - Availability Zone: `us-east-1a`

   - **Private Subnet 2** (for RDS Multi-AZ):
     - Name: `invoiceme-private-subnet-2`
     - VPC: `invoiceme-vpc`
     - CIDR: `10.0.3.0/24`
     - Availability Zone: `us-east-1b`

3. Create **Internet Gateway**:
   - Name: `invoiceme-igw`
   - Attach to `invoiceme-vpc`

4. Update **Route Tables**:
   - Find the route table for public subnet
   - Add route: `0.0.0.0/0` → Internet Gateway

### Step 2: Launch RDS PostgreSQL Database

1. Go to **RDS Console** → **Create database**
   - Engine: **PostgreSQL 15.x**
   - Template: **Free tier**
   - DB instance: `db.t3.micro`

2. **Settings**:
   - DB instance identifier: `invoiceme-db`
   - Master username: `invoiceadmin`
   - Master password: (auto-generate and **save it!**)

3. **Connectivity**:
   - VPC: `invoiceme-vpc`
   - Create new DB subnet group: `invoiceme-db-subnet-group`
   - Subnets: Select both private subnets
   - Public access: **No**
   - Create new VPC security group: `invoiceme-rds-sg`

4. **Additional Configuration**:
   - Initial database name: `invoiceme`
   - Enable automated backups: Yes (7 days retention)

5. Click **Create database** (takes ~10 minutes)

6. **Save the RDS endpoint**:
   - Once created, note the endpoint: `invoiceme-db.xxxxx.us-east-1.rds.amazonaws.com`

### Step 3: Create IAM Role for EC2

1. Go to **IAM Console** → **Roles** → **Create role**
   - Trusted entity: **AWS service**
   - Service: **EC2**
   - Click **Next**

2. **Add permissions**:
   - Click **Create policy** (opens new tab)
   - Use JSON editor:

   ```json
   {
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
         "Action": ["kms:Decrypt"],
         "Resource": "*"
       }
     ]
   }
   ```

   - Name: `InvoiceMeParameterStoreAccess`
   - Click **Create policy**

3. Back to role creation:
   - Refresh and search for `InvoiceMeParameterStoreAccess`
   - Select it and click **Next**
   - Role name: `invoiceme-ec2-role`
   - Click **Create role**

### Step 4: Launch EC2 Instance

1. Go to **EC2 Console** → **Launch instance**

2. **Name and tags**: `invoiceme-backend`

3. **Application and OS Images**:
   - AMI: **Ubuntu Server 22.04 LTS**

4. **Instance type**: `t2.micro` (Free tier)

5. **Key pair**:
   - Create new key pair: `invoiceme-key`
   - Type: RSA
   - Format: `.pem`
   - **Download and save securely!**

6. **Network settings**:
   - VPC: `invoiceme-vpc`
   - Subnet: `invoiceme-public-subnet`
   - Auto-assign public IP: **Enable**
   - Create new security group: `invoiceme-backend-sg`
   - Add rules:
     - SSH (22): My IP
     - HTTP (80): Anywhere (0.0.0.0/0)
     - HTTPS (443): Anywhere (0.0.0.0/0)
     - Custom TCP (8080): Anywhere (for testing)

7. **Configure storage**: 20 GB GP3

8. **Advanced details**:
   - IAM instance profile: `invoiceme-ec2-role`

9. Click **Launch instance**

10. **Allocate Elastic IP**:
    - EC2 Console → Elastic IPs → **Allocate**
    - Select the new IP → Actions → **Associate Elastic IP address**
    - Instance: `invoiceme-backend`
    - **Note this Elastic IP address!**

### Step 5: Update RDS Security Group

1. Go to **EC2 Console** → **Security Groups**
2. Find `invoiceme-rds-sg`
3. Edit **Inbound rules**:
   - Add rule:
     - Type: PostgreSQL (5432)
     - Source: Custom → Select `invoiceme-backend-sg`
   - Save rules

### Step 6: Store Secrets in Parameter Store

From your local machine, run:

```bash
cd deployment/scripts
chmod +x setup-parameter-store.sh
./setup-parameter-store.sh
```

You'll be prompted for:
- AWS region (use `us-east-1`)
- RDS endpoint (from Step 2)
- Database username (`invoiceadmin`)
- Database password (from Step 2)
- OpenAI API key
- OpenAI model (`gpt-4o-mini`)

---

## Phase 2: Backend Deployment

### Step 7: Set Up EC2 Instance

1. **SSH into EC2**:
   ```bash
   chmod 400 ~/Downloads/invoiceme-key.pem
   ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]
   ```

2. **Upload and run setup script**:

   From your local machine (new terminal):
   ```bash
   cd deployment
   scp -i ~/Downloads/invoiceme-key.pem ec2-setup.sh ubuntu@[ELASTIC-IP]:/home/ubuntu/
   ```

   Back on EC2:
   ```bash
   chmod +x ec2-setup.sh
   ./ec2-setup.sh
   ```

3. **Log out and back in** (for docker group):
   ```bash
   exit
   ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]
   ```

### Step 8: Build and Deploy Backend

From your **local machine**:

```bash
cd deployment
chmod +x build-and-upload.sh
./build-and-upload.sh
```

You'll be prompted for:
- EC2 Elastic IP
- Path to SSH key

This script will:
1. Build the Docker image
2. Upload to EC2
3. Deploy the application

### Step 9: Verify Backend

```bash
# Check health
curl http://[ELASTIC-IP]/actuator/health

# Expected output: {"status":"UP"}

# Test API
curl http://[ELASTIC-IP]/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"demo","password":"password"}'

# Should return JWT token
```

---

## Phase 3: Frontend Deployment to Vercel

### Step 10: Deploy to Vercel

**Option A: Using Vercel CLI**

```bash
# Install Vercel CLI
npm install -g vercel

# Login
vercel login

# Deploy
cd frontend
vercel

# Follow prompts:
# - Set up and deploy: Yes
# - Project name: invoiceme-frontend
# - Directory: ./
# - Override settings: No

# Set environment variable
vercel env add NEXT_PUBLIC_API_URL production
# Enter: http://[YOUR-ELASTIC-IP]/api

# Deploy to production
vercel --prod
```

**Option B: Using Vercel Dashboard**

1. Go to https://vercel.com/dashboard
2. Click **Add New** → **Project**
3. Import your GitHub repository
4. Configure:
   - Framework: **Next.js**
   - Root Directory: `frontend`
   - Build Command: `npm run build` (or `bun run build`)
   - Output Directory: `.next`
   - Install Command: `npm install` (or `bun install`)

5. **Environment Variables**:
   - Key: `NEXT_PUBLIC_API_URL`
   - Value: `http://[YOUR-ELASTIC-IP]/api`

6. Click **Deploy**

7. **Note your Vercel URL**: `https://invoiceme-frontend-xxx.vercel.app`

### Step 11: Update CORS for Vercel

Update the CORS_ORIGINS on EC2:

```bash
ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]

# Edit the docker-compose.yml or add to load-env.sh
cd /home/ubuntu/invoiceme
nano load-env.sh

# Add this line before the end:
export CORS_ORIGINS="https://your-vercel-url.vercel.app,http://localhost:3000"

# Redeploy
./deploy.sh
```

---

## Phase 4: Testing

### Step 12: End-to-End Testing

1. **Open your Vercel URL**: `https://invoiceme-frontend-xxx.vercel.app`

2. **Login**:
   - Username: `demo`
   - Password: `password`

3. **Test features**:
   - [ ] Dashboard loads
   - [ ] Create a customer
   - [ ] Create an invoice
   - [ ] Record a payment
   - [ ] Test AI chat
   - [ ] View analytics

4. **Check browser console**:
   - Should see no CORS errors
   - API calls should succeed

---

## Troubleshooting

### Backend won't start
```bash
ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]
cd /home/ubuntu/invoiceme
docker-compose logs -f
```

Common issues:
- Database connection failure → Check RDS security group
- Missing environment variables → Run `source load-env.sh && env | grep -E 'DATABASE|OPENAI'`

### Frontend can't connect to backend
- Verify `NEXT_PUBLIC_API_URL` in Vercel dashboard
- Check CORS settings in backend
- Test API directly: `curl http://[ELASTIC-IP]/api/auth/login`

### Database connection timeout
```bash
# From EC2, test connection:
telnet [RDS-ENDPOINT] 5432
```

If fails, check RDS security group allows EC2 SG on port 5432.

---

## Post-Deployment

### Optional: Set up HTTPS with Let's Encrypt

```bash
ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]

# Install Certbot
sudo apt install -y certbot python3-certbot-nginx

# Get certificate (requires domain name)
sudo certbot --nginx -d api.yourdomain.com

# Update Vercel env var to use HTTPS
# NEXT_PUBLIC_API_URL=https://api.yourdomain.com/api
```

### Monitoring

**View logs**:
```bash
ssh -i ~/Downloads/invoiceme-key.pem ubuntu@[ELASTIC-IP]
cd /home/ubuntu/invoiceme
docker-compose logs -f
```

**Restart application**:
```bash
cd /home/ubuntu/invoiceme
docker-compose restart
```

**Update backend code**:
```bash
# From local machine
cd deployment
./build-and-upload.sh
```

---

## Costs (AWS Free Tier)

- **Year 1**: $0 (covered by Free Tier)
- **After 12 months**: ~$25-35/month
  - EC2 t2.micro: ~$8-10/month
  - RDS db.t3.micro: ~$15-20/month
  - Storage + Data transfer: ~$2-5/month

**Cost Optimization**:
- Stop EC2 and RDS when not in use (demo purposes)
- Use AWS Budget alerts

---

## Success Checklist

- [ ] RDS database created and accessible from EC2
- [ ] EC2 instance running with Elastic IP
- [ ] Parameter Store contains all secrets
- [ ] Backend health check returns 200 OK
- [ ] Frontend deployed to Vercel
- [ ] Frontend can communicate with backend
- [ ] Demo login works (demo/password)
- [ ] All CRUD operations work
- [ ] AI chat functionality works
- [ ] No CORS errors in browser console

---

## Next Steps

1. Set up custom domain (optional)
2. Enable HTTPS/SSL
3. Configure automated backups
4. Set up monitoring alerts
5. Implement CI/CD pipeline

---

## Support

If you encounter issues:
1. Check the troubleshooting section above
2. Review logs: `docker-compose logs -f`
3. Verify security groups and network configuration
4. Check Parameter Store values: `aws ssm get-parameters-by-path --path '/invoiceme/prod' --recursive`

---

**Total Deployment Time**: ~2-3 hours

Good luck! 🚀
