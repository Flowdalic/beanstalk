// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023-2025 Florian Schmaus
package eu.geekplace.beanstalk.core.loom;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import java.util.concurrent.StructuredTaskScope;

public class StructuredTaskScopeSpawnSync implements SpawnSync {

	private final StructuredTaskScope<Object> scope = new StructuredTaskScope<>();

	@Override
	@SuppressWarnings("preview")
	public <T> Supplier<T> spawn(Callable<? extends T> fun) {
		var subtask = scope.fork(fun);
		return new Supplier<T>() {
			@Override
			public T get() {
				return subtask.get();
			}
		};
	}

	@Override
	public void sync() throws InterruptedException {
		scope.join();
	}

	public void syncAndReuse() throws InterruptedException {
		sync();
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
			return new StructuredTaskScopeSpawnSync();
		}

	}
}
