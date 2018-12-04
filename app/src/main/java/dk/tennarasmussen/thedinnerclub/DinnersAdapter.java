package dk.tennarasmussen.thedinnerclub;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import dk.tennarasmussen.thedinnerclub.Model.Dinner;

//Code modified from https://youtu.be/Vyqz_-sJGFk
public class DinnersAdapter extends RecyclerView.Adapter<DinnersAdapter.ViewHolder> {
    private static final String TAG = "DinnersAdapter";

    ArrayList<Dinner> mDinnerList = new ArrayList<>();
    private Context mContext;

    public DinnersAdapter(ArrayList<Dinner> mDinnerList, Context mContext) {
        this.mDinnerList = mDinnerList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.dinner_listitem, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int i) {
        Log.i(TAG, "onBindViewHolder: called");

        if(mDinnerList.get(i).getImageURL() != null)
        {
            Glide.with(mContext)
                    .asBitmap()
                    .load(mDinnerList.get(i).getImageURL())
                    .into(viewHolder.dinnerImage);
        } else {
            Glide.with(mContext)
                    .asBitmap()
                    .load("http://www.dinktoons.com/wp-content/uploads/2011/06/dinosaur-veggie.jpg")
                    .into(viewHolder.dinnerImage);
        }

        viewHolder.dinnerHost.setText(mDinnerList.get(i).getHost().getName() + "'s");
        Date date = new Date(mDinnerList.get(i).getDateTime());
        SimpleDateFormat simpleDate =  new SimpleDateFormat("EEE, d MMM yyyy HH:mm");
        String strDt = simpleDate.format(date);
        viewHolder.dinnerDateTime.setText(strDt);

        viewHolder.dinnerItemParent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "onClick: clicked on dinner: " + mDinnerList.get(i).getDateTime());

                Toast.makeText(mContext, "Clicked on Dinner at: " + mDinnerList.get(i).getHost().getName(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDinnerList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        ImageView dinnerImage;
        TextView dinnerHost;
        TextView dinnerDateTime;
        LinearLayout dinnerItemParent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            dinnerImage = itemView.findViewById(R.id.ivDinnerImage);
            dinnerHost = itemView.findViewById(R.id.tvDinnerItemHost);
            dinnerDateTime = itemView.findViewById(R.id.tvDinnerItemDateTime);
            dinnerItemParent = itemView.findViewById(R.id.list_item);
        }
    }

    public void setmDinnerList(ArrayList<Dinner> mDinnerList) {
        this.mDinnerList = mDinnerList;
        notifyDataSetChanged();
    }
}
