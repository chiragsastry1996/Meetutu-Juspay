package com.example.meet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.android.gms.common.internal.safeparcel.SafeParcelable.NULL;

public class SignUp_Fragment extends Fragment implements OnClickListener {
    private View view;
    private EditText fullName, emailId, mobileNumber, description, password, confirmPassword;
    private TextView login;
    private Button signUpButton;
    public RadioButton rb1, rb2;
    public RadioGroup rg;
    private LinearLayout signupLayout;
    public static String getType = NULL;
    private static Animation shakeAnimation;
    private static final String REGISTER_URL = "http://juspay-com.stackstaging.com/auth/signup.php";
    public ProgressBar pb2;

    public SignUp_Fragment() {

    }


    //Inflate SignUp Fragment in MainActivity
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.signup_layout, container, false);
        initViews();                                                                    //Initializing all the views
        setListeners();                                                                    //Setting Listeners to Buttons and required widgets
        return view;
    }

    // Initialize all views
    private void initViews() {
        signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);                        //Linear Layout
        fullName = (EditText) view.findViewById(R.id.fullName);                                        //Name
        emailId = (EditText) view.findViewById(R.id.userEmailId);                                    //Email-ID
        mobileNumber = (EditText) view.findViewById(R.id.mobileNumber);                                //Mobile-Number
        description = (EditText) view.findViewById(R.id.location);                                    //Description
        password = (EditText) view.findViewById(R.id.password);                                        //Password
        confirmPassword = (EditText) view.findViewById(R.id.confirmPassword);                        //Confirm password for human errors
        signUpButton = (Button) view.findViewById(R.id.signUpBtn);                                    //Register Button
        login = (TextView) view.findViewById(R.id.already_user);                                    //Login Button
        rb1 = (RadioButton) view.findViewById(R.id.terms_conditions);                                //Radio button for students
        rb2 = (RadioButton) view.findViewById(R.id.terms_conditions1);                                //Radio button for teacher
        rg = (RadioGroup) view.findViewById(R.id.rg);                                                //Radio group for Student and Teacher
        pb2 = (ProgressBar) view.findViewById(R.id.progress2);                                        //progressBar

        //Check which radio button is clicked
        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.terms_conditions:
                        getType = "5";
                        break;                                //Set type to 5 if Teacher because in database 5 is Student
                    case R.id.terms_conditions1:
                        getType = "4";
                        break;                                //Set type to 4 if Student because in database 4 is Student
                }
            }
        });

        //Shake animation during CustomToast
        shakeAnimation = AnimationUtils.loadAnimation(getActivity(),
                R.anim.shake);

    }

    // Set Listeners
    private void setListeners() {
        signUpButton.setOnClickListener(this);
        login.setOnClickListener(this);
    }

    //Determine id of clicked widget, depending on the widget take respective action
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.signUpBtn:

                // Call checkValidation method if clicked Sighup
                checkValidation();
                break;

            case R.id.already_user:

                // Replace login fragment in clicked Login
                new MainActivity().replaceLoginFragment();
                break;
        }

    }

    // Check Validation Method
    private void checkValidation() {

        // Get all edittext texts
        String getFullName = fullName.getText().toString();
        String getEmailId = emailId.getText().toString();
        String getMobileNumber = mobileNumber.getText().toString();
        String getDescription = description.getText().toString();
        String getPassword = password.getText().toString();
        String getConfirmPassword = confirmPassword.getText().toString();
        String ggtype = getType;

        // Pattern match for email id
        Pattern p = Pattern.compile(Utils.regEx);
        Matcher m = p.matcher(getEmailId);

        // Check if all strings are null or not
        if (getFullName.equals("") || getFullName.length() == 0
                || getEmailId.equals("") || getEmailId.length() == 0
                || getMobileNumber.equals("") || getMobileNumber.length() == 0
                || getDescription.equals("") || getDescription.length() == 0
                || getPassword.equals("") || getPassword.length() == 0
                || getConfirmPassword.equals("")
                || getConfirmPassword.length() == 0
                || ggtype.equals("0")) {

            new CustomToast().Show_Toast(getActivity(), view,
                    "All fields are required.");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check if email id valid or not
        else if (!m.find()) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Your Email Id is Invalid.");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check if both passowrds are equal
        else if (!getConfirmPassword.equals(getPassword)) {
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
            new CustomToast().Show_Toast(getActivity(), view,
                    "Both password doesn't match.");
        }
        // Check if passowrd length is atleast 6 characters
        else if (getPassword.length() < 7) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Password should contain minimum 6 characters");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check if mobile length is 10 characters
        else if (!(getMobileNumber.length() == 10)) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Inavlid Mobile number");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check if description length is atleast 20 characters
        else if (getDescription.length() < 20) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Description must contain minimum 20 characters");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check if account type is selected or not
        else if (getType == NULL) {
            new CustomToast().Show_Toast(getActivity(), view,
                    "Select Teacher or Student");
            signupLayout = (LinearLayout) view.findViewById(R.id.signup_layout);
            signupLayout.startAnimation(shakeAnimation);
        }
        // Check again if account type is selected, then do sign up
        else if (getType != NULL) {
            register(getFullName, getEmailId, ggtype, getMobileNumber, getDescription, getPassword);
        }


    }


    //Function to register(POST) email and password to Database
    private void register(String name, String email, String type, String phone, String disc, String password) {
        class RegisterUser extends AsyncTask<String, Void, String> {

            //Perform the POST request through Async Task
            RegisterUserClass ruc = new RegisterUserClass();


            //Before starting the Post request start the progressBar
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pb2.setVisibility(view.VISIBLE);
            }

            //After the Execution of POST request -
            @Override
            protected void onPostExecute(String s) {
                //Set progressBar to GONE
                super.onPostExecute(s);
                pb2.setVisibility(view.GONE);
                //Set a Custom Toast when registration is Successful
                new CustomToastSuccess().Show_Toast(getActivity(), view,
                        "Registration Successful");
                //As registration is Successful, change the fragment from registration to Login
                Intent i2 = new Intent(getActivity(), MainActivity.class);
                startActivity(i2);
                getActivity().finish();

            }

            //In background thread, send the name,email,teacher/student,phone number,description and password
            @Override
            protected String doInBackground(String... params) {

                HashMap<String, String> data = new HashMap<>();
                data.put("name", params[0]);
                data.put("email", params[1]);
                data.put("type", params[2]);
                data.put("phone", params[3]);
                data.put("disc", params[4]);
                data.put("password", params[5]);
                String result = ruc.sendPostRequest(REGISTER_URL, data);
                return result;

            }
        }
        RegisterUser ru = new RegisterUser();
        ru.execute(name, email, type, phone, disc, password);

    }
}
