package com.example;

import com.example.model.Message;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Path("/")
public class SubscriberResource {

    private static final Logger LOG = Logger.getLogger(SubscriberResource.class);

    @ConfigProperty(name = "dapr.pubsub.name")
    String pubsubName;

    @Path("/messages")
    @POST
    @Consumes(MediaType.WILDCARD)
    @Produces(MediaType.APPLICATION_JSON)
    @Topic(name = "${dapr.pubsub.topic}", pubsubName = "${dapr.pubsub.name}")
    public void receiveMessage(CloudEvent<Message> cloudEvent) {
        Message message = cloudEvent.getData();
        LOG.infof("Received message: %s at timestamp: %d", 
                  message.getContent(), 
                  message.getTimestamp());
    }
} 