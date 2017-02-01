package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

/**
 * Created by Scorple on 1/9/2017.
 */

public class CharacterSelectActivity extends AppCompatActivity
{
    boolean host;
    long    playerID;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.character_select);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        host = getIntent().getBooleanExtra("host", false);

        playerID = getIntent().getLongExtra("player", 0);

        Button btnGo = (Button) findViewById(R.id.btnGo);

        final RadioButton char0 = (RadioButton) findViewById(R.id.char0);
        char0.setChecked(true);
        final RadioButton char1 = (RadioButton) findViewById(R.id.char1);
        final RadioButton char2 = (RadioButton) findViewById(R.id.char2);
        final RadioButton char3 = (RadioButton) findViewById(R.id.char3);

        btnGo.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Starting AR Activity", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("host", host);
                intent.putExtra("player", playerID);
                if (char0.isChecked())
                {
                    intent.putExtra("character", 0);
                }
                else if (char1.isChecked())
                {
                    intent.putExtra("character", 1);
                }
                else if (char2.isChecked())
                {
                    intent.putExtra("character", 2);
                }
                else if (char3.isChecked())
                {
                    intent.putExtra("character", 3);
                }
                startActivity(intent);
            }
        });
    }
}
