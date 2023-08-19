for abc_file in *.abc; do
  midi_file="${abc_file%.abc}"
  midi_file="${midi_file,,}"
  midi_file="${midi_file//[^a-z0-9_]/_}"
  echo "./common/src/main/resources/data/immersive_melodies/melodies/$midi_file"
  abc2midi "$abc_file" -o "../common/src/main/resources/data/immersive_melodies/melodies/$midi_file.midi"
done
