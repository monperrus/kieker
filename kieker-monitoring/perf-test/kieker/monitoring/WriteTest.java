/***************************************************************************
 * Copyright 2015 Kieker Project (http://kieker-monitoring.net)
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

package kieker.monitoring;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.SeekableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Christian Wulf (chw)
 *
 * @since 1.13
 */
public class WriteTest {

	// private static final int WARMUP_ITERATIONS = 2;
	private static final int CAPACITY = 8192;
	private static final int ITERATIONS = 200000;

	// chw-home measurements:
	// writeByteBuffer: 1700 ms
	// writeDataOutputStream: 5000 ms
	// writeWrappedByteBuffer: 1700 ms

	// @BeforeClass
	// public static void beforeClass() throws Exception {
	// final WriteTest warmupInstance = new WriteTest();
	// for (int i = 0; i < WARMUP_ITERATIONS; i++) {
	// warmupInstance.writeDataOutputStream();
	// warmupInstance.writeByteBuffer();
	// warmupInstance.writeWrappedByteBuffer();
	// }
	// }

	@Test
	@Ignore
	public void writeByteBuffer() throws Exception {
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY);

		final Path path = Files.createTempFile("bytebuffer", null);
		final SeekableByteChannel fileChannel = Files.newByteChannel(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);

		final long start = System.nanoTime();

		for (long i = 0; i < ITERATIONS; i++) {
			byteBuffer.clear();

			long value = i;
			for (int j = 0; j < (CAPACITY / 8); j++) {
				byteBuffer.putLong(value++);
			}

			byteBuffer.flip();
			fileChannel.write(byteBuffer);
		}

		fileChannel.close();

		final long end = System.nanoTime();

		final long duration = end - start;

		System.out.println("WriteTest.writeByteBuffer(): " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms");
	}

	@Test
	public void writeDataOutputStream() throws Exception {
		final Path path = Files.createTempFile("dataoutputstream", null);
		OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		// important for performance: instantiate zip stream before buffered stream
		outputStream = new GZIPOutputStream(outputStream);
		outputStream = new BufferedOutputStream(outputStream, CAPACITY);
		final DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

		final long start = System.nanoTime();

		for (long i = 0; i < ITERATIONS; i++) {
			long value = i;
			for (int j = 0; j < (CAPACITY / 8); j++) {
				dataOutputStream.writeLong(value++);
			}
		}

		dataOutputStream.close();

		final long end = System.nanoTime();

		final long duration = end - start;

		System.out.println("WriteTest.writeDataOutputStream(): " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms");
	}

	@Test
	public void writeWrappedByteBuffer() throws Exception {
		final ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY);

		final Path path = Files.createTempFile("wrappedbytebuffer", null);
		OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		// outputStream = new BufferedOutputStream(outputStream, CAPACITY);
		outputStream = new GZIPOutputStream(outputStream);

		final WritableByteChannel channel = Channels.newChannel(outputStream);

		final long start = System.nanoTime();

		for (long i = 0; i < ITERATIONS; i++) {
			byteBuffer.clear();

			long value = i;
			for (int j = 0; j < (CAPACITY / 8); j++) {
				byteBuffer.putLong(value++);
			}

			byteBuffer.flip();
			channel.write(byteBuffer);
		}

		channel.close();

		final long end = System.nanoTime();

		final long duration = end - start;

		System.out.println("WriteTest.writeWrappedByteBuffer(): " + TimeUnit.NANOSECONDS.toMillis(duration) + " ms");
	}
}
