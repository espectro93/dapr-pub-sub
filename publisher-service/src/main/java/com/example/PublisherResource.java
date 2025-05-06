package com.example;

import com.example.model.Message;
import io.dapr.client.DaprClient;
import io.dapr.client.DaprClientBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;

@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherResource {
    
    private static final Logger LOG = Logger.getLogger(PublisherResource.class);

    @ConfigProperty(name = "dapr.pubsub.name")
    String pubsubName;

    @ConfigProperty(name = "dapr.pubsub.topic")
    String topic;

    private DaprClient client;

    @PostConstruct
    void initialize() {
        LOG.info("Initializing Dapr client");
        client = new DaprClientBuilder().build();
        LOG.info("Publisher configured with pubsub: " + pubsubName + ", topic: " + topic);
    }

    @POST
    public Response publishMessage(Message message) {
        LOG.info("Attempting to publish message: " + message.getContent());
        try {
            // Set timestamp if not already set
            if (message.getTimestamp() == 0) {
                message.setTimestamp(System.currentTimeMillis());
            }
            
            client.publishEvent(pubsubName, topic, message).block();
            LOG.info("Successfully published message to " + topic);
            return Response.ok().build();
        } catch (Exception e) {
            LOG.error("Failed to publish message", e);
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}