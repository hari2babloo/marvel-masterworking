package ServiceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RegisterUser
{
    @GET("bio/register?")
    Call<ResponseBody> RegisterUserResponse(@Query("fingerprintvalue") String fingerprintvalue,
                                            @Query("empID") String empID);
}
