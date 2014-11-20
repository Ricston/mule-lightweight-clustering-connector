package com.ricston.clustering;

import java.util.Collection;

import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.context.MuleContextAware;
import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.registry.MuleRegistry;
import org.mule.api.source.MessageSource;
import org.mule.api.transport.MessageReceiver;
import org.mule.construct.Flow;
import org.mule.context.notification.MuleContextNotification;
import org.mule.transport.AbstractConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PollDiscovery implements MuleContextAware, MuleContextNotificationListener<MuleContextNotification> {

	private MuleContext context;

	protected Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	public void setMuleContext(MuleContext context) {
		this.context = context;
	}

	public void initialise() throws MuleException {
		MuleRegistry registry = context.getRegistry();
		Collection<FlowConstruct> flows = registry.lookupFlowConstructs();

		for (FlowConstruct flowConstruct : flows) {
			Flow flow = (Flow) flowConstruct;
			logger.info("Analysing flow: " + flow.getName());

			MessageSource source = flow.getMessageSource();

			if (source instanceof InboundEndpoint) {
				InboundEndpoint inboundEndpoint = (InboundEndpoint) source;
				AbstractConnector connector = (AbstractConnector) inboundEndpoint.getConnector();
				MessageReceiver receiver = connector.getReceiver(flow, inboundEndpoint);
				receiver.stop();
				inboundEndpoint.stop();
			}
			logger.info(source.getClass()
								.getName());

		}

	}

	@Override
	public void onNotification(MuleContextNotification notification) {

		try {
			switch (notification.getAction()) {
			case MuleContextNotification.CONTEXT_STARTED: {
				initialise();
			}
				break;
			}
		} catch (MuleException e) {
			e.printStackTrace();
		}

	}

}
