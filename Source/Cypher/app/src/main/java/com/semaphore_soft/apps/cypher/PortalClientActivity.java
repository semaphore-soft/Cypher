package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
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
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author scorple
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

    private static int    playerId;
    private static String characterName;

    private static boolean turn;

    private static ArrayList<Integer> reservedMarkers;

    private static ArrayList<String> playerTargets;
    private static ArrayList<String> nonPlayerTargets;
    private static ArrayList<String> special;

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
                    clientService.write(
                        NetworkConstants.PREFIX_MARK_REQUEST + firstUnreservedMarker);
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

        if (msg.startsWith(NetworkConstants.PREFIX_RESERVE))
        {
            // Expect the MarkerID to be reserved
            String[] splitMsg = msg.split(":");

            reservedMarkers.add(Integer.parseInt(splitMsg[1]));
        }
        else if (msg.equals(NetworkConstants.GAME_WAIT))
        {
            uiPortalOverlay.overlayWaitingForHost();
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_ATTACH))
        {
            // Expect the PlayerID of player to be attached,
            // and the MarkerID for the marker the player should be attached to.
            String[] splitMsg = msg.split(":");

            renderer.setPlayerMarker(Integer.parseInt(splitMsg[2]), Integer.parseInt(splitMsg[3]));
        }
        else if (msg.equals(NetworkConstants.GAME_START))
        {
            uiPortalOverlay.overlayWaitingForTurn();
        }
        else if (msg.equals("turn"))
        {
            uiPortalOverlay.overlayAction();
        }
        else if (msg.equals("over"))
        {
            uiPortalOverlay.overlayWaitingForTurn();
        }
        else if (msg.startsWith("create_room"))
        {
            // Expect the RoomId of the room to create,
            // and a list of wall descriptors
            String[] splitMsg = msg.split(":");

            int arRoomId = Integer.parseInt(splitMsg[1]);

            String[] wallDescriptors = new String[4];

            for (int i = 2; i < wallDescriptors.length + 2 && i < splitMsg.length; ++i)
            {
                wallDescriptors[i - 2] = splitMsg[i];
            }

            renderer.createRoom(arRoomId, wallDescriptors);
        }
        else if (msg.startsWith("update_room_walls"))
        {
            // Expect the RoomID of the room to update,
            // and a list of wall descriptors
            String[] splitMsg = msg.split(":");

            int arRoomId = Integer.parseInt(splitMsg[1]);

            String[] wallDescriptors = new String[4];

            for (int i = 2; i < wallDescriptors.length + 2 && i < splitMsg.length; ++i)
            {
                wallDescriptors[i - 2] = splitMsg[i];
            }

            renderer.createRoom(arRoomId, wallDescriptors);
        }
        else if (msg.startsWith("update_room_alignment"))
        {
            // Expect the RoomId of the room to update,
            // and the new alignment of the room
            String[] splitMsg = msg.split(":");

            int   arRoomId      = Integer.parseInt(splitMsg[1]);
            short roomAlignment = Short.parseShort(splitMsg[2]);

            renderer.updateRoomAlignment(arRoomId, roomAlignment);
        }
        else if (msg.startsWith("update_room_residents"))
        {
            // Expect the RoomID of the room to update,
            // and list of pairs(bool, string) of residents
            String[] splitMsg = msg.split(":");

            int arRoomId = Integer.parseInt(splitMsg[1]);

            ConcurrentHashMap<Integer, Pair<Boolean, String>> residents = new ConcurrentHashMap<>();

            for (int i = 2; i < splitMsg.length; ++i)
            {
                String[] splitResident = splitMsg[i].split(":");
                String[] splitPair     = splitResident[1].split(",");

                Pair<Boolean, String> residentPair =
                    new Pair<>(Boolean.parseBoolean(splitPair[0]), splitPair[1]);

                residents.put(Integer.parseInt(splitResident[0]), residentPair);
            }

            renderer.updateRoomResidents(arRoomId, residents);
        }
        else if (msg.startsWith("show_action"))
        {
            // Expect arguments for show action function
            String[] splitMsg = msg.split(":");

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
