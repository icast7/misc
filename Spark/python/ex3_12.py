inputRDD = sc.textFile("README.md")
pyRDD = inputRDD.filter(lambda x : "Python" in x)
scalaRDD = inputRDD.filter(lambda x : "Scala"  in x)
unionRDD = pyRDD.union(scalaRDD)
unionRDD.count()
print "Input had " + str(unionRDD.count()) + " pyscala lines"
print "Here are 10 examples: "
for line in unionRDD.take(10):
	print line