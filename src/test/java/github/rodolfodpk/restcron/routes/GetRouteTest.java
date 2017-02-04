package github.rodolfodpk.restcron.routes;

import com.fasterxml.jackson.databind.ObjectMapper;
import github.rodolfodpk.restcron.JobRepresentation;
import github.rodolfodpk.restcron.RestCronApiClient;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.SneakyThrows;
import org.apache.camel.impl.DefaultCamelContext;
import static org.assertj.core.api.StrictAssertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RunWith(MockitoJUnitRunner.class)
public class GetRouteTest {

  static Retrofit retrofit;
  static RestCronApiClient apiClient;

  DefaultCamelContext context;
  ConcurrentHashMap<String, JobRepresentation> jobs;

  List<JobRepresentation> fixture = Arrays.asList(
          new JobRepresentation("job1", "hi from job1", "* * * * *"),
          new JobRepresentation("job2", "hi from job2", "* * * * *"),
          new JobRepresentation("job3", "hi from job3", "* * * * *"));

  @BeforeClass
  static public void setUp() throws Exception {
    retrofit = new Retrofit.Builder()
            .baseUrl("http://localhost:8080/")
            .addConverterFactory(JacksonConverterFactory.create())
            .build();
    apiClient = retrofit.create(RestCronApiClient.class);
  }

  @Before
  public void setup() throws Exception {
    context = new DefaultCamelContext();
    jobs = new ConcurrentHashMap<>();
    fixture.forEach(job -> jobs.put(job.getName(), job)); // populate jobs
    context.addRoutes(new GetRoute(jobs));
    context.start();
  }

  @After
  public void afterRun() throws Exception {
    context.stop();
  }

  @Test
  public void when_get_it_should_return_correct_fixture() throws Exception {
    Response<List<JobRepresentation>> response = apiClient.getJobs().execute();
    assertThat(response.code()).isEqualTo(200);
    assertThat(jobs.size()).isEqualTo(3);
    assertThat(jobs.get("job1")).isEqualTo(fixture.get(0));
    assertThat(jobs.get("job2")).isEqualTo(fixture.get(1));
    assertThat(jobs.get("job3")).isEqualTo(fixture.get(2));
  }

  @Test
  @SneakyThrows
  public void json() {
    ObjectMapper mapper = new ObjectMapper();
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "0/2 * * * *");
    String asJson = mapper.writeValueAsString(job);
    JobRepresentation fromJson = mapper.reader().forType(JobRepresentation.class).readValue(asJson);
    assertThat(job).isEqualTo(fromJson);
  }

}
