package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIPortalActivity;
import com.semaphore_soft.apps.cypher.ui.UIPortalOverlay;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author scorple
 */

public class PortalClientActivity extends ARActivity implements UIListener,
                                                                ResponseReceiver.Receiver,
                                                                GameController
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

    private static int playerMarker;
    private static int playerRoomMarker;

    private static ArrayList<Integer> reservedMarkers;
    private static ArrayList<Integer> playerMarkers;
    private static ArrayList<Integer> roomMarkers;
    private static ArrayList<Integer> placedRoomMarkers;

    private static HashMap<Integer, String>                                 nonPlayerTargets;
    private static HashMap<Integer, String>                                 playerTargets;
    private static HashMap<Integer, Pair<String, Special.E_TARGETING_TYPE>> specials;

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
        PortalRenderer.setGameController(this);

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        playerId = getIntent().getExtras().getInt("player");
        characterName = getIntent().getStringExtra("character");

        PortalRenderer.setHandler(handler);

        turn = false;

        reservedMarkers = new ArrayList<>();
        playerMarkers = new ArrayList<>();
        roomMarkers = new ArrayList<>();
        placedRoomMarkers = new ArrayList<>();

        nonPlayerTargets = new HashMap<>();
        playerTargets = new HashMap<>();
        specials = new HashMap<>();
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

    /**
     * {@inheritDoc}
     *
     * @return
     */
    //pass our rendering program to the ar framework
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
    //pass the the frame to draw the camera feed and
    //the ar graphics within to the ar framework
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
        if (cmd.startsWith("cmd_btn"))
        {
            switch (cmd)
            {
                case "cmd_btnPlayerMarkerSelect":
                    int firstUnreservedMarker = getFirstUnreservedMarker();

                    if (firstUnreservedMarker > -1)
                    {
                        clientService.write(
                            NetworkConstants.PREFIX_MARK_REQUEST + firstUnreservedMarker);
                    }
                    break;
                case "cmd_btnEndTurn":
                    moveActor();
                    break;
                case "cmd_btnGenerateRoom":
                    generateRoom();
                    break;
                case "cmd_btnOpenDoor":
                    openDoor();
                    break;
                case "cmd_btnAttack":
                    ArrayList<Pair<String, String>> attackOptions = new ArrayList<>();

                    for (int i : nonPlayerTargets.keySet())
                    {
                        String               targetName = nonPlayerTargets.get(i);
                        Pair<String, String> targetPair = new Pair<>(targetName, "cmd_attack:" + i);
                        attackOptions.add(targetPair);
                    }

                    uiPortalOverlay.overlaySelect(attackOptions);
                    break;
                case "cmd_btnDefend":
                    clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + "cmd_defend");
                    break;
                case "cmd_btnSpecial":
                    ArrayList<Pair<String, String>> specialOptions = new ArrayList<>();

                    for (int i : specials.keySet())
                    {
                        Pair<String, Special.E_TARGETING_TYPE> specialDescriptionPair =
                            specials.get(i);
                        String specialName =
                            specialDescriptionPair.first;
                        Pair<String, String> specialOptionPair =
                            new Pair<>(specialName, "cmd_special:" + i);
                        specialOptions.add(specialOptionPair);
                    }

                    uiPortalOverlay.overlaySelect(specialOptions);
                    break;
                case "cmd_btnCancel":
                    uiPortalOverlay.overlayAction();
                    break;
            }
        }
        else
        {
            String[] splitCmd    = cmd.split("_");
            String[] splitAction = splitCmd[1].split(":");

            if (splitAction[0].equals("special"))
            {
                int specialId = Integer.parseInt(splitAction[1]);

                Special.E_TARGETING_TYPE specialType = specials.get(specialId).second;

                if (splitAction.length < 3)
                {
                    if (specialType != Special.E_TARGETING_TYPE.AOE_PLAYER && specialType !=
                                                                              Special.E_TARGETING_TYPE.AOE_NON_PLAYER)
                    {
                        ArrayList<Pair<String, String>> targetOptions = new ArrayList<>();

                        if (specialType == Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER)
                        {
                            for (int i : nonPlayerTargets.keySet())
                            {
                                Pair<String, String> targetPair =
                                    new Pair<>(nonPlayerTargets.get(i),
                                               "cmd_special:" + specialId + ":" + i);

                                targetOptions.add(targetPair);
                            }
                        }
                        else
                        {
                            for (int i : playerTargets.keySet())
                            {
                                Pair<String, String> targetPair =
                                    new Pair<>(playerTargets.get(i),
                                               "cmd_special:" + specialId + ":" + i);

                                targetOptions.add(targetPair);
                            }
                        }

                        uiPortalOverlay.overlaySelect(targetOptions);
                    }
                    else
                    {
                        clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
                    }
                }
                else
                {
                    clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
                }
            }
            else
            {
                clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg    Message read from network
     * @param unused This is always 0
     */
    @Override
    public void handleRead(String msg, int unused)
    {
        if (msg.startsWith(NetworkConstants.PREFIX_ASSIGN_MARK))
        {
            // Expect the MarkerID to be assigned
            String[] splitMsg = msg.split(":");

            playerMarker = Integer.parseInt(splitMsg[1]);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_ASSIGN_ROOM_MARK))
        {
            String[] splitMsg = msg.split(":");

            playerRoomMarker = Integer.parseInt(splitMsg[1]);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_RESERVE_PLAYER))
        {
            // Expect the MarkerID to be reserved
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);

            playerMarkers.add(mark);
            reservedMarkers.add(mark);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_PLACE_ROOM))
        {
            // Expect the MarkerID to be reserved
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);

            placedRoomMarkers.add(mark);
            reservedMarkers.add(mark);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_RESERVE_ROOM_MARKER))
        {
            String[] splitMsg = msg.split(":");

            int mark = Integer.parseInt(splitMsg[1]);

            roomMarkers.add(mark);
            reservedMarkers.add(mark);
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

            renderer.setPlayerMarker(Integer.parseInt(splitMsg[1]), Integer.parseInt(splitMsg[2]));
        }
        else if (msg.equals(NetworkConstants.GAME_START))
        {
            uiPortalOverlay.overlayWaitingForTurn();
        }
        else if (msg.equals(NetworkConstants.GAME_TURN))
        {
            uiPortalOverlay.overlayAction();
        }
        else if (msg.equals(NetworkConstants.GAME_TURN_OVER))
        {
            uiPortalOverlay.overlayWaitingForTurn();
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_CREATE_ROOM))
        {
            // Expect the MarkerID of the room to create,
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
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_ROOM_WALLS))
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

            renderer.updateRoomWalls(arRoomId, wallDescriptors);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_ROOM_ALIGNMENT))
        {
            // Expect the RoomId of the room to update,
            // and the new alignment of the room
            String[] splitMsg = msg.split(":");

            int   arRoomId      = Integer.parseInt(splitMsg[1]);
            short roomAlignment = Short.parseShort(splitMsg[2]);

            renderer.updateRoomAlignment(arRoomId, roomAlignment);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_ROOM_RESIDENTS))
        {
            // Expect the RoomID of the room to update,
            // and list of pairs(bool, string) of residents
            // Formatted as: ~id;flag,pose~id;flag,pose
            // Use a different delimiter, because of how residents are stored
            String[] splitMsg = msg.split("~");

            int arRoomId = Integer.parseInt(splitMsg[1]);

            ConcurrentHashMap<Integer, Pair<Boolean, String>> residents = new ConcurrentHashMap<>();

            for (int i = 2; i < splitMsg.length; ++i)
            {
                String[] splitResident = splitMsg[i].split(";");
                String[] splitPair     = splitResident[1].split(",");

                Pair<Boolean, String> residentPair =
                    new Pair<>(Boolean.parseBoolean(splitPair[0]), splitPair[1]);

                residents.put(Integer.parseInt(splitResident[0]), residentPair);
            }

            renderer.updateRoomResidents(arRoomId, residents);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_SHOW_ACTION))
        {
            // Expect arguments for show action function
            String[] splitMsg = msg.split(":");

            renderer.showAction(Integer.parseInt(splitMsg[1]),
                                Integer.parseInt(splitMsg[2]),
                                Integer.parseInt(splitMsg[3]),
                                Long.parseLong(splitMsg[4]),
                                splitMsg[5],
                                splitMsg[6].equals("") ? null : splitMsg[6],
                                Boolean.parseBoolean(splitMsg[7]),
                                Boolean.parseBoolean(splitMsg[8]));
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_NON_PLAYER_TARGETS))
        {
            nonPlayerTargets.clear();

            String[] splitMsg = msg.split(":");

            if (splitMsg.length > 1)
            {
                String[] nonPlayerTargetPairs = splitMsg[1].split(",");

                for (String nonPlayerTargetPair : nonPlayerTargetPairs)
                {
                    String[] splitNonPlayerTargetPair = nonPlayerTargetPair.split("\\.");

                    nonPlayerTargets.put(Integer.parseInt(splitNonPlayerTargetPair[0]),
                                         splitNonPlayerTargetPair[1]);
                }
            }
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_PLAYER_TARGETS))
        {
            playerTargets.clear();

            String[] splitMsg = msg.split(":");

            if (splitMsg.length > 1)
            {
                String[] playerTargetPairs = splitMsg[1].split(",");

                for (String playerTargetPair : playerTargetPairs)
                {
                    String[] splitPlayerTargetPair = playerTargetPair.split("\\.");

                    playerTargets.put(Integer.parseInt(splitPlayerTargetPair[0]),
                                      splitPlayerTargetPair[1]);
                }
            }
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_PLAYER_SPECIALS))
        {
            specials.clear();

            String[] splitMsg = msg.split(":");

            if (splitMsg.length > 1)
            {
                String[] specialTriads = splitMsg[1].split(",");

                for (String specialTriad : specialTriads)
                {
                    String[] splitSpecialTriad = specialTriad.split("\\.");

                    Pair<String, Special.E_TARGETING_TYPE> specialNameTargetingPair =
                        new Pair<>(splitSpecialTriad[1],
                                   Special.E_TARGETING_TYPE.valueOf(splitSpecialTriad[2]));

                    specials.put(Integer.parseInt(splitSpecialTriad[0]), specialNameTargetingPair);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param msg    Status update
     * @param unused This is always 0
     */
    @Override
    public void handleStatus(String msg, int unused)
    {
        Toast.makeText(this, "Status: " + msg + " from <" + unused + ">", Toast.LENGTH_SHORT)
             .show();
    }

    /**
     * {@inheritDoc}
     *
     * @param msg    Error message
     * @param unused This is always 0
     */
    @Override
    public void handleError(String msg, int unused)
    {
        Toast.makeText(this, "Error: " + msg + " from <" + unused + ">", Toast.LENGTH_SHORT)
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

    /**
     * Get the reference ID of the nearest visible AR marker to a given AR
     * marker via the {@link PortalRenderer} which is not associated with an
     * {@link Actor}, or {@code -1} if either the given marker is not visible
     * or there are no other markers in view.
     *
     * @param mark0 int: The reference ID of the desired AR marker to get the
     *              nearest marker to.
     *
     * @return int: The reference ID of the nearest visible AR marker to a
     * given AR marker via the {@link PortalRenderer} which is not associated
     * with an {@link Actor}, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @see PortalRenderer
     * @see PortalRenderer#getNearestMarkerExcluding(int, ArrayList)
     * @see Actor
     * @see Actor#getMarker()
     */
    private int getNearestNonPlayerMarker(final int mark0)
    {
        int foundMarker = renderer.getNearestMarkerExcluding(mark0, playerMarkers);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    /**
     * Get the reference ID of the nearest visible AR marker to a given AR
     * marker via the {@link PortalRenderer} which is not associated with an
     * {@link Actor} and is not in the list of AR marker reference IDs
     * indicated as excluded, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @param mark0  int: The reference ID of the desired AR marker to get the
     *               nearest marker to.
     * @param marksX ArrayList: A list of marker reference IDs to exclude when
     *               searching for the nearest marker to a given marker.
     *
     * @return int: the reference ID of the nearest visible AR marker to a
     * given AR marker via the {@link PortalRenderer} which is not associated
     * with an {@link Actor} and is not in the list of AR marker reference IDs
     * indicated as excluded, or {@code -1} if either the given marker is not
     * visible or there are no other markers in view.
     *
     * @see PortalRenderer
     * @see PortalRenderer#getNearestMarkerExcluding(int, ArrayList)
     * @see Actor
     * @see Actor#getMarker()
     */
    private int getNearestNonPlayerMarkerExcluding(final int mark0, final ArrayList<Integer> marksX)
    {
        for (int i : playerMarkers)
        {
            marksX.add(i);
        }

        int foundMarker = renderer.getNearestMarkerExcluding(mark0, marksX);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private void moveActor()
    {
        int nearestMarkerId =
            getNearestNonPlayerMarker(playerMarker);

        if (nearestMarkerId > -1)
        {
            clientService.write(NetworkConstants.PREFIX_MOVE_REQUEST + nearestMarkerId);
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Couldn't Find Valid Room",
                           Toast.LENGTH_SHORT)
                 .show();
        }
    }

    private void generateRoom()
    {
        int firstUnreservedMarker = getFirstUnreservedMarker();

        if (firstUnreservedMarker > -1)
        {
            clientService.write(
                NetworkConstants.PREFIX_GENERATE_ROOM_REQUEST + firstUnreservedMarker);
        }
    }

    private static short getWallFromAngle(final float angle)
    {
        if (angle > 315 || angle <= 45)
        {
            return Room.WALL_TOP;
        }
        else if (angle > 45 && angle <= 135)
        {
            return Room.WALL_RIGHT;
        }
        else if (angle > 135 && angle <= 225)
        {
            return Room.WALL_BOTTOM;
        }
        else
        {
            return Room.WALL_LEFT;
        }
    }

    private void openDoor()
    {
        int nearestMarkerID =
            getNearestNonPlayerMarkerExcluding(playerRoomMarker,
                                               placedRoomMarkers);

        if (nearestMarkerID > -1)
        {
            float angle0 =
                renderer.getAngleBetweenMarkers(playerRoomMarker,
                                                nearestMarkerID);
            float angle1 = renderer.getAngleBetweenMarkers(nearestMarkerID,
                                                           playerRoomMarker);

            short sideOfStartRoom = getWallFromAngle(angle0);
            short sideOfEndRoom   = getWallFromAngle(angle1);

            clientService.write(NetworkConstants.PREFIX_OPEN_DOOR_REQUEST + nearestMarkerID + ":" +
                                sideOfStartRoom + ":" + sideOfEndRoom);
        }
        else
        {
            Toast.makeText(getApplicationContext(),
                           "Couldn't Find Valid Marker",
                           Toast.LENGTH_SHORT)
                 .show();
        }
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

    @Override
    public void onFinishedLoading()
    {
        Runnable uiUpdate = new Runnable()
        {
            @Override
            public void run()
            {
                uiPortalOverlay.overlayPlayerMarkerSelect();
            }
        };

        runOnUiThread(uiUpdate);
    }

    @Override
    public void onFinishedAction(int actorId)
    {

    }

    @Override
    public void feedback(String message)
    {
        // DO NOT USE
    }

    @Override
    public void onActorAction(int sourceId, int targetId, String action)
    {
        // DO NOT USE
    }

    @Override
    public void onActorMove(int actorId, int roomId)
    {
        // DO NOT USE
    }

    @Override
    public void turnPassed(int turnId)
    {
        // DO NOT USE
    }
}
