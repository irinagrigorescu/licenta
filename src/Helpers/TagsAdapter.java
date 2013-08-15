package Helpers;

import java.util.List;

import com.example.licenta.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class TagsAdapter extends BaseAdapter {
	
	private List<Tag> tagList;
	private Context context;
	
	public TagsAdapter(Context context, List<Tag> tagList){
        this.tagList = tagList;
        this.context = context;
    }

	@Override
	public int getCount() {
		return this.tagList.size();
	}

	@Override
	public Object getItem(int position) {
		if (position < 0 || position > this.tagList.size() - 1)
            return null;
        return this.tagList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View view, ViewGroup parent) {
		
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        view = inflater.inflate(R.layout.tag_element_layout, parent, false);
        
        Tag currentTag = this.tagList.get(position);
        
        TextView tagName = (TextView) view.findViewById(R.id.tag_element_name_layout);
        tagName.setText(currentTag.getTagName());
        		
		return view;
	}

}
