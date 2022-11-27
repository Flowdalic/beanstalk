package eu.geekplace.beanstalk.core.loom;

import jdk.incubator.concurrent.StructuredTaskScope;

public class MinimalStructuredTaskScope {

    public static void run() throws InterruptedException {
        try (var scope = new StructuredTaskScope<>()) {
            scope.fork(() -> { System.out.println("Hello first"); return null; });
            scope.fork(() -> { System.out.println("Hello second"); return null; });

            scope.join();
        }
    }
}
