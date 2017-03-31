package kieker.test.toolsteetime.junit.writeRead.amqp;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import org.apache.qpid.server.Broker;
import org.apache.qpid.server.BrokerOptions;

import com.google.common.io.Files;

/**
 * We shouldn't need external things for testing
 */
public class EmbeddedAMQPBroker {

	private final int port;
	private final Broker broker = new Broker();
	private final BrokerOptions brokerOptions = new BrokerOptions();

	public EmbeddedAMQPBroker() {
		this.port = this.getFreePort();

		final String configPath = System.getProperty("user.dir")
				+ File.separator + "config"
				+ File.separator + "basicAmqpWriterReaderTest"
				+ File.separator;
		final String configFileName = "qpid-config.json";
		final String passwordFileName = "passwd.properties";

		// prepare options
		this.brokerOptions.setConfigProperty("broker.name", "embedded-broker");
		this.brokerOptions.setConfigProperty("qpid.amqp_port", String.valueOf(this.port));
		this.brokerOptions.setConfigProperty("qpid.pass_file", configPath + passwordFileName);
		this.brokerOptions.setConfigProperty("qpid.work_dir", Files.createTempDir().getAbsolutePath());
		this.brokerOptions.setInitialConfigurationLocation(configPath + configFileName);
	}

	public void start() throws Exception {
		this.broker.startup(this.brokerOptions);
	}

	public void stop() {
		this.broker.shutdown();
	}

	public int getPort() {
		return this.port;
	}

	private int getFreePort() {
		int port = 0;

		ServerSocket s;
		try {
			s = new ServerSocket(0);
			port = s.getLocalPort();
			s.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		return port;
	}

}
