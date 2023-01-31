package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
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

	private long fibNum = 18;

	@Benchmark
	public void fibNaiveSpawnSync(Blackhole blackhole) throws InterruptedException {
		SpawnSyncFactory spawnSyncFactory = () -> { return new NaiveSpawnSync(); };
		var res = Fib.fib(fibNum, spawnSyncFactory);
		blackhole.consume(res);
	}

	@Benchmark
	public void fibNowaSpawnSync(Blackhole blackhole) throws InterruptedException {
		SpawnSyncFactory spawnSyncFactory = () -> { return new NowaSpawnSync(); };
		var res = Fib.fib(fibNum, spawnSyncFactory);
		blackhole.consume(res);
	}

	@Benchmark
	public void fibInlinedNowaSpawnSync(Blackhole blackhole) throws InterruptedException {
		SpawnSyncFactory spawnSyncFactory = () -> { return new InlinedNowaSpawnSync(); };
		var res = Fib.fib(fibNum, spawnSyncFactory);
		blackhole.consume(res);
	}

	@Benchmark
	public void fibStructuredTaskScopeSpawnSync(Blackhole blackhole) throws InterruptedException {
		SpawnSyncFactory spawnSyncFactory = () -> { return new StructuredTaskScopeSpawnSync(); };
		var res = Fib.fib(fibNum, spawnSyncFactory);
		blackhole.consume(res);
	}

}
