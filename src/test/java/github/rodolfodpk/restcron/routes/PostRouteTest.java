package github.rodolfodpk.restcron.routes;

import github.rodolfodpk.restcron.JobRepresentation;
import github.rodolfodpk.restcron.RestCronApiClient;
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RunWith(MockitoJUnitRunner.class)
public class PostRouteTest {

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
    context.start();
  }

  @After
  public void afterRun() throws Exception {
    context.stop();
  }

  @Test
  public void a_valid_job_should_return_201() throws Exception {
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "* * * * *");
    Response<Void> response = apiClient.postJobs(job).execute();
    assertThat(response.code()).isEqualTo(201);
    assertThat(jobs.size()).isEqualTo(1);
    assertThat(jobs.get(job.getName())).isEqualTo(job);
  }

  @Test
  public void an_existent_job_should_return_200() throws Exception {
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "* * * * *");
    Response<Void> response = apiClient.postJobs(job).execute();
    assertThat(response.code()).isEqualTo(201);
    assertThat(jobs.size()).isEqualTo(1);
    assertThat(jobs.get(job.getName())).isEqualTo(job);
    // then let's update it
    JobRepresentation job1WithNewCron = new JobRepresentation("job1", "hi from job1", "*/2 * * * *");
    Response<Void> response2 = apiClient.postJobs(job1WithNewCron).execute();
    assertThat(response2.code()).isEqualTo(200);
    assertThat(jobs.get(job.getName())).isEqualTo(job1WithNewCron);
    assertThat(jobs.size()).isEqualTo(1);
  }

  @Test
  public void an_invalid_job_should_return_400() throws Exception {
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "_cron_expr_");
    Call<Void> call =  apiClient.postJobs(job);
    Response<Void> response = call.execute();
    assertThat(response.code()).isEqualTo(400);
    assertThat(jobs.size()).isEqualTo(0);
    verifyNoMoreInteractions(jobSideEffect);
  }

}


