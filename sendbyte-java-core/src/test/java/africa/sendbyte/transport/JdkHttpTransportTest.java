package africa.sendbyte.transport;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

final class JdkHttpTransportTest {
    @Test
    void executesSynchronousRequestAgainstLocalServer() throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> method = new AtomicReference<>();
        final AtomicReference<String> header = new AtomicReference<>();
        final AtomicReference<byte[]> body = new AtomicReference<>();

        server.createContext(
                "/sync",
                exchange -> {
                    method.set(exchange.getRequestMethod());
                    header.set(exchange.getRequestHeaders().getFirst("X-SendByte-Test"));
                    body.set(exchange.getRequestBody().readAllBytes());

                    final byte[] response = "sync-ok".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().add("X-SendByte-Response", "one");
                    exchange.getResponseHeaders().add("X-SendByte-Response", "two");
                    exchange.sendResponseHeaders(201, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                });
        server.start();

        try (JdkHttpTransport transport = new JdkHttpTransport(Duration.ofSeconds(2))) {
            final byte[] requestBody = "sync-body".getBytes(StandardCharsets.UTF_8);
            final TransportResponse response =
                    transport.execute(
                            new TransportRequest(
                                    "POST",
                                    uri(server, "/sync"),
                                    headers("X-SendByte-Test", "sync-header"),
                                    requestBody,
                                    Duration.ofSeconds(2)));

            assertEquals("POST", method.get());
            assertEquals("sync-header", header.get());
            assertArrayEquals(requestBody, body.get());
            assertEquals(201, response.statusCode());
            assertEquals("one", response.firstHeader("x-sendbyte-response"));
            assertEquals("sync-ok", new String(response.body(), StandardCharsets.UTF_8));
        } finally {
            server.stop(0);
        }
    }

    @Test
    void executesAsynchronousRequestAgainstLocalServer() throws Exception {
        final HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        final AtomicReference<String> method = new AtomicReference<>();
        final AtomicReference<String> header = new AtomicReference<>();
        final AtomicReference<byte[]> body = new AtomicReference<>();

        server.createContext(
                "/async",
                exchange -> {
                    method.set(exchange.getRequestMethod());
                    header.set(exchange.getRequestHeaders().getFirst("X-SendByte-Test"));
                    body.set(exchange.getRequestBody().readAllBytes());

                    final byte[] response = "async-ok".getBytes(StandardCharsets.UTF_8);
                    exchange.getResponseHeaders().set("X-SendByte-Response", "async-header");
                    exchange.sendResponseHeaders(202, response.length);
                    exchange.getResponseBody().write(response);
                    exchange.close();
                });
        server.start();

        try (JdkHttpTransport transport = new JdkHttpTransport(Duration.ofSeconds(2))) {
            final byte[] requestBody = "async-body".getBytes(StandardCharsets.UTF_8);
            final TransportResponse response =
                    transport
                            .executeAsync(
                                    new TransportRequest(
                                            "PUT",
                                            uri(server, "/async"),
                                            headers("X-SendByte-Test", "async-header"),
                                            requestBody,
                                            Duration.ofSeconds(2)))
                            .get(5, TimeUnit.SECONDS);

            assertEquals("PUT", method.get());
            assertEquals("async-header", header.get());
            assertArrayEquals(requestBody, body.get());
            assertEquals(202, response.statusCode());
            assertEquals("async-header", response.firstHeader("x-sendbyte-response"));
            assertEquals("async-ok", new String(response.body(), StandardCharsets.UTF_8));
        } finally {
            server.stop(0);
        }
    }

    private static URI uri(final HttpServer server, final String path) {
        return URI.create("http://127.0.0.1:" + server.getAddress().getPort() + path);
    }

    private static Map<String, List<String>> headers(final String name, final String value) {
        final Map<String, List<String>> headers = new LinkedHashMap<>();
        headers.put(name, Collections.singletonList(value));
        return headers;
    }
}
