# Rate checking code

import random

def getposition(pos):
	t = ""
	for l in langs:
		t += l[pos]
	return t

def getRateMatrixResult(t):
	if t == "000" or t == "111":
		print t + ": " + "m + m + m" 
		return m + m + m
	elif t == "100" or t == "010" or t == "001":
		print t + ": " + "m + m*(b/2+1) + m*(b/2+1)"
		return m + m*(b/2+1) + m*(b/2+1)
	else:
		print t + ": " + "m + m + (b+1.0)*m"
		return m + m + (b+1.0)*m

def birthreduction(borrowSum):
	for i in range(len(langs[0])):
		t = getposition(i)
		if t == "111":
			print t + ": 3 positions make no difference"
			borrowSum -= 3
		elif t == "110" or t == "101" or t == "011":
			print t + ": 1 position makes no difference"
			borrowSum -= 1
	return borrowSum

def births():
	births = 0.0
	for l in langs:
		births += l.count("1")
	return births

# MAIN

for i in range(100):
	print 
	print i
	#l1 = ''.join(random.choice(["0","1"]) for _ in range(5))
	#l2 = ''.join(random.choice(["0","1"]) for _ in range(5))
	#l3 = ''.join(random.choice(["0","1"]) for _ in range(5))
	l1 = "10101"
	l2 = "11111"
	l3 = "10101"
	langs = [l1,l2,l3]
	m = 0.5
	b = 0.5

	for l in langs:
		print l

	print ''

	# Rate matrix calc
	print "Rate matrix calc:"
	print ''
	res = 0.0
	for i in range(len(langs[0])):
		res += getRateMatrixResult(getposition(i))
	print ''
	print "Rate matrix total rate = " + str(res)

	# Simulation calc
	print ''
	print "Simulation calc:"
	print ''
	p = (birthreduction(births()))
	print m*(3+5)
	print (b*m)
	print p
	totalRateCalc = m*(3*5) + b * m * p

	print "Simulation total rate = " + str(totalRateCalc)