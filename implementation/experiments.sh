#!/bin/bash

arg1="$1"
shift
arg2="$1"
shift


mkdir -p results
rm -rf results/*

mkdir -p results/environments
mkdir -p results/opaque
mkdir -p results/see_through

mkdir -p results/opaque/separate_with_advice
mkdir -p results/opaque/separate_without_advice
mkdir -p results/opaque/together_with_advice
mkdir -p results/opaque/together_without_advice

mkdir -p results/see_through/separate_with_advice
mkdir -p results/see_through/separate_without_advice
mkdir -p results/see_through/together_with_advice
mkdir -p results/see_through/together_without_advice


mkdir -p results/opaque/separate_with_advice/output
mkdir -p results/opaque/separate_without_advice/output
mkdir -p results/opaque/together_with_advice/output
mkdir -p results/opaque/together_without_advice/output

mkdir -p results/see_through/separate_with_advice/output
mkdir -p results/see_through/separate_without_advice/output
mkdir -p results/see_through/together_with_advice/output
mkdir -p results/see_through/together_without_advice/output


echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/see_through/separate_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/see_through/separate_without_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/see_through/together_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/see_through/together_without_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/opaque/separate_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/opaque/separate_without_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/opaque/together_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/opaque/together_without_advice/output.csv


echo "Starting simulations..."

for (( i=1; i <= $arg1; i++ ))
do
   dir=results/environments/env$i
   mkdir -p $dir
   dir=results/environments/env$i/temp
   echo "Generating random environment: $i"
   python3 ran_env_gen.py $dir --see-through
   echo "Starting planning on input file: $dir"
   for (( j=1; j <= $arg2; j++))
   do
      echo "    Repetition $j"
      echo "        Together without advice (see through)"
      java -jar planning.jar $dir --debug --no-advice --output-directory results/see_through/together_without_advice/ > results/see_through/together_without_advice/output/output_$i-$j.txt
      echo "        Together with advice (see through)"
      java -jar planning.jar $dir --debug --output-directory results/see_through/together_with_advice/ > results/see_through/together_with_advice/output/output_$i-$j.txt
      timeout=$(<temp/timeout.txt)
      echo "        Separate without advice (see through)"
      java -jar planning.jar $dir --debug --first-expl-then-plan --timeout $timeout --no-advice --output-directory results/see_through/separate_without_advice/ > results/see_through/separate_without_advice/output/output_$i-$j.txt
      echo "        Separate with advice (see through)"
      java -jar planning.jar $dir --debug --first-expl-then-plan --timeout $timeout --output-directory results/see_through/separate_with_advice/ > results/see_through/separate_with_advice/output/output_$i-$j.txt


      echo "        Together without advice (opaque)"
      java -jar planning.jar $dir --debug --no-advice --only-opaque-obstacles --output-directory results/opaque/together_without_advice/ > results/opaque/together_without_advice/output/output_$i-$j.txt
      echo "        Together with advice (opaque)"
      java -jar planning.jar $dir --debug --only-opaque-obstacles --output-directory results/opaque/together_with_advice/ > results/opaque/together_with_advice/output/output_$i-$j.txt
      timeout=$(<temp/timeout.txt)
      echo "        Separate without advice (opaque)"
      java -jar planning.jar $dir --debug --first-expl-then-plan --only-opaque-obstacles --timeout $timeout --no-advice --output-directory results/opaque/separate_without_advice/ > results/opaque/separate_without_advice/output/output_$i-$j.txt
      echo "        Separate with advice (opaque)"
      java -jar planning.jar $dir --debug --first-expl-then-plan --only-opaque-obstacles --timeout $timeout --output-directory results/opaque/separate_with_advice/ > results/opaque/separate_with_advice/output/output_$i-$j.txt
   done
done   


python3 compute_result.py results/see_through/separate_with_advice/output.csv > results/see_through/separate_with_advice/final_result.txt
python3 compute_result.py results/see_through/separate_without_advice/output.csv > results/see_through/separate_without_advice/final_result.txt
python3 compute_result.py results/see_through/together_with_advice/output.csv > results/see_through/together_with_advice/final_result.txt
python3 compute_result.py results/see_through/together_without_advice/output.csv > results/see_through/together_without_advice/final_result.txt

python3 compute_result.py results/opaque/separate_with_advice/output.csv > results/opaque/separate_with_advice/final_result.txt
python3 compute_result.py results/opaque/separate_without_advice/output.csv > results/opaque/separate_without_advice/final_result.txt
python3 compute_result.py results/opaque/together_with_advice/output.csv > results/opaque/together_with_advice/final_result.txt
python3 compute_result.py results/opaque/together_without_advice/output.csv > results/opaque/together_without_advice/final_result.txt


