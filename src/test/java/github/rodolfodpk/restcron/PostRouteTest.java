package github.rodolfodpk.restcron;

import github.rodolfodpk.restcron.routes.PostRoute;
import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import org.apache.camel.impl.DefaultCamelContext;
import static org.assertj.core.api.StrictAssertions.assertThat;
import org.junit.*;
import org.junit.runner.RunWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
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

  @Test
  @Ignore // because Crontab precision is minutes, not seconds
  public void a_scheduled_job_should_call_side_effect() throws Exception {
    JobRepresentation job = new JobRepresentation("job1", "hi from job1", "0/1 * * * * ?");
    Call<Void> call =  apiClient.postJobs(job);
    Response<Void> response = call.execute();
    Thread.sleep(2000); // just to give some time in order to check if scheduling is working
    assertThat(response.code()).isEqualTo(201);
    assertThat(jobs.get(job.getName())).isEqualTo(job);
    verify(jobSideEffect, atLeast(1)).accept(eq(job), any(LocalDateTime.class));
  }

}

// segundo problema deve ser um outro projeto com kotlin gerando javascript
// https://kotlinlang.org/docs/tutorials/javascript/getting-started-maven/getting-started-with-maven.html
// usar isto para iterar as tuplas https://kotlinlang.org/docs/reference/multi-declarations.html


