require("data.table")
require(graphics)			# ts.plot

loadResponseTimesInNs = function(filename, numFirstValuesToIgnore) {
	csvTable <- fread(filename, skip=numFirstValuesToIgnore)
	values <- csvTable[,V2]
	return(values)
}

getConfidenceInterval = function(values) {
	stdDeviation	<- sd(values)
	numElements		<- length(values)
	# a confidence level of 95% corresponds to the argument value 0.975
	ci = qnorm(0.975)*stdDeviation/sqrt(numElements)
	return(ci)
}

args <- commandArgs(trailingOnly = TRUE)

inputDir		= args[1]
filename		= args[2]

totalcalls 		= 2000000
methodtime 		= 0
threadcount		= 1
recursiondepth	= 30


### input

quartileVector <- c(0,50,100)/100

numRows = 1

means 		<- array(0, c(numRows,1) )
confIntvls	<- array(0, c(numRows,1) )
quartiles 	<- array(0, c(numRows,length(quartileVector)) )

rows <- array(0, c(numRows, length(quartileVector)+1+1) )

#filename = sprintf("%s%d-%d-%d-%d.csv", inputDir, totalcalls, methodtime, threadcount, recursiondepth)
responseTimesInNs <- loadResponseTimesInNs(filename, totalcalls/2)

means[1,]		<- as.integer( mean(responseTimesInNs, trim=0) )
confIntvls[1,]	<- as.integer( getConfidenceInterval(responseTimesInNs) )
quartiles[1,]	<- quantile(responseTimesInNs, names=FALSE, probs=quartileVector)

rowIndex = 1
rows[rowIndex,] <- c( means[rowIndex,], confIntvls[rowIndex,], quartiles[rowIndex,] )

### output: csv

outputFilename = sprintf("%s-results.csv", filename)
write.csv(rows, file = outputFilename)

### output: pdf

#pdfOutputFilename=paste(resultDir, "/results.pdf", sep="")
#pdf(pdfOutputFilename, width=10, height=6.25, paper="special")