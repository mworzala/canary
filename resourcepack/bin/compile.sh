#!/bin/bash

# Move to `resourcepack` directory
cd $( dirname $0 )/..

# Create working directories
mkdir tmp
mkdir out

# Copy src folder
cp -r src/** tmp

# todo replace version str

# Compile resource pack
echo "Compiling resource pack..."
cd tmp
zip -r ../out/canary_helper.zip .
cd ..

# Create hash
echo "Creating sha1 hash..."
shasum -a 1 out/canary_helper.zip | \
  cut -d' ' -f1 | \
  xargs > out/canary_helper.sha1

# Cleanup
rm -rf tmp

echo "Done!"