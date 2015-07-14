import json
from pprint import pprint

"""
Convert a resultSet JSON into a Graph JSON valid input for D3
"""

hash_data = {}
hash_nodes = []
hash_links = []

spam = [u'retweet', u'porn', u'xxx', u'teamretweet', u'retweets', u'retweet_this', u'mzbretweeters', u'rt', u'naked', u'sex', u'fanbase', 
    u'ff', u'pussy', u'anal', u'followback', u'followtrick', u'followngain', u'follow', u'mgwv', u'teamfollowback', u'f4f', u'analsex',
    u'mustfollow', u'anotherfollowtrain', u'sexyasfuck']

# Parse the Jena JSON and extract the nodes with attributes
with open('hash_tags_d3/HashtagGroupSize.json') as json_data_nodes:
    dataNodes = json.load(json_data_nodes)
    vocabulary = []
    # iterate over all bindings in the input file
    
    maxVal = 721
    minVal = 1
    
    oldRange = (1.0 - 0.0) # OldRange = (OldMax - OldMin)
    newRange = (50 - 5) # NewRange = (NewMax - NewMin)
    
    for binding in dataNodes["results"]["bindings"]:
	aNode = {}
	"""
	Example of a binding
	{
	    "name": { "type": "literal" , "value": "galway2020" } ,
	    "size": { "datatype": "http://www.w3.org/2001/XMLSchema#integer" , "type": "typed-literal" , "value": "389" } ,
	    "group": { "datatype": "http://www.w3.org/2001/XMLSchema#integer" , "type": "typed-literal" , "value": "1" }
        }
	"""
	#create node for d3 for this binding
	nodeName = binding["name"]["value"]
	
	if not nodeName in vocabulary:
	    """
	    A node in the graph
	    {"name":"Myriel","group":1}
	    """
	    
	    # normalize the size
	    # http://stats.stackexchange.com/questions/70801/how-to-normalize-data-to-0-1-range
	    size = binding["size"]["value"]
	    vocabulary.append(nodeName)
	    
	    val = (int(size) - minVal) / float((maxVal - minVal))
	    valNorm = (((val - 0.0) * newRange) / oldRange) + 5 # (((OldValue - OldMin) * NewRange) / OldRange) + NewMin
	    
	    aNode["radius"] = valNorm
	    aNode["group"] = binding["group"]["value"]
	    aNode["name"] = nodeName
	
	    # add node to the hash of nodes
	    hash_nodes.append(aNode)


for word in spam:
    print "Removing ", word
    #print [d for d in hash_nodes if d["name"] == word], vocabulary.index(word)
    hash_nodes[:] = [d for d in hash_nodes if d["name"] != word]
    vocabulary.remove(word)

# Parse the Jena JSON and extract links between hashtags
with open('hash_tags_d3/HashtagLinks.json') as json_data_links:
    dataLinks = json.load(json_data_links)
    # Convert the bindings into links of the final JSON
    for binding in dataLinks["results"]["bindings"]:
	# create links for d3 for this binding
	aLink = {}
	"""
	Example of a binding
	{
	    "source": { "type": "literal" , "value": "galway" } ,
	    "target": { "type": "literal" , "value": "ibackgalway" } ,
	    "value": { "datatype": "http://www.w3.org/2001/XMLSchema#integer" , "type": "typed-literal" , "value": "132" }
	}
	"""
	label1 = binding["source"]["value"]
	label2 = binding["target"]["value"]
	value = binding["value"]["value"]
	
	"""
	Example of a link in the graph
	{ "source":1, "target":0, "value":1 }
	"""
	# these labels should already be included in vocabulary from the above step
	#if label1 not in vocabulary:
	    ## if source node is not in the vocabulary, do not consider the link 
	    ## and remove target node
	    #if label2 in vocabulary:
		##ele = [d for (index, d) in enumerate(hash_nodes) if d["name"] == label2][0]
		##print ele
		#hash_nodes[:] = [d for d in hash_nodes if d.get("name") != label2]
		## and remove any link in hash_link with target equals to label2
		## print [d for d in hash_links if d["target"] == vocabulary.index(label2)]
		#hash_links[:] = [d for d in hash_links if d["target"] != vocabulary.index(label2)]
		#hash_links[:] = [d for d in hash_links if d["source"] != vocabulary.index(label2)]
	    #continue
	#else:
	    #aLink["source"] = vocabulary.index(label1)
	    
	if label1 in vocabulary:
	    aLink["source"] = vocabulary.index(label1)
	else:
	    continue
	    
	#if label2 not in vocabulary:
	    ## if target node is not in the vocabulary, do not consider the link 
	    ## and remove source node
	    #if label1 in vocabulary:
		#hash_nodes[:] = [d for d in hash_nodes if d.get("name") != label1]
		## and remove any link in hash_link with source equals to label1
		#hash_links[:] = [d for d in hash_links if d["source"] != vocabulary.index(label1)]
		#hash_links[:] = [d for d in hash_links if d["target"] != vocabulary.index(label1)]
	    #continue
	#else:
	    #aLink["target"] = vocabulary.index(label2)
	    
	if label2 in vocabulary:
	    aLink["target"] = vocabulary.index(label2)
	else:
	    continue
	    	
	aLink["value"] = value
	hash_links.append(aLink)

# remove spam
#for word in spam:
    #print "Removing hashtags with word=", word
    ## remove links where spam word is the source
    #hash_links[:] = [d for d in hash_links if d["source"] != vocabulary.index(word)]
    ## remove links where spam word is the target
    #hash_links[:] = [d for d in hash_links if d["target"] != vocabulary.index(word)]

# Append nodes and links into one JSON object
hash_data["nodes"] = hash_nodes
hash_data["links"] = hash_links
#output_data = json.dumps(hash_data)
#print output_data

# Write JSON into a file
with open('hash_tags_d3_v2.json', 'w') as outfile:
    json.dump(hash_data, outfile, indent=4)

## END
