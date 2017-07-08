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


public class PostInsert extends AsyncTask<String, Void, String> {
    Context context;
    PostInsert(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String location = params[0];
        String description = params[1];
        String price = params[2];
        String city = params[3];
        String imageName = params[4];
        String userId = params[5];
        String email = params[6];
        try {
            URL url = new URL(Constant.IP_ADDRESS + "post.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
            String getData = "location=" + location + "&description=" + description + "&price=" + price + "&city=" + city + "&image_name=" + imageName+ "&user_id=" + userId+ "&email=" + email;
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
            if(result.equalsIgnoreCase("inserted")){
                Message.message(context,"successfully post");
            }else if(result.equalsIgnoreCase("failed")){
                Message.message(context,"Something Wrong");
            }
        } else {
            Message.message(context, "Something Wrong");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}