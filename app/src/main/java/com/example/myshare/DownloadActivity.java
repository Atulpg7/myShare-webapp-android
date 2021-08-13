package com.example.myshare;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DownloadActivity extends AppCompatActivity {

    String downloadLink;
    TextView downloadLinkTV;
    FrameLayout shareFL, sendMailFL;
    EditText mailEDT;
    String uuid;
    TextView logoutTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        initializeUIAndReferenceVariables();
        initializeClickActions();
    }

    private void initializeUIAndReferenceVariables() {
        downloadLinkTV = findViewById(R.id.idTVDownloadLink);
        shareFL = findViewById(R.id.idFLShare);
        sendMailFL = findViewById(R.id.idFLSendMail);
        mailEDT = findViewById(R.id.idEDTMailId);
        logoutTV = findViewById(R.id.idTVLogout);

        Intent intent = getIntent();
        if (intent != null) {
            downloadLink = intent.getStringExtra("downloadlink");
            uuid = downloadLink.substring(downloadLink.lastIndexOf('/') + 1);
        }
        downloadLinkTV.setText(downloadLink);
    }

    private void initializeClickActions() {
        shareFL.setOnClickListener(v -> {
            String titleForShare = "Share link via....";
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_TEXT, downloadLink);
            intent.setType("text/plain");
            Intent intentChooser = Intent.createChooser(intent, titleForShare);
            startActivity(intentChooser);
        });

        sendMailFL.setOnClickListener(view -> {
            String email = mailEDT.getText().toString();
            if (TextUtils.isEmpty(email)) {
                mailEDT.requestFocus();
                mailEDT.setError("Email required!");
            } else {
                Toast.makeText(this, "Sending mail", Toast.LENGTH_SHORT).show();
                sendEmailToUser(email);
            }
        });

        logoutTV.setOnClickListener(view -> {
            SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE).edit();
            sharedPrefEditor.putBoolean(Constant.isAlreadyLoggedIn, false);
            sharedPrefEditor.putString(Constant.email, "");
            sharedPrefEditor.apply();
            Intent i = new Intent(DownloadActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void sendEmailToUser(String email) {
        Map<String, String> params = new HashMap<>();
        String urlToSendEmail = Config.sendEmailUrl;
        SharedPreferences sharedPreferences = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE);

        String currentUserEmail = sharedPreferences.getString(Constant.email, "");

        params.put("uuid", uuid);
        params.put("emailTo", email);
        params.put("emailFrom", currentUserEmail);

        JSONObject jsonObject = new JSONObject(params);

        JsonObjectRequest postJsonObjectRequest = new JsonObjectRequest(Request.Method.POST, urlToSendEmail, jsonObject,
                response -> {
                    //try {
//                        Log.e("Response=> ", response.toString());
//                        String status = response.getString("status");
//                        if (status.equals("success")) {
                    Toast.makeText(this, "Email Sent", Toast.LENGTH_SHORT).show();
                    mailEDT.setText("");
//                        } else {
//                            Toast.makeText(this, "Already Sent Email", Toast.LENGTH_SHORT).show();
//                        }
//                    } catch (JSONException e) {
//                        Log.e("Error in response==> ", e.toString());
//                    }
                }, error -> {
            Log.e("Error in response==> ", error.toString());
        });

        Volley.newRequestQueue(this).add(postJsonObjectRequest);
    }
}