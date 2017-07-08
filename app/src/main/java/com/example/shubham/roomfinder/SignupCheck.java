package com.example.shubham.roomfinder;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SignupCheck extends AsyncTask<String, Void, String> {
    Context context;
    SignupCheck(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String userName = params[0];
        String email = params[1];
        String contact = params[2];
        String password = params[3];
        String userType = params[4];
        try {
            URL url = new URL(Constant.IP_ADDRESS + "SignUp.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String getData = "user_name=" + userName + "&user_email=" + email + "&contact=" + contact + "&password=" + password + "&user_type="+userType;
            bos.write(getData);
            bos.flush();
            bos.close();
            os.close();
            InputStream io = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(io, "iso-8859-1"));
            String result = br.readLine();
            /*String line = "";
            while ((line = br.readLine()) != null) {
                result += line;
            }*/
            br.close();
            io.close();
            httpURLConnection.disconnect();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        if (result != "") {
            if(result.split("<")[0].equalsIgnoreCase("inserted")){
                Message.message(context,"Signup Success");
            }else if(result.equalsIgnoreCase("failed")){
                Message.message(context,"Email already exist");
            }
        } else {
            Message.message(context, "Signup failed");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
}
