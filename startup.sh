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

# AWS configuration (using EC2 IAM role, but setting region)
export AWS_REGION="ap-northeast-2"
export S3_MEDIA_BUCKET_NAME=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/s3-bucket-name" --query "Parameter.Value" --output text)
export CLOUDFRONT_DOMAIN=$(aws ssm get-parameter --region ap-northeast-2 --name "/myce/cloudfront-domain" --query "Parameter.Value" --output text)

# Set profile for production
export PROFILE="product"

echo "✅ Successfully loaded environment variables from SSM Parameter Store"
echo "🚀 Starting Spring Boot application..."

# Start the Spring Boot application
exec java -jar /app/app.jar