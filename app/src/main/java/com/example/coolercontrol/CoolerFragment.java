package com.example.coolercontrol;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class CoolerFragment extends Fragment {
    public TextView showCountTextView;
    Integer temp_count;

    private void countUp(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count = Integer.parseInt(countString);
        temp_count++;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }

    private void countDwn(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count= Integer.parseInt(countString);
        temp_count--;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if(savedInstanceState != null){
            temp_count = savedInstanceState.getInt("temp_count",0);
        }
    }
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        //super.onCreateView(inflater,container,savedInstanceState);
        LayoutInflater lf = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = lf.inflate(R.layout.fragment_cooler, null);

        //Get the count text view
        showCountTextView = (TextView) v.findViewById(R.id.textview_first);
        return v;
    }
/*    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        showCountTextView = (TextView) getActivity().findViewById(R.id.textview_first);
        showCountTextView.setText(temp_count);
    }*/
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(getActivity(), "System Updated!", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

        view.findViewById(R.id.down_count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDwn(view);
            }
        });
        view.findViewById(R.id.up_count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countUp(view);
            }
        });

    }

    public void onSavedInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putInt("temp_count", temp_count);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}