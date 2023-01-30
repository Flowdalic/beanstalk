// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

/**
 * Examples using the {@link NowaSpawnSync} API for efficient fork/join parallelism.
 */
public class NowaExamples {

	public static void main(String[] args) throws InterruptedException {
		long res = fib(12);
		System.out.println("Res: " + res);
	}

	public static long fib(long num) throws InterruptedException {
		if (num < 2)
			return num;

		var nowa = new NowaSpawnSync();

		var resA = nowa.spawn(() -> fib(num - 1));
		var resB = fib(num - 2);

		nowa.sync();

		return resA.get() + resB;
	}

}
