package com.example.meet;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.widget.Toast.LENGTH_LONG;

public class Teacher extends Activity {

    public int uuid;
    public String url;
    public String uid, type, phone, disc, name, email;
    ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);
        //Array List to Store all the details of the contact
        //i.e Particular Student or a Teacher
        contactList = new ArrayList<>();

        //Initialize the image view which changes to different image if student or teacher
        imageView = (ImageView) findViewById(R.id.imageView123);

        //Get the UID value of the marker clicked to fetch the corresponding data
        Intent mIntent = getIntent();
        Bundle bundle = mIntent.getExtras();
        if (bundle != null) {
            uuid = bundle.getInt("uuid");
        }

        //URL for php from where data is fetched for a particular id (JSON)
        url = "http://juspay-com.stackstaging.com/getDetails.php?uid=" + uuid;
    }


    //Intent to Whatsapp for  the Number provided by the User
    public void openWhatsApp(View view) {
        String smsNumber = "91" + phone;                                                              //without '+'
        try {
            Intent sendIntent = new Intent("android.intent.action.MAIN");
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.setType("text/plain");
            sendIntent.putExtra(Intent.EXTRA_TEXT, "Type your message here");                       //Message body in Whatsapp
            sendIntent.putExtra("jid", PhoneNumberUtils.stripSeparators(smsNumber)
                    + "@s.whatsapp.net");                                                           //phone number without "+" prefix
            sendIntent.setPackage("com.whatsapp");
            startActivity(sendIntent);
        } catch (Exception e) {
            Toast.makeText(this, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    //Intent to Caller to call the Number provided by the User
    public void caller(View view) {
        try {
            //Intent Dialer Action
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + phone));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Error/n" + e.toString(), Toast.LENGTH_SHORT).show();
        }

    }

    //Intent to Mailer to Mail the Email provided by the User
    public void mailer(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                "mailto", email, null));                                                             //Sender Email Address
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Let's Meet");                                   //Subject of the Mail
        emailIntent.putExtra(Intent.EXTRA_TEXT, "<Type your text here>");                           //Body of the Mail
        startActivity(Intent.createChooser(emailIntent, "Send email..."));
    }


    private String TAG = Teacher.class.getSimpleName();

    //Array list of Hashmaps
    //Hashmaps used to get assign particular value to a particular named attribute
    ArrayList<HashMap<String, String>> contactList;

    TextView tv1, tv2, tv4, tv5, tv6;


    protected void onPause() {
        super.onPause();

    }

    protected void onResume() {
        super.onResume();
        //onResume, get the data again just so if they have been updated
        new GetContacts().execute();
    }

    //Fetching the JSON Object
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        //GET request done in background
        @Override
        protected Void doInBackground(Void... arg0) {
            //Calling the HTTPHandler
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);
            //Initializing all the textviews
            tv1 = (TextView) findViewById(R.id.name);
            tv2 = (TextView) findViewById(R.id.type);
            tv4 = (TextView) findViewById(R.id.disc);
            tv5 = (TextView) findViewById(R.id.number);
            tv6 = (TextView) findViewById(R.id.email);

            Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);                                   //Creating a Json Object

                    JSONArray contacts = jsonObj.getJSONArray("result");                            //Json Array

                    JSONObject c = contacts.getJSONObject(0);
                    //Get all the attribute values from the Json Object
                    uid = c.getString("uid");
                    type = c.getString("type");
                    disc = c.getString("disc");
                    phone = c.getString("phone");
                    name = c.getString("name");
                    email = c.getString("email");

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //On UI Thread, perform Front-End tasks
                    tv1.setText(name);                                                              //Setting name
                    //If 4, its student, so set the corresponding image
                    if (type.equals("4")) {
                        imageView.setImageResource(R.mipmap.student);
                        tv2.setText("Student");                                                     //Set as Student
                    }
                    //If 5, its teacher, so set the corresponding image
                    else if (type.equals("5")) {
                        imageView.setImageResource(R.mipmap.teacher);
                        tv2.setText("Teacher");                                                     //Set as Teacher
                    }
                    tv4.setText(disc);                                                              //Set Description
                    tv5.setText(phone);                                                             //Set Phone Number
                    tv6.setText(email);                                                             //Set Email

                }
            });
        }


    }

    @Override
    public void onBackPressed() {
        //On Backpress, the intent should start map activity again
        Intent i8 = new Intent(Teacher.this, MapsActivity.class);
        startActivity(i8);
        finish();
    }

}
