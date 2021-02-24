import numpy as np
import networkx as nx

import matplotlib
# matplotlib.use('TkAgg')
from matplotlib import pyplot as plt
import csv

plt.figure()
plt.gca().set_aspect('equal', 'datalim')
# axs.set_xlim((0, 6))
# axs.set_ylim((0, 3))
plt.xlim(-1,7)
plt.ylim(-1,4)

import pandas as pd

def plotfile(filename, col, dashed, thick):
    csv_reader = pd.read_csv(filename, delimiter=',')
    
    x1 = csv_reader['x1']
    x2 = csv_reader['x2']
    y1 = csv_reader['y1']
    y2 = csv_reader['y2']
    # plt.plot((x1, x2), (y1, y2), color = col)
        

    # for i in range(len(x1)):
    if(dashed):
        plt.plot((x1, x2), (y1, y2), color = col, linestyle = 'dashed')
    elif(thick):
        plt.plot((x1, x2), (y1, y2), color = col, linewidth = 3)
    else: 
        plt.plot((x1, x2), (y1, y2), color = col)



    # with open(filename) as csv_file:
        # line_count = 0
        # for row in csv_reader:
        #     if line_count == 0:
        #         line_count += 1
    # with open(filename) as csv_file:
        #     else:
        #         # x = [float(row[0]), float(row[2])]
        #         # y = [float(row[1]), float(row[3])]
        #         if(dashed):
        #             plt.plot(x, y, color = col, linestyle = 'dashed')
        #         elif(thick):
        #             plt.plot(x, y, color = col, linewidth = 2)
        #         else: 
        #             plt.plot(x, y, color = col)
        #         line_count += 1

plotfile("firstMove.csv", "#20168a", False, False)
plotfile("obstacles.csv", "red", False, True)
plotfile("labels.csv", "black", True, False)
circle = plt.Circle((0.1,1.5), 1.0, color = "orange", fill = False, linewidth = 2)
plt.plot([0.1], [1.5], marker='o', markersize=7, color="#33e026", )
plt.gca().add_artist(circle)
plt.show()

plotfile("bin.csv", "#20168a", False, False)
plotfile("bin-movement.csv", "#33e026", False, True)
plotfile("obstacles.csv", "red", False, True)
plotfile("labels.csv", "black", True, False)
plt.show()

# plotfile("room.csv", "#20168a", False, False)
# plotfile("room-movement.csv", "#33e026", False, True)
# plotfile("obstacles.csv", "red", False, True)
# plotfile("labels.csv", "black", True, False)
# plt.show()

plotfile("end.csv", "#20168a", False, False)
plotfile("end-movement.csv", "#33e026", False, True)
# plotfile("end-finalpath.csv", "orange", False, True)
plotfile("obstacles.csv", "red", False, True)
plotfile("labels.csv", "black", True, False)
plt.show()

# plotfile("known.csv", "#20168a", False, False)
# plotfile("known-movement.csv", "#33e026", False, True)
# # plotfile("end-finalpath.csv", "orange", False, True)
# plotfile("obstacles.csv", "red", False, True)
# plotfile("labels.csv", "black", True, False)
# plt.show()
