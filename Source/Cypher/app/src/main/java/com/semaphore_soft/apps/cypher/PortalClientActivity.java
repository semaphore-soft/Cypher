package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIPortalActivity;
import com.semaphore_soft.apps.cypher.ui.UIPortalOverlay;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;

/**
 * Created by rickm on 3/1/2017.
 */

public class PortalClientActivity extends ARActivity implements UIListener,
                                                                ResponseReceiver.Receiver
{

    private UIPortalActivity uiPortalActivity;
    private UIPortalOverlay  uiPortalOverlay;

    private PortalRenderer renderer;

    private static ResponseReceiver responseReceiver;
    private static ClientService    clientService;
    private static boolean mClientBound = false;
    private static Handler handler      = new Handler();

    private static int    playerId;
    private static String characterName;

    private static boolean turn;

    private static ArrayList<Integer> reservedMarkers;

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty);

        uiPortalActivity = new UIPortalActivity(this);
        ((FrameLayout) this.findViewById(R.id.empty)).addView(uiPortalActivity);
        uiPortalActivity.setUIListener(this);

        uiPortalOverlay = new UIPortalOverlay(this);
        ((FrameLayout) this.findViewById(R.id.empty)).addView(uiPortalOverlay);
        uiPortalOverlay.setUIListener(this);

        renderer = new PortalRenderer();
        renderer.setContext(this);
        //PortalRenderer.setGameController(this);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        playerId = getIntent().getExtras().getInt("player");
        characterName = getIntent().getStringExtra("character");

        turn = false;

        reservedMarkers = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart()
    {
        super.onStart();
        // Bind to ClientService
        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop()
    {
        super.onStop();
        if (mClientBound)
        {
            unbindService(mClientConnection);
            mClientBound = false;
        }
    }

    @Override
    protected ARRenderer supplyRenderer()
    {
        return renderer;
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    protected FrameLayout supplyFrameLayout()
    {
        return (FrameLayout) this.findViewById(R.id.portal_frame);
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
            case "cmd_btnPlayerMarkerSelect":
            {
                int firstUnreservedMarker = getFirstUnreservedMarker();

                if (firstUnreservedMarker > -1)
                {
                    clientService.write("mark_request;" + firstUnreservedMarker);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg      Message read from network
     * @param readFrom Device that message was received from
     */
    @Override
    public void handleRead(String msg, int readFrom)
    {
        Toast.makeText(this, "Read: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();

        if (msg.startsWith("reserve"))
        {
            String[] splitMsg = msg.split(";");

            reservedMarkers.add(Integer.parseInt(splitMsg[1]));
        }
        else if (msg.equals("wait"))
        {
            uiPortalOverlay.overlayWaitingForHost();
        }
        else if (msg.startsWith("attach"))
        {
            String[] splitMsg = msg.split(";");

            renderer.setPlayerMarker(Integer.parseInt(splitMsg[3]), Integer.parseInt(splitMsg[2]));
        }
        else if (msg.startsWith("show_action"))
        {
            String[] splitMsg = msg.split(";");

            renderer.showAction(Integer.parseInt(splitMsg[1]),
                                Integer.parseInt(splitMsg[2]),
                                Integer.parseInt(splitMsg[3]),
                                Long.parseLong(splitMsg[4]),
                                splitMsg[5],
                                splitMsg[6],
                                Boolean.parseBoolean(splitMsg[7]));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg      Status update
     * @param readFrom Device that update was received from
     */
    @Override
    public void handleStatus(String msg, int readFrom)
    {
        Toast.makeText(this, "Status: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();
    }

    /**
     * {@inheritDoc}
     *
     * @param msg      Error message
     * @param readFrom Device that error was received from
     */
    @Override
    public void handleError(String msg, int readFrom)
    {
        Toast.makeText(this, "Error: " + msg + " from <" + readFrom + ">", Toast.LENGTH_SHORT)
             .show();
    }

    /**
     * Get the first AR marker reference ID corresponding to an AR marker which
     * is visible to the {@link ARRenderer} and not in the reserved marker IDs
     * list.
     *
     * @return int: The first AR marker reference ID corresponding to an AR
     * marker which is visible to the {@link ARRenderer} and not in the
     * reserved marker IDs list.
     */
    private int getFirstUnreservedMarker()
    {
        int foundMarker = renderer.getFirstMarkerExcluding(reservedMarkers);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

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
