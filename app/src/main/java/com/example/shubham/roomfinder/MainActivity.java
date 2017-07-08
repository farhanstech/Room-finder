package com.example.shubham.roomfinder;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private String imagesJSON;

    private static final String JSON_ARRAY ="result";
    private static final String IMAGE_URL = "url";

    private JSONArray arrayImages= null;

    String roomInfo = "";

    private static final String IMAGES_URL = Constant.IP_ADDRESS+"get_room_images.php";

    String userType,userId;

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    String selectedCity = "All";

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        sharedPreferences = getSharedPreferences(LoginActivity.USER_DATA, Context.MODE_PRIVATE);
        userType = sharedPreferences.getString("user_type",null);
        userId = sharedPreferences.getString("user_id",null);

        spinner = (Spinner)findViewById(R.id.spinner);
        /*if(userType.equals("1")){
            spinner.
        }else{*/
            setSpinnerData();
        //}

        if(internet_connection()){
            //getAllImages();
        }else{
            final Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                    "No internet connection.",
                    Snackbar.LENGTH_SHORT);
            snackbar.setActionTextColor(ContextCompat.getColor(getApplicationContext(),
                    R.color.colorPrimaryDark));
            snackbar.setAction(R.string.try_again, new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            }).show();
        }


        sharedPreferences = getSharedPreferences(LoginActivity.USER_DATA, Context.MODE_PRIVATE);

        //TextView appBarMainName = (TextView)findViewById(R.id.app_bar_main_name);
        //TextView appBarMainEmail = (TextView)findViewById(R.id.app_bar_main_email);
        //appBarMainName.setText(sharedPreferences.getString("name",null));
        //appBarMainEmail.setText(sharedPreferences.getString("email",null));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,PostActivity.class);
                startActivity(intent);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void setSpinnerData() {
        adapter = ArrayAdapter.createFromResource(this,R.array.mainCities,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = "";
                selectedCity = parent.getItemAtPosition(position)+"";

                LinearLayout main = (LinearLayout)findViewById(R.id.mainLinearLayout);
                main.removeAllViews();

                getAllImages();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",null);
            editor.putString("password",null);
            editor.commit();

            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            Intent intent = new Intent(MainActivity.this,ProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_browse) {
            Intent intent = new Intent(MainActivity.this,MainActivity.class);
            startActivity(intent);
        }else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email",null);
            editor.putString("password",null);
            editor.commit();

            Intent intent = new Intent(MainActivity.this,LoginActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    /*------------------------get images fom dbms--------------------------*/
    private void getAllImages() {
        class GetAllImages extends AsyncTask<String,Void,String> {
            ProgressDialog loading;
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this, "Fetching Data...","Please Wait...",true,true);
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                loading.dismiss();
                if(s.equalsIgnoreCase("notexist")){
                    Message.message(getApplicationContext(),"Nothing to show");
                }else{
                    String[] str = s.split("@");
                    imagesJSON = str[0];
                    roomInfo = str[1];
                    extractJSON();
                }
            }

            @Override
            protected String doInBackground(String... params) {
                String uri = params[0];
                BufferedReader bufferedReader = null;
                try {
                    URL url = new URL(uri);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();

                    con.setRequestMethod("POST");
                    con.setDoInput(true);
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();
                    BufferedWriter bos = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    String getData = "user_type=" + params[1] + "&user_id=" + params[2]+"&city_id="+params[3];
                    bos.write(getData);
                    bos.flush();
                    bos.close();
                    os.close();

                    StringBuilder sb = new StringBuilder();

                    bufferedReader = new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String json;
                    while((json = bufferedReader.readLine())!= null){
                        sb.append(json+"\n");
                    }

                    return sb.toString().trim();

                }catch(Exception e){
                    return null;
                }
            }
        }
        int cityId = 0;
        if(selectedCity.equalsIgnoreCase("All")){
            cityId=0;
        }else if(selectedCity.equalsIgnoreCase("Jabalpur")){
            cityId=1;
        }else if(selectedCity.equalsIgnoreCase("Bhopal")){
            cityId=2;
        }else if(selectedCity.equalsIgnoreCase("Indore")){
            cityId=3;
        }else if(selectedCity.equalsIgnoreCase("Sihora")){
            cityId=4;
        }
        GetAllImages gai = new GetAllImages();
        gai.execute(IMAGES_URL,userType,userId,cityId+"");
    }
    private void getImage(String urlToImage,final String row){
        class GetImage extends AsyncTask<String,Void,Bitmap>{
            ProgressDialog loading;
            //String price;
            @Override
            protected Bitmap doInBackground(String... params) {
                URL url = null;
                Bitmap image = null;

                String urlToImage = params[0];

                try {
                    url = new URL(urlToImage);
                    image = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return image;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(MainActivity.this,"Downloading Image...","Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                loading.dismiss();
                String[] str = row.split("_");
                //-----------------------------------------------------
                LinearLayout roomShowLayout = (LinearLayout)getLayoutInflater().inflate(R.layout.room_show_layout, null);
                LinearLayout main = (LinearLayout)findViewById(R.id.mainLinearLayout);
                main.addView(roomShowLayout);
                ImageView img = (ImageView)findViewById(R.id.roomImage);
                TextView ct = (TextView)findViewById(R.id.city);
                TextView pr = (TextView)findViewById(R.id.price);
                img.setId(Integer.parseInt(str[0]));
                ct.setId(Integer.parseInt(str[0]));
                pr.setId(Integer.parseInt(str[0]));
                img.setImageBitmap(bitmap);
                ct.setText("  "+str[2]);
                pr.setText("  Rs. "+str[3]);

                img.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this,PostDetailActivity.class);
                        intent.putExtra("roomId",view.getId()+"");
                        startActivity(intent);
                    }
                });
                //----------------------------------------------------
            }
        }
        GetImage gi = new GetImage();
        gi.execute(urlToImage);
    }
    private void extractJSON(){
        try {
            JSONObject jsonObject = new JSONObject(imagesJSON);
            arrayImages = jsonObject.getJSONArray(JSON_ARRAY);
            String[] arrayInfo = roomInfo.split("#");
            for(int i=0;i<arrayImages.length();i++){
                showImage(arrayImages.getJSONObject(i),arrayInfo[i]);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showImage(JSONObject jsonObject,String row){
        try {
            //JSONObject jsonObject = arrayImages.getJSONObject(TRACK);
            getImage(jsonObject.getString(IMAGE_URL),row);
        } catch (JSONException e) {
            e.printStackTrace();
        }
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