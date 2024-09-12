package io.camunda.keycloak;


import io.camunda.zeebe.spring.client.EnableZeebeClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Map;

@SpringBootApplication
@EnableZeebeClient

public class LocalApplication implements CommandLineRunner {

  private final static Logger logger = LoggerFactory.getLogger(LocalApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(LocalApplication.class, args);
  }

  // @Autowired
  // private ZeebeClient client;

  @Override
  public void run(final String... args) throws Exception {
    logger.info("Start LocalApplication");

  }

}
