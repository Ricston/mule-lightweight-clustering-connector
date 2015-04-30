/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connectors.lightweightclustering;

import org.mule.transport.PollingController;

public class LightWeightClusteringPollingController implements PollingController {

	private boolean primaryPollingInstance;

	public LightWeightClusteringPollingController(boolean primaryPollingInstance) {
		this.primaryPollingInstance = primaryPollingInstance;
	}

	public void setPrimaryPollingInstance(boolean primaryPollingInstance) {
		this.primaryPollingInstance = primaryPollingInstance;
	}

	@Override
	public boolean isPrimaryPollingInstance() {
		return primaryPollingInstance;
	}

}
