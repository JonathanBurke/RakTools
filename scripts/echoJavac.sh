#!/bin/sh

if [ "$INF_ARGS" = "" ];
then
    echo "You must set the INF_ARGS environment variable to a file you wish the arguments to be dumped into."
    exit 1
fi

echo "" >> $INF_ARGS
echo "---" >> $INF_ARGS
echo $* >> $INF_ARGS
cat ${1:1} >> $INF_ARGS
echo "---" >> $INF_ARGS
echo "" >> $INF_ARGS
javac $*