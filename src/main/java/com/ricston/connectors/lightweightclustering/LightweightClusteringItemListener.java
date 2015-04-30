/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connectors.lightweightclustering;

import java.io.Serializable;

import org.mule.api.callback.SourceCallback;

import com.hazelcast.core.IQueue;
import com.hazelcast.core.ItemEvent;
import com.hazelcast.core.ItemListener;

public class LightweightClusteringItemListener implements ItemListener<Serializable> {

	private IQueue<Serializable> queue;
	private SourceCallback callback;

	public LightweightClusteringItemListener(IQueue<Serializable> queue, SourceCallback callback) {
		this.queue = queue;
		this.callback = callback;
	}

	@Override
	public void itemAdded(ItemEvent<Serializable> item) {
		try {
			Serializable addedItem = queue.poll();
			if (addedItem != null) {
				callback.process(addedItem);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void itemRemoved(ItemEvent<Serializable> item) {
		// do not do anything here
		// we are only interested when a new item is added
	}

}
