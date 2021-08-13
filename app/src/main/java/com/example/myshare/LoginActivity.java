package com.example.myshare;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
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

public class LoginActivity extends AppCompatActivity {

    private EditText emailEDT, passwordEDT;
    private TextView loginTV, createAccountTV;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initializeUIAndReferenceVariables();
        initializeClickActions();
    }

    private void initializeUIAndReferenceVariables() {
        emailEDT = findViewById(R.id.idEDTEmailId);
        passwordEDT = findViewById(R.id.idEDTPassword);
        loginTV = findViewById(R.id.idTVLogin);
        createAccountTV = findViewById(R.id.idTVCreateAccount);
        progressDialog = new ProgressDialog(this);
    }

    private void initializeClickActions() {

        loginTV.setOnClickListener(v -> {
            String emailId = emailEDT.getText().toString();
            String password = passwordEDT.getText().toString();

            if (TextUtils.isEmpty(emailId)) {
                emailEDT.requestFocus();
                emailEDT.setError("Email ID required");
            } else if (TextUtils.isEmpty(password)) {
                passwordEDT.requestFocus();
                passwordEDT.setError("Password required");
            } else {
                progressDialog.setMessage("Please wait logging you in");
                progressDialog.setCancelable(false);
                progressDialog.show();
                loginUserWithCredentials(emailId, password);
            }
        });

        createAccountTV.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        });
    }

    private void loginUserWithCredentials(String emailId, String password) {
        String loginURL = Config.loginURL;
        Map<String, String> params = new HashMap<>();
        params.put("email", emailId);
        params.put("password", password);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, loginURL,
                new JSONObject(params), response -> {
            try {
                progressDialog.dismiss();
                String status = response.getString(Constant.status);
                if (Constant.notSuccess.equals(status)) {
                    Toast.makeText(this, "Wrong credentials!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                    String email = response.getString(Constant.email);
                    SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE).edit();
                    sharedPrefEditor.putBoolean(Constant.isAlreadyLoggedIn, true);
                    sharedPrefEditor.putString(Constant.email, email);
                    sharedPrefEditor.apply();
                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    finish();
                }
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("Error Login=> ", e.getMessage());
                Toast.makeText(this, "Something not right!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }, error -> {
            Log.e("Error Login=> ", error.getMessage());
            Toast.makeText(this, "Something not right!", Toast.LENGTH_SHORT).show();
            progressDialog.dismiss();
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}