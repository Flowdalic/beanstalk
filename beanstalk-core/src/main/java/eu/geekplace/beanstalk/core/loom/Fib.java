// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom;

import java.util.function.Supplier;

public class Fib {

	public static long fib(long num, SpawnSyncFactory spawnSyncFactory) throws InterruptedException {
		return pseudoFib(num, spawnSyncFactory, 2);
	}

	public static long pseudoFib(long num, SpawnSyncFactory spawnSyncFactory, int breadth) throws InterruptedException {
		if (num < breadth)
			return num;

		var spawnSyncApi = spawnSyncFactory.create();

		var spawnCount = breadth - 1;
		@SuppressWarnings("unchecked")
		Supplier<Long>[] asyncResults = new Supplier[spawnCount];
		for (int i = 0; i < spawnCount; i++) {
			long newNum = num - (i + 1);
			asyncResults[i] = spawnSyncApi.spawn(() -> pseudoFib(newNum, spawnSyncFactory, breadth));
		}
		var result = pseudoFib(num - breadth, spawnSyncFactory, breadth);

		spawnSyncApi.sync();

		for (var asyncResult : asyncResults) {
			result += asyncResult.get();
		}
		return result;
	}
}
