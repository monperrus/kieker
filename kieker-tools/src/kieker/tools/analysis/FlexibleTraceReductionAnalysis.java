/***************************************************************************
 * Copyright 2014 Kieker Project (http://kieker-monitoring.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***************************************************************************/

package kieker.tools.analysis;

import java.util.concurrent.TimeUnit;

import kieker.analysis.AnalysisController;
import kieker.analysis.IAnalysisController;
import kieker.analysis.exception.AnalysisConfigurationException;
import kieker.analysis.plugin.filter.flow.EventRecordTraceReconstructionFilter;
import kieker.analysis.plugin.filter.flow.TraceAggregationFilter;
import kieker.analysis.plugin.filter.forward.CountingPrinter;
import kieker.analysis.plugin.filter.sink.CountingPrintSink;
import kieker.analysis.plugin.reader.AbstractReaderPlugin;
import kieker.analysis.plugin.reader.tcp.NewTcpReader;
import kieker.analysis.plugin.reader.tcp.TCPReader;
import kieker.analysis.plugin.reader.tcp.TcpReader110;
import kieker.analysis.plugin.reader.tcp.v1.TcpReaderV0;
import kieker.common.configuration.Configuration;
import kieker.common.logging.Log;
import kieker.common.logging.LogFactory;

// Command-Line:
// java -javaagent:lib/kieker-1.10-SNAPSHOT_aspectj.jar -Dkieker.monitoring.writer=kieker.monitoring.writer.tcp.TCPWriter -Dkieker.monitoring.writer.tcp.TCPWriter.QueueFullBehavior=1 -jar dist\OverheadEvaluationMicrobenchmark.jar --recursiondepth 10 --totalthreads 1 --methodtime 0 --output-filename raw.csv --totalcalls 10000000
/**
 * @author Christian Wulf
 */
public final class FlexibleTraceReductionAnalysis {

	static final int NUM_ELEMENTS_LOG_TRIGGER = 2 * 100 * 1000;
	static final String MONITORING_RECORD_PORT = "10133";
	static final String STRING_RECORD_PORT = "10134";

	private static final Log LOG = LogFactory.getLog(FlexibleTraceReductionAnalysis.class);

	private FlexibleTraceReductionAnalysis() {}

	public static void main(final String[] args) throws IllegalStateException, AnalysisConfigurationException {
		final int analysisVersion = Integer.parseInt(args[0]);

		final IAnalysisController analysisController = new AnalysisController("TCPThroughput");

		final AbstractReaderPlugin reader;
		final String outputPortName;

		switch (analysisVersion) {
		case 0: {
			final Configuration readerConfig = new Configuration();
			readerConfig.setProperty(TcpReader110.CONFIG_PROPERTY_NAME_PORT1, MONITORING_RECORD_PORT);
			readerConfig.setProperty(TcpReader110.CONFIG_PROPERTY_NAME_PORT2, STRING_RECORD_PORT);
			reader = new TcpReader110(readerConfig, analysisController);
			outputPortName = TcpReader110.OUTPUT_PORT_NAME_RECORDS;
			break;
		}
		case 1: {
			final Configuration readerConfig = new Configuration();
			readerConfig.setProperty(TcpReaderV0.CONFIG_PROPERTY_NAME_PORT1, MONITORING_RECORD_PORT);
			// readerConfig.setProperty(TcpReaderSt.CONFIG_PROPERTY_NAME_PORT2, STRING_RECORD_PORT);
			reader = new TcpReaderV0(readerConfig, analysisController);
			outputPortName = TcpReaderV0.OUTPUT_PORT_NAME_RECORDS;
			break;
		}
		case 2: {
			final Configuration readerConfig = new Configuration();
			readerConfig.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT1, MONITORING_RECORD_PORT);
			readerConfig.setProperty(TCPReader.CONFIG_PROPERTY_NAME_PORT2, STRING_RECORD_PORT);
			reader = new TCPReader(readerConfig, analysisController);
			outputPortName = TCPReader.OUTPUT_PORT_NAME_RECORDS;
			break;
		}
		case 3: {
			final Configuration readerConfig = new Configuration();
			readerConfig.setProperty(NewTcpReader.CONFIG_PROPERTY_NAME_PORT1, MONITORING_RECORD_PORT);
			readerConfig.setProperty(NewTcpReader.CONFIG_PROPERTY_NAME_PORT2, STRING_RECORD_PORT);
			reader = new NewTcpReader(readerConfig, analysisController);
			outputPortName = NewTcpReader.OUTPUT_PORT_NAME_RECORDS;
			break;
		}
		default: {
			throw new IllegalArgumentException("Invalid analysis version: " + analysisVersion + ". Valid versions: [0,..,3]");
		}
		}

