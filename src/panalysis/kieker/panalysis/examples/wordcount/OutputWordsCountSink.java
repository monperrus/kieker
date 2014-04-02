package kieker.panalysis.examples.wordcount;

import java.io.File;
import java.util.List;

import kieker.panalysis.base.AbstractSink;

public class OutputWordsCountSink extends AbstractSink<OutputWordsCountSink.INPUT_PORT> {

	static public enum INPUT_PORT {
		FILE_WORDCOUNT_TUPLE
	}

	private long overallDuration;
	private int numFiles = 0;

	public OutputWordsCountSink() {
		super(INPUT_PORT.class);
	}

	public void execute() {
		final long start = System.currentTimeMillis();

		final List<?> tuple = (List<?>) this.take(INPUT_PORT.FILE_WORDCOUNT_TUPLE);
		final File file = (File) tuple.get(0);
		final Number wordsCount = (Number) tuple.get(1);
		System.out.println(wordsCount + " words in file '" + file.getAbsolutePath() + "'");
		this.numFiles++;

		final long end = System.currentTimeMillis();
		final long duration = end - start;
		this.overallDuration += duration;
	}

	public long getOverallDuration() {
		return this.overallDuration;
	}

	public int getNumFiles() {
		return this.numFiles;
	}

}
