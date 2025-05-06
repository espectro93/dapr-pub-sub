package com.example;

import com.example.model.Message;
import io.dapr.Topic;
import io.dapr.client.domain.CloudEvent;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.event.Observes;
import java.util.Collections;
import java.util.Map;

@Path("/")
public class SubscriberResource {

    private static final Logger LOG = Logger.getLogger(SubscriberResource.class);

    @ConfigProperty(name = "dapr.pubsub.name")
    String pubsubName;

    @ConfigProperty(name = "dapr.pubsub.topic")
    String topic;

    void onStart(@Observes StartupEvent ev) {
        LOG.info("Subscriber starting up...");
        LOG.info("Configured to use pubsub: " + pubsubName);
        LOG.info("Subscribing to topic: " + topic);
    }

    @GET
    @Path("/dapr/subscribe")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSubscriptions() {
        var subscription = Map.of(
            "pubsubName", pubsubName,
            "topic", topic,
            "route", "/messages"
        );
        LOG.info("Returning subscription configuration: " + subscription);
        return Response.ok(Collections.singletonList(subscription)).build();
    }

    @Path("/messages")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @Topic(name = "messages", pubsubName = "pubsub")
    public Response receiveMessage(CloudEvent<Message> cloudEvent) {
        try {
            LOG.info("=== RECEIVED CLOUD EVENT === " + cloudEvent);
            
            if (cloudEvent == null) {
                LOG.warn("Received null cloud event");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
            
            Message message = cloudEvent.getData();
            if (message != null) {
                LOG.infof("=== RECEIVED MESSAGE CONTENT: %s at timestamp: %d ===", 
                          message.getContent(), 
                          message.getTimestamp());
                return Response.ok().build();
            } else {
                LOG.warn("Received null message data in cloud event");
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            LOG.error("Error processing received message", e);
            return Response.serverError().build();
        }
    }
}