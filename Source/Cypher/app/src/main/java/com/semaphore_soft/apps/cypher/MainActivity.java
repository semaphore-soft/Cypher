package com.semaphore_soft.apps.cypher;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.ui.GetNameDialogFragment;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIMainActivity;

public class MainActivity extends AppCompatActivity implements GetNameDialogFragment.GetNameDialogListener,
                                                               UIListener
{
    boolean host = false;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        UIMainActivity UIMainActivity = new UIMainActivity(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(UIMainActivity);
        UIMainActivity.setUIListener(this);

        setSupportActionBar(UIMainActivity.getToolbar());
    }

    public void showGetNameDialog()
    {
        FragmentManager       fm                    = getSupportFragmentManager();
        GetNameDialogFragment getNameDialogFragment = new GetNameDialogFragment();
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
        if (host)
        {
            Toast.makeText(getApplicationContext(),
                           "Moving to Connection Lobby",
                           Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getBaseContext(), ConnectionLobbyActivity.class);
            intent.putExtra("host", host);
            intent.putExtra("name", name);
            startActivity(intent);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Moving to Join Game Lobby", Toast.LENGTH_SHORT)
                 .show();
            Intent intent = new Intent(getBaseContext(), JoinGameActivity.class);
            intent.putExtra("name", name);
            startActivity(intent);
        }
    }

    @Override
    public void onCommand(String cmd)
    {
        switch (cmd)
        {
            case "cmd_btnHost":
                host = true;
                showGetNameDialog();
                break;
            case "cmd_btnJoin":
                host = false;
                showGetNameDialog();
                break;
            case "cmd_btnLaunch":
                Toast.makeText(getApplicationContext(), "Launching AR Activity", Toast.LENGTH_SHORT)
                     .show();

                Intent intent = new Intent(getBaseContext(), PortalActivity.class);
                intent.putExtra("host", true);
                intent.putExtra("player", 0);
                intent.putExtra("character", 0);
                startActivity(intent);
                break;
            default:
                Toast.makeText(this, "UI interaction not handled", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
