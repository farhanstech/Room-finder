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

public class LoginCheck extends AsyncTask<String, Void, String> {
    Context context;
    LoginCheck(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String email = params[0];
        String password = params[1];

        try {
            URL url = new URL(Constant.IP_ADDRESS + "Login.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String getData = "user_email=" + email +  "&password=" + password;
            bos.write(getData);
            bos.flush();
            bos.close();
            os.close();
            InputStream io = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(io, "iso-8859-1"));
            String result = br.readLine();
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
            String[] substr = result.split("-");
            if(substr[0].equalsIgnoreCase("correct")){
                Message.message(context,"Successfully LoggedIn");
            }else if(substr[0].equalsIgnoreCase("failed")){
                Message.message(context,"Wrong email or password");
            }
        } else {
            Message.message(context, "something went wrong");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
