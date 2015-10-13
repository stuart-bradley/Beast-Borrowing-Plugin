""" 
Python Cognate Matrix Class
Stuart Bradley - 5931269
29-07-2015

This class is a container for a set of language classes, as well as 
methods that act upon these languages. 

It it's current state, 80 randomized languages are produced. 

Language evolution can occur under three models:
- CMTC
- Covarion
- Stollo-Dollo
- Rate Variable
"""

from Language import Language
import random
from bisect import bisect
import math
import numpy as np

class CognateSet:
	# Produces a cognate matrix,  with it's sequence of binary cognates.
	def __init__(self, langs=[]):
		self.language_list = langs
		self.stollo_length = len(self.language_list[0].sequence)

	# Returns a language specified by it's name.
	def find_lang(self,name):
		for i in self.language_list:
			if i.name == name:
				return i

	# Gets the binary vector for a particular cognate. 
	def get_cognate_set(self,n):
		c_s = []
		try: 
			for language in language_list:
				c_s.append(language.sequence[n])
		except IndexError:
			return 

	# Produces an exponentially distributed random variable.
	def exponential(self,rate):
		return -math.log(random.random())/rate

	# Mutates language traits according to the reversible 
	# continuous time Markov chain model. 
	# Given a probability matrix.
	def mutate_language_GTR_timed(self, lang, Q, T):
		new_lang = Language(seq=lang.sequence)
		for i in range(len(new_lang.sequence)):
			rate = Q.get_exp_rate(new_lang.sequence[i])
			t = self.exponential(-rate)
			while t < T:
				mutatable_items = list(Q.items)
				mutatable_items.remove(new_lang.sequence[i])
				new_lang.sequence[i] = np.random.choice(mutatable_items, p=Q.get_rate_probs(new_lang.sequence[i]))
				rate = Q.get_exp_rate(new_lang.sequence[i])
				t += self.exponential(-rate)
		self.language_list.append(new_lang)
		return new_lang

	# Mutates language traits according to the reversible 
	# continuous time Markov chain model. 
	# Given a probability matrix.
	# Differs from above by not computing each individual mutation.
	def mutate_language_GTR_timed_2(self, lang, Q, T):
		new_lang = Language(seq=lang.sequence)
		Q.create_pMatrix(T)
		for i in range(len(new_lang.sequence)):
			new_lang.sequence[i] = np.random.choice(Q.items, p=Q.get_p_row(new_lang.sequence[i]))
		self.language_list.append(new_lang)
		return new_lang

	# Mutates language traits according to the Stochastic-Dollo model.
	# Stops once time is exceeded for each trait.
	def mutate_language_stochastic_dollo_timed(self, lang, b, d, T):
		new_lang = Language(seq=lang.sequence)
		# Rand_exp at rate b + d * k
		t = self.exponential(b + d*new_lang.get_births())
		# Total rate for uniform generation.
		while t < T:
			# Death:
			if np.random.choice(['death', 'birth'], p=[(d * new_lang.get_births() /(b+d*new_lang.get_births())),(b/(b+d*new_lang.get_births()))]) == 'death':
				# Pick a random site that is not dead, and kill it.
				while True:
					i = random.randint(0, len(lang.sequence) - 1)
					if new_lang.sequence[i] != 0:
						new_lang.sequence[i] = 0
						break
				# Birth:
			else:
				new_lang.sequence.append(1)
				self.stollo_length += 1
			t += self.exponential(b + d*new_lang.get_births())	
		self.language_list.append(new_lang)
		return new_lang