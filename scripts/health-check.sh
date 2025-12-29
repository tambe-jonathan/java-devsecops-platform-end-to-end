#!/bin/bash
# Enterprise Smoke Test
URL=$1
MAX_RETRIES=5
COUNT=0

echo "Starting Smoke Test for $URL..."

while [ $COUNT -lt $MAX_RETRIES ]; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" $URL)
  if [ $STATUS -eq 200 ]; then
    echo "SUCCESS: Application is reachable and returning 200 OK."
    exit 0
  fi
  echo "Attempt $((COUNT+1)) failed. Retrying in 5s..."
  sleep 5
  COUNT=$((COUNT+1))
done

echo "FAILURE: Application health check failed after $MAX_RETRIES attempts."
exit 1
