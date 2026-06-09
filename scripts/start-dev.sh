#!/bin/bash
# Start development environment (infrastructure + backend services)
set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "=== Starting infrastructure (PostgreSQL, Redis, MinIO, Nacos, RocketMQ) ==="
cd "$PROJECT_DIR/infrastructure/docker"
docker compose up -d postgres redis minio nacos rocketmq-namesrv rocketmq-broker rocketmq-dashboard
echo "Waiting for services to be healthy..."
sleep 10

echo "=== Starting backend services ==="
cd "$PROJECT_DIR/backend"
tmux new-session -d -s zhangyuan -n api './gradlew :api:bootRun' \; \
  new-window -n auth './gradlew :auth-service:bootRun' \; \
  new-window -n user './gradlew :user-service:bootRun' \; \
  new-window -n order './gradlew :order-service:bootRun' \; \
  new-window -n payment './gradlew :payment-service:bootRun' \; \
  new-window -n system './gradlew :system-service:bootRun' \; \
  new-window -n gateway './gradlew :gateway:bootRun' \; \
  new-window -n ai './gradlew :ai-service:bootRun'

echo "=== Starting frontends (in background) ==="
cd "$PROJECT_DIR/frontend/admin" && npm run dev &
cd "$PROJECT_DIR/frontend/web" && npm run dev &

echo "=== All services starting ==="
echo "API:        http://localhost:8088"
echo "Gateway:    http://localhost:8080"
echo "Auth:       http://localhost:8082"
echo "Order:      http://localhost:8083"
echo "Payment:    http://localhost:8084"
echo "User:       http://localhost:8085"
echo "AI:         http://localhost:8086"
echo "System:     http://localhost:8081"
echo "Admin UI:   http://localhost:5173"
echo "Web UI:     http://localhost:3000"
echo "Nacos:      http://localhost:8848/nacos"
echo "RocketMQ:   http://localhost:8086"
echo ""
echo "Use 'tmux attach -t zhangyuan' to view backend logs"
