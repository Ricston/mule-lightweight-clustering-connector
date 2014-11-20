/**
 * (c) 2003-2014 Ricston, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

/**
 * (c) 2003-2014 MuleSoft, Inc. The software in this package is published under the terms of the CPAL v1.0 license,
 * a copy of which has been included with this distribution in the LICENSE.md file.
 */

package org.mule.modules.lightweightclustering;

import org.junit.Test;
import org.mule.modules.tests.ConnectorTestCase;

public class LightweightClusteringConnectorTest extends ConnectorTestCase {
    
    @Override
    protected String getConfigResources() {
        return "lightweight-clustering-config.xml";
    }

    @Test
    public void testFlow() throws Exception {
    	Thread.sleep(300000);
//        runFlowAndExpect("testFlow", "Another string");
    }
}
