
import sys
import random

random_gen = random.SystemRandom()
file_name = sys.argv[1]

# Property
f_prop = open(file_name + ".pr", "w")
f_prop.write("F (r1 & b) & F (r2 & b) & F (r3 & b) & F (r4 & b) & F (r5 & b) & F (r6 & b)")
f_prop.close()


# Environment
f_env = open(file_name + ".env", "w")
f_label = open(file_name + ".lb", "w")

f_env.write("[(0,0),(6,0),(6,3),(0,3)]\n")
f_env.write("(0.1,1.5)\n")
f_env.write("obstacles\n")
f_env.write("[(0,0.95), (0.8,0.95), (0.8,1.05), (0,1.05)]\n")
f_env.write("[(1.2,0.95), (2,0.95), (2,1.05), (1.2,1.05)]\n")
f_env.write("[(0,1.95), (0.8,1.95), (0.8,2.05), (0,2.05)]\n")
f_env.write("[(1.2,1.95), (2,1.95), (2,2.05), (1.2,2.05)]\n")
f_env.write("[(2,0.95), (2.8,0.95), (2.8,1.05), (2,1.05)]\n")
f_env.write("[(3.2,0.95), (4,0.95), (4,1.05), (3.2,1.05)]\n")
f_env.write("[(2,1.95), (2.8,1.95), (2.8,2.05), (2,2.05)]\n")
f_env.write("[(3.2,1.95), (4,1.95), (4,2.05), (3.2,2.05)]\n")
f_env.write("[(4,0.95), (4.8,0.95), (4.8,1.05), (4,1.05)]\n")
f_env.write("[(5.2,0.95), (6,0.95), (6,1.05), (5.2,1.05)]\n")
f_env.write("[(4,1.95), (4.8,1.95), (4.8,2.05), (4,2.05)]\n")
f_env.write("[(5.2,1.95), (6,1.95), (6,2.05), (5.2,2.05)]\n")
f_env.write("[(1.95,0), (2.05,0), (2.05,0.95), (1.95,0.95)]\n")
f_env.write("[(1.95,2.05), (2.05,2.05), (2.05,3), (1.95,3)]\n")
f_env.write("[(3.95,0), (4.05,0), (4.05,0.95), (3.95,0.95)]\n")
f_env.write("[(3.95,2.05), (4.05,2.05), (4.05,3), (3.95,3)]\n")

# Tables
try:
    if(sys.argv[2] == "--see-through"):
        f_env.write("see through obstacles\n")
except IndexError:
    pass

l_bin = 0.2
w_bin = 0.2

def contains(x, y, l, w, x_bin, y_bin):
    if(x < x_bin and x_bin < x+l):
        if(y < y_bin and y_bin < y+w):
            return True
    return False

def collisionFree(x, y, l, w, x_bin, y_bin):
    if(contains(x, y, l, w, x_bin, y_bin)):
        return False    
    if(contains(x, y, l, w, x_bin + l_bin, y_bin)):
        return False
    if(contains(x, y, l, w, x_bin + l_bin, y_bin + w_bin)):
        return False
    if(contains(x, y, l, w, x_bin, y_bin + w_bin)):
        return False
    return True

def clamp(x):
    if(x<0):
        return 0
    return x


# Table 1
if(random_gen.random() < 0.5): # set orientation
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(0, 1.95 - l)
y = random_gen.uniform(0, 0.55 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = ("[(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")


# Bin 1
x_bin = random_gen.uniform(0, 1.95 - l)
y_bin = random_gen.uniform(0, 0.55 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(0, 1.95 - l)
    y_bin = random_gen.uniform(0, 0.55 - w)
bins = ("[(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")


#  Table 2
if(random_gen.random() < 0.5):
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(0, 1.95 - l)
y = random_gen.uniform(2.45, 3 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = tables + (" & [(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")

# Bin 2
x_bin = random_gen.uniform(0, 1.95 - l)
y_bin = random_gen.uniform(2.45, 3 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(0, 1.95 - l)
    y_bin = random_gen.uniform(2.45, 3 - w)
bins = bins + (" & [(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")


# Table 3
if(random_gen.random() < 0.5):
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(2.05, 3.95 - l)
y = random_gen.uniform(0, 0.55 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = tables + (" & [(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")

# Bin 3
x_bin = random_gen.uniform(2.05, 3.95 - l)
y_bin = random_gen.uniform(0, 0.55 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(2.05, 3.95 - l)
    y_bin = random_gen.uniform(0, 0.55 - w)
bins = bins + (" & [(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")



#  Table 4
if(random_gen.random() < 0.5):
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(2.05, 3.95 - l)
y = random_gen.uniform(2.45, 3 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = tables + (" & [(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")

#  Bin 4
x_bin = random_gen.uniform(2.05, 3.95 - l)
y_bin = random_gen.uniform(2.45, 3 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(2.05, 3.95 - l)
    y_bin = random_gen.uniform(2.45, 3 - w)
bins = bins + (" & [(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")


#  Table 5
if(random_gen.random() < 0.5):
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(4.05, 6 - l)
y = random_gen.uniform(0, 0.55 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = tables + (" & [(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")

# Bin 5
x_bin = random_gen.uniform(4.05, 6 - l)
y_bin = random_gen.uniform(0, 0.55 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(4.05, 6 - l)
    y_bin = random_gen.uniform(0, 0.55 - w)
bins = bins + (" & [(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")


#  Table 6
if(random_gen.random() < 0.5):
    l = 0.5
    w = 0.3
else:
    l = 0.3
    w = 0.5
x = random_gen.uniform(4.05, 6 - l)
y = random_gen.uniform(2.45, 3 - w)
f_env.write("[(" + str(x) + "," + str(y) + "), (" + str(x+l) + "," + str(y) + "), (" + str(x+l) + "," + str(y+w) + "), (" + str(x) + "," + str(y+w) + ")]\n")
tables = tables + (" & [(" + str(clamp(x-0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y-0.1)) + "), (" + str(clamp(x+l+0.1)) + "," + str(clamp(y+w+0.1)) + "), (" + str(clamp(x-0.1)) + "," + str(clamp(y+w+0.1)) + ")]")

# Bin 6
x_bin = random_gen.uniform(4.05, 6 - l)
y_bin = random_gen.uniform(2.45, 3 - w)
while(not collisionFree(x, y, l, w, x_bin, y_bin)):
    x_bin = random_gen.uniform(4.05, 6 - l)
    y_bin = random_gen.uniform(2.45, 3 - w)
bins = bins + (" & [(" + str(x_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin) + "), (" + str(x_bin+l_bin) + "," + str(y_bin+w_bin) + "), (" + str(x_bin) + "," + str(y_bin+w_bin) + ")]")


f_label.write("h: [(0,1), (6,1), (6,2), (0,2)]\n")
f_label.write("r1: [(0,0), (2,0), (2,1), (0,1)]\n")
f_label.write("r2: [(0,2), (2,2), (2,3), (0,3)]\n")
f_label.write("r3: [(2,0), (4,0), (4,1), (2,1)]\n")
f_label.write("r4: [(2,2), (4,2), (4,3), (2,3)]\n")
f_label.write("r5: [(4,0), (6,0), (6,1), (4,1)]\n")
f_label.write("r6: [(4,2), (6,2), (6,3), (4,3)]\n")
f_label.write("b: " + bins + "\n")
f_label.write("t: " + tables + "\n")

f_env.close()

f_label.close()




