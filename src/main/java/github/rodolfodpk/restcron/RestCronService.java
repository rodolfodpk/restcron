package github.rodolfodpk.restcron;

import github.rodolfodpk.restcron.routes.DeleteRoute;
import github.rodolfodpk.restcron.routes.GetRoute;
import github.rodolfodpk.restcron.routes.PostRoute;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.main.Main;

/**
 * This is the main service class
 */
public class RestCronService {

  static final DefaultCamelContext context = new DefaultCamelContext();
  static final ConcurrentHashMap<String, JobRepresentation> jobs = new ConcurrentHashMap<>();

  static final BiConsumer<JobRepresentation, LocalDateTime> jobSideEffect =
          (jobRepresentation, localDateTime) -> System.out.println(jobRepresentation.getMsg()
                  + " at " + localDateTime);

  public static void main(String... args) throws Exception {

    final RestCronService app = new RestCronService();

    context.addRoutes(new PostRoute(context, jobs, jobSideEffect));
    context.addRoutes(new GetRoute(jobs));
    context.addRoutes(new DeleteRoute(context, jobs));

    app.start(context);
  }

  public static void stop() throws Exception {
    context.stop();
  }

  private void start(DefaultCamelContext context) throws Exception {
    final Main main = new Main();
    main.getCamelContexts().clear();
    main.getCamelContexts().add(context);
    main.run();
  }


}

