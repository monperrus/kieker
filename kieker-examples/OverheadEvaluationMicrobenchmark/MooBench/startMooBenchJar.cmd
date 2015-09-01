set JAVABIN=""

set JAVAARGS=-server ^
	-d64 ^
	-Xms1G ^
	-Xmx4G

set WEAVER_JAR=./lib/kieker-1.12-SNAPSHOT-aspectj.jar

set JAVAARGS_LTW=%JAVAARGS% -javaagent:%WEAVER_JAR% ^
	-Dorg.aspectj.weaver.showWeaveInfo=false ^
	-Daj.weaving.verbose=false ^
	-Dkieker.monitoring.skipDefaultAOPConfiguration=true ^
	-Dorg.aspectj.weaver.loadtime.configuration=META-INF/kieker.aop.xml

set JAVAARGS_KIEKER_LOGGING_TCP=%JAVAARGS_LTW% ^
	-Dkieker.monitoring.writer=kieker.monitoring.writer.tcp.TCPWriter

set JAR=-jar MooBench.jar -a mooBench.monitoredApplication.MonitoredClassSimple

set TOTALCALLS=2000000
set METHODTIME=0
set THREADS=1
set RECURSIONDEPTH=10
set OUTPUT_FILENAME=%TOTALCALLS%-%METHODTIME%-%THREADS%-%RECURSIONDEPTH%
set MOREPARAMS=--quickstart ^
	-r kieker.Logger

%JAVABIN%java  %JAVAARGS_KIEKER_LOGGING_TCP% %JAR% ^
	--totalcalls %TOTALCALLS% ^
	--methodtime %METHODTIME% ^
	--totalthreads %THREADS% ^
	--recursiondepth %RECURSIONDEPTH% ^
	--output-filename %OUTPUT_FILENAME%.csv ^
	%MOREPARAMS%
