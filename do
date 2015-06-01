#!/bin/sh
GRADLE=`which gradle`
if [ -z "$GRADLE" ]
then
    GRADLE=./gradlew
fi
carton exec ./fetch-commits -r "$1" | "$GRADLE" -q run
