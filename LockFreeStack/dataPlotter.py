import matplotlib.pyplot as pt
import csv

data = ["pop", "push", "size"]
shapes = ["o", "s", "v"]
for idx in range(len(data)):
  with open("%s.csv" % data[idx], "rb") as c:
    a = csv.reader(c)
    x = []
    y = []
    for row in a:
      x.append(row[0])
      y.append(row[1])
    pt.title("Thread count vs Time")
    pt.xlabel("threads")
    pt.ylabel("time (ms)")
    pt.plot(x[1:],y[1:], "-%s" % shapes[idx], label=data[idx])
pt.legend(loc='best')
pt.show()
