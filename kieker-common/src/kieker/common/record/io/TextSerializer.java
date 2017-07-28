package kieker.common.record.io;

import java.io.PrintWriter;

public class TextSerializer implements IValueSerializer {

	private final PrintWriter printWriter;

	public TextSerializer(final PrintWriter fileWriter) {
		this.printWriter = fileWriter;
	}

	@Override
	public void putBoolean(boolean value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putByte(byte value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putInt(int value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putLong(long value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putDouble(double value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putBytes(byte[] value) {
		// TODO Auto-generated method stub

	}

	@Override
	public void putString(String value) {
		// TODO Auto-generated method stub

	}

}
