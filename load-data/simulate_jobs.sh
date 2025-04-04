#!/bin/bash

for i in {1..100}; do
  curl -s http://localhost:8080/jobs/register-user
  curl -s http://localhost:8080/jobs/generate-report
  curl -s http://localhost:8080/jobs/process-payment
  curl -s http://localhost:8080/jobs/inventory-sync
  sleep 2  # pause between rounds
done
