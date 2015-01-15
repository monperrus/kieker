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

/**
 * @author Rexhep Hamiti
 *
 * @since 1.11
 */
public class JNBridgeMonitoringController {

	private final Queue<IMonitoringRecord> recordQueue;
	private final long offset;

	public JNBridgeMonitoringController() {
		this.recordQueue = new LinkedList<IMonitoringRecord>();
		this.offset = System.nanoTime();
	}

	/**
	 *
	 */
	public final boolean newMonitoringRecord(final IMonitoringRecord record) {
		return this.recordQueue.add(record);
	}

	/**
	 *
	 */
	public final long getTime() {
		return System.nanoTime() - this.offset;
	}

	public final Queue<IMonitoringRecord> getQueue() {
		return this.recordQueue;
	}

	// GET SINGLETON INSTANCE
	// #############################
	public static final JNBridgeMonitoringController getInstance() {
		return LazyHolder.INSTANCE;
	}

	/**
	 * SINGLETON.
	 */
	private static final class LazyHolder { // NOCS
		static final JNBridgeMonitoringController INSTANCE = JNBridgeMonitoringController.createInstance(ConfigurationFactory.createSingletonConfiguration()); // NOPMD
		// package
	}

	public static JNBridgeMonitoringController createInstance(final Configuration createSingletonConfiguration) {
		return new JNBridgeMonitoringController();
	}
}
