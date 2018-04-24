package michaeloade.mallbeacon;

import java.util.List;

import michaeloade.mallbeacon.models.User;
import michaeloade.mallbeacon.models.Visit;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by aitspeko on 16/04/2018.
 */

public interface ServiceInterface {
    @FormUrlEncoded
    @POST("/beep")
    Call<Visit> beep(@Field("target") String target);

    @FormUrlEncoded
    @POST("/auth/register")
    Call<User> register(@Field("first_name") String firstName,
                        @Field("last_name") String lastName,
                        @Field("email") String email,
                        @Field("password") String password);

    @FormUrlEncoded
    @POST("/auth/login")
    Call<User> login(@Field("username") String username,
                     @Field("password") String password);
}
