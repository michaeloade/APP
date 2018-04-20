package michaeloade.mallbeacon;

import java.util.List;

import michaeloade.mallbeacon.models.Offer;
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
    Call<List<Visit>> beep(@Field("target") String target);
}
