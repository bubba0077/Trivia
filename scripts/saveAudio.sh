#!/bin/sh -e

cd /mnt/c/Apache/htdocs/trivia/data/audio

STARTTIME='2020-04-18 13:00 EDT'

now=$(date +%s)
start=$(date -d "${STARTTIME}" +%s)
diff=$((${now} - ${start}))
hr=$((${diff} / 3600))
min=$(( (${diff} - ${hr} * 3600) / 60 ))
hr=$(($hr + 1))
HH=$(printf "%02d" $hr)
MM=$(printf "%02d" $min)

mkdir -p Hour_${HH}
ffmpeg -v quiet -i http://corn.kvsc.org:8000/broadband -acodec copy -t 330 Hour_${HH}/Hour_${HH}_${MM}m.mp3
