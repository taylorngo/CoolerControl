package com.example.coolercontrol;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amplifyframework.datastore.generated.model.Coordinate;

import java.util.ArrayList;

public class RecyclerAdapter extends ArrayAdapter<Coordinate> {

    private LayoutInflater layoutInflater;
    private Context mContext;
    private ArrayList<Coordinate> data;
    private int ViewId;
    //private List<Coordinate.Item> mData = new ArrayList<>();

    public RecyclerAdapter(Context mContext, int viewId, ArrayList<Coordinate> data) {
        super(mContext, viewId, data);
        this.mContext = mContext;
        this.data = data;
        layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ViewId = viewId;

    }

    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(ViewId, null);
        Coordinate coord = data.get(position);
        if (coord != null) {
            TextView cloudId = (TextView) convertView.findViewById(R.id.cloud_id);
            TextView cloudDate = (TextView) convertView.findViewById(R.id.cloud_date);

            if (cloudId != null) {
                cloudId.setText(coord.getId());
            }
            if (cloudDate != null) {
                cloudDate.setText(coord.getDatetime().toString());
            }
        }
        return convertView;
    }
}
