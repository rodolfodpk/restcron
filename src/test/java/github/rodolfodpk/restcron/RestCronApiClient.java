package github.rodolfodpk.restcron;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface RestCronApiClient {

  @POST("/api/jobs")
  Call<Void> postJobs(@Body JobRepresentation job);

  @DELETE("/api/jobs/{name}/")
  Call<Void> deleteJob(@Path("name") String jobName);

  @GET("/api/jobs")
  Call<List<JobRepresentation>> getJobs();

}
