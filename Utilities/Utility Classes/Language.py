""" 
Python Language Class
Stuart Bradley - 5931269
29-07-2015

This language class is designed to hold a sequence of binary traits, 
representing whether or not the language contains a word for some cognate.

If a sequence is not provided, it produces a random set of traits. 

births carries the total number of births (0->1) that have happened to a language in
the Stocastic-Dollo model. 
"""

import random

class Language:
	# Produces a language with it's sequence of binary cognates.
	def __init__(self, seq=None):
		if seq is not None:
			self.sequence = seq
		else:
			self.sequence = [random.randint(0,1) for b in range(0,20)]

	# Computes births.
	def get_births(self):
		return self.sequence.count(1)

	# Returns a stringified list of the form:
	# [0,1,0] -> '010'.
	def __str__(self):
		return ''.join(str(i) for i in self.sequence)



