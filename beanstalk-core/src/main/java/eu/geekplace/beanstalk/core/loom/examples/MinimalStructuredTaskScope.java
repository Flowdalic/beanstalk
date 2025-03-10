// SPDX-License-Identifier: Apache-2.0
// Copyright © 2023-2025 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.examples;

import java.util.concurrent.StructuredTaskScope;

public class MinimalStructuredTaskScope {

	public static void run() throws InterruptedException {
		try (var scope = new StructuredTaskScope<>()) {
			scope.fork(() -> { System.out.println("Hello first"); return null; });
			scope.fork(() -> { System.out.println("Hello second"); return null; });

			scope.join();
		}
	}

}
