package com.example.meet;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.example.meet.R;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.XmlResourceParser;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Login_Fragment extends Fragment implements OnClickListener {
    private View view;

    private EditText emailid, password;
    private Button loginButton;
    private Button signUp;
    private CheckBox show_hide_password;
    private LinearLayout loginLayout;
    private static Animation shakeAnimation;
    private static FragmentManager fragmentManager;
    private static final String REGISTER_URL = "http://juspay-com.stackstaging.com/auth/login.php";
    public static int uid = -1;
    public ProgressBar pb;

    public Login_Fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.login_layout, container, false);                            //Inflate Views
        initViews();                                                                                //Initialise Views i.e Bind views from the Login Fragment
        setListeners();                                                                                //Setting Listeners to Buttons and required widgets
        return view;
    }


    // Initiate Views
    private void initViews() {

        fragmentManager = getActivity().getSupportFragmentManager();                                //Fragment
        pb = (ProgressBar) view.findViewById(R.id.progress);                                            //ProgressBar
        emailid = (EditText) view.findViewById(R.id.login_emailid);                                    //Email-ID Field
        password = (EditText) view.findViewById(R.id.login_password);                                //Password Field
        loginButton = (Button) view.findViewById(R.id.loginBtn);                                    //Login Button
        signUp = (Button) view.findViewById(R.id.createAccount);                                    //Register Button
        show_hide_password = (CheckBox) view.findViewById(R.id.show_hide_password);                    //Show password CheckBox
        loginLayout = (LinearLayout) view.findViewById(R.id.login_layout);                            //LinearLayout

        shakeAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);                    //Initializing Shake Animation
    }

    // Set Listeners to Required Widgets
    private void setListeners() {
        loginButton.setOnClickListener(this);
        signUp.setOnClickListener(this);


        // Set check listener over checkbox for showing and hiding password
        show_hide_password
                .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                    @Override
                    public void onCheckedChanged(CompoundButton button,
                                                 boolean isChecked) {

                        // If it is checked then show password else hide
                        // password
                        if (isChecked) {

                            show_hide_password.setText(R.string.hide_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT);
                            password.setTransformationMethod(HideReturnsTransformationMethod
                                    .getInstance());// show password
                        } else {
                            show_hide_password.setText(R.string.show_pwd);// change
                            // checkbox
                            // text

                            password.setInputType(InputType.TYPE_CLASS_TEXT
                                    | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            password.setTransformationMethod(PasswordTransformationMethod
                                    .getInstance());// hide password

                        }

                    }
                });
    }


    //Determine id of clicked widget, depending on the widget take respective action
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            //If login is clicked, go for email and password character validation
            case R.id.loginBtn:
                checkValidation();
                break;

            //If Register is clicked, inflate SignUp Fragment
            case R.id.createAccount:

                // Replace signup fragment with animation
                fragmentManager
                        .beginTransaction()
                        .setCustomAnimations(R.anim.right_enter, R.anim.left_out)
                        .replace(R.id.frameContainer, new SignUp_Fragment(),
                                Utils.SignUp_Fragment).commit();
                break;
        }

    }

    // Check Validation before login
    private void checkValidation() {
        // Get email id and password
        String getEmailId = emailid.getText().toString();
        String getPassword = password.getText().toString();

        // Check pattern for email id
        Pattern p = Pattern.compile(Utils.regEx);

        Matcher m = p.matcher(getEmailId);

        // Check for both field is empty or not
        if (getEmailId.equals("") || getEmailId.length() == 0
                || getPassword.equals("") || getPassword.length() == 0) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view,
                    "Enter both credentials.");
            return;
        }

        // Check if email id is valid or not
        else if (!m.find()) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view,
                    "Your Email Id is Invalid.");
            return;
        }

        //Check the length of password for availability
        else if (getPassword.length() < 6) {
            loginLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view,
                    "Password should contain minimum 6 characters");
            return;
        }
        // Else do Login
        else
            register(getEmailId, getPassword);
    }


    //Function to register(POST) email and password to Database
    private void register(String email, String password) {

        //Perform the POST request through Async Task
        class RegisterUser extends AsyncTask<String, Void, String> {

            RegisterUserClass ruc = new RegisterUserClass();

            //Before starting the Post request start the progressBar
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb.setVisibility(view.VISIBLE);
            }


            //After the Execution of POST request -
            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                //Set progressBar to GONE
                pb.setVisibility(view.GONE);
                // If the sesponse if String0 or 0 then it's a error. i.e Inavalid Email ID or Password
                if ((s.equals("string0") || s.equals("0"))) {
                    Toast.makeText(getActivity(), "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                }
                // If email-id and password match, response will be the UID of the respective email-id
                else {
                    try {
                        //Convert the UID string to integer
                        uid = Integer.parseInt(s);
                    } catch (Exception e) {
                        Log.d("UID ERROR", "" + e);
                    }
                    // If UID value is not a error code, send the UID to MapsActivity to fetch appropriate Values
                    if (uid != 0) {
                        Intent i5 = new Intent(getActivity(), MapsActivity.class);
                        i5.putExtra("uid", (int) uid);
                        startActivity(i5);
                        getActivity().finish();
                    }
                }

            }

            //In background thread, send the email-id and password values to the php file
            @Override
            protected String doInBackground(String... params) {

                HashMap<String, String> data = new HashMap<>();
                data.put("email", params[0]);
                data.put("password", params[1]);
                String result = ruc.sendPostRequest(REGISTER_URL, data);
                return result;

            }
        }

        RegisterUser ru = new RegisterUser();
        ru.execute(email, password);

    }
}
