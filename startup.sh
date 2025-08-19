#!/bin/bash
set -e

echo "🔐 Fetching environment variables from AWS Systems Manager Parameter Store..."

# Fetch all required environment variables from SSM
export DB_URL=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/db-url" --with-decryption --query "Parameter.Value" --output text)
export DB_USERNAME=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/db-username" --query "Parameter.Value" --output text)
export DB_PASSWORD=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/db-password" --with-decryption --query "Parameter.Value" --output text)
export DB_DRIVER_CLASS_NAME="com.mysql.cj.jdbc.Driver"

export MONGODB_URI=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/mongodb-uri" --with-decryption --query "Parameter.Value" --output text)
export REDIS_URL=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/redis-url" --with-decryption --query "Parameter.Value" --output text)
export JWT_SECRET=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/jwt-secret" --with-decryption --query "Parameter.Value" --output text)

# AWS configuration - AwsConfig will automatically use EC2 IAM role
export AWS_REGION="ap-northeast-2"
export S3_MEDIA_BUCKET_NAME=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/s3-bucket-name" --query "Parameter.Value" --output text)
export CLOUDFRONT_DOMAIN=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/cloudfront-domain" --query "Parameter.Value" --output text)

# PortOne payment configuration
export PORTONE_BASE_URL=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/portone-base-url" --query "Parameter.Value" --output text)
export PORTONE_API_KEY=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/portone-api-key" --with-decryption --query "Parameter.Value" --output text)
export PORTONE_API_SECRET=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/portone-api-secret" --with-decryption --query "Parameter.Value" --output text)
export PORTONE_CUSTOMER_CODE=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/portone-customer-code" --query "Parameter.Value" --output text)

# Amazon SES email configuration
export MAIL_HOST=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/ses-smtp-host" --query "Parameter.Value" --output text)
export MAIL_USERNAME=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/ses-smtp-username" --with-decryption --query "Parameter.Value" --output text)
export MAIL_PASSWORD=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/ses-smtp-password" --with-decryption --query "Parameter.Value" --output text)

# OAuth2 configuration (Google and Kakao SNS login)
export GOOGLE_CLIENT_ID=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/GOOGLE_CLIENT_ID" --query "Parameter.Value" --output text)
export GOOGLE_CLIENT_SECRET=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/GOOGLE_CLIENT_SECRET" --with-decryption --query "Parameter.Value" --output text)
export KAKAO_CLIENT_ID=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/KAKAO_CLIENT_ID" --query "Parameter.Value" --output text)
export KAKAO_CLIENT_SECRET=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/KAKAO_CLIENT_SECRET" --with-decryption --query "Parameter.Value" --output text)

# Note: AWS credentials (ACCESS_KEY_ID, SECRET_ACCESS_KEY) are NOT set
# AwsConfig will automatically detect and use the EC2 IAM role for authentication

# Set profile for production
export PROFILE="product"

echo "✅ Successfully loaded environment variables from SSM Parameter Store"
echo "🚀 Starting Spring Boot application..."

# Start the Spring Boot application with Asia/Seoul timezone
exec java -Duser.timezone=Asia/Seoul -jar /app/app.jar