/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.lightweightclustering;

import java.io.Serializable;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.mule.api.MuleContext;
import org.mule.api.annotations.Configurable;
import org.mule.api.annotations.Connector;
import org.mule.api.annotations.Processor;
import org.mule.api.annotations.Source;
import org.mule.api.annotations.param.Optional;
import org.mule.api.annotations.param.Payload;
import org.mule.api.callback.SourceCallback;
import org.mule.api.store.ObjectStore;
import org.mule.api.store.ObjectStoreException;

import com.hazelcast.core.IMap;
import com.hazelcast.core.IQueue;

/**
 * Mule Lightweight Clustering Connector
 * 
 * @author Ricston Ltd.
 */
@Connector(name = "lightweight-clustering", schemaVersion = "1.0", friendlyName = "LightweightClustering")
public class LightweightClusteringConnector implements ObjectStore<Serializable> {

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
	 * Weather or not to only allow one instance to poll
	 */
	@Configurable
	@Optional
	private Boolean clusterSharedResources = true;
	
	private IMap<Serializable, Serializable> objectStore;

	/**
	 * Initialise the connector by creating a cluster instance and initialise it. We had some issues with <code>@Start and <code>@Connect</code>, hence we are
	 * using <code>@PostConstruct</code>. The reason is that with <code>@Start</code>, <code>@Source</code> was being invoked before <code>@Start</code> in a
	 * separate thread, while <code>@Connect</code> is only invoked lazily. See https://www.mulesoft.org/jira/browse/DEVKIT-1184
	 */
	@PostConstruct
	public synchronized void initialiseLightweightClusteringConnector() {
		if (clusteringManager == null) {
			clusteringManager = new LightweightClusteringManager(muleContext, instanceName);
			clusteringManager.initialiseCluster(clusterSharedResources);
			
			objectStore = clusteringManager.getClusterInstance().getMap("objectStore");
		}
	}

	/**
	 * Stop the connector by shutting down the clustering instance
	 */
	@PreDestroy
	public synchronized void disposeLightweightClusteringConnector() {
		if (clusteringManager != null) {
			objectStore.clear();
			objectStore = null;
			
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

	/**
	 * 
	 * @return Weather or not to only allow one instance to poll
	 */
	public boolean isClusterSharedResources() {
		return clusterSharedResources;
	}

	/**
	 * 
	 * @param clusterSharedResources Weather or not to only allow one instance to poll
	 */
	public void setClusterSharedResources(boolean clusterSharedResources) {
		this.clusterSharedResources = clusterSharedResources;
	}

	@Override
	public boolean contains(Serializable key) throws ObjectStoreException {
		return objectStore.containsKey(key);
	}

	@Override
	public void store(Serializable key, Serializable value) throws ObjectStoreException {
		objectStore.put(key, value);
	}

	@Override
	public Serializable retrieve(Serializable key) throws ObjectStoreException {
		return objectStore.get(key);
	}

	@Override
	public Serializable remove(Serializable key) throws ObjectStoreException {
		return objectStore.remove(key);
	}

	@Override
	public boolean isPersistent() {
		return false;
	}

	@Override
	public void clear() throws ObjectStoreException {
		objectStore.clear();
	}

}