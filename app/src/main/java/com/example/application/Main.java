package com.example.application;

import akka.javasdk.DependencyProvider;
import akka.javasdk.annotations.Setup;
import akka.javasdk.ServiceSetup;
import com.example.application.agents.JudgeAgent;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main service setup class for the AI Judge service.
 * This class is automatically discovered by the Akka SDK runtime.
 */
@Setup
public class Main implements ServiceSetup {
  
  private static final Logger logger = LoggerFactory.getLogger(Main.class);
  private final Config config;

  public Main(Config config) {
    this.config = config;
  }

  @Override
  public void onStartup() {
    logger.info("AI Judge Service starting up...");
    logger.info("Service initialized successfully");
  }

  @Override
  public DependencyProvider createDependencyProvider() {
    // Create JudgeAgent instance for dependency injection
    // JudgeAgent requires Config to initialize LLM provider
    final JudgeAgent judgeAgent = new JudgeAgent(config);

    return new DependencyProvider() {
      @Override
      public <T> T getDependency(Class<T> clazz) {
        if (clazz == JudgeAgent.class) {
          return (T) judgeAgent;
        }
        throw new RuntimeException("No such dependency found: " + clazz);
      }
    };
  }
}

