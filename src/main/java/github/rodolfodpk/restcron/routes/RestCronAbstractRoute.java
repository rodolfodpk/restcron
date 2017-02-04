package github.rodolfodpk.restcron.routes;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;

/**
 * A boilerplate route just to centralize REST API configuration
 */
public abstract class RestCronAbstractRoute extends RouteBuilder {

    static String routeName(String jobName) {
        return "route-" + jobName;
    }

    @Override
    public void configure() throws Exception {

        restConfiguration().component("jetty").bindingMode(RestBindingMode.off)
          .dataFormatProperty("prettyPrint", "true")
          .contextPath("/").port(8080)
          .apiContextPath("/api-doc")
          .apiProperty("api.title", "RestCron Jobs API").apiProperty("api.version", "1.0.0")
          .enableCORS(true);

    }

}
