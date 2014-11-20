/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.lightweightclustering;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.core.MembershipEvent;
import com.hazelcast.core.MembershipListener;

public class LightweightClusteringMembershipListener implements MembershipListener{
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected LightweightClusteringManager clusteringManager;
	
	public LightweightClusteringMembershipListener(LightweightClusteringManager clusteringManager){
		this.clusteringManager = clusteringManager;
	}

	@Override
	public void memberAdded(MembershipEvent membershipEvent) {
		logger.info("Added member to cluster with uid: " + membershipEvent.getMember().getUuid());
	}

	@Override
	public void memberRemoved(MembershipEvent membershipEvent) {
		logger.info("Dropped member from cluster with uid: " + membershipEvent.getMember().getUuid());
		clusteringManager.configurePrimaryPollingInstance(membershipEvent.getCluster());
	}
	
	
}