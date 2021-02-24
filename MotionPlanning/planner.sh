#!/bin/bash

rm temp/output/*
echo "iterations,sampled,rrgsize,movement,remaining,time," > temp/output/output.csv

arg1="$1"
shift

for (( i=1; i <= $arg1; i++ ))
do
   echo "Current run: $i"
   java -jar planning.jar "$@" > temp/output/output$i.txt
done

python3 temp/avg.py 
