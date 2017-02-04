package github.rodolfodpk.restcron;

import github.rodolfodpk.restcron.routes.DeleteRoute;
import github.rodolfodpk.restcron.routes.GetRoute;
import github.rodolfodpk.restcron.routes.PostRoute;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.camel.builder.RouteBuilder;
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

    final List<RouteBuilder> routes = Arrays.asList(new PostRoute(context, jobs, jobSideEffect),
            new GetRoute(jobs), new DeleteRoute(context, jobs));

    app.start(context, routes);

  }

  public static void stop() throws Exception {
    context.stop();
  }

  private void start(DefaultCamelContext context, List<RouteBuilder> routes) throws Exception {

    routes.forEach(route -> {
      try {
        context.addRoutes(route);
      } catch (Exception e) {
        e.printStackTrace();
      }
    });

    final Main main = new Main();
    main.getCamelContexts().clear();
    main.getCamelContexts().add(context);
    main.run();
  }


}

