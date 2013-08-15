package General;

import android.content.Context;
import android.content.SharedPreferences;

public class Preferences {
    private Context context;
    private static final String PREFERENCE_FILE = "com.example.licenta.prefs";
    private SharedPreferences prefs;
    
    public Preferences(Context context){
        this.context = context;
        
        prefs = context.getSharedPreferences(PREFERENCE_FILE, Context.MODE_PRIVATE);
        
    }
    
    public void saveString(String key, String value){
        prefs.edit().putString(key, value).commit();
    }
    
    public String getValue(String key){
        return prefs.getString(key, "");
    
    }

}
