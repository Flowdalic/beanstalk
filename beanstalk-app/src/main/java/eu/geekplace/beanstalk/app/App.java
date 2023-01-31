package eu.geekplace.beanstalk.app;

import eu.geekplace.beanstalk.core.loom.nowa.NowaExamples;

public class App {

	public static void main(String[] args) throws InterruptedException {
		eu.geekplace.beanstalk.core.loom.examples.MinimalStructuredTaskScope.run();
		var test = new eu.geekplace.beanstalk.core.scala.BeanstalkScala();
		test.sayHello();

		long fibSixteen = NowaExamples.fib(16);
		System.out.println("fib(16): " + fibSixteen);
	}

}
