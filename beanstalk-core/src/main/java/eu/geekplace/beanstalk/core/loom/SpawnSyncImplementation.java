// SPDX-License-Identifier: GPL-2.0-with-classpath-exception
// Copyright © 2023 Florian Schmaus
package eu.geekplace.beanstalk.core.loom;

import eu.geekplace.beanstalk.core.loom.nowa.InlinedNowaSpawnSync;
import eu.geekplace.beanstalk.core.loom.nowa.NowaSpawnSync;

public enum SpawnSyncImplementation {

	naive(NaiveSpawnSync.FACTORY),
	nowa(NowaSpawnSync.FACTORY),
	nowaInlined(InlinedNowaSpawnSync.FACTORY),
	structuredTaskScope(StructuredTaskScopeSpawnSync.FACTORY)
	;

	public final SpawnSyncFactory factory;

	SpawnSyncImplementation(SpawnSyncFactory factory) {
		this.factory = factory;
	}

	public SpawnSync create() {
		return factory.create();
	}
}
