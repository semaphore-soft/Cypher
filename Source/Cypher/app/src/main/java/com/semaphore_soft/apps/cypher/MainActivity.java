package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;

public class MainActivity extends AppCompatActivity
{
    private static MainPresenter presenter;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final RadioButton char0 = (RadioButton) findViewById(R.id.char0);
        char0.setChecked(true);
        final RadioButton char1 = (RadioButton) findViewById(R.id.char1);
        final RadioButton char2 = (RadioButton) findViewById(R.id.char2);
        final RadioButton char3 = (RadioButton) findViewById(R.id.char3);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Starting AR Activity", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("player", 0);
                if (char0.isChecked()) {
                    intent.putExtra("character", 0);
                }
                else if (char1.isChecked()) {
                    intent.putExtra("character", 1);
                }
                else if (char2.isChecked()) {
                    intent.putExtra("character", 2);
                }
                else if (char3.isChecked()) {
                    intent.putExtra("character", 3);
                }
                startActivity(intent);
            }
        });

        Button btnHost = (Button) findViewById(R.id.btnHost);

        btnHost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Starting AR Activity", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                startActivity(intent);
            }
        });

        Button btnJoin = (Button) findViewById(R.id.btnJoin);
        
        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Snackbar.make(view, "Starting AR Activity", Snackbar.LENGTH_LONG).show();
                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                startActivity(intent);
            }
        });

        if (presenter == null)
        {
            presenter = new MainPresenter();
        }

        presenter.setView(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings)
        {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
