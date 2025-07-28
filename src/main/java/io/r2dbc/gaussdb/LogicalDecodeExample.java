package io.r2dbc.gaussdb;

import java.util.concurrent.CountDownLatch;

import io.r2dbc.gaussdb.api.GaussDBReplicationConnection;
import io.r2dbc.gaussdb.replication.LogSequenceNumber;
import io.r2dbc.gaussdb.replication.ReplicationRequest;
import io.r2dbc.gaussdb.replication.ReplicationSlotRequest;
import io.r2dbc.gaussdb.replication.ReplicationStream;
import io.r2dbc.spi.ConnectionFactories;
import reactor.core.publisher.Mono;

public class LogicalDecodeExample {
  public static void main(String[] args) throws Exception {

    GaussDBConnectionFactory connectionFactory = (GaussDBConnectionFactory) ConnectionFactories.get(
        "r2dbc:gaussdb://GaussdbExamples:Gaussdb-Examples-123@localhost:8000/postgres?" +
            "assumeMinServerVersion=9.4&preferQueryMode=simple&connectTimeout=20s&statementTimeout=30s");

    createReplicationSlot(connectionFactory);
    startReplication(connectionFactory);
  }

  private static void startReplication(GaussDBConnectionFactory connectionFactory) throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Mono<GaussDBReplicationConnection> replicationMono = connectionFactory.replication();

    replicationMono.subscribe(replicationConnection -> {
      ReplicationRequest replicationRequest = ReplicationRequest.logical()
          .slotName("my_slot")
          .startPosition(LogSequenceNumber.valueOf(0))
          .slotOption("skip-empty-xacts", true)
          .slotOption("include-xids", true)
          .slotOption("parallel-decode-num", 10)
          .slotOption("white-table-list", "public.t1,public.t2")
          .slotOption("decode-style", "t")
          .slotOption("sending-batch", 0)
          .slotOption("max-reorderbuffer-in-memory", 2)
          .build();

      Mono<ReplicationStream> replicationStreamMono = replicationConnection.startReplication(replicationRequest);

      replicationStreamMono
          .flatMapMany(replicationStream -> replicationStream
              .map(byteBuf -> byteBuf))
          .doFinally(signal -> {
            // close resource
            replicationConnection.close().subscribe();
          }).subscribe(
              directBuf -> {
                int readableBytes = directBuf.readableBytes();
                byte[] result = new byte[readableBytes];
                if (directBuf.hasArray()) {
                  System.arraycopy(
                      directBuf.array(),
                      directBuf.arrayOffset() + directBuf.readerIndex(),
                      result,
                      0,
                      readableBytes
                  );
                } else {
                  directBuf.readBytes(result);
                }

                System.out.println(new String(result));
              },
              error -> System.err.println("Error：" + error.getMessage()),
              () -> System.out.println("Finished"));
    });
    countDownLatch.await();
  }

  private static void createReplicationSlot(GaussDBConnectionFactory connectionFactory) throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);

    Mono<GaussDBReplicationConnection> replicationMono = connectionFactory.replication();

    replicationMono.subscribe(replicationConnection -> {
      ReplicationSlotRequest request = ReplicationSlotRequest.logical()
          .slotName("my_slot")
          .outputPlugin("mppdb_decoding")
          .build();
      // ignore error and result.
      replicationConnection.createSlot(request).doOnError(error -> {
        System.out.println("slot failed " + error.getMessage());
        countDownLatch.countDown();
      }).subscribe(slot -> {
        System.out.println("slot created " + slot);
        countDownLatch.countDown();
      });
    });

    countDownLatch.await();
  }
}
