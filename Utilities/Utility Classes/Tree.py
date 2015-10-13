"""
Python Tree Class
Stuart Bradley - 5931269
23-05-2014

15/08/2015 - Modified to use Networkx to make drawing easier.
"""

import random
import math
from collections import deque
import networkx as nx
import matplotlib.pyplot as plt

class Tree:

	# Initialize tree with CognateSet 'data', where first Language
	# in cognate set is root.
	def __init__(self, data=None):
		self.G = nx.DiGraph()
		self.data = data
		self.G.add_node(1, lang=self.data.language_list[0])
		self.node_labels = {}
		self.node_labels[1] = str(self.G.node[1]['lang'])
		self.edge_labels = {}

	# Generate a binary tree using the GTR mutation model. 'total_langs' should be an odd number.
	def generate_tree_GTR(self, branch_rate ,total_langs, Q, borrow_rate=0):
		current_langs = 1
		# Create a FIFO queue, add root.
		node_queue = deque([1])
		while (total_langs - current_langs) > 0:
			# Get node to branch.
			curr_node = node_queue.popleft()
			# Create two new languages and create their nodes.
			for i in range(2):
				t = self.data.exponential(branch_rate)
				l = self.data.mutate_language_GTR_timed_2(self.G.node[curr_node]['lang'], Q, t)
				current_langs += 1
				# Add node to graph.
				self.G.add_node(current_langs, lang=l)
				self.node_labels[current_langs] = str(self.G.node[current_langs]['lang'])
				# Add edge to graph.
				self.G.add_edge(curr_node, current_langs, weight=t)
				self.edge_labels[(curr_node,current_langs)] = round(self.G.edge[curr_node][current_langs]['weight'], 3)
				# Add to FIFO queue.
				node_queue.append(current_langs)

	# Generate a binary tree using the GTR mutation model. 'total_langs' should be an odd number.
	def generate_tree_SD(self, branch_rate ,total_langs, b, d, borrow_rate=0):
		current_langs = 1
		# Create a FIFO queue, add root.
		node_queue = deque([1])
		while (total_langs - current_langs) > 0:
			# Get node to branch.
			curr_node = node_queue.popleft()
			# Create two new languages and create their nodes.
			for i in range(2):
				t = self.data.exponential(branch_rate)
				l = self.data.mutate_language_stochastic_dollo_timed(self.G.node[curr_node]['lang'], b, d, t)
				current_langs += 1
				# Add node to graph.
				self.G.add_node(current_langs, lang=l)
				self.node_labels[current_langs] = str(self.G.node[current_langs]['lang'])
				# Add edge to graph.
				self.G.add_edge(curr_node, current_langs, weight=t)
				self.edge_labels[(curr_node,current_langs)] = round(self.G.edge[curr_node][current_langs]['weight'], 3)
				# Add to FIFO queue.
				node_queue.append(current_langs)

	# Returns all nodes at a given level of the binary tree,
	# useful for global borrowing.
	def get_nodes_in_row(self, r):
		return range(2**r, 2*2**r)

	# Get languages at the time slice t for borrowing. 
	def get_poss_langs_global(self, current, langs_in_row, t_giver):
		# Remove language that is sending.
		result = list(langs_in_row)
		result.remove(current)
		for l in list(result):
			parent = self.G.predecessors(l)[0]
			t_reciever = self.G.edge[parent][l]['weight']
			# Check recieving language total mutation time is greater than the giver.
			if t_reciever < t_giver:
				result.remove(l)
		return result

	# Get languages at the time slice t for borrowing, where languages have a common ancestor before z. 
	def get_poss_langs_local(self, current, langs_in_row, t_giver, z):
		# Remove language that is sending.
		result = list(langs_in_row)
		result.remove(current)
		for l in list(result):	
			parent = self.G.predecessors(l)[0]
			t_reciever = self.G.edge[parent][l]['weight']
			# Check recieving language total mutation time is greater than the giver.
			if t_reciever < t_giver or not self.ancestor_check(current, l, z):
				result.remove(l)
		return result

	# Works backwards up the tree looking for a common ancestor before time z. 
	def ancestor_check(self, giver, reciever, z):
		giver_parent = self.G.predecessors(giver)[0]
		reciever_parent = self.G.predecessors(reciever)[0]
		giver_t = self.G.edge[giver_parent][giver]['weight']
		reciever_t = self.G.edge[reciever_parent][reciever]['weight']

		while giver_t < z and reciever_t < z:
			if giver_parent == reciever_parent:
				return True
			else:
				giver = self.G.predecessors(giver)[0]
				reciever = self.G.predecessors(reciever)[0]
				giver_parent = self.G.predecessors(giver)[0]
				reciever_parent = self.G.predecessors(reciever)[0]
				giver_t += self.G.edge[giver_parent][giver]['weight']
				reciever_t += self.G.edge[reciever_parent][reciever]['weight']
		return False

	# Runs global borrowing on the entire tree.
	def simulate_borrowing(self, b, total_langs, z=0):
		# Caclulate number of rows in the tree given the total languages.
		rows = range(1, int(math.ceil(math.log(total_langs+1)/math.log(2)-1))+1) 
		for n in rows:
			langs = self.get_nodes_in_row(n)
			for l in langs:
				# Get mutation time, and run borrowing.
				parent = self.G.predecessors(l)[0]
				T = self.G.edge[parent][l]
	   		t = self.data.exponential(b)
			while t < T:
				# Languages cannot borrow with themselves.
				if z == 0:
					poss_langs = self.get_poss_langs_global(l, langs, t)
				else:
					poss_langs = self.get_poss_langs_local(l, langs, t, z)
				# IF there are a no languages to receive move onto next language. 
				if len(poss_langs) == 0:
					break
				# Find a cognate the second language doesn't have,
				# that the first does and update.
				random_order = range(len(self.G.node[l]['lang'].sequence))
				random.shuffle(random_order)
				reciever = random.choice(poss_langs)
				for i in random_order:
					if self.G.node[l]['lang'].sequence[i] == 1:
						if self.G.node[reciever]['lang'].sequence[i] == 0:
							self.G.node[reciever]['lang'].sequence[i] = 1
							break
			t += self.data.exponential(b)

   	# Draws a tree using pygraphviz layouts, and matplotlib.
	def draw_tree(self):
		plt.title("GTR Tree - 15 Languages")
		pos=nx.graphviz_layout(self.G,prog='dot')
		nx.draw(self.G,pos,with_labels=True,arrows=False,labels = self.node_labels, node_color='w', node_shape="s", node_size=2500)
		#nx.draw_networkx_edge_labels(self.G, pos, edge_labels= self.edge_labels)
		plt.show()
