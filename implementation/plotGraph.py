import sys
import pandas as pd
import matplotlib
from matplotlib import pyplot as plt
import csv


def plot_file(filename, col, dashed, thickness):
    csv_reader = pd.read_csv(filename, delimiter=',')
    x1 = csv_reader['x1']
    x2 = csv_reader['x2']
    y1 = csv_reader['y1']
    y2 = csv_reader['y2']

    if(dashed):
        plt.plot((x1, x2), (y1, y2), color = col, linestyle = 'dashed')
    elif(thickness == 1):
        plt.plot((x1, x2), (y1, y2), color = col, linewidth = 3)
    elif(thickness == 2):
        plt.plot((x1, x2), (y1, y2), color = col, linewidth = 5)
    else: 
        plt.plot((x1, x2), (y1, y2), color = col)

def plot_last_point(filename, col):
    csv_reader = pd.read_csv(filename, delimiter = ',')
    x2 = csv_reader['x2']
    y2 = csv_reader['y2']
    plt.plot(x2[x2.size-1], y2[y2.size-1], marker = 'o', markersize = 15, color = col)

def plot_first_point(filename, col):
    csv_reader = pd.read_csv(filename, delimiter = ',')
    x1 = csv_reader['x1']
    y1 = csv_reader['y1']
    plt.plot(x1[0], y1[0], marker = 'o', markersize = 15, color = col)

def set_axes():
    plt.figure(figsize=(19.8,10.0))
    plt.gca().set_aspect('equal', 'datalim')
    plt.xlim(0,6)
    plt.ylim(0,3)
    plt.xticks([0, 1, 2, 3, 4, 5, 6])
    plt.yticks([0, 1, 2, 3])
    plt.tick_params(axis = 'both', labelsize = '20')



dir_together = sys.argv[1] + "together/"

set_axes()
plot_file(dir_together + "firstMove.csv", "#20168a", False, 0)
plot_file(dir_together + "obstacles.csv", "black", False, 1)
plot_file(dir_together + "labels.csv", "black", True, 0)
circle = plt.Circle((0.1,1.5), 1.0, color = "orange", fill = False, linewidth = 5)
plt.gca().add_artist(circle)
plt.plot([0.1], [1.5], marker = 'o', markersize = 15, color = "#33e026", )
plt.savefig(dir_together + "tog_firstMove.png", format = 'png', dpi = 300, bbox_inches='tight')


set_axes()
plot_file(dir_together + "bin.csv", "#20168a", False, 0)
plot_file(dir_together + "bin-movement.csv", "#33e026", False, 2)
plot_file(dir_together + "obstacles.csv", "black", False, 1)
plot_file(dir_together + "labels.csv", "black", True, 0)
plot_last_point(dir_together + "bin-movement.csv", "#33e026")
plt.savefig(dir_together + "tog_bin.png", format = 'png', dpi = 300, bbox_inches='tight')


set_axes()
plot_file(dir_together + "room.csv", "#20168a", False, 0)
plot_file(dir_together + "room-movement.csv", "#33e026", False, 2)
plot_file(dir_together + "obstacles.csv", "black", False, 1)
plot_file(dir_together + "labels.csv", "black", True, 0)
plot_last_point(dir_together + "room-movement.csv", "#33e026")
plt.savefig(dir_together + "tog_room.png", format = 'png', dpi = 300, bbox_inches='tight')


set_axes()
plot_file(dir_together + "end.csv", "#20168a", False, 0)
plot_file(dir_together + "end-movement.csv", "#33e026", False, 2)
plot_file(dir_together + "end-finalpath.csv", "red", False, 2)
plot_file(dir_together + "obstacles.csv", "black", False, 1)
plot_file(dir_together + "labels.csv", "black", True, 0)
plot_last_point(dir_together + "end-movement.csv", "#33e026")
plot_last_point(dir_together + "end-finalpath.csv", "red")
plt.savefig(dir_together + "tog_final.png", format = 'png', dpi = 300, bbox_inches='tight')





dir_separate = sys.argv[1] + "separate/"

set_axes()
# plot_file(dir_separate+"bin.csv", "#20168a", False, False)
plot_file(dir_separate + "bin-movement.csv", "#33e026", False, 2)
plot_file(dir_separate + "obstacles.csv", "black", False, 1)
plot_file(dir_separate + "labels.csv", "black", True, 0)
plot_last_point(dir_separate + "bin-movement.csv", "#33e026")
plt.savefig(dir_separate + "sep_bin.png", format = 'png', dpi = 300, bbox_inches='tight')


set_axes()
plot_file(dir_separate + "room.csv", "#20168a", False, 0)
plot_file(dir_separate + "room-movement.csv", "#33e026", False, 2)
plot_file(dir_separate + "obstacles.csv", "black", False, 1)
plot_file(dir_separate + "labels.csv", "black", True, 0)
plot_last_point(dir_separate + "room-movement.csv", "#33e026")
plt.savefig(dir_separate + "sep_room.png", format = 'png', dpi = 300, bbox_inches='tight')


set_axes()
plot_file(dir_separate + "end.csv", "#20168a", False, 0)
# plot_file(dir_separate + "end-movement.csv", "#33e026", False, True)
plot_file(dir_separate + "end-finalpath.csv", "red", False, 2)
plot_file(dir_separate + "obstacles.csv", "black", False, 1)
plot_file(dir_separate + "labels.csv", "black", True, 0)
plot_first_point(dir_separate + "end-finalpath.csv", "#33e026")
plt.savefig(dir_separate + "sep_final.png", format = 'png', dpi = 300, bbox_inches='tight')
