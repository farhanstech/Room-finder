package com.example.shubham.roomfinder;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ExecutionException;

//-------------------------

public class ProfileActivity extends AppCompatActivity {

    ImageView img1;

    TextView name,contact,password,address,email;

    SharedPreferences sharedPreferences;

    private String imagesJSON;
    private static final String JSON_ARRAY ="result";
    private static final String IMAGE_URL = "url";

    private JSONArray arrayImages= null;

    private int TRACK = 0;

    private static final int SELECT_IMAGE = 3;
    private String selectedPath;
    private TextView textViewResponse;

    private static final String IMAGES_URL = Constant.IP_ADDRESS+"get_profile.php";
    private static final String UPLOAD_IMAGE_URL = Constant.IP_ADDRESS+"upload_user_profile_pic.php";

    /*----------------------------------------------------*/
    private int PICK_IMAGE_REQUEST = 1;
    private Bitmap bitmap;
    private String KEY_IMAGE = "image";
    private String KEY_NAME = "name";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /*-------------------------------------------------------------------------------*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*--------------------------------------------------------------------------------*/

        if(internet_connection()){
            /*--------------------------------------*/
            name = (TextView) findViewById(R.id.edit_name);
            contact = (TextView)findViewById(R.id.edit_contact);
            password = (TextView)findViewById(R.id.edit_password);
            address = (TextView)findViewById(R.id.edit_address);
            email = (TextView)findViewById(R.id.user_email);
            img1 = (ImageView)findViewById(R.id.user_profile_photo);
            img1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    chooseImage();
                }
            });

            textViewResponse = (TextView)findViewById(R.id.textViewResponse);
            /*--------set values-----------------*/
            setValues();

             /*------get profile--------*/
            getAllImages();

            /*----chnage image to circular------*/
            /*int value = R.drawable.profile*/
            //setProfileImage(R.drawable.profile);
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

                    }
                }
            }).show();
        }
    }

    /*------------------------action bar back button-----------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(ProfileActivity.this,MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*------------------------------------------------------------------------------------*/

    /*------------------------------*/
    public void onEditClick(View view){
        if(view.getId()==R.id.edit_name){
            openDialog("Name",view);
        }else if(view.getId()==R.id.edit_contact){
            openDialog("Contact",view);
        }else if(view.getId()==R.id.edit_password){
            openDialog("Password",view);
        }else if(view.getId()==R.id.edit_address){
            openDialog("Address",view);
        }
    }

    public void setValues(){
        sharedPreferences = getSharedPreferences(LoginActivity.USER_DATA, Context.MODE_PRIVATE);
        String name1 = sharedPreferences.getString("name",null);
        String email1 = sharedPreferences.getString("email",null);
        String contact1 = sharedPreferences.getString("contact",null);
        String password1 = sharedPreferences.getString("password",null);
        String address1 = sharedPreferences.getString("address",null);

        name.setText(name1);
        contact.setText(contact1);
        password.setText(password1);
        address.setText(address1);
        email.setText(email1);
    }

    public void setProfileImage(Bitmap bm) {
        //Bitmap bm = BitmapFactory.decodeResource(getResources(),value);
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
        img1.setImageBitmap(result);
    }

    private void openDialog(final String title,View view){
        LayoutInflater inflater = LayoutInflater.from(ProfileActivity.this);
        View subView;
        final EditText subEditText;
        if(title.equalsIgnoreCase("Contact")){
            subView = inflater.inflate(R.layout.contact_dialog_layout, null);
            subEditText = (EditText)subView.findViewById(R.id.dialogEditTextContact);
        }else{
            subView = inflater.inflate(R.layout.dialog_layout, null);
            subEditText = (EditText)subView.findViewById(R.id.dialogEditText);
        }

        if(view.getId()==R.id.edit_name){
            subEditText.setText(name.getText().toString());
        }else if(view.getId()==R.id.edit_contact){
            subEditText.setText(contact.getText().toString());
        }else if(view.getId()==R.id.edit_password){
            subEditText.setText(password.getText().toString());
        }else if(view.getId()==R.id.edit_address){
            subEditText.setText(address.getText().toString());
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setView(subView);
        AlertDialog alertDialog = builder.create();

        builder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String userId = sharedPreferences.getString("user_id",null);

                /*-----update detail----*/
                UpdateUserDetail con = new UpdateUserDetail(getApplicationContext());
                try {
                    String output = con.execute(subEditText.getText().toString(),title,userId).get();

                    if(output.equalsIgnoreCase("updated")){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        String value = subEditText.getText().toString();
                        if(title.equalsIgnoreCase("Name")){
                            editor.putString("name",value);
                            name.setText(value);
                        }else if(title.equalsIgnoreCase("Contact")){
                            editor.putString("contact", value);
                            contact.setText(value);
                        }else if(title.equalsIgnoreCase("Password")){
                            editor.putString("password", value);
                            password.setText(value);
                        }else if(title.equalsIgnoreCase("Address")){
                            editor.putString("address", value);
                            address.setText(value);
                        }
                        editor.commit();
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
                Toast.makeText(ProfileActivity.this, "Cancel", Toast.LENGTH_LONG).show();
            }
        });

        builder.show();
    }

    /*-----------------------------------------------------------------------------------------------------------*/
    private void getAllImages() {
        class GetAllImages extends AsyncTask<String,Void,String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                imagesJSON = s;
                extractJSON();
                showImage();
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
                    String getData = "user_email=" + params[1];
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
        String userEmail = sharedPreferences.getString("email",null);
        GetAllImages gai = new GetAllImages();
        gai.execute(IMAGES_URL,userEmail);
    }
    private void getImage(String urlToImage){
        class GetImage extends AsyncTask<String,Void,Bitmap>{
            ProgressDialog loading;
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
                loading = ProgressDialog.show(ProfileActivity.this,"Fetching Data...","Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                loading.dismiss();
                img1.setImageBitmap(bitmap);
                setProfileImage(bitmap);
            }
        }
        GetImage gi = new GetImage();
        gi.execute(urlToImage);
    }
    private void extractJSON(){
        try {
            JSONObject jsonObject = new JSONObject(imagesJSON);
            arrayImages = jsonObject.getJSONArray(JSON_ARRAY);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    private void showImage(){
        try {
            JSONObject jsonObject = arrayImages.getJSONObject(TRACK);
            getImage(jsonObject.getString(IMAGE_URL));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /*------------------------------------------------------------------*/

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri filePath = data.getData();
            try {
                //Getting the Bitmap from Gallery
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                uploadImage();

                Thread thread = new Thread( new Runnable() {
                    @Override
                    public void run() {
                        try
                        {
                            Thread.sleep(2000);
                            Intent intent = new Intent(ProfileActivity.this, ProfileActivity.class);
                            startActivity(intent);

                        }catch (Exception e) {
                            e.printStackTrace();
                        }finally {
                            finish();
                        }
                    }
                });
                thread.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(this,"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_IMAGE_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        //Toast.makeText(ProfileActivity.this, s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        //Toast.makeText(ProfileActivity.this, volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = "hjcbhdbhdm";

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                //params.put(KEY_NAME, name);
                params.put("email", email.getText()+"");

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Adding request to the queue
        requestQueue.add(stringRequest);
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
    /*----------------------------------------------------------*/
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
