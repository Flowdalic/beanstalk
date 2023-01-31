package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface SpawnSync extends AutoCloseable {

	public <T> Supplier<T> spawn(Callable<? extends T> fun);

	public void sync() throws InterruptedException;

	public void syncAndReuse() throws InterruptedException;

}
