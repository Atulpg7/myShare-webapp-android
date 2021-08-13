package com.example.myshare;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {

    private EditText firstNameEDT, lastNameEDT, emailEDT, passwordEDT;
    private TextView loginTV, createAccountTV;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initializeUIAndReferenceVariables();
        initializeClickActions();
    }

    private void initializeUIAndReferenceVariables() {
        firstNameEDT = findViewById(R.id.idEDTFirstName);
        lastNameEDT = findViewById(R.id.idEDTLastName);
        emailEDT = findViewById(R.id.idEDTEmailId);
        passwordEDT = findViewById(R.id.idEDTPassword);
        loginTV = findViewById(R.id.idTVLogin);
        createAccountTV = findViewById(R.id.idTVCreateAccount);
        progressDialog = new ProgressDialog(this);
    }

    private void initializeClickActions() {

        loginTV.setOnClickListener(view -> {
            finish();
        });

        createAccountTV.setOnClickListener(v -> {
            String firstname = firstNameEDT.getText().toString();
            String lastname = lastNameEDT.getText().toString();
            String emailId = emailEDT.getText().toString();
            String password = passwordEDT.getText().toString();

            if (TextUtils.isEmpty(firstname)) {
                firstNameEDT.requestFocus();
                firstNameEDT.setError("First name required");
            } else if (TextUtils.isEmpty(lastname)) {
                lastNameEDT.requestFocus();
                lastNameEDT.setError("Last name required");
            } else if (TextUtils.isEmpty(emailId)) {
                emailEDT.requestFocus();
                emailEDT.setError("Email ID required");
            } else if (TextUtils.isEmpty(password)) {
                passwordEDT.requestFocus();
                passwordEDT.setError("Password required");
            } else {
                progressDialog.setMessage("Please wait registering you in");
                progressDialog.setCancelable(false);
                progressDialog.show();
                registerUserWithCredentials(firstname, lastname, emailId, password);
            }
        });
    }

    private void registerUserWithCredentials(String firstname, String lastname, String emailId, String password) {
        String signupURL = Config.signUpURL;
        Map<String, String> params = new HashMap<>();
        params.put("firstname", firstname);
        params.put("lastname", lastname);
        params.put("email", emailId);
        params.put("password", password);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, signupURL,
                new JSONObject(params), response -> {
            try {
                progressDialog.dismiss();
                String status = response.getString(Constant.status);
                if (Constant.notSuccess.equals(status)) {
                    Toast.makeText(this, "Something not right!", Toast.LENGTH_SHORT).show();
                } else {
                    String email = response.getString(Constant.email);
                    Toast.makeText(this, "Register Successful", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE).edit();
                    sharedPrefEditor.putBoolean(Constant.isAlreadyLoggedIn, true);
                    sharedPrefEditor.putString(Constant.email, email);
                    sharedPrefEditor.apply();
                    Intent i = new Intent(SignUpActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Error Signup=> ", e.getMessage());
                Toast.makeText(this, "Something not right!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Error Signup=> ", error.getMessage());
            Toast.makeText(this, "Something not right!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}