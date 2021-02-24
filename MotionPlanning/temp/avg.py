import pandas as pd

filename = "temp/output/output.csv"
csv_reader = pd.read_csv(filename, delimiter=',')

iterations = csv_reader['iterations']
sampled = csv_reader['sampled']
rrgsize = csv_reader['rrgsize']
movement = csv_reader['movement']
remaining = csv_reader['remaining']
time = csv_reader['time']

iterations_avg = iterations.sum() / iterations.size
sampled_avg = sampled.sum() / sampled.size
rrgsize_avg = rrgsize.sum() / rrgsize.size
movement_avg = movement.sum() / movement.size
remaining_avg = remaining.sum() / remaining.size
time_avg = time.sum() / time.size

print("\n======= Averages computed over " + str(time.size) + " run(s) =======")
print("Number of iterations | " + str(iterations_avg))
print("Total sampled points | " + str(sampled_avg))
print("RRG size             | " + str(rrgsize_avg))
print("Movement length      | " + str(movement_avg))
print("Remaining length     | " + str(remaining_avg))
print("Time taken (in ms)   | " + str(time_avg))
print("===============================================")
