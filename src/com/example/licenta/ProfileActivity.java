package com.example.licenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import General.Constants;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class ProfileActivity extends Activity {

    private static final String DEBUG_TAG = "PROFILE";
    private static final String JSON_ACCESS_GRANTED = "User.Exists";
    private static final String JSON_UPDATE_GRANTED = "User.ProfileUpdated";
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";
    
    private String user_id = null;
    private String tagSetUpdate = null;
    
    private EditText name;
    private EditText username;
    private EditText pass;
    private TextView tagSet; 
    
    private Button butonUpdate;
    private String alertTitle = "Profile";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Intent sourceIntent = getIntent();
        user_id = sourceIntent.getStringExtra("uid");
        Log.d(DEBUG_TAG, "USERID: " + user_id);
        
        name = (EditText)findViewById(R.id.editTextNameUpdate);
        username = (EditText)findViewById(R.id.editTextUserUpdate);
        pass = (EditText)findViewById(R.id.editTextPassUpdate);
        butonUpdate = (Button) findViewById(R.id.buttonOkUpdate);
        tagSet = (TextView) findViewById(R.id.textViewTagSetUpdateTags);
        
        // SUGGESTIONS 
        final MultiAutoCompleteTextView suggestTags = (MultiAutoCompleteTextView) this
                .findViewById(R.id.autocompleteUpdateProfile);

        ArrayAdapter<String> arrayTokens = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.words);
        
        suggestTags.setAdapter(arrayTokens);
        suggestTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        
        /*
         * Ask for information about current location
         */
        String url = "/get_profile/" + user_id;
        Log.d(DEBUG_TAG, "USERID: " + user_id);
        connectionDjango(url);
        
        // Update Profile Button
        butonUpdate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

	            Log.d("UPDATE", "Chosen Interests: " + suggestTags.getText().toString());
	            tagSetUpdate = suggestTags.getText().toString();
            	
            	// Verifying the update properties
                if (username.getText().toString().matches("")
                        || pass.getText().toString().matches("")
                        || name.getText().toString().matches("")
                        || tagSetUpdate.matches("") || tagSetUpdate.isEmpty()) {
                    // Placing an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(
                            ProfileActivity.this);

                    builder.setMessage("Choose Interests");
                    builder.setTitle(alertTitle);

                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });

                    builder.create().show();

                } // if the update properties are ok
                else {
		            ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		            String url = "/update_profile/" ;//+ user_id;
		            
		            // If network connection is ok
		            if (networkInfo != null && networkInfo.isConnected()) {
		                
		                DownloadWebPageTask task = new DownloadWebPageTask();
		                task.execute(new String[] { "POST", "http://" + Constants.SERVER_IP + ":" + Constants.PORT + url});
		            }
                }
                
                /*connectionDjango(url);*/
                
                /*Intent intent = new Intent(getApplicationContext(), ViewBoothsActivity.class);
                intent.putExtra("uid", user_id);
                startActivity(intent);
                finish();*/
            }
        });
        
    }
    
    private void connectionDjango(String url) { 
        // Connection stuff
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

        // If network connection is ok
        if (networkInfo != null && networkInfo.isConnected()) {
            //progressBar.setVisibility(View.VISIBLE);
            DownloadWebPageTask task = new DownloadWebPageTask();
            task.execute(new String[] { "GET", "http://" + Constants.SERVER_IP + ":" + Constants.PORT + url });
        } // If network connection is NOT ok
        else {
            //progressBar.setVisibility(View.INVISIBLE);
            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ProfileActivity.this);

            builder.setMessage("No network connection available");
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

            builder.create().show();
        }
    }
    
    private class DownloadWebPageTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                if (urls[0].equals("GET"))
                    return downloadUrl(urls[1]);
                else
                    return connectPOST(urls[1]);
            } catch (IOException e) {
                return INVALID_URL;
            } catch (JSONException e) {
                return "JSON EXCEPTION";
            }
        }

        @Override
        protected void onPostExecute(String result) {
            //progressBar.setVisibility(View.INVISIBLE);

            Log.d(DEBUG_TAG, "RESULT IS: " + result);

            // Placing an alert dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(
                    ProfileActivity.this);

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
                            "JSON STUFF here: " + responseJSON.getString("msg"));
                    
                    // If I have received ok code than I can present the user's information                    
                    if (responseJSON.getString("msg").equalsIgnoreCase(
                            JSON_ACCESS_GRANTED)) {
                        
                        String unameS = responseJSON.getString("username");
                        String nameS = responseJSON.getString("name");
                        String passS = responseJSON.getString("password");
                        String tagsS = responseJSON.getString("tagSet");
                        
                        name.setText(nameS);
                        username.setText(unameS);
                        pass.setText(passS);
                        tagSet.setText(tagsS);
                        
                    }
                    // update granted
                    else if (responseJSON.getString("msg").equalsIgnoreCase(JSON_UPDATE_GRANTED
                            )) {
                    	
                    	Log.d(DEBUG_TAG,
                                 "JSON STUFF here UPDATE GRANTED: " + responseJSON.getString("msg"));
                    	
                    	builder.setMessage("Update was ok");
                        builder.setTitle(alertTitle);
                    	builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.cancel();
                                    Intent intent = new Intent(
                                            getApplicationContext(),
                                            ProfileActivity.class);
                                    intent.putExtra("uid", user_id);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                    	
                    }
                    // Otherwise try again
                    else {
                        builder.setMessage("Try again.");
                        builder.setTitle(alertTitle);

                        builder.setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                            int which) {
                                        dialog.cancel();
                                        
                                        /*
                                         * Ask for information about current location
                                         */
                                        String url = "/get_profile/" + user_id;
                                        Log.d(DEBUG_TAG, "USERID: " + user_id);
                                        connectionDjango(url);
                                        
                                    }
                                });
                    }
                    
                } catch (JSONException e) {
                	
                	Log.d(DEBUG_TAG,
                            "CATCH:" + e.getMessage());
                    e.printStackTrace();

                    // Something went wrong
                    builder.setMessage("Something went wrong here. Try again.");
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
    
    private String connectPOST(String myurl) throws IOException, JSONException {
        BufferedReader input = null;

        //StringBuilder response = new StringBuilder();
        
        //instantiates httpclient to make request
        HttpClient client = new DefaultHttpClient();
        //url with the post data
        HttpPost post = new HttpPost(myurl);
        //sets a request header so the page receving the request
        //will know what to do with it
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-type", "application/json");
        
        	//create JSON object
        	JSONObject jo = new JSONObject();
        	
        	// put data into json object
        	jo.put("name", name.getText().toString());
        	jo.put("username", username.getText().toString());
        	jo.put("password", pass.getText().toString());
        	jo.put("userId", user_id);
        	if (tagSetUpdate.endsWith(", "))
        		jo.put("tagSet", tagSetUpdate.substring(0, tagSetUpdate.length() - 2));
        	else if (tagSetUpdate.endsWith(","))
        		jo.put("tagSet", tagSetUpdate.substring(0, tagSetUpdate.length() - 1));
        	else
        		jo.put("tagSet", tagSetUpdate);
        	
            //passes the results to a string builder/entity
            StringEntity se = new StringEntity(jo.toString());
            // sets encoding
            //se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
            //sets the post request as the resulting string
            post.setEntity(se);
            
            //Handles what is returned from the page 
            //ResponseHandler responseHandler = new BasicResponseHandler();
            HttpResponse response = client.execute(post);//, responseHandler);
            //HttpResponse response = client.execute(post);
          
            String r = EntityUtils.toString(response.getEntity());
            Log.d("connectPOST", "Con post: " + r);
            
            return r;
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
        getMenuInflater().inflate(R.menu.profile, menu);
        return true;
    }

}
