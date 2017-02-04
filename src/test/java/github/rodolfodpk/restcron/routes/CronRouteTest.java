package github.rodolfodpk.restcron.routes;

import github.rodolfodpk.restcron.JobRepresentation;
import github.rodolfodpk.restcron.RestCronApiClient;
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
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import org.mockito.runners.MockitoJUnitRunner;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

@RunWith(MockitoJUnitRunner.class)
@Ignore
public class CronRouteTest {

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
