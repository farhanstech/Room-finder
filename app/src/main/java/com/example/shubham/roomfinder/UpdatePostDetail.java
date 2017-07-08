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

public class UpdatePostDetail extends AsyncTask<String, Void, String> {
    Context context;
    UpdatePostDetail(Context ctx) {
        context = ctx;
    }

    @Override
    protected String doInBackground(String... params) {
        String updatedValue = params[0];
        String type = params[1].toLowerCase();
        int roomId = Integer.parseInt(params[2]);
        try {
            URL url = new URL(Constant.IP_ADDRESS + "update_post_detail.php");
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            OutputStream os = httpURLConnection.getOutputStream();
            BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));

            String getData = "";
            if(type.equalsIgnoreCase("price")){
                getData = "price=" + updatedValue +  "&type=" + type +  "&room_id=" + roomId;
            }else if(type.equalsIgnoreCase("location")){
                getData = "location=" + updatedValue +  "&type=" + type +  "&room_id=" + roomId;
            }else if(type.equalsIgnoreCase("discription")){
                getData = "discription=" + updatedValue +  "&type=" + type +  "&room_id=" + roomId;
            }

            bos.write(getData);
            bos.flush();
            bos.close();
            os.close();
            InputStream io = httpURLConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(io, "iso-8859-1"));
            String result = "";
            String line = "";
            while((line=br.readLine())!=null){
                result +=line;
            }
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
            Message.message(context,result);
        } else {
            Message.message(context, "something went wrong");
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
