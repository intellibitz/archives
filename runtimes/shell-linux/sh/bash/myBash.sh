#!/usr/bin/env sh
# https://explainshell.com/explain?cmd=set+-e

set -x
# -x     The  shell shall write to standard error a trace for each command after it expands the command and
#        before it executes it. It is unspecified whether the command that turns tracing off is traced.

set -e
# -e     When this option is on, if a simple command fails for any of the reasons listed in Consequences of
#        Shell  Errors or returns an exit status value >0, and is not part of the compound list following a
#        while, until, or if keyword, and is not a part of an AND  or  OR  list,  and  is  not  a  pipeline
#        preceded by the ! reserved word, then the shell shall immediately exit.

