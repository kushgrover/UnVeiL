#!/bin/bash

arg1="$1"
shift

mkdir -p results
rm -rf results/*

# mkdir -p results/separate_with_advice
# mkdir -p results/separate_without_advice
# mkdir -p results/together_with_advice
# mkdir -p results/together_without_advice

# mkdir -p results/separate_with_advice/output
# mkdir -p results/separate_without_advice/output
# mkdir -p results/together_with_advice/output
# mkdir -p results/together_without_advice/output

# touch results/separate_with_advice/output.csv
# touch results/separate_without_advice/output.csv
# touch results/together_with_advice/output.csv
# touch results/together_without_advice/output.csv

# echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/separate_with_advice/output.csv
# echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/separate_without_advice/output.csv
# echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/together_with_advice/output.csv
# echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/together_without_advice/output.csv


# echo "Separate with advice:"
# for (( i=1; i <= $arg1; i++ ))
# do
#    echo "Current run: $i"
#    java -jar planning.jar Examples/6rooms/example --property-file myAut.hoa --only-opaque-obstacles --first-expl-then-plan --output-directory results/separate_with_advice/ > results/separate_with_advice/output/output$i.txt
# done
# echo "Output written to results/separate_with_advice/final_result.txt"
# echo " "
# python3 avg.py results/separate_with_advice/output.csv > results/separate_with_advice/final_result.txt



# echo "Separate without advice:"
# for (( i=1; i <= $arg1; i++ ))
# do
#    echo "Current run: $i"
#    java -jar planning.jar Examples/6rooms/example --property-file myAut.hoa --only-opaque-obstacles --first-expl-then-plan --no-advice --output-directory results/separate_without_advice/ > results/separate_without_advice/output/output$i.txt
# done
# echo "Output written to results/separate_without_advice/final_result.txt"
# echo " "
# python3 avg.py results/separate_without_advice/output.csv > results/separate_without_advice/final_result.txt



# echo "Together with advice:"
# for (( i=1; i <= $arg1; i++ ))
# do
#    echo "Current run: $i"
#    java -jar planning.jar Examples/6rooms/example --property-file myAut.hoa --only-opaque-obstacles --output-directory results/together_with_advice/ > results/together_with_advice/output/output$i.txt
# done
# echo "Output written to results/together_with_advice/final_result.txt"
# echo " "
# python3 avg.py results/together_with_advice/output.csv > results/together_with_advice/final_result.txt



# echo "Together without advice:"
# for (( i=1; i <= $arg1; i++ ))
# do
#    echo "Current run: $i"
#    java -jar planning.jar Examples/6rooms/example --property-file myAut.hoa --only-opaque-obstacles --no-advice --output-directory results/together_without_advice/ > results/together_without_advice/output/output$i.txt
# done
# echo "Output written to results/together_without_advice/final_result.txt"
# python3 avg.py results/together_without_advice/output.csv > results/together_without_advice/final_result.txt






mkdir -p results/random
mkdir -p results/random/separate_with_advice
mkdir -p results/random/separate_without_advice
mkdir -p results/random/together_with_advice
mkdir -p results/random/together_without_advice

mkdir -p results/random/separate_with_advice/output
mkdir -p results/random/separate_without_advice/output
mkdir -p results/random/together_with_advice/output
mkdir -p results/random/together_without_advice/output

touch results/random/separate_with_advice/output.csv
touch results/random/separate_without_advice/output.csv
touch results/random/together_with_advice/output.csv
touch results/random/together_without_advice/output.csv

echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/random/separate_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/random/separate_without_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/random/together_with_advice/output.csv
echo "iterations,sampled,rrgsize,movement,remaining,total,time," > results/random/together_without_advice/output.csv

echo "Separate with advice for random environents:"
for (( i=1; i <= $arg1; i++ ))
do
   echo "Current run: $i"
   java -jar planning.jar --random-env --property-file myAut.hoa --only-opaque-obstacles --first-expl-then-plan --output-directory results/random/separate_with_advice/ > results/random/separate_with_advice/output/output$i.txt
done
echo "Output written to results/random/separate_with_advice/final_result.txt"
echo " "
python3 avg.py results/random/separate_with_advice/output.csv > results/random/separate_with_advice/final_result.txt



echo "Separate without advice for random environments:"
for (( i=1; i <= $arg1; i++ ))
do
   echo "Current run: $i"
   java -jar planning.jar --random-env --property-file myAut.hoa --only-opaque-obstacles --first-expl-then-plan --no-advice --output-directory results/random/separate_without_advice/ > results/random/separate_without_advice/output/output$i.txt
done
echo "Output written to results/random/separate_without_advice/final_result.txt"
echo " "
python3 avg.py results/random/separate_without_advice/output.csv > results/random/separate_without_advice/final_result.txt



echo "Together with advice for random environments:"
for (( i=1; i <= $arg1; i++ ))
do
   echo "Current run: $i"
   java -jar planning.jar --random-env --property-file myAut.hoa --only-opaque-obstacles --output-directory results/random/together_with_advice/ > results/random/together_with_advice/output/output$i.txt
done
echo "Output written to results/random/together_with_advice/final_result.txt"
echo " "
python3 avg.py results/random/together_with_advice/output.csv > results/random/together_with_advice/final_result.txt



echo "Together without advice for random environments:"
for (( i=1; i <= $arg1; i++ ))
do
   echo "Current run: $i"
   java -jar planning.jar --random-env --property-file myAut.hoa --only-opaque-obstacles --no-advice --output-directory results/random/together_without_advice/ > results/random/together_without_advice/output/output$i.txt
done
echo "Output written to results/random/together_without_advice/final_result.txt"
python3 avg.py results/random/together_without_advice/output.csv > results/random/together_without_advice/final_result.txt

