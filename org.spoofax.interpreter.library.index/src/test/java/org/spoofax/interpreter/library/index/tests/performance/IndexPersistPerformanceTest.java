package org.spoofax.interpreter.library.index.tests.performance;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectOutputStream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spoofax.interpreter.library.index.IndexEntry;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;
import com.google.common.collect.Iterables;

@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 3, callgc = true, clock = Clock.CPU_TIME)
@RunWith(value = Parameterized.class)
public class IndexPersistPerformanceTest extends IndexPerformanceTest {
	@Rule
	public BenchmarkRule benchmarkRun;

	public IndexPersistPerformanceTest(int numItems, int numFiles) {
		super(numItems, numFiles);

		try {
			benchmarkRun =
				new BenchmarkRule(new CSVResultsConsumer((this.numItems * 5) + "," + this.numFiles, new FileWriter(
					"persist_" + this.numFiles + ".csv", true)));
		} catch(IOException e) {
			e.printStackTrace();
		}

		index.reset();

		for(int i = 0; i < this.numItems; ++i) {
			collect(def1, getNextFile());
			collect(def2, getNextFile());
			collect(def3, getNextFile());
			collect(use1, getNextFile());
			collect(type1, getNextFile());
		}
	}

	@Test
	public void persist() throws IOException {
		ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream("persist.dat", false));
		outputStream.writeObject(Iterables.toArray(index.getAll(), IndexEntry.class));
		outputStream.flush();
		outputStream.close();
	}
}
