package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

import eu.geekplace.beanstalk.core.loom.nowa.NowaSpawnSync;
import eu.geekplace.beanstalk.core.loom.nowa.InlinedNowaSpawnSync;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 3, time = 10)
@Measurement(iterations = 5, time = 10)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
public class SpawnSyncJmh {

	@Param({"18"})
	private long fibNum;

	@Param
	private SpawnSyncImplementation spawnSyncImplementation;

	@Benchmark
	public void fib(Blackhole blackhole) throws InterruptedException  {
		var spawnSyncFactory = spawnSyncImplementation.factory;
		var res = Fib.fib(fibNum, spawnSyncFactory);
		blackhole.consume(res);
	}

}
