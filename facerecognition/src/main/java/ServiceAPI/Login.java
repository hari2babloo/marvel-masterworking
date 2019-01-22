package ServiceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Login
{
    @GET("bio/admin?")
    Call<ResponseBody> getLoginResponse(@Query("userName") String userName,
                                        @Query("password") String password);
}
