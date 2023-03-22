// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class NowaTest {

	@Test
	public void fib() throws InterruptedException {
		long res = NowaExamples.fib(16);
		assertEquals(987L, res);
	}

}