		FlexibleTraceReductionAnalysis.createAndConnectPlugins(analysisController, reader, outputPortName);
		try {
			analysisController.run();
		} catch (final AnalysisConfigurationException ex) {
			FlexibleTraceReductionAnalysis.LOG.error("Failed to start the example project.", ex);
		}
	}

	private static void createAndConnectPlugins(final IAnalysisController analysisController, final AbstractReaderPlugin reader,
			final String output_port_name_records)
			throws IllegalStateException, AnalysisConfigurationException {
		final Configuration configTraceRecon = new Configuration();
		configTraceRecon.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_TIMEUNIT, TimeUnit.SECONDS.name());
		configTraceRecon.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_DURATION, Long.toString(Long.MAX_VALUE));
		configTraceRecon.setProperty(EventRecordTraceReconstructionFilter.CONFIG_PROPERTY_NAME_MAX_TRACE_TIMEOUT, Long.toString(Long.MAX_VALUE));
		final EventRecordTraceReconstructionFilter traceRecon = new EventRecordTraceReconstructionFilter(configTraceRecon, analysisController);

		analysisController.connect(reader, output_port_name_records, traceRecon, EventRecordTraceReconstructionFilter.INPUT_PORT_NAME_TRACE_RECORDS);

		final Configuration configForCountingPrinter = new Configuration();
		configForCountingPrinter.setProperty(CountingPrintSink.CONF_THRESHOLD, Integer.toString(NUM_ELEMENTS_LOG_TRIGGER));
		final CountingPrinter countingPrinter = new CountingPrinter(configForCountingPrinter, analysisController);

		analysisController.connect(traceRecon, EventRecordTraceReconstructionFilter.OUTPUT_PORT_NAME_TRACE_VALID, countingPrinter,
				CountingPrinter.INPUT_PORT_NAME_EVENTS);

		final Configuration configTraceAggr = new Configuration();
		configTraceAggr.setProperty(TraceAggregationFilter.CONFIG_PROPERTY_NAME_TIMEUNIT, TimeUnit.SECONDS.name());
		configTraceAggr.setProperty(TraceAggregationFilter.CONFIG_PROPERTY_NAME_MAX_COLLECTION_DURATION, "1");
		final TraceAggregationFilter traceAggr = new TraceAggregationFilter(configTraceAggr, analysisController);

		// analysisController.connect(teeFilter, TeeFilter.OUTPUT_PORT_NAME_RELAYED_EVENTS, traceAggr,
		// TraceAggregationFilter.INPUT_PORT_NAME_TRACES);
		analysisController.connect(countingPrinter, CountingPrinter.OUTPUT_PORT_NAME_RELAY_EVENT, traceAggr,
				TraceAggregationFilter.INPUT_PORT_NAME_TRACES);

		final Configuration configuration = new Configuration();
		configuration.setProperty(CountingPrintSink.CONF_THRESHOLD, Integer.toString(1));
		final CountingPrintSink countingPrintSink = new CountingPrintSink(configuration, analysisController);

		analysisController.connect(traceAggr, TraceAggregationFilter.OUTPUT_PORT_NAME_TRACES, countingPrintSink,
				CountingPrintSink.INPUT_PORT_NAME_EVENTS);
	}
}
