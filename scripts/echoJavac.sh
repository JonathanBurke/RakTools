#!/bin/sh
echo "" >> /Users/jburke/Documents/tmp/all.args 
echo "---" >> /Users/jburke/Documents/tmp/all.args 
echo $* >> /Users/jburke/Documents/tmp/all.args 
cat ${1:1} >> /Users/jburke/Documents/tmp/all.args
echo "---" >> /Users/jburke/Documents/tmp/all.args 
echo "" >> /Users/jburke/Documents/tmp/all.args 
javac $*