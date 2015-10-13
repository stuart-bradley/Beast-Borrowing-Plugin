""" 
Python Rate Matrix Class
Stuart Bradley - 5931269
29-07-2015

This rate matrix class is designed to hold the rates, and the probabilities for 
moving from one state to another. 
- i is a list object containing the states.
- item_rates is a 2D list objects where interior lists represent rows of probabilities
in the order of the item list.
- rMatrix is the internal class representation of item_rates.
- pMatrix is the internal class representation of matrix exponential of rMatrix at time t. 
- items is the internal class representation of i. 
- item_row_pair is a dictionary that links items to their respective rows.
"""

from scipy.linalg import expm
import numpy as np

class RateMatrix:
	def __init__(self, i, item_rates):
		self.rMatrix = item_rates
		self.pMatrix = []
		self.items = i

		self.item_row_pair = {}
		for i,it in enumerate(self.items):
			self.item_row_pair[it] = i

	# Create a probability matrix from the rate matrix at time t.
	def create_pMatrix(self, t):
		numpy_rMatrix = np.array(self.rMatrix)
		numpy_pMatrix = expm(numpy_rMatrix * t)
		p_list = numpy_pMatrix.tolist()
		self.pMatrix = p_list

	# Get the probability row associated with item.
	def get_p_row(self, item):
		return self.pMatrix[self.item_row_pair[item]]

	def get_exp_rate(self, item):
		item_row = self.item_row_pair[item]
		return self.rMatrix[item_row][item_row]

	def get_rate_probs(self, item):
		item_row = self.item_row_pair[item]
		rate_row = list(self.rMatrix[item_row])
		rate_row.pop(item_row)
		for i, rate in enumerate(rate_row):
			rate_row[i] = rate/sum(rate_row)
		return rate_row