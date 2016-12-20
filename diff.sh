#!/bin/bash 

cd "${1}"
diff -u -B -b ${2} ${3}