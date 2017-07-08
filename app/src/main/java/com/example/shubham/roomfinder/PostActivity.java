package com.example.shubham.roomfinder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PostActivity extends AppCompatActivity implements View.OnClickListener,OnMapReadyCallback {

    private GoogleMap mMap;

    int isSelected = 0;

    public static final String UPLOAD_URL= Constant.IP_ADDRESS+"upload.php";
    Bitmap bitmap;

    private Button buttonChoose;
    private Button buttonUpload;
    //private TextView textViewResponse;
    private TextView textView;
    private static final int SELECT_IMAGE = 3;

    private String selectedPath;

    String imageName;

    Spinner spinner;
    ArrayAdapter<CharSequence> adapter;
    ImageView imageView;
    String selectedCity = "Jabalpur";
    EditText locationField;
    EditText descriptionField;
    EditText priceField;
    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        locationField = (EditText)findViewById(R.id.editText1);
        descriptionField = (EditText)findViewById(R.id.editText2);
        priceField = (EditText)findViewById(R.id.editText3);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        //textViewResponse = (TextView) findViewById(R.id.textViewResponse);
        textView = (TextView) findViewById(R.id.textView);
        buttonChoose.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);

        spinner = (Spinner)findViewById(R.id.spinner);
        //imageView = (ImageView)findViewById(R.id.imageView);
        setSpinnerData();

        /*-------------------------------------------------------------------------------*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*--------------------------------------------------------------------------------*/
    }

    /*------------------------action bar back button-----------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(PostActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*------------------------------------------------------------------------------------*/

    private void setSpinnerData() {
        adapter = ArrayAdapter.createFromResource(this,R.array.cities,android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCity = "";
                selectedCity = parent.getItemAtPosition(position)+"";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select a Image "), SELECT_IMAGE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_IMAGE) {
                System.out.println("SELECT_IMAGE");
                Uri selectedImageUri = data.getData();
                selectedPath = getPath(selectedImageUri);
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                textView.setText("image selected......");
                isSelected = 1;
            }
        }
    }

    public String getPath(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        String document_id = cursor.getString(0);
        document_id = document_id.substring(document_id.lastIndexOf(":") + 1);
        cursor.close();

        cursor = getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null, MediaStore.Images.Media._ID + " = ? ", new String[]{document_id}, null);
        cursor.moveToFirst();
        String path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
        cursor.close();

        return path;
    }
    //-------------------------------------------------------------------------------------
    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put("image", image);
                params.put("name", imageName);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }
    public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        if(bitmap.getHeight()>1500 || bitmap.getWidth()>1500){
            Bitmap newBitmap = getResizedBitmap(bmp,1500);
            bitmap = newBitmap;
        }
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);

        byte[] imageBytes = baos.toByteArray();

        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    //-------------------------------------------------------------------------------------


    @Override
    public void onClick(View v) {
        if (v == buttonChoose) {
            chooseImage();
        }
        if (v == buttonUpload) {
            if(internet_connection()){
                /*------------------save data into database-----------------*/
                String locationValue = locationField.getText()+"";
                String descriptionValue = descriptionField.getText()+"";
                String priceValue = priceField.getText()+"";
                String city = selectedCity;
                if(!locationValue.equals("") && !descriptionValue.equals("") && !priceValue.equals("") && isSelected==1){
                    imageName = new Date().getTime()+".jpg";
                    PostInsert postInsert = new PostInsert(this);
                    String output = null;
                    try {
                        SharedPreferences sharedPreferences = getSharedPreferences("UserData", Context.MODE_PRIVATE);
                        String userId = sharedPreferences.getString("user_id",null);
                        String email = sharedPreferences.getString("email",null);

                        loading = ProgressDialog.show(PostActivity.this,"uploading...","Please wait...",true,true);
                        output = postInsert.execute(locationValue,descriptionValue,priceValue,city,imageName,userId,email).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    uploadImage();

                    Thread thread = new Thread( new Runnable() {
                        @Override
                        public void run() {
                            try
                            {
                                Thread.sleep(2000);
                                Intent intent = new Intent(PostActivity.this, MainActivity.class);
                                startActivity(intent);

                            }catch (Exception e) {
                                e.printStackTrace();
                            }finally {
                                finish();
                            }
                        }
                    });
                    thread.start();
                }else if (locationValue.equals("")){
                    Message.message(getApplicationContext(),"please enter a location");
                }else if (descriptionValue.equals("")){
                    Message.message(getApplicationContext(),"please enter description");
                }else if (priceValue.equals("")){
                    Message.message(getApplicationContext(),"please enter price");
                }else if (isSelected!=1){
                    Message.message(getApplicationContext(),"please select a image");
                }
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.1815,79.9864)).title("Marker"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(23.1815,79.9864)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(23.1815,79.9864)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    public void onSearch(View view){
        EditText locationInput = (EditText)findViewById(R.id.editText1);
        String location = locationInput.getText().toString();
        List<Address> addressList = null;
        if(location!=null || location.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(location,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(location));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
    }
}
