// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import jdk.internal.vm.annotation.Contended;

import eu.geekplace.beanstalk.core.loom.SpawnSync;
import eu.geekplace.beanstalk.core.loom.SpawnSyncFactory;

/**
 * An API for efficient fork/join parallelism with the {@link NowaSemaphore} inlined.
 */
public class InlinedNowaSpawnSync implements SpawnSync {

	private int requiredSignalCount;

	@Contended
	private volatile int counter = Integer.MAX_VALUE;
	private static final VarHandle COUNTER;
	static {
		var l = MethodHandles.lookup();
		try {
			COUNTER = l.findVarHandle(InlinedNowaSpawnSync.class, "counter", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new Error(e);
		}
	}

	private final Thread owner = Thread.currentThread();

	private void throwIfNotOwningThread() {
		if (!NowaConfiguration.CHECK_IF_THREAD_IS_OWNER) return;

		var currentThread = Thread.currentThread();
		if (currentThread != owner)
			throw new WrongThreadException("Current thread '" + currentThread + "' is not the owner ('" + owner + "')");
	}

	private static class NowaSupplier<T> implements Supplier<T> {
		private T value;

		@Override
		public T get() {
			return value;
		}
	}

	@SuppressWarnings("preview")
	public <T> Supplier<T> spawn(Callable<? extends T> fun) {
		throwIfNotOwningThread();
		var supplier = new NowaSupplier<T>();
		requiredSignalCount++;
		Thread.startVirtualThread(() -> {
			try {
				T result = fun.call();
				supplier.value = result;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				int oldCounter = (int) COUNTER.getAndAddRelease(this, -1);
				assert oldCounter >= 1;

				if (oldCounter > 1)
					// Counter is still non-zero after decrement, somebody else is responsible for
					// releasing a potential waiter.
					return;

				LockSupport.unpark(owner);
			}
		});
		return supplier;
	}

	public void sync() throws InterruptedException {
		throwIfNotOwningThread();

		int delta = Integer.MAX_VALUE - requiredSignalCount;

		if (((int) COUNTER.getOpaque(this)) - delta == 0)
			return;

		int oldCounter = (int) COUNTER.getAndAddAcquire(this, -delta);
		if (oldCounter == delta)
			return;

		while (((int) COUNTER.getAcquire(this)) > 0) {
			LockSupport.park(this);
			if (Thread.interrupted())
				throw new InterruptedException();
		}
	}

	public void syncAndReuse() throws InterruptedException {
		sync();
		requiredSignalCount = 0;
		counter = Integer.MAX_VALUE;
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
			return new InlinedNowaSpawnSync();
		}

	}
}
