#!/bin/bash

# Monitoring Access Script for MYCE Server
# This script sets up SSH tunnels to access Prometheus and Grafana

set -e

# Colors for output
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Configuration
SSH_KEY="$HOME/.ssh/aws/likelion-terraform-key"
EC2_USER="ubuntu"

# Function to get EC2 IP from infrastructure project
get_ec2_ip() {
    local infrastructure_dir="../infrastructure-project"
    
    if [ -f "$infrastructure_dir/ansible/group_vars/all.yml" ]; then
        EC2_IP=$(grep "ec2_public_ip:" "$infrastructure_dir/ansible/group_vars/all.yml" | cut -d'"' -f2)
        if [ -z "$EC2_IP" ]; then
            echo -e "${RED}❌ Could not extract EC2 IP from ansible configuration${NC}"
            echo "Please enter EC2 IP manually:"
            read -r EC2_IP
        fi
    else
        echo -e "${YELLOW}⚠️  Could not find ansible configuration${NC}"
        echo "Please enter EC2 IP:"
        read -r EC2_IP
    fi
}

# Function to check if port is already in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to create SSH tunnel
create_tunnel() {
    local local_port=$1
    local remote_port=$2
    local service_name=$3
    
    if check_port $local_port; then
        echo -e "${YELLOW}⚠️  Port $local_port already in use (possibly existing tunnel)${NC}"
        echo "   $service_name might already be accessible at http://localhost:$local_port"
    else
        echo -e "${BLUE}🔗 Creating SSH tunnel for $service_name...${NC}"
        ssh -f -N -L $local_port:localhost:$remote_port -i "$SSH_KEY" "$EC2_USER@$EC2_IP" 2>/dev/null
        
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✅ $service_name tunnel created successfully${NC}"
            echo "   Access at: http://localhost:$local_port"
        else
            echo -e "${RED}❌ Failed to create tunnel for $service_name${NC}"
            return 1
        fi
    fi
}

# Function to kill existing tunnels
kill_tunnels() {
    echo -e "${YELLOW}🔄 Killing existing SSH tunnels...${NC}"
    
    # Kill processes using our monitoring ports
    for port in 9090 3000 9100; do
        if check_port $port; then
            local pid=$(lsof -ti:$port)
            if [ ! -z "$pid" ]; then
                kill $pid 2>/dev/null
                echo "   Killed process on port $port"
            fi
        fi
    done
    
    # Also kill any SSH tunnels to our EC2 instance
    if [ ! -z "$EC2_IP" ]; then
        pkill -f "ssh.*$EC2_IP" 2>/dev/null || true
    fi
    
    echo -e "${GREEN}✅ Existing tunnels cleared${NC}"
}

# Main script
echo -e "${BLUE}🚀 MYCE Monitoring Access Tool${NC}"
echo "=================================="

# Parse command line arguments
case "${1:-}" in
    stop|kill)
        get_ec2_ip
        kill_tunnels
        echo -e "${GREEN}✨ All monitoring tunnels stopped${NC}"
        exit 0
        ;;
    restart)
        get_ec2_ip
        kill_tunnels
        sleep 2
        ;;
    help|--help|-h)
        echo "Usage: $0 [start|stop|restart|help]"
        echo ""
        echo "Commands:"
        echo "  start    - Start SSH tunnels (default)"
        echo "  stop     - Stop all SSH tunnels"
        echo "  restart  - Restart SSH tunnels"
        echo "  help     - Show this help message"
        echo ""
        echo "Once tunnels are created, access:"
        echo "  Prometheus: http://localhost:9090"
        echo "  Grafana:    http://localhost:3000 (admin/grafana123)"
        echo "  Node Exporter: http://localhost:9100/metrics"
        exit 0
        ;;
esac

# Get EC2 IP
get_ec2_ip

if [ -z "$EC2_IP" ]; then
    echo -e "${RED}❌ EC2 IP is required${NC}"
    exit 1
fi

echo -e "${BLUE}📡 Connecting to EC2: $EC2_IP${NC}"

# Check SSH key exists
if [ ! -f "$SSH_KEY" ]; then
    echo -e "${RED}❌ SSH key not found at: $SSH_KEY${NC}"
    exit 1
fi

# Test SSH connection first
echo -e "${BLUE}🔍 Testing SSH connection...${NC}"
if ssh -o ConnectTimeout=5 -i "$SSH_KEY" "$EC2_USER@$EC2_IP" "echo 'SSH connection successful'" 2>/dev/null; then
    echo -e "${GREEN}✅ SSH connection successful${NC}"
else
    echo -e "${RED}❌ Cannot connect to EC2 instance${NC}"
    echo "Please check:"
    echo "  - EC2 instance is running"
    echo "  - Security group allows SSH (port 22)"
    echo "  - SSH key is correct"
    exit 1
fi

# Create tunnels
echo -e "\n${BLUE}🔗 Creating SSH tunnels...${NC}"
create_tunnel 9090 9090 "Prometheus"
create_tunnel 3000 3000 "Grafana"
create_tunnel 9100 9100 "Node Exporter"

# Display access information
echo -e "\n${GREEN}🎉 Monitoring Access Ready!${NC}"
echo "=================================="
echo -e "${BLUE}📊 Access your monitoring tools:${NC}"
echo ""
echo "  Prometheus:    http://localhost:9090"
echo "  Grafana:       http://localhost:3000"
echo "                 Username: admin"
echo "                 Password: grafana123"
echo "  Node Exporter: http://localhost:9100/metrics"
echo ""
echo -e "${BLUE}📋 Useful Endpoints:${NC}"
echo ""
echo "  Spring Boot Metrics: http://localhost:9090/targets"
echo "  Grafana Dashboards:  http://localhost:3000/dashboards"
echo ""
echo -e "${YELLOW}💡 Tips:${NC}"
echo "  - Import dashboard ID 4701 for JVM metrics"
echo "  - Import dashboard ID 1860 for system metrics"
echo "  - Check 'MYCE Spring Boot Application' dashboard"
echo ""
echo -e "${YELLOW}⚠️  To stop tunnels, run: $0 stop${NC}"
echo ""
echo -e "${GREEN}✨ Happy monitoring! 📈${NC}"