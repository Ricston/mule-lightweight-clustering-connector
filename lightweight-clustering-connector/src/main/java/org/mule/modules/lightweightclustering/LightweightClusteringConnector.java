/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.lightweightclustering;

import java.io.Serializable;

import javax.inject.Inject;

import org.mule.api.ConnectionException;
import org.mule.api.MuleContext;
import org.mule.api.annotations.Connect;
import org.mule.api.annotations.ConnectStrategy;
import org.mule.api.annotations.ConnectionIdentifier;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Disconnect;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.ValidateConnection;
import org.mule.api.annotations.param.ConnectionKey;
import org.mule.api.annotations.param.Payload;
import org.mule.api.callback.SourceCallback;

import com.hazelcast.core.IQueue;

/**
 * Mule Lightweight Clustering Connector 
 * 
 * @author Ricston Ltd.
 */
@Connector(name = "lightweight-clustering", schemaVersion = "1.0", friendlyName = "LightweightClustering")
public class LightweightClusteringConnector {
	/**
	 * The cluster instance name
	 */
//	@Optional
//	@Configurable
//	private String instanceName;

	/**
	 * The Mule Context
	 */
	@Inject
	private MuleContext context;

	/**
	 * The lightweight clustering manager
	 */
	private LightweightClusteringManager clusteringManager;

	/**
	 * Initialise the connector by creating a cluster instance and initialise it
	 */
//	@Start
	@Connect
	public void initialiseLightweightClusteringConnector(@ConnectionKey String instanceName) throws ConnectionException {
		clusteringManager = new LightweightClusteringManager(context, instanceName);
		clusteringManager.initialiseCluster();
	}

	/**
	 * Stop the connector by shutting down the clustering instance
	 */
//	@Stop
	@Disconnect
	public void disposeLightweightClusteringConnector() {
		clusteringManager.disposeCluster();
	}
	
	@ConnectionIdentifier
	public String connectionIdentifier(){
		return "001";
	}
	
	@ValidateConnection
	public boolean validateConnection(){
		return clusteringManager != null;
	}
	

	/**
	 * Enqueue message from clustered queue
	 * 
	 * {@sample.xml ../../../doc/lightweight-clustering-connector.xml.sample lightweight-clustering:enqueue}
	 * 
	 * @param queue
	 *            The queue name
	 * @param content
	 *            The payload to be enqueued on the clustered queue
	 * @throws InterruptedException
	 *             Thrown in case enqueuing operation throws an error
	 */
	@Processor
	public void enqueue(String queue, @Payload Serializable content) throws InterruptedException {

		IQueue<Serializable> clusteredQueue = clusteringManager.getClusterInstance()
																.getQueue(queue);
		clusteredQueue.put(content);
	}

	/**
	 * Retrieve a message from the clustered queue
	 * 
	 * {@sample.xml ../../../doc/lightweight-clustering-connector.xml.sample lightweight-clustering:dequeue}
	 * 
	 * @param queue
	 *            The queue name
	 * @param callback
	 *            Mule SourceCallback, will be called when a message appears on the queue
	 */
	@Source
	public void dequeue(String queue, SourceCallback callback) {
		IQueue<Serializable> clusteredQueue = clusteringManager.getClusterInstance()
																.getQueue(queue);
		clusteredQueue.addItemListener(new LightweightClusteringItemListener(clusteredQueue, callback), false);
	}

	/**
	 * 
	 * @return The cluster instance name
	 */
//	public String getInstanceName() {
//		return instanceName;
//	}

	/**
	 * 
	 * @param instanceName
	 *            The cluster instance name
	 */
//	public void setInstanceName(String instanceName) {
//		this.instanceName = instanceName;
//	}

	/**
	 * 
	 * @return The Mule Context
	 */
	public MuleContext getContext() {
		return context;
	}

	/**
	 * 
	 * @param context
	 *            The Mule Context
	 */
	public void setContext(MuleContext context) {
		this.context = context;
	}

}