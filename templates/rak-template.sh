#!/bin/sh

rakScripts="`dirname $0`"
libDir=$rakScripts"/../build/libs"

programName="rak.programs."$1

if [ "$#" -lt 1 ]
then
   echo "You must specify a program to run or -p to list all programs!."
   echo "Usage: rak program-name [program-args]"
   exit 1
fi

#below this line will be an inserted list of all rak programs
programs[0]="program1"


if [ "$1" = "-p" ];
then
    echo "known rak programs:"
    for program in "${programs[@]}"; do
        echo "    $program"
    done
    exit 0
fi

found=0
for program in "${programs[@]}"; do
    if [ "$program" = "$programName" ]
    then
        found=1
    fi
done

if [ $found -eq 1 ]
then
    #the classpath below will be substituted in the build file
    command="java -cp classpath rak.programs."$@
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