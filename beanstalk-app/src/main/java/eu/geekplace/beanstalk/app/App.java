package eu.geekplace.beanstalk.app;

public class App {

	public static void main(String[] args) throws InterruptedException {
		eu.geekplace.beanstalk.core.loom.MinimalStructuredTaskScope.run();
		var test = new eu.geekplace.beanstalk.core.scala.BeanstalkScala();
		test.sayHello();
	}

}
