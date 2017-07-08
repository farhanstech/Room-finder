package com.example.shubham.roomfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private static final int REQUEST_SIGNUP = 0;

    SharedPreferences sharedPreferences;
    public static final String USER_DATA = "UserData";

    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_login) Button _loginButton;
    @InjectView(R.id.link_signup) TextView _signupLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.inject(this);

        sharedPreferences = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
        String email = sharedPreferences.getString("email",null);
        String passsword = sharedPreferences.getString("password",null);

        if(email!=null && passsword!=null){
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }

        _loginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                login();
            }
        });

        _signupLink.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Start the Signup activity
                Intent intent = new Intent(getApplicationContext(), SignupActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP);
            }
        });
    }

    public void login() {
        Log.d(TAG, "Login");

        if(internet_connection()){
            if (!validate()) {
                onLoginFailed();
                return;
            }

            _loginButton.setEnabled(false);

        /*final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.AppTheme_Dark_Dialog);*/
        /*final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                R.style.ProgressBar);*/
            final ProgressDialog progressDialog = new ProgressDialog(LoginActivity.this,
                    ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Authenticating...");
            progressDialog.show();

            String email = _emailText.getText().toString();
            String password = _passwordText.getText().toString();

            // TODO: Implement your own authentication logic here.

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            // On complete call either onLoginSuccess or onLoginFailed
                            String email = _emailText.getText().toString();
                            String password = _passwordText.getText().toString();
                            try {
                                onLoginSuccess(email,password);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // onLoginFailed();
                            progressDialog.dismiss();
                        }
                    }, 500);
        }else {
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection.",
                    Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorPrimaryDark));
            snackbar.setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(internet_connection()){
                        login();
                    }
                }
            }).show();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP) {
            if (resultCode == RESULT_OK) {

                // TODO: Implement successful signup logic here
                // By default we just finish the Activity and log them in automatically
                //this.finish();
            }
        }
    }

    @Override
    public void onBackPressed() {
        // disable going back to the MainActivity
        moveTaskToBack(true);
    }

    public void onLoginSuccess(String email,String password) throws ExecutionException, InterruptedException {
        _loginButton.setEnabled(true);

        LoginCheck con = new LoginCheck(this);
        String output = con.execute(email,password).get();

        String[] substr = output.split("-");
        String[] data = substr[1].split("_");

        if (substr[0].equalsIgnoreCase("failed")){
            sharedPreferences = null;
        }else if(substr[0].equalsIgnoreCase("correct")){
            // save data session
            sharedPreferences = getSharedPreferences(USER_DATA, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("user_id",data[0]);
            editor.putString("name",data[1]);
            editor.putString("email",data[2]);
            editor.putString("contact",data[3]);
            editor.putString("password",data[4]);
            editor.putString("address",data[5]);
            editor.putString("user_type",data[6]);
            editor.putString("path","Uploads/"+email+""+email.split("@")[0]+".jpg");
            editor.commit();

            /*---------start activity-------*/
            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
            startActivity(intent);
        }
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "Login failed", Toast.LENGTH_LONG).show();

        _loginButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String email = _emailText.getText().toString();
        String password = _passwordText.getText().toString();

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 20) {
            _passwordText.setError("between 4 and 20 alphanumeric characters");
            valid = false;
        } else {
            _passwordText.setError(null);
        }

        return valid;
    }

    boolean internet_connection(){
        //Check if connected to internet, output accordingly
        ConnectivityManager cm =
                (ConnectivityManager)this.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
        return isConnected;
    }
}