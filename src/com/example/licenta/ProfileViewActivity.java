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
import Helpers.MyTagComparator;
import Helpers.MyUserComparator;
import Helpers.Tag;
import Helpers.TagsAdapter;
import Helpers.User;
import Helpers.UserAdapter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.widget.ListView;

public class ProfileViewActivity extends Activity {

	
	private static final String DEBUG_TAG = "VIEW PROFILE";
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";
    private static final String JSON_ACCESS_GRANTED = "User.Exists";
    private static final String JSON_NOT_READY = "UserProfile.NotReady";

    private String alertTitle = "View Improved Profile";
	
	private String user_id = null;
	
	private ListView listview;
    private TagsAdapter adapter;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_profile_view);
		Intent sourceIntent = getIntent();
        user_id = sourceIntent.getStringExtra("uid");
        
        /*
         * Ask for information about profile
         */
        String url = "/get_sa_profile/" + user_id;
        Log.d(DEBUG_TAG, "USERID: " + user_id);
        connectionDjango(url);
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
            		ProfileViewActivity.this);

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
            		ProfileViewActivity.this);

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
                    // I can present the profile
                    if (responseJSON.getString("msg").equalsIgnoreCase(
                            JSON_ACCESS_GRANTED)) {

                    	// USER NAME
                        String user = responseJSON.getString("username");
                        String userDisplayName = responseJSON.getString("name");
                        String tagSet = responseJSON.getString("tagSet");
                        String tagSetNew = responseJSON.getString("tagSetNew");
                        
                        String [] valTags = tagSet.split(",");
                        String [] valNewTags = null;
                        
                        if (!tagSetNew.equalsIgnoreCase("[]"))
                        	valNewTags = tagSetNew.substring(1, tagSetNew.length()).split(",");
                        
                        List<Tag> tagList1 = new ArrayList<Tag>();
                        List<Tag> tagList2 = new ArrayList<Tag>();
                        for (int i = 0; i < valTags.length; i++)
                        	tagList1.add(new Tag(valTags[i].trim()));
                        if (valNewTags != null)
                        	for (int i = 0; i < valNewTags.length; i++)
                        		tagList2.add(new Tag(valNewTags[i].split("'")[1].trim()));
                        else
                        	tagList2.add(new Tag("Nothing found"));
                        
                        Collections.sort(tagList1, new MyTagComparator());
                        Collections.sort(tagList2, new MyTagComparator());
                        listview = (ListView) findViewById(R.id.listviewTags);
                        adapter = new TagsAdapter(ProfileViewActivity.this, tagList1);
                        listview.setAdapter(adapter);                        
                        
                        listview = (ListView) findViewById(R.id.listviewTags_updated);
                        adapter = new TagsAdapter(ProfileViewActivity.this, tagList2);
                        listview.setAdapter(adapter);
                        
                    }
                    // if similarities not ready
                    else if (responseJSON.getString("msg").equalsIgnoreCase(
                    		JSON_NOT_READY)) {
                    	
                    	// Something went wrong
                        builder.setMessage("Profile not ready. Try again later.");
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.profile_view, menu);
		return true;
	}
	
}
