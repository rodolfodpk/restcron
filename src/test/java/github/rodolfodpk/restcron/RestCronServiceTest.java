package github.rodolfodpk.restcron;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import static java.util.concurrent.TimeUnit.SECONDS;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.assertj.core.api.Assertions;
import static org.assertj.core.api.StrictAssertions.assertThat;
import static org.awaitility.Awaitility.await;
import org.junit.Test;

public class RestCronServiceTest {


  @Test
  public void it_should_start_and_stop() throws Exception {

    assertThat(isPortOpen("localhost", 8080)).isFalse();

    new Thread(() -> {
      try {
        RestCronService.main();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();

    await().atMost(5, SECONDS).until(() -> Assertions.assertThat(isPortOpen("localhost", 8080)).isTrue());

    RestCronService.serviceInstance.stop();
    assertThat(isPortOpen("localhost", 8080)).isFalse();
  }

  @Test
  public void starting_with_invalid_route_should_fail() throws Exception {


    final DefaultCamelContext context = new DefaultCamelContext();

    assertThat(isPortOpen("localhost", 8080)).isFalse();

    final List<RouteBuilder> routes = Collections.singletonList(new RouteBuilder() {
      @Override
      public void configure() throws Exception {
        throw new Exception();
      }
    }); // an invalid route

    final RestCronService service = new RestCronService(context, routes);

    AtomicReference<Exception> exception = new AtomicReference<>();

    new Thread(() -> {
      try {
          service.start();
      } catch (Exception e) {
        exception.set(e);
      }
    }).start();

    assertThat(isPortOpen("localhost", 8080)).isFalse();
    assertThat(exception).isNotNull();

  }

  boolean isPortOpen(String host, int port) {
    try {
      new Socket(host, port);
      return true;
    } catch (UnknownHostException e) {
    } catch (IOException e) {
    }
    return false;
  }
}