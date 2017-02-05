package github.rodolfodpk.restcron;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.camel.impl.DefaultCamelContext;
import static org.assertj.core.api.StrictAssertions.assertThat;
import org.junit.Test;

/**
 * This test may eventually fail on some machines since it uses Thread.sleep
 */
public class RestCronServiceTest {

  @Test
  public void it_should_start_and_stop() throws Exception {

    assertThat(isPortOpen("localhost", 8080)).isFalse();
    new Thread(() -> {
      try {
        RestCronService.main(null);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }).start();
    Thread.sleep(3000);
    assertThat(isPortOpen("localhost", 8080)).isTrue();
    RestCronService.stop();
    assertThat(isPortOpen("localhost", 8080)).isFalse();
  }

  @Test
  public void starting_with_invalid_route_should_fail() throws Exception {

    final DefaultCamelContext context = new DefaultCamelContext();

    assertThat(isPortOpen("localhost", 8080)).isFalse();
    RestCronService service = new RestCronService();
    AtomicReference<Exception> exception = new AtomicReference<>();

    new Thread(() -> {
      try {
          service.start(context, Arrays.asList(null));
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