module eu.geekplace.beanstalk.core {
	exports eu.geekplace.beanstalk.core.loom.examples;
	exports eu.geekplace.beanstalk.core.loom.nowa;
	exports eu.geekplace.beanstalk.core.loom;
	exports eu.geekplace.beanstalk.core.scala;

	requires jdk.incubator.concurrent;
	requires org.scala.lang.scala3.library;
}
