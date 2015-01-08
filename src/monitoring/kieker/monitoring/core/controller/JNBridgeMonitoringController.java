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

package kieker.monitoring.core.controller;

import java.util.LinkedList;
import java.util.Queue;

import kieker.common.configuration.Configuration;
import kieker.common.record.IMonitoringRecord;
import kieker.monitoring.core.configuration.ConfigurationFactory;
import kieker.monitoring.timer.ITimeSource;

/**
 * @author Rexhep Hamiti
 *
 * @since 1.11
 */
public class JNBridgeMonitoringController {

	private final Queue<JNBridgeMonitoringController> recordQueue;
	private final TimeSourceController timeSourceController;
	private final WriterController writerController;

	private JNBridgeMonitoringController(final Configuration configuration) {
		this.timeSourceController = new TimeSourceController(configuration);
		this.writerController = new WriterController(configuration);
		this.recordQueue = new LinkedList<JNBridgeMonitoringController>();
	}

	public final boolean newMonitoringRecord(final IMonitoringRecord record) {
		return this.writerController.newMonitoringRecord(record);
	}

	public final ITimeSource getTimeSource() {
		return this.timeSourceController.getTimeSource();
	}

	// GET SINGLETON INSTANCE
	// #############################
	public static final IMonitoringController getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * SINGLETON.
	 */
	private static final class LazyHolder { // NOCS
		static final IMonitoringController INSTANCE = MonitoringController.createInstance(ConfigurationFactory.createSingletonConfiguration()); // NOPMD package
	}
}
