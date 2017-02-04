package github.rodolfodpk.restcron.routes;

import github.rodolfodpk.restcron.JobRepresentation;
import java.util.concurrent.ConcurrentHashMap;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultCamelContext;

/**
 * A Route for delete scheduled jobs
 */
@AllArgsConstructor
public class DeleteRoute extends RestCronAbstractRoute {

  final static String JOB_NAME = "name";

  final DefaultCamelContext camelContext;
  final ConcurrentHashMap<String, JobRepresentation> jobs;

  @Override
  public void configure() throws Exception {

    super.configure();

    rest("/api")
            .id("delete-job")
            .delete("/jobs/{name}").description("delete a job")
            .consumes("application/x-www-form-urlencoded").produces("text/plain")
            .route()
            .routeId("delete-job")
            .process(e -> {
              String jobName = e.getIn().getHeader(JOB_NAME, String.class);
              JobRepresentation job = jobs.get(jobName);
              e.getOut().setBody(job);
              e.getOut().setHeader(JOB_NAME, jobName);
            })
            .choice()
              .when(bodyAs(JobRepresentation.class).isNotNull())
                .process(e -> {
                  JobRepresentation job = e.getIn().getBody(JobRepresentation.class);
                  camelContext.stopRoute(routeName(job.getName()));
                  camelContext.removeRoute(routeName(job.getName()));
                  jobs.remove(job.getName());
                  e.getOut().setBody(null);
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(200))
              .otherwise()
                .process(e -> {
                  String jobName = e.getIn().getHeader(JOB_NAME, String.class);
                  e.getOut().setBody(String.format("job [%s] was not found - NO CONTENT", jobName));
                })
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(204))
                .setHeader(Exchange.CONTENT_TYPE, constant("text/plain"))
            .end();

  }

}
