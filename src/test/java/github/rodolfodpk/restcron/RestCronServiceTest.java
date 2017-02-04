package github.rodolfodpk.restcron;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
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
    Thread.sleep(2000);
    assertThat(isPortOpen("localhost", 8080)).isTrue();
    RestCronService.stop();
    assertThat(isPortOpen("localhost", 8080)).isFalse();
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