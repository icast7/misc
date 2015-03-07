inputRDD = sc.textFile("README.md")
word = inputRDD.filter(lambda s : "Python" in s)
word.collect()

def containsPython(s):
	return "Python" in s


word = inputRDD.filter(containsPython)
word.collect()

class WordFunction(object):
	def __init__(self, query):
		self.query = query
	def isMatch(self, s):
		return self.query in s
	def getMatchesFuncRef(self, rdd):
		#Prob: References the entire object in self.isMatch
		return rdd.filter(self.isMatch)
	def getMatchesMemberRef(self, rdd):
		#Prob: ref entire self object in self.query
		return rdd.filter(lambda x: self.query in x)
	def getMatchesNoRef(self, rdd):
		#Extract only what is needed to a local variable
		query = self.query
		return rdd.filter(lambda x :  query in x)


o = WordFunction("Python")
o.isMatch("Python :)")
o.getMatchesFuncRef(inputRDD).collect()
o.getMatchesMemberRef(inputRDD).collect()
o.getMatchesNoRef(inputRDD).collect()
