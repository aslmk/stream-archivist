#!/bin/bash

DIR="/app/data/recordings"

if [ ! -d "$DIR" ]; then
  mkdir -p "$DIR"
fi

OWNER=$(stat -c '%U' "$DIR")

if [ "$OWNER" = "root" ]; then
  chown -R recording-user:recording-group "$DIR"
fi

exec su-exec recording-user java -jar app.jar