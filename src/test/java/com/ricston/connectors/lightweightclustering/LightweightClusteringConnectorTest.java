/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package com.ricston.connectors.lightweightclustering;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.filefilter.TrueFileFilter;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mule.modules.tests.ConnectorTestCase;
import org.mule.util.FileUtils;

public class LightweightClusteringConnectorTest extends ConnectorTestCase {
	
	private static final File items = new File("./items");
	
	public LightweightClusteringConnectorTest(){
		this.setDisposeContextPerClass(true);
	}
	
	@BeforeClass
	public static void cleanDirectory() throws IOException{
//		remove items from the items folder
		FileUtils.deleteQuietly(items);
	}
    
    @Override
    protected String getConfigResources() {
        return "lightweight-clustering-config.xml";
    }

    @Test
    public void testPollingAndQueue() throws Exception {
    	
    	//poller is creating a message every second
    	//5sec of waiting time should create around 5 messages 
    	Thread.sleep(5000);
    	
    	Collection<?> itemFiles = FileUtils.listFiles(items, TrueFileFilter.INSTANCE, TrueFileFilter.INSTANCE);
    	Assert.assertTrue(itemFiles.size() >= 5);
    }
    
    @Test
    public void testObjectStore() throws Exception{
    	runFlow("objectStore-Store", "test");
    	runFlowAndExpect("objectStore-Retrieve", "test");
    	runFlowAndExpect("objectStore-Contains", true);
    	runFlow("objectStore-Remove", "test");
    	runFlowAndExpect("objectStore-Contains", false);
    }
}
