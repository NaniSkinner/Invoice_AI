#!/bin/bash
# Script to set up AWS Systems Manager Parameter Store
# Run this script to store all secrets for the InvoiceMe application

set -e

echo "======================================"
echo "InvoiceMe - Parameter Store Setup"
echo "======================================"
echo ""

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    echo "Error: AWS CLI is not installed."
    echo "Please install it: https://aws.amazon.com/cli/"
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    echo "Error: AWS credentials not configured."
    echo "Please run: aws configure"
    exit 1
fi

# Get AWS region
read -p "Enter AWS region (default: us-east-1): " AWS_REGION
AWS_REGION=${AWS_REGION:-us-east-1}

echo ""
echo "Using AWS Region: $AWS_REGION"
echo ""

# Get RDS endpoint
read -p "Enter RDS endpoint (e.g., invoiceme.xxxxx.us-east-1.rds.amazonaws.com): " RDS_ENDPOINT
if [ -z "$RDS_ENDPOINT" ]; then
    echo "Error: RDS endpoint is required"
    exit 1
fi

# Get database credentials
read -p "Enter database username (default: invoiceadmin): " DB_USERNAME
DB_USERNAME=${DB_USERNAME:-invoiceadmin}

read -sp "Enter database password: " DB_PASSWORD
echo ""
if [ -z "$DB_PASSWORD" ]; then
    echo "Error: Database password is required"
    exit 1
fi

# Get OpenAI API key
read -sp "Enter OpenAI API key (sk-...): " OPENAI_KEY
echo ""
if [ -z "$OPENAI_KEY" ]; then
    echo "Error: OpenAI API key is required"
    exit 1
fi

# Get OpenAI model
read -p "Enter OpenAI model (default: gpt-4o-mini): " OPENAI_MODEL
OPENAI_MODEL=${OPENAI_MODEL:-gpt-4o-mini}

# Generate JWT secret
echo ""
echo "Generating secure JWT secret..."
JWT_SECRET=$(openssl rand -base64 48)

# Construct database URL
DATABASE_URL="jdbc:postgresql://${RDS_ENDPOINT}:5432/invoiceme"

echo ""
echo "======================================"
echo "Storing parameters in AWS..."
echo "======================================"
echo ""

# Store DATABASE_URL
echo "Storing DATABASE_URL..."
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_URL" \
  --value "$DATABASE_URL" \
  --type "SecureString" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_URL" \
  --value "$DATABASE_URL" \
  --type "SecureString" \
  --region "$AWS_REGION"

# Store DATABASE_USERNAME
echo "Storing DATABASE_USERNAME..."
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_USERNAME" \
  --value "$DB_USERNAME" \
  --type "SecureString" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_USERNAME" \
  --value "$DB_USERNAME" \
  --type "SecureString" \
  --region "$AWS_REGION"

# Store DATABASE_PASSWORD
echo "Storing DATABASE_PASSWORD..."
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_PASSWORD" \
  --value "$DB_PASSWORD" \
  --type "SecureString" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/DATABASE_PASSWORD" \
  --value "$DB_PASSWORD" \
  --type "SecureString" \
  --region "$AWS_REGION"

# Store OPENAI_API_KEY
echo "Storing OPENAI_API_KEY..."
aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_API_KEY" \
  --value "$OPENAI_KEY" \
  --type "SecureString" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_API_KEY" \
  --value "$OPENAI_KEY" \
  --type "SecureString" \
  --region "$AWS_REGION"

# Store OPENAI_MODEL
echo "Storing OPENAI_MODEL..."
aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_MODEL" \
  --value "$OPENAI_MODEL" \
  --type "String" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/OPENAI_MODEL" \
  --value "$OPENAI_MODEL" \
  --type "String" \
  --region "$AWS_REGION"

# Store JWT_SECRET
echo "Storing JWT_SECRET..."
aws ssm put-parameter \
  --name "/invoiceme/prod/JWT_SECRET" \
  --value "$JWT_SECRET" \
  --type "SecureString" \
  --region "$AWS_REGION" \
  --overwrite 2>/dev/null || \
aws ssm put-parameter \
  --name "/invoiceme/prod/JWT_SECRET" \
  --value "$JWT_SECRET" \
  --type "SecureString" \
  --region "$AWS_REGION"

echo ""
echo "======================================"
echo "SUCCESS! All parameters stored."
echo "======================================"
echo ""
echo "Stored parameters:"
echo "  - /invoiceme/prod/DATABASE_URL"
echo "  - /invoiceme/prod/DATABASE_USERNAME"
echo "  - /invoiceme/prod/DATABASE_PASSWORD"
echo "  - /invoiceme/prod/OPENAI_API_KEY"
echo "  - /invoiceme/prod/OPENAI_MODEL"
echo "  - /invoiceme/prod/JWT_SECRET"
echo ""
echo "To verify, run:"
echo "  aws ssm get-parameters-by-path --path '/invoiceme/prod' --recursive --region $AWS_REGION"
echo ""
