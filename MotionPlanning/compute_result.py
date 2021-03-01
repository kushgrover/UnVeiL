import pandas as pd
import math
import sys

filename = sys.argv[1]
csv_reader = pd.read_csv(filename, delimiter=',')

iterations = csv_reader['iterations']
sampled = csv_reader['sampled']
rrgsize = csv_reader['rrgsize']
movement = csv_reader['movement']
remaining = csv_reader['remaining']
total = csv_reader['total']
time = csv_reader['time']

iterations_avg = iterations.sum() / iterations.size
sampled_avg = sampled.sum() / sampled.size
rrgsize_avg = rrgsize.sum() / rrgsize.size
movement_avg = movement.sum() / movement.size
remaining_avg = remaining.sum() / remaining.size
total_avg = total.sum() / total.size
time_avg = time.sum() / time.size

iterations_temp = (iterations - iterations_avg) ** 2
sampled_temp = (sampled - sampled_avg) ** 2
rrgsize_temp = (rrgsize - rrgsize_avg) ** 2
movement_temp = (movement - movement_avg) ** 2
remaining_temp = (remaining - remaining_avg) ** 2
total_temp = (total - total_avg) ** 2
time_temp = (time - time_avg) ** 2

iterations_var = iterations_temp.sum() / iterations.size
sampled_var = sampled_temp.sum() / sampled.size
rrgsize_var = rrgsize_temp.sum() / rrgsize.size
movement_var = movement_temp.sum() / movement.size
remaining_var = remaining_temp.sum() / remaining.size
total_var = total_temp.sum() / total.size
time_var = time_temp.sum() / time.size

iterations_sd = math.sqrt(iterations_var)
sampled_sd = math.sqrt(sampled_var)
rrgsize_sd = math.sqrt(rrgsize_var)
movement_sd = math.sqrt(movement_var)
remaining_sd = math.sqrt(remaining_var)
total_sd = math.sqrt(total_var)
time_sd = math.sqrt(time_var)


print("\nComputed over " + str(time.size) + " run(s):")
print("-------------------------------------------------------------------")
print("                     |           Mean           |        SD        ")
print("-------------------------------------------------------------------")
print("Number of iterations | " + str(iterations_avg) + "\t\t\t|" + str(iterations_sd))
print("Total sampled points | " + str(sampled_avg) + "\t\t|" + str(sampled_sd))
print("RRG size             | " + str(rrgsize_avg) + "\t\t\t|" + str(rrgsize_sd))
print("Movement length      | " + str(movement_avg) + "\t\t|" + str(movement_sd))
print("Remaining length     | " + str(remaining_avg) + "\t\t|" + str(remaining_sd))
print("Total length         | " + str(total_avg) + "\t\t|" + str(total_sd))
print("Time taken (in ms)   | " + str(time_avg) + "\t\t|" + str(time_sd))
print("-------------------------------------------------------------------")