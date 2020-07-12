#!/bin/sh

# Git push is implemented in the script to make sure we are not leaking GH key to the output
# Expects:
# - tag name as a parameter of this script

set -e
TAG_NAME=$1

echo "Running git push"
git push origin $TAG_NAME