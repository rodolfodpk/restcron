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

  static RestCronService serviceInstance;

  final DefaultCamelContext context;
  final List<RouteBuilder> routes;

  public RestCronService(DefaultCamelContext context, List<RouteBuilder> routes) {
    this.context = context;
    this.routes = routes;
  }

  /**
   * Start this service
   * @throws Exception
   */
  public void start() throws Exception {

    for (RouteBuilder route: routes) {
      context.addRoutes(route);
    }

    final Main main = new Main();
    main.getCamelContexts().clear();
    main.getCamelContexts().add(context);
    main.run();
  }

  /**
   * Stop this service
   * @throws Exception
   */
  public void stop() throws Exception {
    context.stop();
  }

  /**
   * Main static method
   * @param args
   * @throws Exception
   */
  public static void main(String... args) throws Exception {

    final DefaultCamelContext context = new DefaultCamelContext();

    final ConcurrentHashMap<String, JobRepresentation> jobs = new ConcurrentHashMap<>();

    final BiConsumer<JobRepresentation, LocalDateTime> jobSideEffect =
            (jobRepresentation, localDateTime) -> System.out.println(jobRepresentation.getMsg()
                    + " at " + localDateTime);

    final List<RouteBuilder> routes = Arrays.asList(new PostRoute(context, jobs, jobSideEffect),
            new GetRoute(jobs), new DeleteRoute(context, jobs));

    final RestCronService service = new RestCronService(context, routes);

    serviceInstance = service;

    service.start();

  }

}

