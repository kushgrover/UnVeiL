import sys
import pandas as pd
import numpy as np
import matplotlib
from matplotlib import pyplot as plt
from matplotlib.animation import FuncAnimation
import csv
import math
import datetime

total_time=0

def set_axes():
    # plt.figure(figsize=(13.2,6.6))
    plt.figure(figsize=(19.8,10))
    plt.gca().set_aspect('equal', 'datalim')
    plt.xlim(0,6)
    plt.ylim(0,3)
    plt.xticks([0, 1, 2, 3, 4, 5, 6])
    plt.yticks([0, 1, 2, 3])
    plt.tick_params(axis = 'both', labelsize = '20')

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


# Animation function to show robot moving
def animate(x1,y1,x2,y2,col,circle,small_circle,delay):
    x_data = []
    y_data = []
    slope = (y2-y1)/(x2-x1)
    ax = plt.gca()
    line, = ax.plot(x1, y1, marker = 'o', markersize = 3, color = col)
    distance = math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))

    def init():
        circle.center = (5, 5)
        small_circle.center = (5, 5)
        ax.add_patch(circle)
        ax.add_patch(small_circle)
        return circle, small_circle

    def animation_frame(i):
        if(i<=distance):
            x = x1 + (x2-x1) * i/distance
            y = y1 + slope * (x - x1)
            x_data.append(x)
            y_data.append(y)
            line.set_xdata(x_data)
            line.set_ydata(y_data)
            line.set_linewidth(5)
            circle.center = (x, y)
            small_circle.center = (x, y)
            return line, circle, small_circle
        else:
            return

    animation = FuncAnimation(plt.gcf(), func=animation_frame, frames=np.arange(0,distance+delay,0.01), interval=1)
    return animation

# Animation function to show robot moving
def animate(x1,y1,x2,y2,col,circle,small_circle,delay):
    x_data = []
    y_data = []
    if(x2 != x1):
        slope = (y2-y1)/(x2-x1)
    else:
        slope = 0
    ax = plt.gca()
    line, = ax.plot(x1, y1, marker = 'o', markersize = 3, color = col)
    distance = math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1))

    def init():
        circle.center = (5, 5)
        small_circle.center = (5, 5)
        ax.add_patch(circle)
        ax.add_patch(small_circle)
        return circle, small_circle

    def animation_frame(i):
        if(i<=distance):
            if(x1 != x2):
                x = x1 + (x2-x1) * i/distance
                y = y1 + slope * (x - x1)
            else:
                x = x1
                y = y1 + (y2-y1) * i/distance
            x_data.append(x)
            y_data.append(y)
            line.set_xdata(x_data)
            line.set_ydata(y_data)
            line.set_linewidth(5)
            circle.center = (x, y)
            small_circle.center = (x, y)
            return line, circle, small_circle
        else:
            return

    animation = FuncAnimation(plt.gcf(), func=animation_frame, frames=np.arange(0,distance+delay,0.03), interval=1)
    return animation

# main function to plot the movement
def plot_movement(filename, col, thickness, dir):
    csv_reader = pd.read_csv(filename, delimiter=',')
    X1 = csv_reader['x1']
    X2 = csv_reader['x2']
    Y1 = csv_reader['y1']
    Y2 = csv_reader['y2']

    circle = plt.Circle((X1[0], Y1[0]), 1.0, color = "orange", fill = False, linewidth = 5, zorder = 100000000)
    # small_circle_dest = plt.Circle((X2[X2.size-1], Y2[Y2.size-1]), 0.02, color = "red", linewidth = 5, zorder = 100000000)
    small_circle = plt.Circle((X1[0], Y1[0]), 0.02, color = "orange", linewidth = 5, zorder = 100000000)
    plt.gca().add_patch(circle)
    # plt.gca().add_patch(small_circle_dest)
    plt.gca().add_patch(small_circle)

    Writer = matplotlib.animation.writers['ffmpeg']
    writer = Writer(fps=15, metadata=dict(artist='Me'), bitrate=1800)
    
    for k in range(0, X1.size):
        x1 = X1[k]
        x2 = X2[k]
        y1 = Y1[k]
        y2 = Y2[k]
        if(k == X1.size-1):
            animation = animate(x1,y1,x2,y2,col,circle,small_circle,0.2)
        else:
            animation = animate(x1,y1,x2,y2,col,circle,small_circle,0)

        print("Saving to " + dir + str(k) + ".mp4...")
        start_time = datetime.datetime.now()
        
        animation.save(dir + str(k) + ".mp4", writer = writer)
        
        end_time = datetime.datetime.now()
        time_diff = (end_time - start_time)
        execution_time = time_diff.total_seconds()
        print("done")
        print("Time spent (s): ", execution_time)
        global total_time
        total_time += execution_time
    
    # plt.show()

def plot_last_point(filename, col):
    csv_reader = pd.read_csv(filename, delimiter = ',')
    x2 = csv_reader['x2']
    y2 = csv_reader['y2']
    plt.plot(x2[x2.size-1], y2[y2.size-1], marker = 'o', markersize = 10, color = col)



dir_output = sys.argv[1]
dir_separate = sys.argv[1] + "video_data/"

# print("Plotting movement")
# set_axes()
# plot_file(dir_output + "/obstacles.csv", "black", False, 1)
# plot_file(dir_output + "/labels.csv", "black", True, 0)
# plot_movement(dir_separate + "1-movement.csv", "#33e026", 2, dir_separate + "videos/")
# hou = int(total_time/3600)
# min = int((total_time%3600)/60)
# sec = int((total_time%3600)%60)
# print("Total Time spent: ", hou, ":", min, ":", sec, "\n\n")


# print("Plotting RRG...")
# set_axes()
# plot_file(dir_output + "/obstacles.csv", "black", False, 1)
# plot_file(dir_output + "/labels.csv", "black", True, 0)
# plot_file(dir_separate + "1-movement.csv", "#33e026", False, 2)
# plot_last_point(dir_separate + "1-movement.csv", "orange")

# for i in range(1,5):
#     plot_file(dir_separate + str(i) + ".csv", "#20168a", False, 0)
#     plt.savefig(dir_separate + str(i) + ".png", format = 'png', dpi = 300, bbox_inches='tight')
# print("done")


print("Plotting remaining path")
set_axes()
plot_file(dir_output + "/obstacles.csv", "black", False, 1)
plot_file(dir_output + "/labels.csv", "black", True, 0)
plot_file(dir_separate + "1-movement.csv", "#33e026", False, 2)
for i in range(1,5):
    plot_file(dir_separate + str(i) + ".csv", "#20168a", False, 0)

plot_movement(dir_output + "end-finalpath.csv", "red", 2, dir_separate + "finalPath/")
print("done")





