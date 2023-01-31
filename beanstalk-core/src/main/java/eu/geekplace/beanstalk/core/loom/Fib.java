package eu.geekplace.beanstalk.core.loom;

public class Fib {

	public static long fib(long num, SpawnSyncFactory spawnSyncFactory) throws InterruptedException {
		if (num < 2)
			return num;

		var spawnSyncApi = spawnSyncFactory.create();

		var resA = spawnSyncApi.spawn(() -> fib(num - 1, spawnSyncFactory));
		var resB = fib(num - 2, spawnSyncFactory);

		spawnSyncApi.sync();

		return resA.get() + resB;
	}
}
