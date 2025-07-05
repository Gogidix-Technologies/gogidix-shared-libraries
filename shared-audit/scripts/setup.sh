#!/bin/bash

set -e

# Exalt Shared Audit Library - Development Setup Script
# This script sets up the development environment for the shared-audit library

echo "🚀 Setting up Exalt Shared Audit Library development environment..."

# Configuration
PROJECT_NAME="shared-audit"
JAVA_VERSION="17"
MAVEN_VERSION="3.8.7"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Utility functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Check Java installation
check_java() {
    log_info "Checking Java installation..."
    
    if command_exists java; then
        JAVA_VER=$(java -version 2>&1 | grep -i version | cut -d'"' -f2 | cut -d'.' -f1-2)
        if [ "$JAVA_VER" = "17" ] || [ "$JAVA_VER" = "1.8" ] || [ "$JAVA_VER" = "11" ]; then
            log_success "Java $JAVA_VER is installed"
        else
            log_warning "Java version $JAVA_VER detected. Java 17 is recommended."
        fi
    else
        log_error "Java is not installed. Please install Java $JAVA_VERSION"
        exit 1
    fi
}

# Check Maven installation
check_maven() {
    log_info "Checking Maven installation..."
    
    if command_exists mvn; then
        MVN_VER=$(mvn -version | grep "Apache Maven" | cut -d' ' -f3)
        log_success "Maven $MVN_VER is installed"
    else
        log_error "Maven is not installed. Please install Maven $MAVEN_VERSION"
        exit 1
    fi
}

# Check Docker installation
check_docker() {
    log_info "Checking Docker installation..."
    
    if command_exists docker; then
        DOCKER_VER=$(docker --version | cut -d' ' -f3 | sed 's/,//')
        log_success "Docker $DOCKER_VER is installed"
        
        # Check if Docker daemon is running
        if docker info >/dev/null 2>&1; then
            log_success "Docker daemon is running"
        else
            log_warning "Docker daemon is not running. Please start Docker."
        fi
    else
        log_warning "Docker is not installed. Some features may not be available."
    fi
}

# Setup local database
setup_database() {
    log_info "Setting up local PostgreSQL database with Docker..."
    
    if command_exists docker; then
        # Check if PostgreSQL container is already running
        if [ "$(docker ps -q -f name=postgres-audit)" ]; then
            log_warning "PostgreSQL container 'postgres-audit' is already running"
        else
            log_info "Starting PostgreSQL container..."
            docker run -d \
                --name postgres-audit \
                -e POSTGRES_DB=audit_db \
                -e POSTGRES_USER=audit_user \
                -e POSTGRES_PASSWORD=audit_password \
                -p 5432:5432 \
                postgres:15-alpine
            
            log_success "PostgreSQL container started successfully"
            log_info "Database: audit_db"
            log_info "Username: audit_user"
            log_info "Password: audit_password"
            log_info "Port: 5432"
        fi
    else
        log_warning "Docker not available. Please set up PostgreSQL manually."
    fi
}

# Setup Kafka (optional)
setup_kafka() {
    log_info "Setting up local Kafka with Docker..."
    
    if command_exists docker; then
        # Create docker-compose file for Kafka
        cat > docker-compose-kafka.yml << EOF
version: '3.8'
services:
  zookeeper:
    image: confluentinc/cp-zookeeper:7.4.0
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
    ports:
      - "2181:2181"

  kafka:
    image: confluentinc/cp-kafka:7.4.0
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: true
EOF
        
        log_info "Starting Kafka cluster..."
        docker-compose -f docker-compose-kafka.yml up -d
        log_success "Kafka cluster started successfully"
    else
        log_warning "Docker not available. Kafka setup skipped."
    fi
}

# Install dependencies
install_dependencies() {
    log_info "Installing Maven dependencies..."
    
    if [ -f "pom.xml" ]; then
        mvn clean compile
        log_success "Dependencies installed successfully"
    else
        log_error "pom.xml not found. Please run this script from the project root."
        exit 1
    fi
}

# Run tests
run_tests() {
    log_info "Running tests..."
    
    mvn test
    
    if [ $? -eq 0 ]; then
        log_success "All tests passed!"
    else
        log_error "Some tests failed. Please check the output above."
    fi
}

# Create development configuration
create_dev_config() {
    log_info "Creating development configuration..."
    
    mkdir -p src/main/resources
    
    cat > src/main/resources/application-dev.yml << EOF
spring:
  profiles:
    active: dev
  
  datasource:
    url: jdbc:postgresql://localhost:5432/audit_db
    username: audit_user
    password: audit_password
    driver-class-name: org.postgresql.Driver
  
  jpa:
    hibernate:
      ddl-auto: update
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    show-sql: true

exalt:
  audit:
    enabled: true
    async: true
    storage:
      type: database
    retention:
      days: 30
    include-request-body: true
    include-response-body: false

logging:
  level:
    com.exalt: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
EOF
    
    log_success "Development configuration created"
}

# Main setup function
main() {
    echo "========================================="
    echo "  Exalt Shared Audit Library Setup"
    echo "========================================="
    echo ""
    
    # Perform checks
    check_java
    check_maven
    check_docker
    
    echo ""
    echo "Setting up development environment..."
    echo ""
    
    # Setup components
    install_dependencies
    create_dev_config
    setup_database
    
    # Ask user if they want Kafka
    echo ""
    read -p "Do you want to set up Kafka for event streaming? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        setup_kafka
    fi
    
    # Run tests
    echo ""
    read -p "Do you want to run tests? (y/n): " -n 1 -r
    echo ""
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        run_tests
    fi
    
    echo ""
    echo "========================================="
    log_success "Setup completed successfully!"
    echo "========================================="
    echo ""
    echo "Next steps:"
    echo "1. Run './scripts/dev.sh' to start development mode"
    echo "2. Access database: psql -h localhost -U audit_user -d audit_db"
    echo "3. View application logs: tail -f logs/application.log"
    echo "4. API Documentation: http://localhost:8080/swagger-ui.html"
    echo ""
}

# Run main function
main "$@"