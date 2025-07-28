package io.r2dbc.gaussdb;

import org.reactivestreams.Publisher;

import io.r2dbc.spi.Connection;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.Result;
import io.r2dbc.spi.Statement;
import reactor.core.publisher.Mono;

public class CentralizedLoadBalanceExample {
  public static void main(String[] args) throws Exception {
    // Example 1: One cluster
    ConnectionFactory connectionFactory = ConnectionFactories.get(
        "r2dbc:gaussdb:failover://root:Gaussdb-Examples-123@"
            + "Cluster1-IP1:8000,Cluster1-IP2:8000,Cluster1-IP3:8000"
            + "/postgres?targetServerType=primary&loadBalanceHosts=false");

    createConnectionAndDo(connectionFactory);

    System.in.read(); // Wait input and failover

    createConnectionAndDo(connectionFactory);

    System.in.read();

    // Example 2: Two Clusters
    connectionFactory = ConnectionFactories.get(
        "r2dbc:gaussdb:failover://root:Gaussdb-Examples-123@"
            + "Cluster1-IP1:8000,Cluster1-IP2:8000,Cluster1-IP3:8000,"
            + "Cluster2-IP1:8000,Cluster2-IP2:8000,Cluster2-IP3:8000"
            + "/postgres?targetServerType=primary&loadBalanceHosts=false");
    createConnectionAndDo(connectionFactory);

    System.in.read(); // Wait input and failover

    createConnectionAndDo(connectionFactory);

    System.in.read();
  }

  public static void createConnectionAndDo(ConnectionFactory connectionFactory) {
    Publisher<? extends Connection> connectionPublisher = connectionFactory.create();

    Mono<Connection> connectionMono = Mono.from(connectionPublisher);

    connectionMono.subscribe(connection -> {
      Statement statement = connection.createStatement(
          "insert into tbl_test VALUES (default,10001,'Beijing','Shanghai',false)");
      Mono<Result> result = Mono.from(statement.execute());
      result.subscribe(r -> {
        Mono.from(r.getRowsUpdated()).subscribe(o -> {
          System.out.println("first connection: " + connection + "|" + o);
          connection.close();
        });
      });
    });
  }
}
