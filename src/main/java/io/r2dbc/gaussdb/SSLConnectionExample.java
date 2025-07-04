package io.r2dbc.gaussdb;

import java.util.concurrent.CountDownLatch;

import org.reactivestreams.Publisher;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

public class SSLConnectionExample {
    public static void main(String[] args) throws Exception {
      // sslmode:
      // DISABLE：不使用SSL安全连接。
      // ALLOW：如果数据库服务器要求使用，则可以使用SSL安全加密连接，但不验证数据库服务器的真实性。
      // PREFER：如果数据库支持，那么首选使用SSL连接，但不验证数据库服务器的真实性。
      // REQUIRE：只尝试SSL连接，不会检查服务器证书是否由受信任的CA签发，且不会检查服务器主机名与证书中的主机名是否一致。
      // VERIFY_CA：只尝试SSL连接，并且验证服务器是否具有由可信任的证书机构签发的证书。
      // VERIFY_FULL：只尝试SSL连接，并且验证服务器是否具有由可信任的证书机构签发的证书，以及验证服务器主机名是否与证书中的一致。

      // Example 1: Use sslmode=REQUIRE
      CountDownLatch countDownLatch = new CountDownLatch(1);

      ConnectionFactory connectionFactory = ConnectionFactories.get(
          "r2dbc:gaussdb://GaussdbExamples:Gaussdb-Examples-123@localhost:8000/postgres?ssl=true&sslmode=REQUIRE");

      Publisher<? extends Connection> connectionPublisher = connectionFactory.create();

      Mono<Connection> connectionMono = Mono.from(connectionPublisher);

      connectionMono.subscribe(connection -> {
        Statement statement = connection.createStatement("select 1");
        Mono<Result> result = Mono.from(statement.execute());
        result.subscribe(r -> {
          Mono.from(r.map((row, rowMeta) -> row.get(0))).subscribe(o -> {
            System.out.println(o);
            countDownLatch.countDown();
            connection.close();
          });
        });
      });

      countDownLatch.await();

      // Example 2: Use sslmode=VERIFY_CA
      CountDownLatch countDownLatch2 = new CountDownLatch(1);
      connectionFactory = ConnectionFactories.get(
          "r2dbc:gaussdb://GaussdbExamples:Gaussdb-Examples-123@localhost:8000/postgres?ssl=true&sslmode=VERIFY_CA"
              + "&sslRootCert=ca.pem");

      connectionPublisher = connectionFactory.create();

      connectionMono = Mono.from(connectionPublisher);

      connectionMono.subscribe(connection -> {
        Statement statement = connection.createStatement("select 2");
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
    }
}
