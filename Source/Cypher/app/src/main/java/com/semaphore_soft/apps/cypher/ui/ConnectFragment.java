package com.semaphore_soft.apps.cypher.ui;


import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Evan on 1/31/2017.
 * Dialog to get host address information
 */

public class ConnectFragment extends DialogFragment
{
    private EditText name;
    private EditText addr;

    private Callback myListener;

    public ConnectFragment()
    {

    }

    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View myView = inflater.inflate(R.layout.connect, container, false);

        getDialog().setTitle("Enter connection info");

        name = (EditText) myView.findViewById(R.id.playerName);
        name.requestFocus();
        getDialog().getWindow()
                   .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        addr = (EditText) myView.findViewById(R.id.hostAddr);
        addr.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (actionId == EditorInfo.IME_ACTION_GO)
                {
                    myListener.startClient(addr.getText().toString(), name.getText().toString());
                    return true;
                }
                return false;
            }
        });

        Button cancel = (Button) myView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        Button connect = (Button) myView.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                myListener.startClient(addr.getText().toString(), name.getText().toString());
                dismiss();
            }
        });

        return myView;
    }

    public interface Callback
    {
        /**
         * @param addr Address to connect to
         * @param name Name of the player
         */
        void startClient(String addr, String name);
    }

    public void setListener(Callback c)
    {
        myListener = c;
    }
}
