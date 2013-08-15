package com.example.licenta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import General.Constants;
import Helpers.MyUserComparator;
import Helpers.User;
import Helpers.UserAdapter;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class UserMatchActivity extends Activity {

	private static final String DEBUG_TAG = "USERMATCH";
	
    private String user_id = null;
    ArrayList<String> peopleNameSim;
    private ArrayAdapter<String> similarityAdapter;

    private ListView listview;
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_match);
        Intent sourceIntent = getIntent();
        Bundle bundle = sourceIntent.getExtras();

        //peopleNameSim = new ArrayList<String>();
        List<User> userList = new ArrayList<User>();
        List<String> tagSet = new ArrayList<String>();
        user_id = bundle.getString("uid");

        if (bundle.getString("users").equals(Constants.TAGSET_DELIMITER)) {
            //peopleNameSim.add("No Users Checked In");
        	userList.add(new User("No users checked in", "", ""));
        } else {
            String[] splits = bundle.getString("users").split(
                    Constants.TAGSET_DELIMITER);
            for (int i = 1; i < splits.length - 1; i += 3) {
                userList.add(new User(splits[i], splits[i+1], splits[i+2]));
            }
        }

        Collections.sort(userList, new MyUserComparator());
        listview = (ListView) findViewById(R.id.listviewUsers);
        adapter = new UserAdapter(UserMatchActivity.this, userList);
        listview.setAdapter(adapter);

    }

    /*
     * The Go Back to Current Location Button
     * 
     * When you click on the go back to current location button it takes you to
     * the Current Activity
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
        getMenuInflater().inflate(R.menu.user_match, menu);
        return true;
    }

}
