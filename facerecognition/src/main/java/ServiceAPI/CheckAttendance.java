package ServiceAPI;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CheckAttendance
{
    @GET("bio/check?")
    Call<ResponseBody> CheckAttendanceResponse(@Query("fingerprintvalue") String fingerprintvalue,
                                               @Query("lat") String Latitude,
                                               @Query("lng") String Longitude);
}
