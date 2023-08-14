#!/bin/bash

for file in *.ogg; do
    if [ -f "$file" ]; then
        echo "Processing file: $file"
        ./convert.sh "$file"
    fi
done
