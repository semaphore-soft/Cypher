package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements GetNameDialogFragment.GetNameDialogListener
{
    private static MainPresenter presenter;

    boolean host = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Toast.makeText(getApplicationContext(), "Host or Join a Game", Toast.LENGTH_SHORT).show();
            }
        });

        Button btnHost = (Button) findViewById(R.id.btnHost);

        btnHost.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                host = true;
                showGetNameDialog();
                /*Toast.makeText(getApplicationContext(), "Moving to Connection Lobby", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
                startActivity(intent);*/
            }
        });

        Button btnJoin = (Button) findViewById(R.id.btnJoin);
        
        btnJoin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                host = false;
                showGetNameDialog();
                /*Toast.makeText(getApplicationContext(), "Moving to Join Game Lobby", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
                startActivity(intent);*/
            }
        });

        if (presenter == null)
        {
            presenter = new MainPresenter();
        }

        presenter.setView(this);
    }

    public void showGetNameDialog() {
        FragmentManager fm = getSupportFragmentManager();
        GetNameDialogFragment getNameDialogFragment = new GetNameDialogFragment();
        //getNameDialogFragment.setStyle(DialogFragment.STYLE_NORMAL, R.style.AppTheme);
        getNameDialogFragment.setListener(this);
        getNameDialogFragment.show(fm, "get_name_dialog");
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

    @Override
    public void onFinishGetName(String name)
    {
        if (host) {
            Toast.makeText(getApplicationContext(), "Moving to Connection Lobby", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
            intent.putExtra("host", true);
            intent.putExtra("name", name);
            startActivity(intent);
        }
        else {
            Toast.makeText(getApplicationContext(), "Moving to Join Game Lobby", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), JoinGameActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }
}
