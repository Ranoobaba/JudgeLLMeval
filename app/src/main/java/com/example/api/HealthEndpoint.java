package com.example.api;

import akka.javasdk.annotations.Acl;
import akka.javasdk.annotations.http.Get;
import akka.javasdk.annotations.http.HttpEndpoint;
import akka.javasdk.http.AbstractHttpEndpoint;

import java.time.Instant;
import java.util.Map;

/**
 * Health check endpoint for the AI Judge service.
 * Provides basic health status information.
 */
@HttpEndpoint("/health")
@Acl(allow = @Acl.Matcher(principal = Acl.Principal.ALL))
public class HealthEndpoint extends AbstractHttpEndpoint {

  @Get
  public Map<String, Object> health() {
    return Map.of(
      "status", "healthy",
      "timestamp", Instant.now().toString(),
      "service", "ai-judge-service"
    );
  }
}

