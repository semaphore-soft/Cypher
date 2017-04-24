package com.semaphore_soft.apps.cypher.ui;

import android.app.Activity;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Scorple on 1/12/2017.
 * Dialog to ask for the players name.
 */

public class GetNameDialogFragment extends DialogFragment implements View.OnClickListener
{
    private GetNameDialogListener listener;

    private EditText txtName;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.get_name_dialog, container);
        txtName = (EditText) view.findViewById(R.id.txtName);
        txtName.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent)
            {
                if (actionId == EditorInfo.IME_ACTION_GO)
                {
                    String name = txtName.getText().toString();
                    listener.onFinishGetName(name);
                    return true;
                }
                return false;
            }
        });
        txtName.requestFocus();
        getDialog().getWindow()
                   .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        getDialog().setTitle("Player Name");
        view.findViewById(R.id.btnDone).setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v)
    {
        String name = txtName.getText().toString();
        listener.onFinishGetName(name);
        this.dismiss();
    }

    public interface GetNameDialogListener
    {
        /**
         * @param name Name of the player
         */
        void onFinishGetName(String name);
    }

    public void setListener(Activity activity)
    {
        listener = (GetNameDialogListener) activity;
    }
}
