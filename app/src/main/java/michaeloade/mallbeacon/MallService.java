package michaeloade.mallbeacon;

import android.content.Context;

import michaeloade.mallbeacon.models.User;
import michaeloade.mallbeacon.models.Visit;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by aitspeko on 23/04/2018.
 */

public class MallService {
    private static volatile MallService _MallInstance;
    private ServiceInterface service;

    private MallService(Context context) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getResources().getString(R.string.baseUrl))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ServiceInterface.class);
    }

    public static MallService getInstance(Context context) {
        if (_MallInstance == null) {
            _MallInstance = new MallService(context);
        }
        return _MallInstance;
    }

    public Call<Visit> beep(String target, int distance, String user) {
        return service.beep(target, distance, user);
    }

    public Call<User> login(String username, String password) {
        return service.login(username, password);
    }

    public Call<User> register(String firstName, String lastName, String username, String password) {
        return service.register(firstName, lastName, username, password);
    }
}
