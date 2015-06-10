#!/bin/sh

programName="rak.programs."$1

if [ "$#" -lt 1 ]
then
   echo "You must specify a program to run!."
   echo "Usage: rak program-name [program-args]"
   exit 1
fi

#below this line will be an inserted list of all rak programs
programs[0]="program1"

found=0
for program in "${programs[@]}"; do
    if [ "$program" = "$programName" ]
    then
        found=1
    fi
done

if [ $found -eq 1 ]
then
    command="java -cp $SCALA_HOME/lib/scala-library.jar:build/libs/RakTools.jar rak.programs."$@
    echo "Running: "$command
    eval $command
else
   echo "Could not find program: "$programName
   echo "known programs:"
   for program in "${programs[@]}"; do
    echo "    "$program
   done
   exit 1
fi

exit 0