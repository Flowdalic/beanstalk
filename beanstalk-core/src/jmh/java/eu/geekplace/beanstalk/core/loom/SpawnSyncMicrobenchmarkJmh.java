// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2025 Florian Schmaus
package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
@BenchmarkMode(Mode.Throughput)
@Warmup(time = 20, timeUnit = TimeUnit.SECONDS)
@Measurement(time = 30, timeUnit = TimeUnit.SECONDS)
@Threads(value = Threads.MAX)
@Fork(value = 1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
public class SpawnSyncMicrobenchmarkJmh {

	@Param({"3"})
	private int spawns;

	@Param({"true"})
	private boolean yield;

	@Param
	private SpawnSyncImplementation spawnSyncImplementation;

	@Benchmark
	public void bench(Blackhole blackhole) throws InterruptedException  {
		var spawnSyncFactory = spawnSyncImplementation.factory;
		var spawnSyncApi = spawnSyncFactory.create();

		Supplier<Integer>[] asyncResults = new Supplier[spawns];
		for (int i = 0; i < spawns; i++) {
			asyncResults[i] = spawnSyncApi.spawn(() -> Integer.valueOf(1));
		}
		if (yield) Thread.yield();
		spawnSyncApi.sync();

		int res = 0;
		for (int i = 0; i < spawns; i++) {
			res += asyncResults[i].get();
		}
		blackhole.consume(res);
	}
}
