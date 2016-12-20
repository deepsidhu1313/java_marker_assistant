#!/bin/bash 

cd "${1}"
if [ ${2: -4} == ".zip" ];
then
unzip ${2} -d ${3} && rm ${2}
elif [ ${2: -4} == ".rar" ]; then
file-roller ${2} -e ${3}  --force && sleep 10 && rm ${2}
fi