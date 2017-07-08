package com.example.shubham.roomfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class SignupActivity extends AppCompatActivity {
    private static final String TAG = "SignupActivity";

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    String selectedUserType = "owner";

    @InjectView(R.id.input_name) EditText _nameText;
    @InjectView(R.id.input_email) EditText _emailText;
    @InjectView(R.id.input_contact) EditText _contactText;
    @InjectView(R.id.input_password) EditText _passwordText;
    @InjectView(R.id.btn_signup) Button _signupButton;
    @InjectView(R.id.link_login) TextView _loginLink;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.inject(this);

        _signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });

        _loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the registration screen and return to the Login activity
                finish();
            }
        });

        spinner = (Spinner)findViewById(R.id.spinner);
        setSpinnerData();
    }

    private void setSpinnerData() {
        adapter = ArrayAdapter.createFromResource(this,R.array.userType,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedUserType = parent.getItemAtPosition(position)+"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void signup() {
        Log.d(TAG, "Signup");

        if(internet_connection()){
            if (!validate()) {
                onSignupFailed();
                return;
            }

            _signupButton.setEnabled(false);

        /*final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                R.style.ProgressBar);*/
            final ProgressDialog progressDialog = new ProgressDialog(SignupActivity.this,
                    ProgressDialog.STYLE_SPINNER);
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Creating Account...");
            progressDialog.show();

            String name = _nameText.getText().toString();
            String email = _emailText.getText().toString();
            String contact = _contactText.getText().toString();
            String password = _passwordText.getText().toString();

            // TODO: Implement your own signup logic here.

            new android.os.Handler().postDelayed(
                    new Runnable() {
                        public void run() {
                            String name = _nameText.getText().toString();
                            String email = _emailText.getText().toString();
                            String contact = _contactText.getText().toString();
                            String password = _passwordText.getText().toString();
                            // On complete call either onSignupSuccess or onSignupFailed
                            // depending on success
                            try {
                                onSignupSuccess(name,email,contact,password);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            // onSignupFailed();
                            progressDialog.dismiss();
                        }
                    }, 3000);
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
                        signup();
                    }
                }
            }).show();
        }
    }


    public void onSignupSuccess(String name,String email,String contact,String password) throws ExecutionException, InterruptedException {
        _signupButton.setEnabled(true);
        setResult(RESULT_OK, null);

        String output = "";
        SignupCheck con = new SignupCheck(this);
        if(selectedUserType.equalsIgnoreCase("Owner")){
            output = con.execute(name,email,contact,password,1+"").get();
        }else{
            output = con.execute(name,email,contact,password,0+"").get();
        }

        if(output.split("<")[0].equalsIgnoreCase("inserted")){
            //Intent intent = new Intent("com.example.shubham.roomfinder.SignupActivity");
            Intent intent = new Intent(SignupActivity.this,LoginActivity.class);
            startActivity(intent);
        }

        //finish();
    }

    public void onSignupFailed() {
        Toast.makeText(getBaseContext(), "Signup failed", Toast.LENGTH_LONG).show();

        _signupButton.setEnabled(true);
    }

    public boolean validate() {
        boolean valid = true;

        String name = _nameText.getText().toString();
        String email = _emailText.getText().toString();
        String contact = _contactText.getText().toString();
        String password = _passwordText.getText().toString();

        if (name.isEmpty() || name.length() < 3) {
            _nameText.setError("at least 3 characters");
            valid = false;
        } else {
            _nameText.setError(null);
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _emailText.setError("enter a valid email address");
            valid = false;
        } else {
            _emailText.setError(null);
        }

        if (contact.isEmpty() || !Patterns.PHONE.matcher(contact).matches() || contact.length()<10) {
            _contactText.setError("enter a valid contact number");
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
