package com.example.licenta;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;


import General.Constants;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.MultiAutoCompleteTextView;
import android.widget.ProgressBar;

public class RegisterActivity extends Activity {

    private static final String DEBUG_TAG = "REGISTER_RESPONSE";
    private static final String JSON_ACCESS_GRANTED = "User.RegisterAccepted";
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";
    
    private ProgressBar progressBar;
    private EditText displayName;
    private EditText userName;
    private EditText userPass;
    private EditText userPass2;
    private Button butonRegister;
    
    private String tagSetRegister = null;
    private String alertTitle = "Register";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        displayName = (EditText)findViewById(R.id.editTextNameRegister);
        userName = (EditText)findViewById(R.id.editTextUserRegister);
        userPass = (EditText)findViewById(R.id.editTextPassRegister);
        userPass2 = (EditText)findViewById(R.id.editTextRePassRegister);
        butonRegister = (Button) findViewById(R.id.buttonOkRegister);
        progressBar = (ProgressBar) findViewById(R.id.progressBarRegister);
        
        /*
         *  SUGGESTIONS 
         */
        final MultiAutoCompleteTextView suggestTags = (MultiAutoCompleteTextView) this
                .findViewById(R.id.autocompleteRegister);

        ArrayAdapter<String> arrayTokens = new ArrayAdapter<String>(this,
                android.R.layout.simple_dropdown_item_1line, Constants.words);
        
        suggestTags.setAdapter(arrayTokens);
        suggestTags.setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());
        
        Log.d("CHOOSE", "CHOOSE REGISTER: " + suggestTags.getText().toString());
        
        
        /*
         * Progress bar should be invisible
         */
        progressBar.setVisibility(View.INVISIBLE);
        
        
        /* The OK Button
         * 
         * When you click on the ok button it takes you to
         * the Choose Interests Activity
         * 
         */
        butonRegister.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	
            	tagSetRegister = suggestTags.getText().toString();

                // Placing an alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        RegisterActivity.this);
            	
            	// Verifying the register properties
                if (userName.getText().toString().matches("")
                        || userPass.getText().toString().matches("")
                        || displayName.getText().toString().matches("")
                        || userPass2.getText().toString().matches("")
                        || tagSetRegister.matches("") || tagSetRegister.isEmpty()) {
                
                	builder.setMessage("Enter your name, username, password and preferences");
                    builder.setTitle(alertTitle);
                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
                }
                
                // retyped password not ok
                else if (!userPass.getText().toString().equalsIgnoreCase(userPass2.getText().toString())) {
                    builder.setMessage("The passwords do not match");
                    builder.setTitle(alertTitle);
                    builder.setPositiveButton("Ok",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                        int which) {
                                    dialog.dismiss();
                                }
                            });
                    
                } // if the register properties are ok
                else {
                	ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		
		            String url = "/register/";
		            
		            // If network connection is ok
		            if (networkInfo != null && networkInfo.isConnected()) {
		                
		                DownloadWebPageTask task = new DownloadWebPageTask();
		                task.execute(new String[] { "POST", "http://" + Constants.SERVER_IP + ":" + Constants.PORT + url});
		            }
                }
                builder.create().show();
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
               task.execute(new String[] { "http://" + Constants.SERVER_IP + ":" + Constants.PORT + url });
           } // If network connection is NOT ok
           else {
               progressBar.setVisibility(View.INVISIBLE);
               // Placing an alert dialog
               final AlertDialog.Builder builder = new AlertDialog.Builder(
                       RegisterActivity.this);

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
               progressBar.setVisibility(View.INVISIBLE);

               Log.d(DEBUG_TAG, "RESULT IS: " + result);

               // Placing an alert dialog
               final AlertDialog.Builder builder = new AlertDialog.Builder(
                       RegisterActivity.this);

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
                       
                       // If I have received ok code than successfully registered 
                       // you go to LOGIN (MAIN ACTIVITY)
                       if (responseJSON.getString("msg").equalsIgnoreCase(
                               JSON_ACCESS_GRANTED)) {
                           
                           builder.setMessage("You have successfully registered");
                           builder.setTitle(alertTitle);

                           builder.setPositiveButton("Ok",
                                   new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                               int which) {
                                           dialog.cancel();
                                           Intent intent = new Intent(
                                                   getApplicationContext(),
                                                   MainActivity.class);
                                           startActivity(intent);
                                           finish();
                                       }
                                   });
                       }
                       // Otherwise try again
                       else {
                           builder.setMessage("Register failed. Try again.");
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
           	jo.put("name", displayName.getText().toString());
           	jo.put("username", userName.getText().toString());
           	jo.put("password", userPass.getText().toString());
            jo.put("tagSet", tagSetRegister.substring(0, tagSetRegister.length() - 2));
           	
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }

}
