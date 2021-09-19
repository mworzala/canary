#!/bin/bash

# Move to `resourcepack` directory
cd $( dirname $0 )/..

# Create working directories
mkdir canary_helper
mkdir out

# Copy src folder
cp -r src/** canary_helper

# todo replace version str

# Compile resource pack
echo "Compiling resource pack..."
zip out/canary_helper.zip canary_helper

# Create hash
echo "Creating sha1 hash..."
shasum -a 1 out/canary_helper.zip | \
  cut -d' ' -f1 > out/canary_helper.sha1

# Cleanup
rm -rf canary_helper

echo "Done!"
