# Render Deployment Guide for Invoice_AI

This guide will help you deploy the Invoice_AI application to Render (backend) and Render/Vercel (frontend).

## Prerequisites

- GitHub account with Invoice_AI repository
- Render account (free tier available)
- OpenAI API key

## Architecture Overview

```
Frontend (Render/Vercel) → Backend API (Render Web Service) → PostgreSQL (Render Database)
```

## Part 1: Backend Deployment to Render

### Step 1: Create PostgreSQL Database

1. Go to [Render Dashboard](https://dashboard.render.com/)
2. Click **"New +"** → **"PostgreSQL"**
3. Configure:
   - **Name**: `invoiceme-db`
   - **Database**: `invoiceme`
   - **User**: (auto-generated)
   - **Region**: Choose closest to you
   - **Plan**: Free
4. Click **"Create Database"**
5. Wait for database to provision (takes 1-2 minutes)
6. Note: Render will provide these environment variables automatically:
   - `PGHOST`
   - `PGPORT`
   - `PGDATABASE`
   - `PGUSER`
   - `PGPASSWORD`

### Step 2: Deploy Backend Web Service

1. In Render Dashboard, click **"New +"** → **"Web Service"**
2. Connect your GitHub repository (Invoice_AI)
3. Configure the service:

   **Basic Settings:**
   - **Name**: `invoiceme-backend`
   - **Region**: Same as your database
   - **Branch**: `master` (or your main branch)
   - **Root Directory**: Leave blank (uses repo root)
   - **Runtime**: Java
   - **Build Command**:
     ```bash
     cd backend && mvn clean package -DskipTests
     ```
   - **Start Command**:
     ```bash
     java -jar backend/target/*.jar
     ```

   **Advanced Settings:**
   - **Plan**: Free
   - **Health Check Path**: `/actuator/health`
   - **Auto-Deploy**: Yes (optional, but recommended)

4. **Add Environment Variables** (click "Advanced" → "Add Environment Variable"):

   | Key | Value | Notes |
   |-----|-------|-------|
   | `OPENAI_API_KEY` | `sk-your-key-here` | Your OpenAI API key |
   | `JWT_SECRET` | `your-secure-random-string-min-256-bits` | Generate a secure random string (32+ chars) |
   | `CORS_ORIGINS` | `http://localhost:3000` | Update after frontend deployment |

5. **Link Database**:
   - Scroll down to "Environment Variables"
   - Click "Add from Database"
   - Select your `invoiceme-db` database
   - This will automatically add: `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD`

6. Click **"Create Web Service"**

7. Wait for deployment (first build takes 5-10 minutes):
   - Maven will download dependencies
   - Application will compile
   - JAR will be created
   - Health check will verify startup

### Step 3: Verify Backend Deployment

1. Once deployed, Render will provide a URL like: `https://invoiceme-backend.onrender.com`
2. Test the health endpoint:
   ```bash
   curl https://invoiceme-backend.onrender.com/actuator/health
   ```
   Should return: `{"status":"UP"}`

3. Test authentication:
   ```bash
   curl https://invoiceme-backend.onrender.com/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"demo","password":"password"}'
   ```
   Should return a JWT token.

## Part 2: Frontend Deployment

### Option A: Deploy to Render

1. In Render Dashboard, click **"New +"** → **"Static Site"**
2. Connect your GitHub repository
3. Configure:
   - **Name**: `invoiceme-frontend`
   - **Branch**: `master`
   - **Root Directory**: `frontend`
   - **Build Command**: `npm install && npm run build`
   - **Publish Directory**: `frontend/.next`
4. Add Environment Variable:
   - **Key**: `NEXT_PUBLIC_API_URL`
   - **Value**: `https://invoiceme-backend.onrender.com/api`
5. Click **"Create Static Site"**

### Option B: Deploy to Vercel (Recommended for Next.js)

1. Install Vercel CLI:
   ```bash
   npm install -g vercel
   ```

2. Navigate to frontend directory:
   ```bash
   cd frontend
   ```

3. Deploy:
   ```bash
   vercel --prod
   ```

4. Set environment variable in Vercel dashboard:
   - Go to your project settings
   - Navigate to "Environment Variables"
   - Add:
     - **Key**: `NEXT_PUBLIC_API_URL`
     - **Value**: `https://invoiceme-backend.onrender.com/api`
   - Redeploy

### Step 4: Update CORS Settings

After frontend is deployed:

1. Go to your backend service in Render
2. Update the `CORS_ORIGINS` environment variable:
   ```
   https://your-frontend-url.vercel.app,http://localhost:3000
   ```
3. Click "Save Changes"
4. The service will automatically redeploy

## Part 3: Testing the Full Application

1. Open your frontend URL
2. Login with demo credentials:
   - **Username**: `demo`
   - **Password**: `password`
3. Test key features:
   - Create a customer
   - Create an invoice
   - Test the AI chat assistant
   - Record a payment

## Troubleshooting

### Backend Won't Start - Database Connection Error

**Error**: `Connection to localhost:5432 refused`

**Solution**: Make sure you linked the database in Step 2.5. The environment variables `PGHOST`, `PGPORT`, `PGDATABASE`, `PGUSER`, `PGPASSWORD` must be set.

**Manual Setup** (if auto-link doesn't work):
1. Go to your database in Render dashboard
2. Copy the "Internal Database URL" (not external)
3. It will be in format: `postgresql://user:pass@host:port/database`
4. Manually add these environment variables to your web service:
   - Extract values from the URL and add individually
   - Or use the connection info panel on the database page

### Backend Crashes - Out of Memory

**Error**: `OutOfMemoryError`

**Solution**: The free tier has limited memory (512MB). The default Java heap size might be too large.

1. Update `JAVA_OPTS` environment variable:
   ```
   -Xmx400m -Xms200m
   ```
2. Consider upgrading to a paid tier for production use

### Frontend Can't Connect to Backend

**Error**: CORS errors in browser console

**Solution**:
1. Verify `CORS_ORIGINS` includes your frontend URL
2. Make sure `NEXT_PUBLIC_API_URL` points to correct backend URL
3. Check that both URLs use `https://` (not mixing http/https)

### Flyway Migration Fails

**Error**: `Flyway migration failed`

**Solution**:
1. Check database is accessible
2. Verify `PGUSER` has sufficient permissions
3. Check logs for specific SQL errors
4. Database migrations are in `backend/src/main/resources/db/migration/`

### Render Free Tier Limitations

- **Web services spin down after 15 minutes of inactivity**
  - First request after spin-down takes 30-60 seconds (cold start)
  - Solution: Consider upgrading to paid tier for production, or keep service warm with a monitoring service

- **750 hours/month free** (enough for 1 service running 24/7)
  - Solution: Stop services when not in use, or upgrade

- **Database**: 90 days retention, then deleted
  - Solution: Regular backups, or upgrade to paid tier

## Environment Variables Reference

### Backend Service

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `PGHOST` | Yes | - | Auto-provided by database link |
| `PGPORT` | Yes | - | Auto-provided by database link |
| `PGDATABASE` | Yes | - | Auto-provided by database link |
| `PGUSER` | Yes | - | Auto-provided by database link |
| `PGPASSWORD` | Yes | - | Auto-provided by database link |
| `PORT` | No | 8080 | Auto-provided by Render |
| `OPENAI_API_KEY` | Yes | - | Your OpenAI API key |
| `JWT_SECRET` | Yes | - | Secure random string (32+ chars) |
| `CORS_ORIGINS` | Yes | - | Frontend URL(s), comma-separated |
| `OPENAI_MODEL` | No | gpt-4o-mini | OpenAI model to use |

### Frontend Service

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `NEXT_PUBLIC_API_URL` | Yes | - | Backend API URL (with /api path) |

## Cost Estimate

### Free Tier (First Year)
- Backend: Free (with limitations)
- Database: Free (90-day retention)
- Frontend (Vercel): Free
- **Total**: $0/month

### Paid Tier (Recommended for Production)
- Backend (Starter): $7/month
- Database (Starter): $7/month
- Frontend (Vercel Pro): $20/month
- **Total**: ~$34/month

## Next Steps

1. ✅ Set up custom domain (optional)
2. ✅ Configure SSL/TLS (auto-enabled on Render)
3. ✅ Set up monitoring and alerts
4. ✅ Configure automated backups
5. ✅ Set up CI/CD pipeline
6. ✅ Configure SMTP for email reminders

## Support

- Render Documentation: https://render.com/docs
- Render Community: https://community.render.com/
- Invoice_AI Issues: https://github.com/YourUsername/Invoice_AI/issues
