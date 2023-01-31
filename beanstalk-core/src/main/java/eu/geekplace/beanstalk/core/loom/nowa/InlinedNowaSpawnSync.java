// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

import eu.geekplace.beanstalk.core.loom.SpawnSync;

/**
 * An API for efficient fork/join parallelism with the {@link NowaSemaphore} inlined.
 */
public class InlinedNowaSpawnSync implements SpawnSync {

	private int requiredSignalCount;

	private volatile int counter = Integer.MAX_VALUE;
	private static final VarHandle COUNTER;
	private volatile boolean signalled = false;
	private static final VarHandle SIGNALLED;
	static {
		var l = MethodHandles.lookup();
		try {
			COUNTER = l.findVarHandle(InlinedNowaSpawnSync.class, "counter", int.class);
			SIGNALLED = l.findVarHandle(InlinedNowaSpawnSync.class, "signalled", boolean.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new Error(e);
		}
	}

	private final Thread owner = Thread.currentThread();

	private void throwIfNotOwningThread() {
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

				SIGNALLED.setRelease(this, true);
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

		while (!((boolean) SIGNALLED.getAcquire(this))) {
			LockSupport.park(this);
			if (Thread.interrupted())
				throw new InterruptedException();
		}
	}

	public void syncAndReuse() throws InterruptedException {
		sync();
		requiredSignalCount = 0;
		counter = Integer.MAX_VALUE;
		signalled = false;
	}

	@Override
	public void close() throws InterruptedException {
		sync();
	}
}
