package github.rodolfodpk.restcron;

import github.rodolfodpk.restcron.routes.DeleteRoute;
import github.rodolfodpk.restcron.routes.PostRoute;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.camel.impl.DefaultCamelContext;
import static org.assertj.core.api.StrictAssertions.assertThat;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RunWith(MockitoJUnitRunner.class)
public class DeleteRouteTest {

  static Retrofit retrofit;
  static RestCronApiClient apiClient;

  DefaultCamelContext context;
  ConcurrentHashMap<String, JobRepresentation> jobs;
  @Mock
  BiConsumer<JobRepresentation, LocalDateTime> jobSideEffect;

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
    context.addRoutes(new PostRoute(context, jobs, jobSideEffect));
    context.addRoutes(new DeleteRoute(context, jobs));
    context.start();
  }

  @After
  public void afterRun() throws Exception {
    context.stop();
  }

  @Test
  public void an_existent_job_when_deleted_should_return_200() throws Exception {
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "* * * * *");
    Response<Void> response = apiClient.postJobs(job).execute();
    assertThat(response.code()).isEqualTo(201);
    assertThat(jobs.size()).isEqualTo(1);
    assertThat(jobs.get(job.getName())).isEqualTo(job);
    // then let's delete it
    Response<Void> response2 = apiClient.deleteJob(job.getName()).execute();
    assertThat(response2.code()).isEqualTo(200);
    assertThat(jobs.size()).isEqualTo(0);
  }

  @Test
  public void an_non_existent_job_when_deleted_should_return_204() throws Exception {
    Response<Void> response2 = apiClient.deleteJob("missing-job").execute();
    assertThat(response2.code()).isEqualTo(204);
    assertThat(jobs.size()).isEqualTo(0);
  }

}