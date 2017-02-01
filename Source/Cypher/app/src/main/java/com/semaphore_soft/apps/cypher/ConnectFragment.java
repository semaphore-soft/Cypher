package com.semaphore_soft.apps.cypher;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Evan on 1/31/2017.
 */

public class ConnectFragment extends DialogFragment
{
    private EditText addr;
    private Button cancel;
    private Button connect;

    private callback myListener;

    public ConnectFragment()
    {

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View myView = inflater.inflate(R.layout.connect, container, false);

        getDialog().setTitle("Enter host address");

        addr = (EditText) myView.findViewById(R.id.hostAddr);

        cancel = (Button) myView.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                dismiss();
            }
        });

        connect = (Button) myView.findViewById(R.id.connect);
        connect.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                try
                {
                    myListener.doNetwork(InetAddress.getByName(addr.getText().toString()));
                }
                catch (UnknownHostException e)
                {
                    e.printStackTrace();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Error");
                    builder.setMessage("Unable to connect to host \nPlease try again");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i)
                        {
                            dialogInterface.dismiss();
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }

                // hide keyboard on fragment exit
                InputMethodManager imm = (InputMethodManager) getActivity()
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);

                dismiss();
            }
        });

        return myView;
    }

    public interface callback {
        void doNetwork(InetAddress addr);
    }

    public void setListener(callback c)
    {
        myListener = c;
    }
}
