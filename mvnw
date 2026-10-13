#!/bin/sh
# ----------------------------------------------------------------------------
# Maven Wrapper script for UNIX
# ----------------------------------------------------------------------------

PRG="$0"
while [ -h "$PRG" ]; do
  ls=`ls -ld "$PRG"`
  link=`expr "$ls" : '.*-> \(.*\)$'`
  if expr "$link" : '/.*' > /dev/null; then
    PRG="$link"
  else
    PRG=`dirname "$PRG"`/"$link"
  fi
done
BASE_DIR=`dirname "$PRG"`

if [ -f "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" ]; then
  exec java -jar "$BASE_DIR/.mvn/wrapper/maven-wrapper.jar" "$@"
else
  exec mvn "$@"
fi
