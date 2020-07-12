#!/bin/sh

# Git push is implemented in the script to make sure we are not leaking GH key to the output
# Expects:
# - tag name as a paramter of this script

TAG_NAME=$1

echo "Running git push"
git push https://@github.com/shipkit/org.shipkit.shipkit-auto-version.git $TAG_NAME
EXIT_CODE=$?
echo "'git push --quiet' exit code: $EXIT_CODE"
exit $EXIT_CODE
