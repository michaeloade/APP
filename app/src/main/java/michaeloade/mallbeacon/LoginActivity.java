package michaeloade.mallbeacon;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import michaeloade.mallbeacon.models.User;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class LoginActivity extends AppCompatActivity {

    private MallService mallService;
    private TextView mUsername;
    private TextView mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mallService = MallService.getInstance(this);
        mUsername = findViewById(R.id.email);
        mPassword = findViewById(R.id.password);
        Button mButton = findViewById(R.id.email_sign_in_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        TextView textView = findViewById(R.id.register_link);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });
    }

    protected void goToRegister()
    {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    public void login()
    {
        mallService.login(mUsername.getText().toString(), mPassword.getText().toString()).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.i("TAG", "Activated");
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.i("TAG", "Deactivated");
            }

        });
    }
}
