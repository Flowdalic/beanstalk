package eu.geekplace.beanstalk.core.loom;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public class NaiveSpawnSync implements SpawnSync {
	private final Thread owner = Thread.currentThread();

	private volatile int childCount;
	private static final VarHandle CHILD_COUNT;
	static {
		try {
			CHILD_COUNT = MethodHandles.lookup().findVarHandle(NaiveSpawnSync.class, "childCount", int.class);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new Error(e);
		}
	}

	private static class MySupplier<T> implements Supplier<T> {
		private T value;

		@Override
		public T get() {
			return value;
		}
	}

	@Override
	@SuppressWarnings("preview")
	public <T> Supplier<T> spawn(Callable<? extends T> fun) {
		var supplier = new MySupplier<T>();
		CHILD_COUNT.getAndAddRelease(this, 1);
		Thread.startVirtualThread(() -> {
			try {
				T result = fun.call();
				supplier.value = result;
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				int previousChildCount = (int) CHILD_COUNT.getAndAddRelease(this, -1);
				if (previousChildCount == 1)
					LockSupport.unpark(owner);
			}
		});
		return supplier;
	}

	@Override
	public void sync() throws InterruptedException {
		while (((int) CHILD_COUNT.getAcquire(this)) != 0) {
			LockSupport.park(this);
			if (Thread.interrupted()) throw new InterruptedException();
		}
	}

	public void syncAndReuse() throws InterruptedException {
		sync();
	}

	@Override
	public void close() throws InterruptedException {
		sync();
	}
}
