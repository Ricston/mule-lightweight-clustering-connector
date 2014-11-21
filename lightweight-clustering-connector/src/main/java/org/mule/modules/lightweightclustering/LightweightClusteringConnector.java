/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.lightweightclustering;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.lifecycle.Stop;
import org.mule.api.annotations.param.Optional;
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
	 * The Mule Context
	 */
	@Inject
	private MuleContext muleContext;

	/**
	 * The lightweight clustering manager
	 */
	private LightweightClusteringManager clusteringManager;

	/**
	 * The cluster instance name
	 */
	@Configurable
	@Optional
	private String instanceName;

	/**
	 * Initialise the connector by creating a cluster instance and initialise it. We had some issues with @Start and @Connect, hence we are using
	 * @PostConstruct. The reason is that with @Start, @Source was being invoked before @Start in a separate thread, while @Connect is only invoked lazily.
	 */
	@PostConstruct
	public synchronized void initialiseLightweightClusteringConnector() {
		if (clusteringManager == null) {
			clusteringManager = new LightweightClusteringManager(muleContext, instanceName);
			clusteringManager.initialiseCluster();
		}
	}

	/**
	 * Stop the connector by shutting down the clustering instance
	 */
	@Stop
	public synchronized void disposeLightweightClusteringConnector() {
		if (clusteringManager != null) {
			clusteringManager.disposeCluster();
			clusteringManager = null;
		}
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
	 * @return The Mule Context
	 */
	public MuleContext getMuleContext() {
		return muleContext;
	}

	/**
	 * 
	 * @param context
	 *            The Mule Context
	 */
	public void setMuleContext(MuleContext muleContext) {
		this.muleContext = muleContext;
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
		this.instanceName = instanceName;
	}

}