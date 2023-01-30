// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**
 * An API for efficient fork/join parallelism using {@link NowaSemaphore}.
 */
public class NowaSpawnSync implements AutoCloseable {

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
}
