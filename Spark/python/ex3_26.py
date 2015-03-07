
lines = sc.parallelize(["hello world" , "hi"])
words = lines.flatMap(lambda line : line.split(" "))
words.first() # This should return "hello"

nums = sc.parallelize([1,2,3,4])
squared = nums.map(lambda x: x * x).collect()
for num in squared:
	print "%i " % (num)


sum = nums.reduce( lambda x, y : x + y )
sum

#Book AGGREGATE example
#sumCount = nums.aggregate((0, 0),
#	(lambda acc, value: (acc[0] + value, acc[1] + 1),
#	(lambda acc1, acc2: (acc1[0] + acc2[0], acc1[1] + acc2[1]))))
#return sumCount[0]/float(sumCount[1])
#
# Refactored
lambda1 =  (lambda acc, value: (acc[0] + value, acc[1] + 1))
lambda2 =  (lambda acc1, acc2: (acc1[0] + acc2[0], acc1[1] + acc2[1]))
sumCount = nums.aggregate((0, 0), lambda1, lambda2)
sumCount[0]/float(sumCount[1])
#### end of example ####

#Docs example
seqOp = (lambda x, y :  (x[0] + y, x[1] +1 ))
combOp =  (lambda x, y : (x[0] +  y[0], x[1] + y[1]))
sc.parallelize([1, 2, 3, 4]).aggregate((0,0), seqOp, combOp)

sc.parallelize([]).aggregate((0,0), seqOp, combOp)
