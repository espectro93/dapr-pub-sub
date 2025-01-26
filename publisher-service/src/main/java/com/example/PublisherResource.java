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

@Path("/api/messages")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublisherResource {

    @ConfigProperty(name = "dapr.pubsub.name")
    String pubsubName;

    @ConfigProperty(name = "dapr.pubsub.topic")
    String topic;

    private DaprClient client;

    @PostConstruct
    void initialize() {
        client = new DaprClientBuilder().build();
    }

    @POST
    public Response publishMessage(Message message) {
        try {
            client.publishEvent(pubsubName, topic, message).block();
            return Response.ok().build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
} 