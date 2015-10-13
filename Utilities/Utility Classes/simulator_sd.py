"""
Simulator for Stocastic-Dollo Model
Stuart Bradley - 5931269
22/08/2015

This script checks that the Stocastic-Dollo method is sampling from the correct distribution.
"""
from CognateSet import CognateSet
from RateMatrix import RateMatrix
from Language import Language
import matplotlib.pyplot as plt
import math
import numpy as np
from scipy import stats

d_1 = []
d_2 = []
b = 0.5
d = 0.5
t = 10

starting_traits = np.random.poisson((b/d))
s = []
for i in range(starting_traits):
	s.append(1)

for i in range(100000):
	print i
	Lan = Language(seq=s)
	set_l = CognateSet(langs=[Lan])
	l = set_l.mutate_language_stochastic_dollo_timed(set_l.language_list[0], b, d, t)
	d_1.append(l.sequence.count(1))
	d_2.append(np.random.poisson((b/d)))
print starting_traits

density_1 = stats.kde.gaussian_kde(d_1)
density_2 = stats.kde.gaussian_kde(d_2)
x = np.arange(0, 20, 1)
plt.plot(x, density_1(x),color='b', label='SD Algorithm')
plt.plot(x, density_2(x),color='r', label='Poisson Distribution')
plt.title("Simulation of 100000 Stochastic-Dollo Language Evolutions")
plt.xlabel("Number of birthed traits")
plt.ylabel("Counts")
plt.legend()
plt.show()
