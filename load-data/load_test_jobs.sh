#!/bin/bash

# Number of requests per round
REQUESTS=100
# Concurrent users per endpoint
CONCURRENCY=10
# Delay between each round in seconds
DELAY=5

# Target base URL
BASE_URL="http://localhost:8080/jobs"

echo "Starting sustained load on all job endpoints..."

while true; do
  echo "-------------------------------------------"
  echo "Starting new round at $(date)"

  hey -n $REQUESTS -c $CONCURRENCY $BASE_URL/register-user &
  hey -n $REQUESTS -c $CONCURRENCY $BASE_URL/generate-report &
  hey -n $REQUESTS -c $CONCURRENCY $BASE_URL/process-payment &
  hey -n $REQUESTS -c $CONCURRENCY $BASE_URL/inventory-sync &
  hey -n $REQUESTS -c $CONCURRENCY $BASE_URL/execute-dummy-job &

  wait
  echo "Round completed. Sleeping for $DELAY seconds..."
  sleep $DELAY
done
