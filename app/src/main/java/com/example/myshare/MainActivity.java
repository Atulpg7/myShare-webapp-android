package com.example.myshare;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.DocumentsContract;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    CardView uploadFileMCV;
    LottieAnimationView uploadingLAV, uploadDoneLAV, uploadFailedLAV;
    ImageView uploadIconIV;
    TextView uploadStatusTV;
    private int PICK_FILE_CODE = 101;
    private TextView logoutTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUIAndReferenceVariables();
        initializeClickActions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_CODE && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            if (data != null) {
                Uri fileUri = data.getData();
                makeUploadRelatedChanges();
                uploadFileToServer(fileUri);
            }
        }

        Log.e("", resultCode + " " + data);
    }

    private void initializeUIAndReferenceVariables() {
        uploadFileMCV = findViewById(R.id.idMCVUploadFile);
        uploadStatusTV = findViewById(R.id.idTVUploadStatus);
        uploadingLAV = findViewById(R.id.idLAVUploadStatus);
        uploadDoneLAV = findViewById(R.id.idLAVUploadDone);
        uploadFailedLAV = findViewById(R.id.idLAVUploadFailed);
        uploadIconIV = findViewById(R.id.idIVUploadIcon);
        logoutTV = findViewById(R.id.idTVLogout);
    }

    private void initializeClickActions() {
        uploadFileMCV.setOnClickListener(v -> {
            openFilePicker();
        });

        logoutTV.setOnClickListener(view -> {
            SharedPreferences.Editor sharedPrefEditor = getSharedPreferences(Constant.myPrefs, MODE_PRIVATE).edit();
            sharedPrefEditor.putBoolean(Constant.isAlreadyLoggedIn, false);
            sharedPrefEditor.putString(Constant.email, "");
            sharedPrefEditor.apply();
            Intent i = new Intent(MainActivity.this, LoginActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            finish();
        });
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_FILE_CODE);
    }

    private void makeUploadRelatedChanges() {
        String uploadingFileStr = "Uploading file....";
        uploadStatusTV.setText(uploadingFileStr);
        uploadIconIV.setVisibility(View.GONE);
        uploadingLAV.setVisibility(View.VISIBLE);
    }

    private void uploadFileToServer(Uri fileUri) {

        String urlToHit = Config.SaveFileUrl;
        InputStream inputStream = null;
        try {
            inputStream = getContentResolver().openInputStream(fileUri);
        } catch (FileNotFoundException e) {
            Log.e("Error Input Stream==> ", e.getMessage());
        }

        //our custom volley request
        InputStream finalInputStream = inputStream;
        VolleyMultipartRequest volleyMultipartRequest = new VolleyMultipartRequest(Request.Method.POST, urlToHit,
                response -> {
                    try {
                        JSONObject obj = new JSONObject(new String(response.data));
                        //if (obj.getString("status").equals("success")) {
                        uploadingLAV.setVisibility(View.GONE);
                        uploadDoneLAV.setVisibility(View.VISIBLE);
                        uploadStatusTV.setText("Upload Successful");
                        String fileUrl = obj.getString("file");
                        //createDownloadUrl(fileUrl);
                        Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                        intent.putExtra("downloadlink", fileUrl);
                        startActivity(intent);
                        finish();
                        //}
                    } catch (JSONException e) {
                        Log.e("Error in response==> ", e.getMessage());
                        uploadingLAV.setVisibility(View.GONE);
                        uploadDoneLAV.setVisibility(View.GONE);
                        uploadFailedLAV.setVisibility(View.VISIBLE);
                        uploadStatusTV.setText("Upload Failed");
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show()) {

            /*
             * Here we are passing image by renaming it with a unique name
             * */
            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                params.put("myfile", new DataPart("myfile", getBytes(finalInputStream)));
                return params;
            }
        };

        //adding the request to volley
        Volley.newRequestQueue(this).add(volleyMultipartRequest);
    }

    private void createDownloadUrl(String fileUrl) {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, fileUrl,
                null, response -> {
            try {
                String downloadUrl = response.getString("downloadlink");

                Intent intent = new Intent(MainActivity.this, DownloadActivity.class);
                intent.putExtra("downloadlink", downloadUrl);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Log.e("Error download=> ", error.getMessage());
        });

        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }

    public byte[] getBytes(InputStream inputStream) {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try {
            int bufferSize = 1024;
            byte[] buffer = new byte[bufferSize];

            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                byteBuffer.write(buffer, 0, len);
            }
        } catch (Exception e) {
            Log.e("Error==> ", e.getMessage());
        }
        return byteBuffer.toByteArray();
    }
}