package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Crud Stuntley on 2/5/2017.
 */

public class MainActivityView extends View {
    public MainActivityView(Context context, final UIListener listener) {
        super(context);

        //all buttons live here
        //define on click
            //call UIListener method
        LayoutInflater inflater = LayoutInflater.from(context);

        View v = inflater.inflate(R.layout.activity_main, null);
        //inflater.addView(v);

        //Host Button
        Button btnHost = (Button) v.findViewById(R.id.btnHost);
        btnHost.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                listener.onCommand("cmd_btnHost");
            }
        });

        //Join Button
        Button btnJoin = (Button) v.findViewById(R.id.btnJoin);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listener.onCommand("cmd_btnJoin");
            }
        });

        //Launch Button
        Button btnLaunch = (Button) v.findViewById(R.id.btnLaunch);
        btnLaunch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listener.onCommand("cmd_btnLaunch");
            }
        });
    }
}
