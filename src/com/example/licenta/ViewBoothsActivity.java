package com.example.licenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import General.Constants;
import Helpers.Booth;
import Helpers.BoothsAdapter;
import Helpers.MyBoothComparator;
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
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ViewBoothsActivity extends Activity {

    private static final String DEBUG_TAG = "VIEW BOOTHS";
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";
    private static final String JSON_ACCESS_GRANTED = "Booth.Exists";
    private static final String JSON_NOT_READY = "Booth.SimilaritiesNotReady";

    private String alertTitle = "View Booths";
    private ProgressBar progressBar;
    private LinearLayout parentLayout;
    private ListView listview;
    private BoothsAdapter adapter;

    private String user_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_booths);
        Intent sourceIntent = getIntent();
        user_id = sourceIntent.getStringExtra("uid");
        Log.d(DEBUG_TAG, "USERID: " + user_id);


        listview = (ListView) findViewById(R.id.listviewBooths);

        /*
         * Ask for information about current location
         */
        String url = "/get_booths/" + user_id;
        Log.d(DEBUG_TAG, "USERID: " + user_id);
        connectionDjango(url);

        
        
    }
    
    /*
     * The Go Back to Current Location Button
     * 
     * When you click on the go back to current location button it takes you
     * to the Current Activity
     */
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(),
                CurrentActivity.class);
        intent.putExtra("uid", user_id);
        startActivity(intent);
        finish();
        return;
    }

    
    /*
     * Connection
     */
    private void connectionDjango(String url) {
        // Connection stuff
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If network connection is ok
        if (networkInfo != null && networkInfo.isConnected()) {
            // progressBar.setVisibility(View.VISIBLE);
            DownloadWebPageTask task = new DownloadWebPageTask();
            task.execute(new String[] { "http://" + Constants.SERVER_IP + ":"
                    + Constants.PORT + url });
        } // If network connection is NOT ok
        else {
            // progressBar.setVisibility(View.INVISIBLE);
            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ViewBoothsActivity.this);

            builder.setMessage("No network connection available");
            builder.setTitle(alertTitle);

            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
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
            // progressBar.setVisibility(View.INVISIBLE);

            Log.d(DEBUG_TAG, "RESULT IS: " + result);

            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ViewBoothsActivity.this);

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

                    // If I have received ok code than
                    // I can present all the booths
                    if (responseJSON.getString("msg").equalsIgnoreCase(
                            JSON_ACCESS_GRANTED)) {

                        // number of booths
                        int noBooths = responseJSON.getInt("noBooths");

                        // List of Booths
                        List<Booth> boothList = new ArrayList<Booth>();
                        ArrayList<String> boothsTagSet;
                        
                        String title;
                        String description;
                        int noCheckedIn;
                        String similarities;
                        String boothsLogo;
                        
                        for (int i = 1; i <= noBooths; i++) {
                            JSONObject boothJSON = responseJSON
                                    .getJSONObject("" + i);
                            
                            boothJSON.getString("checkedIn");

                            boothsTagSet = new ArrayList<String>();
                            title = "";
                            description = "";
                            noCheckedIn = 0;
                            similarities = "";
                            boothsLogo = "";
                            
                            // Getting All Titles:
                            if (!boothJSON.getString("title").equals("null"))
                                title = boothJSON.getString("title");
                            // Getting all descriptions
                            if (!boothJSON.getString("description").equals("null"))
                                description = boothJSON.getString("description");
                            // Getting all checked ins
                            if (!boothJSON.getString("checkedIn").equals("null"))
                                noCheckedIn = Integer.parseInt(boothJSON.getString("checkedIn"));
                            // Getting all similarities
                            if (!boothJSON.getString("similarity").equals("null"))
                                similarities = boothJSON.getString("similarity");
                            // Getting all logos
                            if (!boothJSON.getString("logo").equals("null"))
                                boothsLogo = boothJSON.getString("logo");
                            
                            // Getting all tag sets
                            if (!boothJSON.getString("tagSet").equals("null")) {
                                String[] splits = boothJSON.getString("tagSet").split(",");
                                for (int j = 0; j < splits.length; j++)
                                    boothsTagSet.add(splits[j]);
                            }
                            
                            boothList.add(new Booth(boothsLogo, title, description, similarities, noCheckedIn, boothsTagSet));

                        }
                        
                        Collections.sort(boothList, new MyBoothComparator());
                        adapter = new BoothsAdapter(ViewBoothsActivity.this, boothList);
                        listview.setAdapter(adapter);
                    }
                    // if similarities not ready
                    else if (responseJSON.getString("msg").equalsIgnoreCase(
                    		JSON_NOT_READY)) {
                    	
                    	// Something went wrong
                        builder.setMessage("Similarities not ready. Try again later.");
                        builder.setTitle(alertTitle);

                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                    	dialog.cancel();
                                        Intent intent = new Intent(getApplicationContext(), CurrentActivity.class);
                                        intent.putExtra("uid", user_id);
                                        startActivity(intent);
                                        finish();
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
        getMenuInflater().inflate(R.menu.view_booths, menu);
        return true;
    }

}
