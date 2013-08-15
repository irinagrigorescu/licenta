package com.example.licenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

import General.Constants;
import General.Preferences;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

public class MainActivity extends Activity {

    private static final String DEBUG_TAG = "LOGIN_RESPONSE";
    private static final String JSON_ACCESS_GRANTED = "User.AccessGranted";
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";

    private ProgressBar progressBar;
    private EditText userName;
    private EditText userPass;
    private EditText ip;
    private Button butonRegister;
    private Button butonLogin;
    
    private String alertTitle = "Login";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userName = (EditText) findViewById(R.id.editTextUser);
        userPass = (EditText) findViewById(R.id.editTextPass);
        ip = (EditText) findViewById(R.id.editTextIP);
        butonRegister = (Button) findViewById(R.id.buttonRegister);
        butonLogin = (Button) findViewById(R.id.buttonLogin);
        progressBar = (ProgressBar) findViewById(R.id.progressBarLogin);
        
        ip.setText(Constants.SERVER_IP);
        userName.setText("a");
        userPass.setText("a");

        /*
         * Progress bar should be invisible
         */
        progressBar.setVisibility(View.INVISIBLE);

        /*
         * The Register Button
         * 
         * When you click on the register button it takes you to the Register
         * Activity
         */
        butonRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(intent);
            }
        });

        /*
         * The Login Button
         * 
         * When you click on the login button it takes you to the Current
         * Activity
         */
        butonLogin.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                // Verifying the login properties
                if (userName.getText().toString().matches("")
                        || userPass.getText().toString().matches("")) {
                    // Placing an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            MainActivity.this);

                    builder.setMessage("Enter your username and password");
                    builder.setTitle(alertTitle);

                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });

                    builder.create().show();

                } // if the login properties are ok
                else {
                    
                    // TODO: decomentat
                    String url = "/login/"
                            + userName.getText().toString().replaceAll(" ", "%20")
                            + "/"
                            + userPass.getText().toString();
                    connectionDjango(url);
                    
                    /*Intent intent = new Intent(
                            getApplicationContext(),
                            CurrentActivity.class);
                    startActivity(intent);*/
                    
                }
            }
        });

    }

    private void connectionDjango(String url) {
     // Connection stuff
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If network connection is ok
        if (networkInfo != null && networkInfo.isConnected()) {
            progressBar.setVisibility(View.VISIBLE);
            DownloadWebPageTask task = new DownloadWebPageTask();
            
            Constants.SERVER_IP = ip.getText().toString();
            
            task.execute(new String[] { "http://" + Constants.SERVER_IP + ":" + Constants.PORT + url });
        } // If network connection is NOT ok
        else {
            progressBar.setVisibility(View.INVISIBLE);
            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);

            builder.setMessage("No network connection available");
            builder.setTitle(alertTitle);

            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                int which) {
                            dialog.cancel();
                        }
                    });

            builder.create().show();
        }
    }
    
    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                return downloadUrl(urls[0]);
            } catch (IOException e) {
                return INVALID_URL;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            progressBar.setVisibility(View.INVISIBLE);

            Log.d(DEBUG_TAG, "RESULT IS: " + result);

            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    MainActivity.this);

            // If I get INVALID_URL
            if (result.equalsIgnoreCase(INVALID_URL)) {
                builder.setMessage(INVALID_URL);
                builder.setTitle(alertTitle);

                builder.setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                    int which) {
                                dialog.cancel();
                            }
                        });
            } else {
                // Verify what I have received
                try {
                    JSONObject responseJSON = new JSONObject(result);

                    Log.d(DEBUG_TAG,
                            "JSON STUFF: " + responseJSON.getString("msg"));

                    // If I have received ok code than successfully logged in
                    if (responseJSON.getString("msg").equalsIgnoreCase(
                            JSON_ACCESS_GRANTED)) {
                        
                        final String user_id = responseJSON.getString("id");

                        builder.setMessage("You have successfully logged in");
                        builder.setTitle(alertTitle);

                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.cancel();
                                        Intent intent = new Intent(
                                                getApplicationContext(),
                                                CurrentActivity.class);
                                        intent.putExtra("uid", user_id);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                    // Otherwise try again
                    else {
                        builder.setMessage("Login failed. Try again.");
                        builder.setTitle(alertTitle);

                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.cancel();
                                    }
                                });

                    }

                } catch (JSONException e) {
                    e.printStackTrace();

                    // Something went wrong
                    builder.setMessage("Something went wrong. Try again.");
                    builder.setTitle(alertTitle);

                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                }
                            });
                }
            }
            builder.create().show();
        }
    }

    // Given a URL, establishes an HttpUrlConnection and retrieves
    // the web page content as a InputStream, which it returns as
    // a string.
    private String downloadUrl(String myurl) throws IOException {
        BufferedReader input = null;

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(myurl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000); // milliseconds
            conn.setConnectTimeout(15000); // milliseconds
            conn.setRequestMethod("GET");
            conn.setDoInput(true);

            // Starts the query
            conn.connect();

            // Verify response code
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                Log.d(DEBUG_TAG,
                        "The response code is: " + conn.getResponseCode());
                input = new BufferedReader(new InputStreamReader(
                        conn.getInputStream()), 8192);
                String strLine = null;
                while ((strLine = input.readLine()) != null) {
                    response.append(strLine);
                }
                input.close();
            }

            return response.toString();

        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
