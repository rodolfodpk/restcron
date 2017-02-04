package github.rodolfodpk.restcron.routes;

import github.rodolfodpk.restcron.JobRepresentation;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.apache.camel.model.dataformat.JsonLibrary;

/**
 * A Route to get all scheduled jobs
 */
@AllArgsConstructor
public class GetRoute extends RestCronAbstractRoute {

    final ConcurrentHashMap<String, JobRepresentation> jobs;

    @Override
    public void configure() throws Exception {

      super.configure();

      rest("/api")
              .id("get-jobs")
              .get("/jobs").description("list jobs").produces("application/json")
              .route()
              .routeId("get-jobs")
              .process(exchange -> {
                List<JobRepresentation> asList = jobs.entrySet().stream()
                        .map(x -> x.getValue())
                        .collect(Collectors.toList());
                exchange.getOut().setBody(asList, List.class);
              })
              .marshal().json(JsonLibrary.Jackson)
              .log("result = ${body}");

    }

}
