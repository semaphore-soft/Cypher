package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.networking.ServerService;
import com.semaphore_soft.apps.cypher.ui.UICharacterSelect;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by Scorple on 1/9/2017.
 * Activity where players will choose their characters.
 * Only one of each character is allowed.
 *
 * @see UICharacterSelect
 */

public class CharacterSelectActivity extends AppCompatActivity implements ResponseReceiver.Receiver,
                                                                          UIListener
{
    private UICharacterSelect uiCharacterSelect;

    private ResponseReceiver responseReceiver;
    private ServerService    serverService;
    private ClientService    clientService;
    private Handler handler       = new Handler();
    private boolean mServerBound  = false;
    private boolean mClientBound  = false;
    private boolean sendHeartbeat = true;

    private boolean host;
    private int     playerID;
    private int     numClients;

    private int     playersReady = 0;
    private boolean ready        = false;

    private static String                   selection           = "";
    private static HashMap<Integer, String> characterSelections = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        uiCharacterSelect = new UICharacterSelect(this);
        ((FrameLayout) findViewById(R.id.empty)).addView(uiCharacterSelect);
        uiCharacterSelect.setUIListener(this);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        host = getIntent().getBooleanExtra("host", false);

        playerID = getIntent().getIntExtra("player", 0);

        if (host)
        {
            numClients = getIntent().getIntExtra("numClients", 0);
            // Include host when displaying connected players
            uiCharacterSelect.setStatus(0 + "/" + (numClients + 1) + " ready");
        }
        else
        {
            uiCharacterSelect.setStatus("Select a character");
        }

        // Make sure host can't start game until everyone has picked a character
        uiCharacterSelect.setStartEnabled(false);
    }

    // Runnable to send heartbeat signal to client
    private Runnable heartbeat = new Runnable()
    {
        @Override
        public void run()
        {
            if (sendHeartbeat)
            {
                serverService.writeAll(NetworkConstants.GAME_HEARTBEAT);
                handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
            }
        }
    };

    @Override
    protected void onStart()
    {
        super.onStart();
        if (host)
        {
            // Bind to ServerService
            Intent intent = new Intent(this, ServerService.class);
            bindService(intent, mServerConnection, Context.BIND_AUTO_CREATE);
            handler.postDelayed(heartbeat, NetworkConstants.HEARTBEAT_DELAY);
        }
        else
        {
            // Bind to ClientService
            Intent intent = new Intent(this, ClientService.class);
            bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
        }

    }

    @Override
    protected void onStop()
    {
        super.onStop();
        // Unbind from the services
        if (mServerBound)
        {
            unbindService(mServerConnection);
            mServerBound = false;
        }
        else if (mClientBound)
        {
            unbindService(mClientConnection);
            mClientBound = false;
        }
        sendHeartbeat = false;
    }

    /**
     * {@inheritDoc}
     *
     * @param cmd Command from UI interaction
     */
    @Override
    public void onCommand(String cmd)
    {
        switch (cmd)
        {
            case "knight":
            case "soldier":
            case "ranger":
            case "wizard":
                postSelection(cmd);
                break;
            case "clear":
                clearSelection();
                break;
            case "cmd_btnStart":
                if (host)
                {
                    serverService.writeAll(NetworkConstants.GAME_AR_START);
                    startARHost();
                }
                break;
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Verifies that only one of each character is selected.
     *
     * @param msg      Message read from network
     * @param readFrom Device that message was received from
     */
    @Override
    public void handleRead(String msg, int readFrom)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.GAME_KNIGHT) || msg.equals(NetworkConstants.GAME_SOLDIER) ||
            msg.equals(NetworkConstants.GAME_RANGER) || msg.equals(NetworkConstants.GAME_WIZARD))
        {
            updateSelection(msg, readFrom);
        }
        else if (msg.equals(NetworkConstants.GAME_UNREADY))
        {
            removePlayer(readFrom);
            playersReady--;
            if (playersReady < numClients)
            {
                uiCharacterSelect.setStartEnabled(false);
            }
            // Include host when displaying connected players
            uiCharacterSelect.setStatus(playersReady + "/" + (numClients + 1) + " ready");
            serverService.writeAll(
                NetworkConstants.PREFIX_READY + playersReady + ":" + (numClients + 1));
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_LOCK))
        {
            // Use substring to ignore prefix and get the selection
            String sel = msg.substring(5);
            if (!sel.equals(selection))
            {
                uiCharacterSelect.setButtonEnabled(sel, false);
            }
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_FREE))
        {
            uiCharacterSelect.setButtonEnabled(msg.substring(5), true);
        }
        else if (msg.equals(NetworkConstants.GAME_TAKEN))
        {
            ready = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Error");
            builder.setMessage("Character already taken");
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
            uiCharacterSelect.clearSelection();
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_READY))
        {
            if (ready)
            {
                // Expect the number of ready players,
                // and the total number of connected players
                String[] args = msg.split(":");
                uiCharacterSelect.setStatus(args[1] + "/" + args[2] + " ready");
            }
        }
        else if (msg.equals(NetworkConstants.GAME_AR_START))
        {
            startARClient();
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Synchronizes state with client after a reconnect.
     *
     * @param msg      Status update
     * @param readFrom Device that update was received from
     */
    @Override
    public void handleStatus(String msg, int readFrom)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.STATUS_SERVER_START))
        {
            Set<Integer> set = characterSelections.keySet();
            for (Integer key : set)
            {
                // Don't lock the clients last selection
                if (key != readFrom)
                {
                    String str = NetworkConstants.PREFIX_LOCK + characterSelections.get(key);
                    serverService.writeToClient(str, readFrom);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Resets client selections after a reconnect to they can be synchronized by the host.
     *
     * @param msg      Error message
     * @param readFrom Device that error was received from
     */
    @Override
    public void handleError(String msg, int readFrom)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
        if (msg.equals(NetworkConstants.ERROR_DISCONNECT_CLIENT))
        {
            // Reset all selections in case they changed while disconnected
            uiCharacterSelect.setButtonEnabled("knight", true);
            uiCharacterSelect.setButtonEnabled("soldier", true);
            uiCharacterSelect.setButtonEnabled("ranger", true);
            uiCharacterSelect.setButtonEnabled("wizard", true);
        }
    }

    /**
     * Update the host with the players characters selection.
     *
     * @param selection Character selected by player
     */
    private void postSelection(String selection)
    {
        CharacterSelectActivity.selection = selection;
        if (host)
        {
            updateSelection(selection, 0);
        }
        else
        {
            clientService.write(selection);
        }
        ready = true;
    }

    /**
     * Remove a players character selection and update game ready status.
     */
    private void clearSelection()
    {
        if (host)
        {
            uiCharacterSelect.setStartEnabled(false);
            --playersReady;
            uiCharacterSelect.setStatus(
                playersReady + "/" + (numClients + 1) + " ready");
            serverService.writeAll(
                NetworkConstants.PREFIX_READY + playersReady + ":" + (numClients + 1));
            removePlayer(0);
        }
        else
        {
            if (ready)
            {
                clientService.write(NetworkConstants.GAME_UNREADY);
            }
            uiCharacterSelect.setStatus("Select a Character");
        }
        selection = "";
        ready = false;
    }

    /**
     * Checks if players selection is valid and updates the players character selection and
     * ready status.
     *
     * @param selection Character selected by player
     * @param player    Player ID
     */
    private void updateSelection(String selection, int player)
    {
        if (characterSelections.containsValue(selection))
        {
            Logger.logI("Character taken");
            serverService.writeToClient(NetworkConstants.GAME_TAKEN, player);
            return;
        }
        if (characterSelections.containsKey(player))
        {
            // Update players character
            removePlayer(player);
        }
        else
        {
            playersReady++;
        }
        characterSelections.put(player, selection);
        serverService.writeAll(NetworkConstants.PREFIX_LOCK + selection);
        if (!selection.equals(CharacterSelectActivity.selection))
        {
            uiCharacterSelect.setButtonEnabled(selection, false);
        }
        // Since default value is 0, allow host to start game
        // even if numClients == 0 and clients are connected
        if (playersReady >= numClients)
        {
            uiCharacterSelect.setStartEnabled(true);
        }
        // Include host when displaying connected players
        uiCharacterSelect.setStatus(playersReady + "/" + (numClients + 1) + " ready");
        serverService.writeAll(
            NetworkConstants.PREFIX_READY + playersReady + ":" + (numClients + 1));
    }

    /**
     * Remove a given players character selection and notify clients.
     *
     * @param player ID of player
     */
    private void removePlayer(int player)
    {
        String character = characterSelections.get(player);
        serverService.writeAll(NetworkConstants.PREFIX_FREE + character);
        uiCharacterSelect.setButtonEnabled(character, true);
        characterSelections.remove(player);
    }

    /**
     * Starts {@link PortalActivity}.
     */
    private void startARHost()
    {
        Toast.makeText(CharacterSelectActivity.this, "Starting AR Activity", Toast.LENGTH_SHORT)
             .show();
        LocalBroadcastManager.getInstance(CharacterSelectActivity.this)
                             .unregisterReceiver(responseReceiver);
        // Prevent heartbeat runnable from continuing into new activity
        handler.removeCallbacks(heartbeat);

        Intent intent = new Intent(getBaseContext(), PortalActivity.class);
        intent.putExtra("character", selection);
        intent.putExtra("num_clients", numClients);

        intent.putExtra("character_selection", characterSelections);

        startActivity(intent);
    }

    private void startARClient()
    {
        Toast.makeText(CharacterSelectActivity.this, "Starting AR Activity", Toast.LENGTH_SHORT)
             .show();
        LocalBroadcastManager.getInstance(CharacterSelectActivity.this)
                             .unregisterReceiver(responseReceiver);

        Intent intent = new Intent(getBaseContext(), PortalClientActivity.class);
        intent.putExtra("player", playerID);
        intent.putExtra("character", selection);

        startActivity(intent);
    }

    // Defines callbacks for service binding, passed to bindService()
    private ServiceConnection mServerConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ServerService.LocalBinder binder = (ServerService.LocalBinder) iBinder;
            serverService = binder.getService();
            mServerBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mServerBound = false;
        }
    };

    private ServiceConnection mClientConnection = new ServiceConnection()
    {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder)
        {
            // We've bound to ServerService, cast the IBinder and get ServerService instance
            ClientService.LocalBinder binder = (ClientService.LocalBinder) iBinder;
            clientService = binder.getService();
            mClientBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName)
        {
            mClientBound = false;
        }
    };
}
