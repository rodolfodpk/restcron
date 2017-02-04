package github.rodolfodpk.restcron.routes;

import com.cronutils.mapper.CronMapper;
import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinition;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.parser.CronParser;
import github.rodolfodpk.restcron.JobRepresentation;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.quartz.CronExpression;

/**
 * A Route to schedule or reschedule jobs
 */
@AllArgsConstructor
public class PostRoute extends RestCronAbstractRoute {

  final static String JOB_ALREADY_SCHEDULED = "JOB_ALREADY_SCHEDULED";
  final static String CRON_EXPR_AS_QUARTZ = "CRON_EXPR_AS_QUARTZ";
  final static String CRON_EXPR_IS_VALID = "CRON_EXPR_IS_VALID";

  final DefaultCamelContext camelContext;
  final ConcurrentHashMap<String, JobRepresentation> jobs;
  final BiConsumer<JobRepresentation, LocalDateTime> jobSideEffect;

  // these are just to convert from unix cron expression to quartz format
  final CronDefinition cronDefinition = CronDefinitionBuilder.instanceDefinitionFor(CronType.UNIX);
  final CronParser parser = new CronParser(cronDefinition);
  final CronMapper mapper = CronMapper.fromUnixToQuartz();

  @Override
  public void configure() throws Exception {

    super.configure();

    // Processors

    final Processor badRequest = e -> {
      JobRepresentation job = e.getIn().getBody(JobRepresentation.class);
      String response = String.format("%s is not a valid cron  expr for job %s", job.getCron(), job.getName());
      e.getOut().setBody(response);
    };

    final Processor scheduleJob = e -> {
      JobRepresentation job = e.getIn().getBody(JobRepresentation.class);
      String cronExprAsQuartz = e.getIn().getHeader(CRON_EXPR_AS_QUARTZ, String.class);
      CronRoute cronRoute = new CronRoute(job, jobSideEffect, cronExprAsQuartz);
      camelContext.addRoutes(cronRoute);
      jobs.put(job.getName(), job);
      e.getOut().setBody(null);
    };

    final Processor rescheduleJob = e -> {
      JobRepresentation job = e.getIn().getBody(JobRepresentation.class);
      // delete current job
      camelContext.stopRoute(routeName(job.getName()));
      camelContext.removeRoute(routeName(job.getName()));
      jobs.remove(job.getName());
      // recreate job with new message / cron expression
      String cronExprAsQuartz = e.getIn().getHeader(CRON_EXPR_AS_QUARTZ, String.class);
      CronRoute cronRoute = new CronRoute(job, jobSideEffect, cronExprAsQuartz);
      camelContext.addRoutes(cronRoute);
      jobs.put(job.getName(), job);
      e.getOut().setBody(null);
    };

    // post job

    rest("/api")
            .id("post-jobs")
            .post("/jobs").description("schedule a job")
            .bindingMode(RestBindingMode.json)
            .consumes("application/json").type(JobRepresentation.class)
            .produces("text/plain")
            .route()
            .routeId("schedule-job")
            .threads()
            .process(e -> {
              JobRepresentation job = e.getIn().getBody(JobRepresentation.class);
              String quartzCronStr = null;
              try {
                Cron unixCron = parser.parse(job.getCron());
                quartzCronStr = mapper.map(unixCron).asString();
              } catch (IllegalArgumentException ex) {
              }
              e.getOut().setHeader(CRON_EXPR_AS_QUARTZ, quartzCronStr);
              e.getOut().setHeader(CRON_EXPR_IS_VALID, quartzCronStr != null
                      && CronExpression.isValidExpression(quartzCronStr));
              e.getOut().setHeader(JOB_ALREADY_SCHEDULED, jobs.containsKey(job.getName()));
              e.getOut().setBody(job);
            })
            .choice()
              .when(header(CRON_EXPR_IS_VALID).isEqualTo(false))
                .process(badRequest)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
              .when(header(JOB_ALREADY_SCHEDULED).isEqualTo(false))
                .process(scheduleJob)
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))
              .when(header(JOB_ALREADY_SCHEDULED).isEqualTo(true))
                .process(rescheduleJob)
              .otherwise()
                .setBody(constant("what's going on ?"))
            .end()
    ;

  }


}
