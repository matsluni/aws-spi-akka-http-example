package com.github.matsluni.akkahttpspiexample;

import static org.junit.Assert.assertEquals;

import java.net.URI;

import org.elasticmq.rest.sqs.SQSRestServer;
import org.elasticmq.rest.sqs.SQSRestServerBuilder;
import org.junit.Test;
import com.github.matsluni.akkahttpspi.AkkaHttpAsyncHttpService;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;
import software.amazon.awssdk.services.sqs.model.CreateQueueRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

public class TestExample {

  String baseUrl = "http://localhost:9324";

  @Test
  public void testS3() throws Exception {

    SQSRestServer server = SQSRestServerBuilder.withPort(9324).withInterface("localhost").start();
    server.waitUntilStarted();

    try(SdkAsyncHttpClient akkaClient = new AkkaHttpAsyncHttpService().createAsyncHttpClientFactory().build();

		  SqsAsyncClient client = SqsAsyncClient.builder()
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("x", "x")))
            .region(Region.of("elasticmq"))
            .endpointOverride(new URI(baseUrl))
            .httpClient(akkaClient)
            .build()
        ) {

      client.createQueue(CreateQueueRequest.builder().queueName("foo").build()).join();
      client.sendMessage(SendMessageRequest.builder().queueUrl(baseUrl + "/queue/foo").messageBody("123").build()).join();
      ReceiveMessageResponse receivedMessage = client.receiveMessage(ReceiveMessageRequest.builder().queueUrl(baseUrl + "/queue/foo").maxNumberOfMessages(1).build()).join();

      assertEquals("123", receivedMessage.messages().get(0).body());
    } finally {
      server.stopAndWait();
    }
  }
}
