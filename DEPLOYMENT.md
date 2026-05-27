# Deployment Guide

This guide covers deployment options for the Vote App server.

## Prerequisites

- Docker and Docker Compose installed
- Firebase service account JSON file
- PostgreSQL database (or use the included docker-compose)

## Local Development

### Using Docker Compose (Recommended)

```bash
# Start all services (PostgreSQL + Server)
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

### Manual Setup

1. **Set environment variables:**
   ```bash
   export DB_HOST=localhost
   export DB_PORT=5432
   export DB_NAME=voteapp
   export DB_USER=voteapp_user
   export DB_PASSWORD=voteapp_password
   export FIREBASE_SERVICE_ACCOUNT_JSON_PATH=/path/to/firebase-service-account.json
   ```

2. **Build and run:**
   ```bash
   ./gradlew build
   java -jar server/build/libs/server.jar
   ```

## Production Deployment

### Option 1: Docker on VPS

1. **Prepare server:**
   ```bash
   # Install Docker
   curl -fsSL https://get.docker.com -o get-docker.sh
   sh get-docker.sh
   
   # Install Docker Compose
   sudo apt-get install docker-compose
   ```

2. **Upload application:**
   ```bash
   # Build and export image
   docker build -t voteapp-server .
   docker save voteapp-server | gzip > voteapp-server.tar.gz
   
   # Transfer to server
   scp voteapp-server.tar.gz user@your-server:/opt/voteapp/
   ```

3. **Deploy on server:**
   ```bash
   cd /opt/voteapp
   gunzip voteapp-server.tar.gz
   docker load < voteapp-server.tar
   
   # Create .env file
   cat > .env << EOF
   DB_HOST=localhost
   DB_PORT=5432
   DB_NAME=voteapp
   DB_USER=voteapp_user
   DB_PASSWORD=YOUR_STRONG_PASSWORD
   FIREBASE_SERVICE_ACCOUNT_JSON_PATH=/opt/voteapp/firebase.json
   EOF
   
   # Run with systemd
   sudo nano /etc/systemd/system/voteapp.service
   ```

4. **Systemd service file:**
   ```ini
   [Unit]
   Description=Vote App Server
   After=network.target postgresql.service
   
   [Service]
   Type=simple
   User=voteapp
   WorkingDirectory=/opt/voteapp
   EnvironmentFile=/opt/voteapp/.env
   ExecStart=/usr/bin/docker run -d --name voteapp-server \
     --env-file /opt/voteapp/.env \
     -p 8080:8080 \
     voteapp-server
   Restart=always
   
   [Install]
   WantedBy=multi-user.target
   ```

5. **Start service:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable voteapp
   sudo systemctl start voteapp
   ```

### Option 2: Heroku

1. **Create Procfile:**
   ```
   web: java -jar server/build/libs/server.jar
   ```

2. **Setup Heroku:**
   ```bash
   # Install Heroku CLI
   # Login
   heroku login
   
   # Create app
   heroku create your-app-name
   
   # Add PostgreSQL
   heroku addons:create heroku-postgresql:mini
   
   # Set config vars
   heroku config:set FIREBASE_SERVICE_ACCOUNT_JSON_PATH='{"...your json..."}'
   
   # Deploy
   git push heroku main
   ```

### Option 3: Kubernetes

1. **Create deployment manifest:**
   ```yaml
   apiVersion: apps/v1
   kind: Deployment
   metadata:
     name: voteapp-server
   spec:
     replicas: 3
     selector:
       matchLabels:
         app: voteapp-server
     template:
       metadata:
         labels:
           app: voteapp-server
       spec:
         containers:
         - name: server
           image: your-registry/voteapp-server:latest
           ports:
           - containerPort: 8080
           env:
           - name: DB_HOST
             valueFrom:
               secretKeyRef:
                 name: voteapp-secrets
                 key: db-host
           - name: DB_PASSWORD
             valueFrom:
               secretKeyRef:
                 name: voteapp-secrets
                 key: db-password
           livenessProbe:
             httpGet:
               path: /
               port: 8080
             initialDelaySeconds: 40
             periodSeconds: 10
   ```

## Environment Variables

| Variable | Description | Required | Default |
|----------|-------------|----------|---------|
| `PORT` | Server port | No | 8080 |
| `HOST` | Server host | No | 0.0.0.0 |
| `DB_HOST` | Database host | Yes | - |
| `DB_PORT` | Database port | No | 5432 |
| `DB_NAME` | Database name | Yes | - |
| `DB_USER` | Database user | Yes | - |
| `DB_PASSWORD` | Database password | Yes | - |
| `FIREBASE_SERVICE_ACCOUNT_JSON_PATH` | Path to Firebase credentials | Yes | - |

## Monitoring

### Health Checks

- **Root:** `GET /` - Basic server health
- **Auth:** `GET /api/v1/auth/health` - Auth module health

### Logging

Logs are output to stdout/stderr. Configure logging level via `LOG_LEVEL` environment variable.

## Backup Strategy

1. **Database backups:**
   ```bash
   # Daily backup
   pg_dump -h localhost -U voteapp_user voteapp > backup_$(date +%Y%m%d).sql
   
   # Restore
   psql -h localhost -U voteapp_user voteapp < backup_20240101.sql
   ```

2. **Keep Firebase credentials secure** - Use secrets management (Vault, AWS Secrets Manager)

## Troubleshooting

### Database connection issues
```bash
# Check if PostgreSQL is running
docker-compose ps

# Check logs
docker-compose logs postgres

# Test connection
psql -h localhost -U voteapp_user -d voteapp
```

### Server startup failures
```bash
# Check logs
docker-compose logs server

# Verify environment variables
docker-compose exec server env

# Check Firebase credentials
docker-compose exec server cat /app/firebase-service-account.json
```

## Security Checklist

- [ ] Use strong database passwords
- [ ] Enable SSL/TLS for database connections
- [ ] Restrict firewall access to port 8080
- [ ] Use HTTPS in production (reverse proxy with nginx)
- [ ] Rotate Firebase service account keys regularly
- [ ] Enable database connection pooling
- [ ] Set up monitoring and alerting
- [ ] Configure automatic backups
