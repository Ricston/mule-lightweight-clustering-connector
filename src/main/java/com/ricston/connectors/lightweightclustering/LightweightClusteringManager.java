/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connectors.lightweightclustering;

import org.mule.DefaultMuleContext;
import org.mule.api.MuleContext;
import org.mule.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.config.Config;
import com.hazelcast.core.Cluster;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

public class LightweightClusteringManager {

	private DefaultMuleContext context;

	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected LightWeightClusteringPollingController pollingController = new LightWeightClusteringPollingController(false);
	protected HazelcastInstance clusterInstance;
	protected String instanceName;

	public LightweightClusteringManager(MuleContext context) {
		this(context, null);
	}

	public LightweightClusteringManager(MuleContext context, String instanceName) {
		setMuleContext(context);
		setInstanceName(instanceName);
	}

	/**
	 * Start the cluster and add the listener to check if this node became the master to enable polling.
	 */
	public void initialiseCluster(boolean clusterSharedResources) {

		// set the Mule polling controller to our LightWeightClusteringPollingController
		context.setPollingController(pollingController);

		// initialise hazelcast
		clusterInstance = initialiseClusterInstance(instanceName);
		Cluster cluster = clusterInstance.getCluster();

		if (clusterSharedResources){
			// set listener to set polling master whenever a node is down
			cluster.addMembershipListener(new LightweightClusteringMembershipListener(this));
		}

		// check if we are the master polling instance
		configurePrimaryPollingInstance(cluster, clusterSharedResources);
	}

	/**
	 * Shutdown the cluster
	 */
	public void disposeCluster() {
		clusterInstance.shutdown();
	}

	/**
	 * Check if the current node is the first node in the cluster
	 * 
	 * @param cluster
	 *            The Hazelcast cluster
	 * @return
	 */
	protected boolean isMaster(Cluster cluster) {
		// The hazelcast documentation states that the first element is the oldest Member
		// http://docs.hazelcast.org/docs/3.3/javadoc/com/hazelcast/core/Cluster.html#getMembers()
		return cluster.getLocalMember() == cluster.getMembers()
													.iterator()
													.next();
	}

	/**
	 * Check if the current node is the master, or if we are clustering shared resources at all, if yes, set this node to poll
	 * 
	 * @param cluster
	 *            The Hazelcast cluster
	 * @param clusterSharedResources
	 *            Weather or not to cluster shared resources
	 */
	protected void configurePrimaryPollingInstance(Cluster cluster, boolean clusterSharedResources) {
		if (!clusterSharedResources || isMaster(cluster)) {
			pollingController.setPrimaryPollingInstance(true);
			logger.info("This node is now the PRIMARY polling instance");
		}
	}
	
	/**
	 * Check if the current node is the master, if yes, set this node to poll
	 * 
	 * @param cluster
	 *            The Hazelcast cluster
	 */
	protected void configurePrimaryPollingInstance(Cluster cluster) {
		configurePrimaryPollingInstance(cluster, true);
	}

	/**
	 * Create and start a Hazelcast cluster instance
	 * 
	 * @return Hazelcast cluster instance
	 */
	protected HazelcastInstance initialiseClusterInstance(String instanceName) {
		logger.info("Using cluster instance name of: " + instanceName);

		Config config = new Config();
		config.setInstanceName(instanceName);
		return Hazelcast.newHazelcastInstance(config);
	}

	/**
	 * Set the Mule Context
	 */
	public void setMuleContext(MuleContext context) {
		this.context = (DefaultMuleContext) context;
	}

	/**
	 * 
	 * @return The cluster instance name
	 */
	public String getInstanceName() {
		return instanceName;
	}

	/**
	 * 
	 * @param instanceName
	 *            The cluster instance name
	 */
	public void setInstanceName(String instanceName) {
		if (StringUtils.isNotBlank(instanceName)) {
			this.instanceName = instanceName;
		} else {
			instanceName = context.getConfiguration()
									.getId();
		}
	}

	/**
	 * 
	 * @return The Hazelcast clustering instance
	 */
	public HazelcastInstance getClusterInstance() {
		return clusterInstance;
	}

}
