package github.rodolfodpk.restcron.routes;

import github.rodolfodpk.restcron.JobRepresentation;
import java.time.LocalDateTime;
import java.util.function.BiConsumer;
import lombok.AllArgsConstructor;
import org.apache.camel.builder.RouteBuilder;

/**
 * A Route for scheduled jobs
 */
@AllArgsConstructor
class CronRoute extends RouteBuilder {

    final JobRepresentation jobRepresentation;
    final BiConsumer<JobRepresentation, LocalDateTime> jobSideEffect;
    final String quartzCronExpr;

    @Override
    public void configure() throws Exception {

        fromF("quartz2://restcron/%s?cron=%s", jobRepresentation.getName(), quartzCronExpr.replace(' ', '+'))
                .routeId("route-"+ jobRepresentation.getName())
                .log(jobRepresentation.getMsg())
                .process(exchange -> jobSideEffect.accept(jobRepresentation,  LocalDateTime.now()));
    }
}
