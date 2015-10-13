"""
CognateSet Test
Stuart Bradley - 5931269
30/7/2015

This script is used for testing the basic functionality of various scripts.
"""
from CognateSet import CognateSet
from RateMatrix import RateMatrix
from Tree import Tree
from Language import Language

set_l = CognateSet(langs=[Language(seq=[0, 1, 0, 1, 0, 1])])
rm = RateMatrix([0,1], [[-0.5,0.5],[0.5,-0.5]])

tr = Tree(data=set_l)

tr.generate_tree_GTR(0.4 ,15, rm)
#tr.simulate_borrowing(0.8, 15, z=45)
tr.draw_tree()
"""
print set_l.language_list[0].sequence
res = set_l.mutate_language_stochastic_dollo_timed(set_l.language_list[0], 0.4,0.3, 4)
print res.sequence
"""