package Helpers;

import java.util.List;

import com.example.licenta.R;
import com.example.licenta.R.id;
import com.example.licenta.R.layout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BoothsAdapter extends BaseAdapter{
    private List<Booth> boothList;
    private Context context;
    
    public BoothsAdapter(Context context, List<Booth> boothList){
        this.boothList = boothList;
        this.context = context;
    }
    
    @Override
    public int getCount() {
        return this.boothList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position < 0 || position > this.boothList.size() - 1)
            return null;
        return this.boothList.get(position);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        view = inflater.inflate(R.layout.booth_element_layout, parent, false);
        
        Booth currentBooth = this.boothList.get(position);
        
        TextView boothTitle = (TextView) view.findViewById(R.id.booth_title_layout);
        boothTitle.setText(currentBooth.getTitle());
        
        TextView boothDescription = (TextView) view.findViewById(R.id.booth_description_title_layout);
        boothDescription.setText(currentBooth.getDescription());
        
        TextView boothSimilarity = (TextView) view.findViewById(R.id.booth_similarity_layout);
        boothSimilarity.setText(currentBooth.getSimilarity() + "%");
        
        TextView boothNoCheckedIn = (TextView) view.findViewById(R.id.no_checked_in_layout);
        boothNoCheckedIn.setText(String.valueOf(currentBooth.getNoCheckedInPeople()));
        
        if (currentBooth.getTags().size() >= 3)
        {
            TextView boothTag1 = (TextView) view.findViewById(R.id.booth_layout_tag1);
            boothTag1.setText(currentBooth.getTags().get(0));
            
            TextView boothTag2 = (TextView) view.findViewById(R.id.booth_layout_tag2);
            boothTag2.setText(currentBooth.getTags().get(1));
            
            TextView boothTag3 = (TextView) view.findViewById(R.id.booth_layout_tag3);
            boothTag3.setText(currentBooth.getTags().get(2));
        }
        
        Drawable id = null;
        if (currentBooth.getLogo().equalsIgnoreCase("logo1"))
     	   id = view.getResources().getDrawable(R.drawable.logo1);
        if (currentBooth.getLogo().equalsIgnoreCase("logo2"))
     	   id = view.getResources().getDrawable(R.drawable.logo2);
        if (currentBooth.getLogo().equalsIgnoreCase("logo3"))
     	   id = view.getResources().getDrawable(R.drawable.logo3);
        if (currentBooth.getLogo().equalsIgnoreCase("logo4"))
     	   id = view.getResources().getDrawable(R.drawable.logo4);
        
        ImageView boothLogo = (ImageView) view.findViewById(R.id.booth_logo_layout); // LOGO
        boothLogo.setImageDrawable(id);
        
        return view;
    }

}
