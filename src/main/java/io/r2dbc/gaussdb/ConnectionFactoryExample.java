package io.r2dbc.gaussdb;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Publisher;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

public class ConnectionFactoryExample {
  public static void main(String[] args) throws Exception {
    // Example 1: URL Connection Factory Discovery
    CountDownLatch countDownLatch1 = new CountDownLatch(1);

    ConnectionFactory connectionFactory = ConnectionFactories.get(
        "r2dbc:gaussdb://GaussdbExamples:Gaussdb-Examples-123@localhost:8000/postgres");

    Publisher<? extends Connection> connectionPublisher = connectionFactory.create();

    Mono<Connection> connectionMono = Mono.from(connectionPublisher);

    connectionMono.subscribe(connection -> {
      Statement statement = connection.createStatement("select 2");
      Mono<Result> result = Mono.from(statement.execute());
      result.subscribe(r -> {
        Mono.from(r.map((row, rowMeta) -> row.get(0))).subscribe(o -> {
          System.out.println(o);
          countDownLatch1.countDown();
          connection.close();
        });
      });
    });

    countDownLatch1.await();

    // Example 2: Programmatic Connection Factory Discovery
    CountDownLatch countDownLatch2 = new CountDownLatch(1);

    connectionFactory = ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.DRIVER, "gaussdb")
        .option(ConnectionFactoryOptions.HOST, "localhost")
        .option(ConnectionFactoryOptions.PORT, 8000)
        .option(ConnectionFactoryOptions.USER, "GaussdbExamples")
        .option(ConnectionFactoryOptions.PASSWORD, "Gaussdb-Examples-123")
        .option(ConnectionFactoryOptions.DATABASE, "postgres").build());

    connectionPublisher = connectionFactory.create();

    connectionMono = Mono.from(connectionPublisher);

    connectionMono.subscribe(connection -> {
      Statement statement = connection.createStatement("select 3");
      Mono<Result> result = Mono.from(statement.execute());
      result.subscribe(r -> {
        Mono.from(r.map((row, rowMeta) -> row.get(0))).subscribe(o -> {
          System.out.println(o);
          countDownLatch2.countDown();
          connection.close();
        });
      });
    });

    countDownLatch2.await();

    // Example 3: Programmatic Configuration
    CountDownLatch countDownLatch3 = new CountDownLatch(1);
    Map<String, String> options = new HashMap<>();
    GaussDBConnectionFactory gaussDBConnectionFactory = new GaussDBConnectionFactory(
        GaussDBConnectionConfiguration.builder()
            .host("localhost")
            .port(8000)
            .username("GaussdbExamples")
            .password("Gaussdb-Examples-123")
            .database("postgres")  // optional
            .options(options) // optional
            .build());

    connectionPublisher = gaussDBConnectionFactory.create();

    connectionMono = Mono.from(connectionPublisher);

    connectionMono.subscribe(connection -> {
      Statement statement = connection.createStatement("select 4");
      Mono<Result> result = Mono.from(statement.execute());
      result.subscribe(r -> {
        Mono.from(r.map((row, rowMeta) -> row.get(0))).subscribe(o -> {
          System.out.println(o);
          countDownLatch3.countDown();
          connection.close();
        });
      });
    });

    countDownLatch3.await();
  }
}
