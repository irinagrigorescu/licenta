package com.example.licenta;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import General.Constants;
import General.Preferences;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class CurrentActivity extends Activity {
    
    private String alertTitle = "Current Booth";
    private static final String DEBUG_TAG = "CURRENT";
    private static final String JSON_ACCESS_GRANTED = "UserToBooth.Exists";
    private static final String JSON_ACCESS_DENIED = "UserToBooth.DoesNotExist";
    private static final String JSON_QR_GRANTED = "User.CheckInAccepted";
    private static final String JSON_QR_DENIED = "User.CheckOutAccepted"; 
    private static final String INVALID_URL = "Unable to retrieve data. Please try again.";
    
    private String user_id = null;
    
    private RelativeLayout rl;
    private TextView boothName;
    private TextView boothPeople;
    private TextView boothDescription;
    private TextView similarityPerc;
    private TextView similarity;
    private TextView people;
    private TextView tag1;
    private TextView tag2;
    private TextView tag3;
    private TextView noCheckin;
    private ImageView boothLogo;
    private ProgressBar progressBar;
    
    private ImageView qr;
    private ImageView browse;
    private ImageView profile;
    private ImageView profileView; 
    
    String peopleNameSim;
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current);
        Intent sourceIntent = getIntent();
        user_id = sourceIntent.getStringExtra("uid");

        rl = (RelativeLayout) findViewById(R.id.relativeLayoutCurrent);
        noCheckin = (TextView) findViewById(R.id.textViewNoCheckin); // Not checked in
        noCheckin.setVisibility(View.INVISIBLE);
        progressBar = (ProgressBar) findViewById(R.id.progressBarCurrentBooth); // PROGRESS BAR
        
        boothLogo = (ImageView) findViewById(R.id.imageViewCurrent); // LOGO
        boothName = (TextView) findViewById(R.id.textViewCurrentName); // BOOTH NAME
        similarity = (TextView) findViewById(R.id.textViewMatch); // SIMILARITY
        similarityPerc = (TextView) findViewById(R.id.textViewMatchPercentage); // SIMILARITY PERCENTAGE
        people = (TextView) findViewById(R.id.textViewCheckin); // PEOPLE
        boothPeople = (TextView) findViewById(R.id.textViewCheckinNo); // NO OF PEOPLE
        boothDescription = (TextView) findViewById(R.id.textViewDescription); // DESCRIPTION
        tag1 = (TextView) findViewById(R.id.textViewTag1); // TAG 1
        tag2 = (TextView) findViewById(R.id.textViewTag2); // TAG 2
        tag3 = (TextView) findViewById(R.id.textViewTag3); // TAG 3
        
        peopleNameSim = Constants.TAGSET_DELIMITER;
        
        /*
         * Ask for information about current location
         */
        String url = "/current_location/" + user_id;
        Log.d(DEBUG_TAG, "USERID: " + user_id);
        connectionDjango(url);
        
        /*
         * See Matching Users
         * 
         * When you click on the current location layout
         * it takes you to the UserMatchActivity
         */
        rl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserMatchActivity.class);
                Bundle bun = new Bundle();
                bun.putString("uid", user_id);
                bun.putString("users", peopleNameSim);
                intent.putExtras(bun);
                startActivity(intent);
                finish();
            }
            
        });
        
        /*
         * Browse Booths
         * 
         * When you click on the browse button it takes you to
         * the View Booths Activity
         * 
         */
        browse = (ImageView) findViewById(R.id.imageViewBrowse);
        browse.setImageResource(R.drawable.browse);
        browse.setFocusable(true);
        browse.requestFocus();
        browse.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                browse.setImageResource(R.drawable.browse_clicked);
                Intent intent = new Intent(getApplicationContext(), ViewBoothsActivity.class);
                intent.putExtra("uid", user_id);
                startActivity(intent);
                finish();
            }
        });
        
        /*
         * Update Profile
         * 
         * When you click on the profile button it takes you to
         * the Profile Activity
         * 
         */
        profile = (ImageView) findViewById(R.id.imageViewProfile);
        profile.setImageResource(R.drawable.profile_edit);
        profile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.setImageResource(R.drawable.profile_edit_clicked);
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("uid", user_id);
                startActivity(intent);
                finish();
            }
        });
        
        /*
         * View Profile
         * 
         * When you click on the profile button it takes you to
         * the Profile View Activity
         * 
         */
        profileView = (ImageView) findViewById(R.id.imageViewUserProfile);
        profileView.setImageResource(R.drawable.profile);
        profileView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            	profileView.setImageResource(R.drawable.profile_clicked);
                Intent intent = new Intent(getApplicationContext(), ProfileViewActivity.class);
                intent.putExtra("uid", user_id);
                startActivity(intent);
                finish();
            }
        });
        
        /*
         * QR Code Scanner
         * 
         * When you click on the QR button it takes you to
         * the QR Process Activity
         * 
         */
        qr = (ImageView) findViewById(R.id.imageViewCheckin);
        qr.setImageResource(R.drawable.checkin);
        qr.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                qr.setImageResource(R.drawable.checkin_clicked);
                
                Preferences prefs = new Preferences(CurrentActivity.this);
                prefs.saveString("uid", user_id);
                
                // TODO: try-catch
                Intent intent = new Intent("com.google.zxing.client.android.SCAN");
                intent.setPackage("com.google.zxing.client.android");
                intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
                startActivityForResult(intent, 0);
                
                //Intent intent = new Intent(getApplicationContext(), TestActivity.class);
                //startActivity(intent);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Preferences pref = new Preferences(CurrentActivity.this);
        user_id = pref.getValue("uid");
        qr.setImageResource(R.drawable.checkin);
        
        if (requestCode == 0) {
            
            if (resultCode == RESULT_OK) {
                String contents = data.getStringExtra("SCAN_RESULT");
                Log.d("QR_LOG", "QR CODE: " + contents);
                
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

                // If network connection is ok
                if (networkInfo != null && networkInfo.isConnected()) {
                    DownloadWebPageTask task = new DownloadWebPageTask();
                    task.execute(new String[] { "GET", "http://" + Constants.SERVER_IP + contents + "/" + user_id});
                }
            }
            
            if (resultCode == RESULT_CANCELED) {
                Intent intent = new Intent(getApplicationContext(), CurrentActivity.class);
                intent.putExtra("uid", user_id);
                startActivity(intent);
                finish();
            }
        }
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
                       CurrentActivity.this);

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
                       CurrentActivity.this);

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
                       
                       /*
                        * RESULT IS: 
                        * {"checkedIn": 3, "code": 200, "description": null, "title": "Booth_3", "similarity": 5.0, "tagSet": null, 
                        * "msg": "UserToBooth.Exists", "logo": null, 
                        * "users": {"0": {"name": "a", "similarity": 1.23}, 
                        *           "1": {"name": "rares", "similarity": 1.23}, 
                        *           "2": {"name": "i i", "similarity": 1.23}}}
                        */
                       
                       // If I have received ok code than 
                       // I can present the current booth 
                       if (responseJSON.getString("msg").equalsIgnoreCase(
                               JSON_ACCESS_GRANTED)) {
                           
                           progressBar.setVisibility(View.INVISIBLE);
                           
                           // TAG SET
                           if (!responseJSON.getString("tagSet").equals("null")) {
                               // TODO: verify number of tags and setText for the first 3 (or 2 or 1)
                               tag1.setVisibility(View.VISIBLE);
                               tag2.setVisibility(View.VISIBLE);
                               tag3.setVisibility(View.VISIBLE);
                               
                               String tagSetForCurrentBooth = responseJSON.getString("tagSet");
                               String [] values = tagSetForCurrentBooth.split(",");
                               
                               if (values.length == 1) {
                            	   tag1.setText(values[0]);
                               }
                               if (values.length == 2) {
                            	   tag1.setText(values[0]);
                            	   tag2.setText(values[1]);
                               }
                               if (values.length >= 3) {
                            	   tag1.setText(values[0]);
                            	   tag2.setText(values[1]);
                            	   tag3.setText(values[2]);
                               }
                           
                           }
                           // LOGO
                           if (!responseJSON.getString("logo").equals("null")) {
                               // TODO: set image with imageName from logo
                               //boothLogo.setText("j");
                               boothLogo.setVisibility(View.VISIBLE);
                               Drawable id = null;                               
                               if (responseJSON.getString("logo").equalsIgnoreCase("logo1"))
                            	   id = getResources().getDrawable(R.drawable.logo1);
                               if (responseJSON.getString("logo").equalsIgnoreCase("logo2"))
                            	   id = getResources().getDrawable(R.drawable.logo2);
                               if (responseJSON.getString("logo").equalsIgnoreCase("logo3"))
                            	   id = getResources().getDrawable(R.drawable.logo3);
                               if (responseJSON.getString("logo").equalsIgnoreCase("logo4"))
                            	   id = getResources().getDrawable(R.drawable.logo4);
                               boothLogo.setImageDrawable(id);
                           }
                           // DESCRIPTION
                           if (!responseJSON.getString("description").equals("null")) {
                               boothDescription.setText(responseJSON.getString("description"));
                               boothDescription.setVisibility(View.VISIBLE);
                           }
                           // SIMILARITY
                           //if (!responseJSON.getString("similarity").equals("null")) {
                           if (responseJSON.get("similarity") != null && !responseJSON.getString("similarity").equals("null")) {
                               similarityPerc.setText(responseJSON.getString("similarity") + "%");
                               similarity.setVisibility(View.VISIBLE);
                               similarityPerc.setVisibility(View.VISIBLE);
                           }
                           // PEOPLE
                           if (!responseJSON.getString("checkedIn").equals("null")) {
                               boothPeople.setText(responseJSON.getString("checkedIn"));
                               people.setVisibility(View.VISIBLE);
                               boothPeople.setVisibility(View.VISIBLE);
                           }
                           // TITLE
                           if (!responseJSON.getString("title").equals("null")) {
                               boothName.setText(responseJSON.getString("title"));
                               boothName.setVisibility(View.VISIBLE);
                           }
                           
                           
                           // if I have checked in people
                           if (!responseJSON.getString("checkedIn").equals("0")) {
                               
                               peopleNameSim = Constants.TAGSET_DELIMITER;
                               for (int i = 0; i < Integer.parseInt(responseJSON.getString("checkedIn")); i++) {
                                   JSONObject usersBooth = responseJSON.getJSONObject("users").getJSONObject(""+i);
                                   //peopleNameSim.add(new NameSimilarity(usersBooth.getString("name"), usersBooth.getString("similarity")));
                                   peopleNameSim += usersBooth.getString("name") + Constants.TAGSET_DELIMITER + usersBooth.getString("similarity") + Constants.TAGSET_DELIMITER +
                                		   usersBooth.getString("tagSet") + Constants.TAGSET_DELIMITER;
                               }
                               
                               Log.d(DEBUG_TAG, "PEOPLE: " + peopleNameSim);
                           }
                           
                       }
                       else if (responseJSON.getString("msg").equalsIgnoreCase(
                               JSON_ACCESS_DENIED)) {
                           progressBar.setVisibility(View.INVISIBLE);
                           noCheckin.setVisibility(View.VISIBLE);
                       }
                       else if (responseJSON.getString("msg").equalsIgnoreCase(JSON_QR_GRANTED)) {
                           builder.setMessage("Checkin accepted.");
                           builder.setTitle(alertTitle);

                           builder.setPositiveButton("Ok",
                                   new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                               int which) {
                                           dialog.cancel();
                                           progressBar.setVisibility(View.VISIBLE);
                                           noCheckin.setVisibility(View.INVISIBLE);
                                           /*
                                            * Ask for information about current location
                                            */
                                           String url = "/current_location/" + user_id;
                                           Log.d(DEBUG_TAG, "USERID: " + user_id);
                                           connectionDjango(url);                                           
                                       }
                                   });
                       }
                       else if (responseJSON.getString("msg").equalsIgnoreCase(JSON_QR_DENIED)) {
                           builder.setMessage("Checkout accepted.");
                           builder.setTitle(alertTitle);

                           builder.setPositiveButton("Ok",
                                   new DialogInterface.OnClickListener() {
                                       public void onClick(DialogInterface dialog,
                                               int which) {
                                           dialog.cancel();
                                           progressBar.setVisibility(View.INVISIBLE);
                                           noCheckin.setVisibility(View.VISIBLE);
                                           
                                           boothLogo.setVisibility(View.INVISIBLE);
                                           boothName.setVisibility(View.INVISIBLE);
                                           similarity.setVisibility(View.INVISIBLE);
                                           similarityPerc.setVisibility(View.INVISIBLE);
                                           people.setVisibility(View.INVISIBLE);
                                           boothPeople.setVisibility(View.INVISIBLE);
                                           boothDescription.setVisibility(View.INVISIBLE);
                                           tag1.setVisibility(View.INVISIBLE);
                                           tag2.setVisibility(View.INVISIBLE);
                                           tag3.setVisibility(View.INVISIBLE);
                                           
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
                                           progressBar.setVisibility(View.VISIBLE);
                                           
                                           /*
                                            * Ask for information about current location
                                            */
                                           String url = "/current_location/" + user_id;
                                           Log.d(DEBUG_TAG, "USERID: " + user_id);
                                           connectionDjango(url);
                                           
                                       }
                                   });
                       }
                   } catch (JSONException e) {
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
           
           HttpClient client = new DefaultHttpClient();
           HttpPost post = new HttpPost(myurl);
           
           
               JSONObject jo = new JSONObject();
               jo.put("userId", user_id);
               StringEntity se = new StringEntity(jo.toString());
               se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
               post.setEntity(se);
               
               HttpResponse response = client.execute(post);
             
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
        getMenuInflater().inflate(R.menu.current, menu);
        return true;
    }

}
