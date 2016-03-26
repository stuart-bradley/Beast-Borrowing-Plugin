"""
Renames tree nodes according to translation.
Stuart Bradley
"""

import sys


trees = []
taxa = {}

def convert_tree(tree,taxa):
	for k,v in taxa.iteritems():
		tree = tree.replace("(" + k + ":", "(" + v + ":")
		tree = tree.replace("," + k + ":", "," + v + ":")
	return tree

at_trees = False

with open(sys.argv[1]) as f:
    for line in f:
    	
    	line = line.strip()
        if line == "Begin trees;":
        	at_trees = True
        	continue
        if not at_trees:
        	continue
    	if line == "Translate" or line == ";" or line == "End;":
    		continue
    	elif line.startswith("tree"):
    		trees.append(convert_tree(line, taxa))
    	else:
    		line_list = line.split(" ")
    		taxa[line_list[0]] = line_list[1][0:-1]

with open(sys.argv[1]+"_new.nex", "w") as fo:
	fo.write("#NEXUS \n")
	fo.write("Begin trees; \n")
	fo.writelines(trees)
	fo.write("End;")