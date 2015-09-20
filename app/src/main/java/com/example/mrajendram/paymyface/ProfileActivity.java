package com.example.mrajendram.paymyface;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class ProfileActivity extends Activity {
    View view;
    ImageView image;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);
        view = findViewById(R.id.progress_bar);
        view.setZ(100);
        view.setVisibility(View.GONE);
        image = (ImageView) findViewById(R.id.profile);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
        return true;
    }

    public void segueToPhoto(View view) {

//        Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
//        startActivity(intent);
        takePhoto();
        Toast.makeText(getApplicationContext(), "Segue to User camera",
                Toast.LENGTH_LONG).show();
    }

    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;
    private String m_Text = "";

    public void takePhoto() {
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
        File photo = new File(Environment.getExternalStorageDirectory(), "Pic.png");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    view.setVisibility(View.VISIBLE);
                    new AsyncTask<Void, Void, Void>() {
                        String b64;
                        String result;

                        @Override
                        protected Void doInBackground(Void... params) {
                            Bitmap bitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imageUri.getPath()), 250, 400, false);
                            ByteArrayOutputStream byteOut = new ByteArrayOutputStream(bitmap.getByteCount());
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteOut);
                            b64 = Base64.encodeToString(byteOut.toByteArray(), Base64.DEFAULT);
                            HttpClient client = new DefaultHttpClient();
                            // replace with your url
                            HttpPost httpPost = new HttpPost("https://gcp-hackthenorth-3145.appspot.com/v0/users/-JzechjpTKx3XSlhqP-3/images");


                            //Post Data
                            List<NameValuePair> nameValuePair = new ArrayList<>(2);
                            nameValuePair.add(new BasicNameValuePair("image_type", "face"));
                            nameValuePair.add(new BasicNameValuePair("image_string", b64));


                            //Encoding POST data
                            try {
                                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                            } catch (UnsupportedEncodingException e) {
                                // log exception
                                e.printStackTrace();
                            }

                            //making POST request.
                            HttpResponse response;
                            StringBuffer bufferedWriter;
                            StringBuilder sb = new StringBuilder();
                            try {
                                response = client.execute(httpPost);
                                BufferedReader reader =
                                        new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
                                String line = null;

                                while ((line = reader.readLine()) != null) {
                                    sb.append(line);
                                }
                                result = sb.toString();
                                Log.d("Response of POST", result);
                            } catch (IOException e) {
                                // Log exception
                                e.printStackTrace();
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            try {
                                JSONObject obj = new JSONObject(result);
                                ImageView imageView = new ImageView(ProfileActivity.this);

                                imageView.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                                Glide.with(ProfileActivity.this).load(obj.getJSONArray("images")
                                        .getJSONObject(0).getString("cropped_image")).into(image);

                                view.setVisibility(View.GONE);

                                // Display confirmation alert of the user it found
                                new AlertDialog.Builder(ProfileActivity.this)
                                        .setTitle("User Found!")
                                        .setMessage("User kavinLord was found! Is this the user you want to transfer money to?")
                                        .setView(imageView)
                                        .setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // continue with sending money
                                                getTransactionAmount();
                                                Toast.makeText(getApplicationContext(), "Send " + m_Text + " Dollas son",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setNegativeButton("No, user not found.", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                // do nothing
                                                Toast.makeText(getApplicationContext(), "Find another user",
                                                        Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .setIcon(android.R.drawable.ic_dialog_alert)
                                        .show();
                            } catch (JSONException e) {
                                e.printStackTrace();
                                view.setVisibility(View.GONE);
                                Toast.makeText(ProfileActivity.this, "No face found!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }.execute();





                }
        }
    }


    public void getTransactionAmount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter how much you would like to send:");

        // Set up the input
        final EditText input = new EditText(this);
        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                m_Text = input.getText().toString();
                view.setVisibility(View.VISIBLE);
                new AsyncTask<Void, Void, Void>() {
                    String result;

                    @Override
                    protected Void doInBackground(Void... params) {
                        HttpClient client = new DefaultHttpClient();
                        // replace with your url
                        HttpPost httpPost = new HttpPost("https://gcp-hackthenorth-3145.appspot.com/v0/transactions");


                        //Post Data
                        List<NameValuePair> nameValuePair = new ArrayList<>(2);
                        nameValuePair.add(new BasicNameValuePair("amount", m_Text));
                        nameValuePair.add(new BasicNameValuePair("sender_id", "-JzechjpTKx3XSlhqP-3"));
                        nameValuePair.add(new BasicNameValuePair("receiver_id", "-JzecjSo_RYPuT9pHMx2"));


                        //Encoding POST data
                        try {
                            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePair));
                        } catch (UnsupportedEncodingException e) {
                            // log exception
                            e.printStackTrace();
                        }

                        //making POST request.
                        HttpResponse response;
                        StringBuffer bufferedWriter;
                        StringBuilder sb = new StringBuilder();
                        try {
                            response = client.execute(httpPost);
                            BufferedReader reader =
                                    new BufferedReader(new InputStreamReader(response.getEntity().getContent()), 65728);
                            String line = null;

                            while ((line = reader.readLine()) != null) {
                                sb.append(line);
                            }
                            result = sb.toString();
                            Log.d("Response of POST", result);
                        } catch (IOException e) {
                            // Log exception
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        view.setVisibility(View.GONE);
                        Toast.makeText(ProfileActivity.this, "Paid amount " + m_Text, Toast.LENGTH_LONG).show();
                    }
                }.execute();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
