// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public interface SpawnSync extends AutoCloseable {

	<T> Supplier<T> spawn(Callable<? extends T> fun);

	void sync() throws InterruptedException;

	void syncAndReuse() throws InterruptedException;

}
