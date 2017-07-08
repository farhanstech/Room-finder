package com.example.shubham.roomfinder;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class PostDetailActivity extends AppCompatActivity implements OnMapReadyCallback {

    String roomId;
    ImageView roomImage,userImage;
    TextView city,price,userName,discription,location;

    SharedPreferences sharedPreferences;
    String userType;
    String roomImageName;
    private static final int SELECT_IMAGE = 3;
    private String selectedPath;
    Bitmap bitmap;
    public static final String UPLOAD_URL= Constant.IP_ADDRESS+"upload.php";

    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*-------------------------------------------------------------------------------*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*--------------------------------------------------------------------------------*/

        sharedPreferences = getSharedPreferences(LoginActivity.USER_DATA, Context.MODE_PRIVATE);
        userType = sharedPreferences.getString("user_type",null);

        roomId = getIntent().getExtras().getString("roomId");
        roomImage = (ImageView)findViewById(R.id.roomImage);
        userImage = (ImageView)findViewById(R.id.user_profile_photo);
        city = (TextView)findViewById(R.id.city);
        price = (TextView)findViewById(R.id.price);
        userName = (TextView)findViewById(R.id.user_name);
        discription = (TextView)findViewById(R.id.discription);
        location = (TextView)findViewById(R.id.location);

        try {
            getPostDetail();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*------------------------action bar back button-----------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(PostDetailActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*------------------------------------------------------------------------------------*/

    public void getPostDetail() throws ExecutionException, InterruptedException {
        GetPostDetail getPostDetail = new GetPostDetail(this);
        String allPostData = getPostDetail.execute(roomId).get();

        //Message.message(this,allPostData);

        String[] data = allPostData.split("_");
        userName.setText(data[1]);
        city.setText("  "+data[3]);
        discription.setText(data[5]);
        price.setText(data[6]);
        location.setText(data[4]);
        final String userId = data[0];
        final String contact = data[8];
        final String address = data[9];
        final String userEmail = data[2];
        final String userName = data[1];

        roomImageName = data[7].split("/")[1];

        String userProfileUrl = Constant.IP_ADDRESS+"Uploads/"+data[2]+"/"+data[2].split("@")[0]+".jpg";
        getImage(Constant.IP_ADDRESS+data[7],userProfileUrl);

        userImage.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(PostDetailActivity.this,PostUserProfileActivity.class);
                        intent.putExtra("user_id",userId);
                        intent.putExtra("contact",contact);
                        intent.putExtra("address",address);
                        intent.putExtra("email",userEmail);
                        intent.putExtra("user_name",userName);
                        intent.putExtra("roomId",roomId);
                        startActivity(intent);
                    }
                }
        );

        roomImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRoomImage(roomImageName);
            }
        });
        price.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetail(v);
            }
        });
        location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetail(v);
            }
        });
        discription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateDetail(v);
            }
        });
    }

    /*------------------------------------------------------------------------------------------------------------*/
    public void updateDetail(View view){
        if(userType.equals("1")){
            if(view.getId()==R.id.location){
                openDialog("location",view);
            }else if(view.getId()==R.id.discription){
                openDialog("discription",view);
            }else if(view.getId()==R.id.price){
                openDialog("price",view);
            }
        }
    }
    private void openDialog(final String title,View view){
        LayoutInflater inflater = LayoutInflater.from(PostDetailActivity.this);
        View subView;
        final EditText subEditText;
        if(title.equalsIgnoreCase("Price")){
            subView = inflater.inflate(R.layout.contact_dialog_layout, null);
            subEditText = (EditText)subView.findViewById(R.id.dialogEditTextContact);
        }else{
            subView = inflater.inflate(R.layout.dialog_layout, null);
            subEditText = (EditText)subView.findViewById(R.id.dialogEditText);
        }

        if(view.getId()==R.id.location){
            subEditText.setText(location.getText().toString());
        }else if(view.getId()==R.id.discription){
            subEditText.setText(discription.getText().toString());
        }else if(view.getId()==R.id.price){
            subEditText.setText(price.getText().toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(subView);
        AlertDialog alertDialog = builder.create();

        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                /*-----update detail----*/
                UpdatePostDetail con = new UpdatePostDetail(getApplicationContext());
                try {
                    String output = con.execute(subEditText.getText().toString(),title,roomId).get();

                    if(output.equalsIgnoreCase("updated")){
                        String value = subEditText.getText().toString();
                        if(title.equalsIgnoreCase("price")){
                            price.setText(value);
                        }else if(title.equalsIgnoreCase("location")){
                            location.setText(value);
                        }else if(title.equalsIgnoreCase("discription")){
                            discription.setText(value);
                        }
                    }

                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(PostDetailActivity.this, "Cancel", Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }
    /*------------------------------------------------------------------------------------------------------------*/
    /*------------------------------------------------------------------------------------------------------------*/
    public void changeRoomImage(String roomImagePath){
        if(userType.equals("1")){
            chooseImage();
        }else{
            //Message.message(getApplicationContext(),"cannot change");
        }
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
                    uploadImage();
                    roomImage.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                params.put("name", roomImageName);

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
    /*------------------------------------------------------------------------------------------------------------*/

    private void getImage(String postImage,String userProfileUrl){
        class GetImage extends AsyncTask<String,Void,Bitmap[]> {
            ProgressDialog loading;
            @Override
            protected Bitmap[] doInBackground(String... params) {
                URL url1 = null;
                URL url2 = null;
                Bitmap[] bitmaps = new Bitmap[2];
                Bitmap image1 = null;
                Bitmap image2 = null;

                String urlToImage1 = params[0];
                String urlToImage2 = params[1];
                try {
                    url1 = new URL(urlToImage1);
                    url2 = new URL(urlToImage2);
                    image1 = BitmapFactory.decodeStream(url1.openConnection().getInputStream());
                    image2 = BitmapFactory.decodeStream(url2.openConnection().getInputStream());
                    bitmaps[0] = image1;
                    bitmaps[1] = image2;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return bitmaps;
            }

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                loading = ProgressDialog.show(PostDetailActivity.this,"Fetching Data...","Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(Bitmap[] bitmap) {
                super.onPostExecute(bitmap);
                loading.dismiss();
                roomImage.setImageBitmap(bitmap[0]);
                //userImage.setImageBitmap(bitmap[1]);
                setProfileImage(bitmap[1]);

                /*-------------------set location-------------------------------------------------------*/
                setLocation();
            }
        }
        GetImage gi = new GetImage();
        gi.execute(postImage,userProfileUrl);
    }
    public void setProfileImage(Bitmap bm) {
        Bitmap bitmap = Bitmap.createScaledBitmap(bm, 200, 200, true);

        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(result);

            int color = 0xff424242;
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, 400, 400);

            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(color);
            canvas.drawCircle(100, 100, 100, paint);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);

        } catch (NullPointerException e) {
        } catch (OutOfMemoryError o) {
        }
        userImage.setImageBitmap(result);
    }

    /*----------------------------------------------------------*/
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.addMarker(new MarkerOptions().position(new LatLng(23.1815,79.9864)).title("Jabalpur"));
        mMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(23.1815,79.9864)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(23.1815,79.9864)));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        mMap.setMyLocationEnabled(true);
    }
    public void setLocation(){
        String lc = location.getText().toString();
        List<Address> addressList = null;
        if(lc!=null || lc.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(lc,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = null;
            if (addressList != null) {
                address = addressList.get(0);
            }
            LatLng latLng = null;
            if (address != null) {
                latLng = new LatLng(address.getLatitude(),address.getLongitude());
            }
            mMap.addMarker(new MarkerOptions().position(latLng).title(lc));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }
        /*List<Address> addressList = null;
        if(lc!=null || lc.equals("")){
            Geocoder geocoder = new Geocoder(this);
            try {
                addressList = geocoder.getFromLocationName(lc,1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Address address = addressList.get(0);
            LatLng latLng = new LatLng(address.getLatitude(),address.getLongitude());
            mMap.addMarker(new MarkerOptions().position(latLng).title(lc));
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
        }*/
    }
}
