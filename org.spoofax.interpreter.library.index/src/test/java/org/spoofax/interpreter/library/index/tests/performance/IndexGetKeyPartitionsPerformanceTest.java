package org.spoofax.interpreter.library.index.tests.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.spoofax.interpreter.library.index.IndexPartitionDescriptor;

import com.carrotsearch.junitbenchmarks.BenchmarkOptions;
import com.carrotsearch.junitbenchmarks.BenchmarkRule;
import com.carrotsearch.junitbenchmarks.Clock;

@BenchmarkOptions(benchmarkRounds = 5, warmupRounds = 3, callgc = true, clock = Clock.CPU_TIME)
@RunWith(value = Parameterized.class)
public class IndexGetKeyPartitionsPerformanceTest extends IndexPerformanceTest {
    @Rule
    public BenchmarkRule benchmarkRun;

    public IndexGetKeyPartitionsPerformanceTest(int numItems, int numFiles, boolean startTransaction) {
        super(numItems, numFiles, startTransaction);

        try {
            benchmarkRun =
                new BenchmarkRule(new CSVResultsConsumer((this.numItems * 5) + "," + this.numFiles, new FileWriter(
                    "get-key-partitions_" + this.numFiles + "_" + indexTypeString() + ".csv", true)));
        } catch(IOException e) {
            e.printStackTrace();
        }

        index.clearAll();

        for(int i = 0; i < this.numItems; ++i) {
            index.add(def1, getNextFile());
            index.add(def2, getNextFile());
            index.add(def3, getNextFile());
            index.add(use1, getNextFile());
            index.add(type1, getNextFile());
        }
    }

    @Test
    public void getKeyPartitions() {
        startTransaction();
        
        HashSet<IndexPartitionDescriptor> files = new HashSet<IndexPartitionDescriptor>();
        files.addAll(index.getPartitionsOf(def1));
        files.addAll(index.getPartitionsOf(def2));
        files.addAll(index.getPartitionsOf(def3));
        files.addAll(index.getPartitionsOf(use1));
        files.addAll(index.getPartitionsOf(typeTemplate1));
        
        endTransaction();
    }
}
