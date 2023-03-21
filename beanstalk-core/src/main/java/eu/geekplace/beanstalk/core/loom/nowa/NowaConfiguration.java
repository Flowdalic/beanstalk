// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom.nowa;

public class NowaConfiguration {


	public static final boolean CHECK_IF_THREAD_IS_OWNER;
	static {
		boolean assertionsEnabled = NowaConfiguration.class.desiredAssertionStatus();
		if (assertionsEnabled) {
			CHECK_IF_THREAD_IS_OWNER = true;
		} else {
			String checkifThreadIsOwnerString = System
				.getProperty("eu.geekplace.beanstalk.core.loom.nowa.check-if-thread-is-owner", "false");
			CHECK_IF_THREAD_IS_OWNER = Boolean.valueOf(checkifThreadIsOwnerString);
		}
	}

}
