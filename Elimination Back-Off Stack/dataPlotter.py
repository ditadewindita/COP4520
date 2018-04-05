import matplotlib.pyplot as pt
import csv

data = ["push", "pop", "size"]
stackTypes = ["Lock-Free Stack", "Elimination Back-Off Stack"]
shapes = ["o", "s"]
figure_num = 0
for idx in range(len(data)):
    with open("%s.csv" % data[idx], "rb") as c:
        a = csv.reader(c)
        x = []
        y = []
        for row in a:
            x.append(row[0])
            y.append(row[1])
        pt.figure(idx)
        pt.title("Threads vs Time for %s operation" % data[idx])
        pt.xlabel("Threads")
        pt.ylabel("Time (ms)")
        pt.plot(x[1:7],y[1:7], "-%s" % shapes[0], label = stackTypes[0])
        pt.plot(x[7:], y[7:], "-%s" % shapes[1], label = stackTypes[1])
    pt.legend(loc='best')
    pt.show()
