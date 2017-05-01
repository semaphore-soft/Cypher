package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.Pair;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.GameController;
import com.semaphore_soft.apps.cypher.game.ItemConsumable;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.networking.Client;
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
                                                                GameController,
                                                                PortalRenderer.NewMarkerListener
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

    private static HashMap<Integer, String>                                        nonPlayerTargets;
    private static HashMap<Integer, String>                                        playerTargets;
    private static HashMap<Integer, Pair<String, Special.E_TARGETING_TYPE>>        specials;
    private static HashMap<Integer, Pair<String, ItemConsumable.E_TARGETING_TYPE>> items;

    private static int healthMax;
    private static int healthCurrent;
    private static int energyMax;
    private static int energyCurrent;

    private static MediaPlayer mediaPlayer;

    /**
     * {@inheritDoc}
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /*mediaPlayer = MediaPlayer.create(this, R.raw.overworld);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();*/

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
        PortalRenderer.setNewMarkerListener(this);

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
        items = new HashMap<>();

        uiPortalOverlay.setCharPortrait(characterName);
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
        // Make sure we are able to reconnect if we were in the background
        Client.setReconnect(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop()
    {
        super.onStop();
        // Don't reconnect while we are in the background
        Client.setReconnect(false);
        if (mClientBound)
        {
            unbindService(mClientConnection);
            mClientBound = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed()
    {
        //do nothing
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

                    uiPortalOverlay.overlaySelect(attackOptions, false, false);
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

                    uiPortalOverlay.overlaySelect(specialOptions, false, true);
                    break;
                case "cmd_btnCancel":
                    uiPortalOverlay.overlayAction(healthMax,
                                                  healthCurrent,
                                                  energyMax,
                                                  energyCurrent);
                    break;
                case "cmd_btnItems":
                    ArrayList<Pair<String, String>> options = new ArrayList<>();

                    Pair<String, String> inventory = new Pair<>("Inventory", "cmd_btnInventory");
                    options.add(inventory);

                    Pair<String, String> floor = new Pair<>("Floor", "cmd_btnFloor");
                    options.add(floor);

                    uiPortalOverlay.overlaySelect(options, true, true);
                    break;
                case "cmd_btnInventory":
                    clientService.write(NetworkConstants.GAME_INVENTORY_REQUEST);

                    ArrayList<Pair<String, String>> itemOptions = new ArrayList<>();

                    for (int i : items.keySet())
                    {
                        Pair<String, ItemConsumable.E_TARGETING_TYPE> itemDescriptionPair =
                            items.get(i);
                        String itemName =
                            itemDescriptionPair.first;
                        Pair<String, String> itemOptionPair =
                            new Pair<>(itemName,
                                       ((itemDescriptionPair.second !=
                                         null) ? "cmd_invConItem:" : "cmd_invDurItem:") + i);
                        itemOptions.add(itemOptionPair);
                    }

                    uiPortalOverlay.overlaySelect(itemOptions, false, true);
                    break;
                case "cmd_btnFloor":
                    clientService.write(NetworkConstants.GAME_FLOOR_REQUEST);
                    break;
            }
        }
        else
        {
            String[] splitCmd    = cmd.split("_");
            String[] splitAction = splitCmd[1].split(":");

            if (splitAction[0].equals("invConItem"))
            {
                ArrayList<Pair<String, String>> options = new ArrayList<>();

                Pair<String, String> useOption =
                    new Pair<>("Use", "cmd_useItem:" + splitAction[1]);
                options.add(useOption);

                Pair<String, String> dropOption =
                    new Pair<>("Drop", "cmd_dropItem:" + splitAction[1]);
                options.add(dropOption);

                uiPortalOverlay.overlaySelect(options, true, true);
            }
            else if (splitAction[0].equals("invDurItem"))
            {
                ArrayList<Pair<String, String>> options = new ArrayList<>();

                Pair<String, String> dropOption =
                    new Pair<>("Drop", "cmd_dropItem:" + splitAction[1]);
                options.add(dropOption);

                uiPortalOverlay.overlaySelect(options, true, true);
            }
            else if (splitAction[0].equals("floorItem"))
            {
                ArrayList<Pair<String, String>> options = new ArrayList<>();

                Pair<String, String> takeOption =
                    new Pair<>("Take", "cmd_takeItem:" + splitAction[1]);
                options.add(takeOption);

                uiPortalOverlay.overlaySelect(options, true, true);
            }
            else if (splitAction[0].equals("useItem"))
            {
                int itemId = Integer.parseInt(splitAction[1]);

                ItemConsumable.E_TARGETING_TYPE itemType = items.get(itemId).second;

                if (splitAction.length < 3)
                {
                    if (itemType != ItemConsumable.E_TARGETING_TYPE.AOE_PLAYER && itemType !=
                                                                                  ItemConsumable.E_TARGETING_TYPE.AOE_NON_PLAYER)
                    {
                        ArrayList<Pair<String, String>> targetOptions = new ArrayList<>();

                        if (itemType == ItemConsumable.E_TARGETING_TYPE.SINGLE_NON_PLAYER)
                        {
                            for (int i : nonPlayerTargets.keySet())
                            {
                                Pair<String, String> targetPair =
                                    new Pair<>(nonPlayerTargets.get(i),
                                               "cmd_useItem:" + itemId + ":" + i);

                                targetOptions.add(targetPair);
                            }
                        }
                        else
                        {
                            for (int i : playerTargets.keySet())
                            {
                                Pair<String, String> targetPair =
                                    new Pair<>(playerTargets.get(i),
                                               "cmd_useItem:" + itemId + ":" + i);

                                targetOptions.add(targetPair);
                            }
                        }

                        uiPortalOverlay.overlaySelect(targetOptions, false, true);
                    }
                    else
                    {
                        clientService.write(NetworkConstants.PREFIX_USE_ITEM + splitAction[1]);
                        renderer.setCheckingNearestRoomMarker(false);
                    }
                }
                else
                {
                    clientService.write(
                        NetworkConstants.PREFIX_USE_ITEM + splitAction[1] + ":" + splitAction[2]);
                    renderer.setCheckingNearestRoomMarker(false);
                }

                clientService.write(NetworkConstants.PREFIX_USE_ITEM + splitAction[1]);
            }
            else if (splitAction[0].equals("dropItem"))
            {
                clientService.write(NetworkConstants.PREFIX_DROP_ITEM + splitAction[1]);
            }
            else if (splitAction[0].equals("takeItem"))
            {
                clientService.write(NetworkConstants.PREFIX_TAKE_ITEM + splitAction[1]);
            }
            else if (splitAction[0].equals("special"))
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

                        uiPortalOverlay.overlaySelect(targetOptions, false, true);
                    }
                    else
                    {
                        clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
                        //renderer.setCheckingNearestRoomMarker(false);
                    }
                }
                else
                {
                    clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
                    //renderer.setCheckingNearestRoomMarker(false);
                }
            }
            else
            {
                clientService.write(NetworkConstants.PREFIX_ACTION_REQUEST + cmd);
                renderer.setCheckingNearestRoomMarker(false);
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
            renderer.setPlayerMarker(playerMarker);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_ASSIGN_ROOM_MARK))
        {
            String[] splitMsg = msg.split(":");

            playerRoomMarker = Integer.parseInt(splitMsg[1]);
            renderer.setPlayerRoomMarker(playerRoomMarker);
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
        else if (msg.startsWith(NetworkConstants.PREFIX_START))
        {
            String[] splitMsg = msg.split(":");

            healthMax = Integer.parseInt(splitMsg[1]);
            healthCurrent = Integer.parseInt(splitMsg[2]);
            energyMax = Integer.parseInt(splitMsg[3]);
            energyCurrent = Integer.parseInt(splitMsg[4]);

            uiPortalOverlay.overlayWaitingForTurn(healthMax,
                                                  healthCurrent,
                                                  energyMax,
                                                  energyCurrent);

            PortalRenderer.setLookingForNewMarkers(true);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_TURN))
        {
            String[] splitMsg = msg.split(":");

            healthMax = Integer.parseInt(splitMsg[1]);
            healthCurrent = Integer.parseInt(splitMsg[2]);
            energyMax = Integer.parseInt(splitMsg[3]);
            energyCurrent = Integer.parseInt(splitMsg[4]);

            renderer.setCheckingNearestRoomMarker(true);

            uiPortalOverlay.overlayAction(healthMax,
                                          healthCurrent,
                                          energyMax,
                                          energyCurrent);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_TURN_OVER))
        {
            String[] splitMsg = msg.split(":");

            healthMax = Integer.parseInt(splitMsg[1]);
            healthCurrent = Integer.parseInt(splitMsg[2]);
            energyMax = Integer.parseInt(splitMsg[3]);
            energyCurrent = Integer.parseInt(splitMsg[4]);

            uiPortalOverlay.overlayWaitingForTurn(healthMax,
                                                  healthCurrent,
                                                  energyMax,
                                                  energyCurrent);

            renderer.setCheckingNearestRoomMarker(false);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_HEALTH))
        {
            String[] splitMsg = msg.split(":");

            healthCurrent = Integer.parseInt(splitMsg[1]);

            uiPortalOverlay.setHealth(healthCurrent);
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
                                Boolean.parseBoolean(splitMsg[8]),
                                splitMsg[9]);
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
        else if (msg.startsWith(NetworkConstants.PREFIX_UPDATE_PLAYER_ITEMS))
        {
            items.clear();

            String[] splitMsg = msg.split(":");

            if (splitMsg.length > 1)
            {
                String[] itemTriads = splitMsg[1].split(",");

                for (String itemTriad : itemTriads)
                {
                    String[] splitItemTriad = itemTriad.split("\\.");

                    Pair<String, ItemConsumable.E_TARGETING_TYPE> itemNameTargetingPair =
                        new Pair<>(splitItemTriad[1],
                                   (!splitItemTriad[2].equals("DUR") ?
                                    ItemConsumable.E_TARGETING_TYPE.valueOf(splitItemTriad[2]) : null));

                    items.put(Integer.parseInt(splitItemTriad[0]), itemNameTargetingPair);
                }
            }
        }
        else if (msg.equals(NetworkConstants.GAME_WIN_CONDITION))
        {
            uiPortalOverlay.overlayWinCondition();
        }
        else if (msg.equals(NetworkConstants.GAME_LOSE_CONDITION))
        {
            uiPortalOverlay.overlayLoseCondition();
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_INVENTORY_LIST))
        {
            String[] splitMsg = msg.split(":");

            ArrayList<Pair<String, String>> options = new ArrayList<>();

            for (int i = 1; i < splitMsg.length; ++i)
            {
                String[] splitItem = splitMsg[i].split(",");

                if (splitItem[1].equals("consumable"))
                {
                    options.add(new Pair<>(splitItem[0], "cmd_invConItem:" + splitItem[2]));
                }
                else if (splitItem[1].equals("durable"))
                {
                    options.add(new Pair<>(splitItem[0], "cmd_invDurItem:" + splitItem[2]));
                }
            }

            uiPortalOverlay.overlaySelect(options, true, true);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_FLOOR_LIST))
        {
            String[] splitMsg = msg.split(":");

            ArrayList<Pair<String, String>> options = new ArrayList<>();

            for (int i = 1; i < splitMsg.length; ++i)
            {
                String[] splitItem = splitMsg[i].split(",");

                options.add(new Pair<>(splitItem[0], "cmd_floorItem:" + splitItem[1]));
            }

            uiPortalOverlay.overlaySelect(options, true, true);
        }
        else if (msg.startsWith(NetworkConstants.PREFIX_FEEDBACK))
        {
            String[] splitMsg = msg.split("~");

            Toast.makeText(this, splitMsg[1], Toast.LENGTH_SHORT).show();
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
    public final void feedback(String message)
    {
        // DO NOT USE
    }

    @Override
    public final void onActorAction(int sourceId, int targetId, String action, String desc)
    {
        // DO NOT USE
    }

    @Override
    public final void onActorMove(int actorId, int roomId)
    {
        // DO NOT USE
    }

    @Override
    public final void turnPassed(int turnId)
    {
        // DO NOT USE
    }

    @Override
    public void newMarker(int marker)
    {
        clientService.write(NetworkConstants.PREFIX_GENERATE_ROOM_REQUEST + marker);
    }

    /**
     * Simulate a player {@link Actor actor} moving to a new {@link Room}
     *
     * @param marker   New {@link Room} the {@link Actor} is moving too.
     * @param updateId Not used by this function
     */
    @Override
    public void newNearestRoomMarker(int marker, int updateId)
    {
        clientService.write(NetworkConstants.PREFIX_UPDATE_NEAREST_ROOM + marker);
    }
}
