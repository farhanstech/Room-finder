package com.example.shubham.roomfinder;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class PostUserProfileActivity extends AppCompatActivity {

    TextView nameField,contactField,addressField,emailField;
    ImageView userProfileImage;

    String catchRoomId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_user_profile);

        /*-------------------------------------------------------------------------------*/
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        /*--------------------------------------------------------------------------------*/

        String user_id = getIntent().getExtras().getString("user_id");
        String contact = getIntent().getExtras().getString("contact");
        String address = getIntent().getExtras().getString("address");
        String email = getIntent().getExtras().getString("email");
        String userName = getIntent().getExtras().getString("user_name");
        catchRoomId = getIntent().getExtras().getString("roomId");

        //Message.message(this,user_id+"-"+contact+"-"+address+"-"+email+"-"+userName);

        nameField = (TextView) findViewById(R.id.name);
        contactField = (TextView)findViewById(R.id.contact);
        addressField = (TextView)findViewById(R.id.address);
        emailField = (TextView)findViewById(R.id.user_email);
        userProfileImage = (ImageView)findViewById(R.id.user_profile_photo);

        nameField.setText(userName);
        contactField.setText(contact);
        addressField.setText(address);
        emailField.setText(email);

        String path = Constant.IP_ADDRESS+"Uploads/"+email+"/"+email.split("@")[0]+".jpg";
        getImage(path);
    }

    /*------------------------action bar back button-----------------------------------------------*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; go home
                Intent intent = new Intent(PostUserProfileActivity.this,PostDetailActivity.class);
                intent.putExtra("roomId",catchRoomId);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    /*------------------------------------------------------------------------------------*/

    private void getImage(String postImage){
        class GetImage extends AsyncTask<String,Void,Bitmap> {
            ProgressDialog loading;
            @Override
            protected Bitmap doInBackground(String... params) {
                URL url1 = null;
                Bitmap[] bitmaps = new Bitmap[2];
                Bitmap image = null;

                String urlToImage1 = params[0];
                try {
                    url1 = new URL(urlToImage1);
                    image = BitmapFactory.decodeStream(url1.openConnection().getInputStream());
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
                loading = ProgressDialog.show(PostUserProfileActivity.this,"Fetching Data...","Please wait...",true,true);
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);
                loading.dismiss();
                setProfileImage(bitmap);
            }
        }
        GetImage gi = new GetImage();
        gi.execute(postImage);
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
        userProfileImage.setImageBitmap(result);
    }
}
