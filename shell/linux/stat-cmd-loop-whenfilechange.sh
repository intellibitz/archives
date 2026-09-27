#!/bin/bash
USAGE="usage:$0 -s source -c cmd [-t sleep]"
while getopts s:c:t: flag; do
  case "${flag}" in
  s) src=${OPTARG} ;;
  c) cmd=${OPTARG} ;;
  t) dur=${OPTARG} ;;
  *) echo "${USAGE}" ;;
  esac
done
if [ -z "${src}" ]; then
  echo "${USAGE}"
  exit 1
fi
if [ -z "${cmd}" ]; then
  cmd="ls"
fi
if [ -z "${dur}" ]; then
  dur=5
fi
# runs the cmd, when the source is modified
while true; do
  ATIME=$(stat -c %Z "${src}")

  if [[ "$ATIME" != "$LTIME" ]]; then
    sh -c "${cmd}"
    LTIME=$ATIME
  fi
  sleep ${dur}
done
