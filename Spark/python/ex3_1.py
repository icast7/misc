lines = sc.textFile("README.md")
pythonLines = lines.filter(lambda line: "Python" in line)
pythonLines.first()
pythonLines.persist
pythonLines.count()
pythonLines.first()