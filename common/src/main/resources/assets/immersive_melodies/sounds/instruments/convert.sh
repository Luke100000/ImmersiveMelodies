#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: [input]"
    exit 1
fi

input_file="$1"

base_octave=4

if [ ! -f "$input_file" ]; then
    echo "Input file not found: $input_file"
    exit 1
fi

if ! command -v sox &>/dev/null; then
    echo "Please install 'sox' to use this script."
    exit 1
fi

input_filename_without_ext=$(basename -- "$input_file")
input_filename_without_ext="${input_filename_without_ext%.*}"

mkdir "${input_filename_without_ext}"

for (( octave = 1; octave <= 8; octave++ )); do
    output_file="${input_filename_without_ext}/c${octave}.ogg"
    pitch_adjustment=$(((octave - base_octave) * 1200))
    
    sox "$input_file" "$output_file" pitch "$pitch_adjustment"
    
    if [ $? -eq 0 ]; then
        echo "Generated $output_file"
    else
        echo "Error generating $output_file"
    fi
done

echo "Octave copies generated successfully."
