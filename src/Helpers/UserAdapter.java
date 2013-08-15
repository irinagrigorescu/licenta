package Helpers;

import java.util.List;

import com.example.licenta.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class UserAdapter extends BaseAdapter {

    private List<User> userList;
    private Context context;

    public UserAdapter(Context context, List<User> userList) {
        this.userList = userList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return this.userList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position >= this.userList.size())
            return null;
        return this.userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.user_element_layout, parent, false);
        User users = this.userList.get(position);

        TextView userName = (TextView) view
                .findViewById(R.id.user_element_name_layout);
        userName.setText(users.getUserDisplayName());
        
        TextView userSimilarity = (TextView) view.findViewById(R.id.user_element_similarity_layout);
        userSimilarity.setText(users.getUserSimilarity() + "%");
        
        TextView userTagSet = (TextView) view.findViewById(R.id.user_element_tagset_layout);
        userTagSet.setText(users.getUserTagSet());        		

        return view;
    }

}
