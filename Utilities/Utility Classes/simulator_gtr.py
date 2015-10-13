"""
Simulator for GTR models
Stuart Bradley - 5931269
30/7/2015

This script tests whether the two versions of the GTR model produce
the same result. 
"""
from CognateSet import CognateSet
from RateMatrix import RateMatrix
from Language import Language
import matplotlib.pyplot as plt
from scipy import stats
import numpy

Lan = Language(seq=[0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0])
set_l = CognateSet(langs=[Lan])
rm = RateMatrix([0,1], [[-0.5,0.5],[0.5,-0.5]])

# Simulates 1000 instances of the first method to determine the probabilities are
# equal for both methods.
d_1_1 = []
d_2_1 = []
for i in range(1000):
	Lan = Language(seq=[0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0])
	set_l = CognateSet(langs=[Lan])
	d_1_1.append(set_l.mutate_language_GTR_timed(set_l.language_list[0], rm, 100).sequence.count(1))
	Lan = Language(seq=[0, 0, 0, 0, 0, 0, 0, 1, 0, 1, 0, 1, 1, 1, 0, 1, 0, 1, 0, 0])
	set_l = CognateSet(langs=[Lan])
	d_2_1.append(set_l.mutate_language_GTR_timed_2(set_l.language_list[0], rm, 100).sequence.count(1))

numpy.savetxt("file_name.csv", d_2_1, delimiter=",", fmt='%s')

"""
density_1 = stats.kde.gaussian_kde(d_1_1)
density_2 = stats.kde.gaussian_kde(d_2_1)
x = numpy.arange(0, 20, 1)
plt.plot(x, density_1(x),color='b', label='Algorithm 1')
plt.plot(x, density_2(x),color='r', label='Algorithm 2')
plt.title("Simulation of 100000 Language Evolutions")
plt.xlabel("Number of cognates")
plt.ylabel("Proportion")
plt.legend()
plt.show()
"""