// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import eu.geekplace.beanstalk.core.loom.SpawnSync;
import eu.geekplace.beanstalk.core.loom.SpawnSyncFactory;

/**
 * An API for efficient fork/join parallelism using {@link NowaSemaphore}. Note
 * that the code in this class is better readable, but the most efficient
 * implementation is provided by {@link InlinedNowaSpawnSync}.
 */
public class NowaSpawnSync implements SpawnSync {

	private final NowaSemaphore nowaSemaphore = new NowaSemaphore();

	private static class NowaSupplier<T> implements Supplier<T> {
		private T value;

		@Override
		public T get() {
			return value;
		}
	}

	@SuppressWarnings("preview")
	public <T> Supplier<T> spawn(Callable<? extends T> fun) {
		var supplier = new NowaSupplier<T>();
		nowaSemaphore.increment();
		Thread.startVirtualThread(() -> {
			try {
				T result = fun.call();
				supplier.value = result;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				nowaSemaphore.signal();
			}
		});
		return supplier;
	}

	public void sync() throws InterruptedException {
		nowaSemaphore.ownerAwait();
	}

	public void syncAndReuse() throws InterruptedException {
		sync();
		nowaSemaphore.reset();
	}

	@Override
	public void close() throws InterruptedException {
		sync();
	}

	public static final Factory FACTORY = new Factory();

	public static class Factory implements SpawnSyncFactory {

		private Factory() {
		}

		@Override
		public SpawnSync create() {
			return new NowaSpawnSync();
		}

	}
}
