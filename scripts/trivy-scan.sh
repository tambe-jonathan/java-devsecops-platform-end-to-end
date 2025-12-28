#!/bin/bash
IMAGE_NAME=$1
echo "Scanning Image: $IMAGE_NAME"
trivy image --severity HIGH,CRITICAL --format table $IMAGE_NAME
