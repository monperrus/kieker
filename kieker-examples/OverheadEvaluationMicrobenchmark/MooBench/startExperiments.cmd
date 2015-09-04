start java -cp lib/kieker-1.12-SNAPSHOT-aspectj.jar kieker.tools.analysis.FlexibleTraceReductionAnalysis 0
start /W startMooBenchJar-v0 v0
rScript r\quartiles.r v0-

start java -cp lib/kieker-1.12-SNAPSHOT-aspectj.jar kieker.tools.analysis.FlexibleTraceReductionAnalysis 1
start /W startMooBenchJar-v1 v1
rScript r\quartiles.r v1-

start java -cp lib/kieker-1.12-SNAPSHOT-aspectj.jar kieker.tools.analysis.FlexibleTraceReductionAnalysis 2
start /W startMooBenchJar-v1 v2
rScript r\quartiles.r v2-

start java -cp lib/kieker-1.12-SNAPSHOT-aspectj.jar kieker.tools.analysis.FlexibleTraceReductionAnalysis 3
start /W startMooBenchJar-v1 v3
rScript r\quartiles.r v3-
