package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.Entity;
import com.semaphore_soft.apps.cypher.game.Item;
import com.semaphore_soft.apps.cypher.game.Map;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.Hashtable;

import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_OPEN;
import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_UNLOCKED;
import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity implements PortalRenderer.NewMarkerListener,
                                                          ResponseReceiver.Receiver
{
    //TODO this class is too large, some methods should be exported

    public static final short OVERLAY_PLAYER_MARKER_SELECT = 0;
    public static final short OVERLAY_START_MARKER_SELECT  = 1;
    public static final short OVERLAY_ACTION               = 2;
    public static final short OVERLAY_OPEN_DOOR            = 3;
    public static final short OVERLAY_WAITING_FOR_HOST     = 4;
    public static final short OVERLAY_WAITING_FOR_PLAYERS  = 5;

    boolean host;

    Long   playerID;
    String characterName;

    PortalRenderer renderer;
    int            overlayID;
    FrameLayout    overlay_layout;

    private Hashtable<Long, Room>    rooms;
    private Hashtable<Long, Actor>   actors;
    private Hashtable<Long, Special> specials;
    private Hashtable<Long, Entity>  entities;
    private Hashtable<Long, Item>    items;
    private Map                      map;
    private ResponseReceiver         responseReceiver;

    //GameMaster gameMaster;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's actor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        overlay_layout = (FrameLayout) findViewById(R.id.overlay_frame);

        host = getIntent().getBooleanExtra("host", false);

        playerID = getIntent().getLongExtra("player", 0);
        characterName = getIntent().getExtras().getString("character", "knight");

        renderer = new PortalRenderer();
        renderer.setContext(this);

        actors = new Hashtable<>();
        rooms = new Hashtable<>();
        actors = new Hashtable<>();
        specials = new Hashtable<>();
        entities = new Hashtable<>();
        items = new Hashtable<>();

        map = new Map();

        responseReceiver = new ResponseReceiver();
        responseReceiver.setListener(this);
        LocalBroadcastManager.getInstance(this)
                             .registerReceiver(responseReceiver, NetworkConstants.getFilter());

        setOverlay(OVERLAY_PLAYER_MARKER_SELECT);

        //gameMaster = new GameMaster();
        //gameMaster.start();
    }

    private void setOverlay(int id)
    {
        overlay_layout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        View overlay;

        switch (id)
        {
            case OVERLAY_PLAYER_MARKER_SELECT:
            {
                overlay = inflater.inflate(R.layout.overlay_player_marker_select,
                                           (ViewGroup) findViewById(R.id.portal_base),
                                           false);
                overlay_layout.addView(overlay);
                Button btnSelect = (Button) findViewById(R.id.btnSelect);
                btnSelect.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int mark = getFirstUnreservedMarker();
                        if (mark > -1)
                        {
                            Actor actor = new Actor(playerID, characterName, mark);
                            GameStatLoader.loadActorStats(actor,
                                                          characterName,
                                                          specials,
                                                          getApplicationContext());
                            actors.put(playerID, actor);
                            renderer.setPlayerMarker(playerID, mark);

                            Item item = GameStatLoader.loadItemStats("delightful_bread",
                                                                     items,
                                                                     specials,
                                                                     getApplicationContext());

                            Toast.makeText(getApplicationContext(),
                                           "Player Marker Set",
                                           Toast.LENGTH_SHORT)
                                 .show();

                            if (host)
                            {
                                setOverlay(OVERLAY_START_MARKER_SELECT);
                            }
                            else
                            {
                                setOverlay(OVERLAY_ACTION);
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Failed to Find Marker",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });
                break;
            }
            //TODO waiting for players/host case(s)
            case OVERLAY_START_MARKER_SELECT:
            {
                overlay = inflater.inflate(R.layout.overlay_start_marker_select,
                                           (ViewGroup) findViewById(R.id.portal_base), false);
                overlay_layout.addView(overlay);
                Button btnSelect = (Button) findViewById(R.id.btnSelect);
                btnSelect.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int mark = getFirstUnreservedMarker();
                        if (mark > -1)
                        {
                            //generate a new, placed room
                            long roomID = getNextID(rooms);
                            Room room   = new Room(roomID, mark, true);
                            rooms.put(roomID, room);
                            renderer.createRoom(room);

                            //place every player actor in that room
                            for (Actor actor : actors.values())
                            {
                                if (actor.isPlayer())
                                {
                                    actor.setRoom(roomID);
                                    room.addActor(actor.getId());
                                }
                            }

                            renderer.updateRoomResidents(room, actors);

                            map.init(roomID);

                            Toast.makeText(getApplicationContext(),
                                           "Starting Room Established",
                                           Toast.LENGTH_SHORT)
                                 .show();

                            setOverlay(OVERLAY_ACTION);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Failed to Find Valid Marker",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });
                break;
            }
            case OVERLAY_ACTION:
            {
                overlay = inflater.inflate(R.layout.overlay_action,
                                           (ViewGroup) findViewById(R.id.portal_base),
                                           false);
                overlay_layout.addView(overlay);
                Button btnEndTurn = (Button) findViewById(R.id.btnEndTurn);
                btnEndTurn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int nearestMarkerID =
                            getNearestNonPlayerMarker(actors.get(playerID).getMarker());
                        if (nearestMarkerID > -1)
                        {
                            boolean foundRoom     = false;
                            long    nearestRoomID = -1;
                            //check to see if there is already a room associated with the nearest marker
                            for (Long roomID : rooms.keySet())
                            {
                                if (rooms.get(roomID).getMarker() == nearestMarkerID)
                                {
                                    //if there is, use its id
                                    foundRoom = true;
                                    nearestRoomID = roomID;
                                    break;
                                }
                            }
                            if (foundRoom)
                            {
                                if (nearestRoomID == actors.get(playerID).getRoom())
                                {
                                    //player room hasn't changed, do nothing
                                    Toast.makeText(getApplicationContext(),
                                                   "Player Remains in Room",
                                                   Toast.LENGTH_SHORT)
                                         .show();
                                }
                                else
                                {
                                    if (!rooms.get(nearestRoomID).getPlaced())
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                       "Cannot move to room, room has not been placed",
                                                       Toast.LENGTH_SHORT)
                                             .show();
                                    }
                                    //check for valid move
                                    else if (getValidPath(actors.get(playerID).getRoom(),
                                                          nearestRoomID))
                                    {
                                        //if the actor was previously in a room, remove it from that room
                                        if (actors.get(playerID).getRoom() > -1)
                                        {
                                            rooms.get(actors.get(playerID).getRoom())
                                                 .removeActor(playerID);
                                            renderer.updateRoomResidents(rooms.get(actors.get(
                                                playerID).getRoom()), actors);
                                        }

                                        //update the actor's room and the new room's actor list
                                        actors.get(playerID).setRoom(nearestRoomID);
                                        rooms.get(nearestRoomID).addActor(playerID);

                                        renderer.updateRoomResidents(rooms.get(nearestRoomID),
                                                                     actors);

                                        Toast.makeText(getApplicationContext(),
                                                       "Updated Player Room",
                                                       Toast.LENGTH_SHORT)
                                             .show();
                                    }
                                    else
                                    {
                                        Toast.makeText(getApplicationContext(),
                                                       "Bad Move\nNo Valid Path Between Rooms",
                                                       Toast.LENGTH_SHORT)
                                             .show();
                                    }
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                               "Error: Tried to Move to a Room Which does not Exist\nPlease Generate the Room First",
                                               Toast.LENGTH_LONG)
                                     .show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Couldn't Find Valid Room",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });
                Button btnGenerateRoom = (Button) findViewById(R.id.btnGenerateRoom);
                btnGenerateRoom.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int mark = getFirstUnreservedMarker();
                        if (mark > -1)
                        {
                            //generate a new, placed room
                            long roomID = getNextID(rooms);
                            Room room   = new Room(roomID, mark, false);
                            rooms.put(roomID, room);

                            //attach three random entities with type 0 or 1 to the new room
                            for (int i = 0; i < 3; ++i)
                            {
                                long entityID =
                                    getNextID(entities);
                                int newType = ((Math.random() > 0.3) ? 0 : 1);

                                Entity newEntity = new Entity(entityID, newType);
                                entities.put(entityID, newEntity);
                                room.addEntity(entityID);
                            }

                            Actor actor = new Actor(getNextID(actors), room.getId(), "lil_ghost");
                            GameStatLoader.loadActorStats(actor,
                                                          "lil_ghost",
                                                          specials,
                                                          getApplicationContext());
                            actors.put(actor.getId(), actor);
                            room.addActor(actor.getId());

                            System.out.println("put lil_ghost in room: " + roomID);

                            renderer.createRoom(room);

                            Toast.makeText(getApplicationContext(),
                                           "New Room Generated",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Couldn't Find Valid Marker",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });
                Button btnOpenDoor = (Button) findViewById(R.id.btnOpenDoor);
                btnOpenDoor.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(OVERLAY_OPEN_DOOR);
                    }
                });
                Button btnAttack = (Button) findViewById(R.id.btnAttack);
                btnAttack.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Actor            actor   = actors.get(playerID);
                        Room             room    = rooms.get(actor.getRoom());
                        ArrayList<Actor> targets = new ArrayList<>();

                        System.out.println("looking for targets in room: " + actor.getRoom());

                        for (Long targetId : room.getResidentActors())
                        {
                            Actor target = actors.get(targetId);
                            if (!target.isPlayer())
                            {
                                System.out.println("found valid target: " + target.getId());
                                targets.add(target);
                            }
                        }
                        if (targets.size() < 1)
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Nothing to attack here",
                                           Toast.LENGTH_SHORT).show();
                        }
                        else if (targets.size() == 1)
                        {
                            System.out.println("Attacking target: " + targets.get(0).getId());
                            System.out.println(
                                "Actor Attack Rating: " + actor.getRealAttackRating());
                            System.out.println(
                                "Target HP before attack: " + targets.get(0).getHealthCurrent());
                            actor.attack(targets.get(0));
                            System.out.println(
                                "Target HP after attack: " + targets.get(0).getHealthCurrent());
                        }
                        else
                        {
                            //TODO display target selection
                            Toast.makeText(getApplicationContext(),
                                           "Multiple possible targets, not yet implemented",
                                           Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                Button btnDefend = (Button) findViewById(R.id.btnDefend);
                btnDefend.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        actors.get(playerID).setState(Actor.E_STATE.DEFEND);
                        Toast.makeText(getApplicationContext(),
                                       "Happy defending bucko",
                                       Toast.LENGTH_SHORT).show();
                    }
                });
                Button btnSpecial = (Button) findViewById(R.id.btnSpecial);
                btnSpecial.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Actor                    actor         = actors.get(playerID);
                        Hashtable<Long, Special> actorSpecials = actor.getSpecials();


                        if (actorSpecials.size() < 1)
                        {
                            Toast.makeText(getApplicationContext(),
                                           "You have no specials",
                                           Toast.LENGTH_SHORT).show();
                        }
                        else if (actorSpecials.size() == 1)
                        {
                            Special special = actorSpecials.get(0);

                            performPlayerSpecial(actor, special);
                        }
                        else
                        {
                            //TODO display special select
                            Toast.makeText(getApplicationContext(),
                                           "Multiple possible specials, not yet implemented",
                                           Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            }
            case OVERLAY_OPEN_DOOR:
            {
                overlay = inflater.inflate(R.layout.overlay_open_door,
                                           (ViewGroup) findViewById(R.id.portal_base),
                                           false);
                overlay_layout.addView(overlay);
                Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
                btnConfirm.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Room room0 = rooms.get(actors.get(playerID).getRoom());
                        Room room1;

                        ArrayList<Integer> placedRoomMarkers = new ArrayList<>();
                        for (Room room : rooms.values())
                        {
                            if (room.getPlaced())
                            {
                                placedRoomMarkers.add(room.getMarker());
                            }
                        }

                        int nearestMarkerID = getNearestNonPlayerMarkerExcluding(room0.getMarker(),
                                                                                 placedRoomMarkers);
                        if (nearestMarkerID > -1)
                        {
                            boolean foundRoom     = false;
                            long    nearestRoomID = -1;
                            //check to see if there is already a room associated with the nearest marker
                            for (Long roomID : rooms.keySet())
                            {
                                if (rooms.get(roomID).getMarker() == nearestMarkerID)
                                {
                                    //if there is, use its id
                                    foundRoom = true;
                                    nearestRoomID = roomID;
                                    break;
                                }
                            }
                            if (foundRoom)
                            {
                                room1 = rooms.get(nearestRoomID);
                                if (getValidAdjacency(room0, room1))
                                {
                                    room1.setPlaced(true);

                                    short wall0 = getWall(room0, room1);
                                    short wall1 = getWall(room1, room0);

                                    room0.setWallType(wall0, DOOR_OPEN);
                                    room1.setWallType(wall1, DOOR_OPEN);

                                    map.insert(room0.getId(), wall0, room1.getId(), wall1);
                                    Pair<Integer, Integer> room1MapPos =
                                        map.getPosition(room1.getId());
                                    System.out.println(
                                        "New Room Map Position: " + room1MapPos.first + ", " +
                                        room1MapPos.second);
                                    map.print();

                                    renderer.updateRoomWalls(room0);
                                    renderer.updateRoomWalls(room1);

                                    Hashtable<Long, Pair<Short, Short>> adjacentRoomsAndWalls =
                                        map.getAdjacentRoomsAndWalls(nearestRoomID);
                                    for (Long id : adjacentRoomsAndWalls.keySet())
                                    {
                                        if (id != room0.getId())
                                        {
                                            room1.setWallType(adjacentRoomsAndWalls.get(id).first,
                                                              DOOR_OPEN);
                                            rooms.get(id)
                                                 .setWallType(adjacentRoomsAndWalls.get(id).second,
                                                              DOOR_OPEN);

                                            renderer.updateRoomWalls(room1);
                                            renderer.updateRoomWalls(rooms.get(id));
                                        }
                                    }

                                    Toast.makeText(getApplicationContext(),
                                                   "Door Opened",
                                                   Toast.LENGTH_SHORT)
                                         .show();

                                    setOverlay(OVERLAY_ACTION);
                                }
                                else
                                {
                                    Toast.makeText(getApplicationContext(),
                                                   "Connection is Not Valid",
                                                   Toast.LENGTH_SHORT)
                                         .show();
                                }
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                               "Error: Tried to Open Door to a Room Which does not Exist\nPlease Generate the Room First",
                                               Toast.LENGTH_SHORT)
                                     .show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Couldn't Find Valid Marker",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });
                Button btnCancel = (Button) findViewById(R.id.btnCancel);
                btnCancel.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(OVERLAY_ACTION);
                    }
                });
                break;
            }
            default:
                break;
        }
    }

    @Override
    protected ARRenderer supplyRenderer()
    {
        return renderer;
    }

    @Override
    protected FrameLayout supplyFrameLayout()
    {
        return (FrameLayout) this.findViewById(R.id.portal_frame);
    }

    private int getFirstUnreservedMarker()
    {
        ArrayList<Integer> marksX = new ArrayList<>();
        for (Actor actor : actors.values())
        {
            marksX.add(actor.getMarker());
        }
        for (Room room : rooms.values())
        {
            marksX.add(room.getMarker());
        }

        int foundMarker = renderer.getFirstMarkerExcluding(marksX);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private int getNearestNonPlayerMarker(int mark0)
    {
        ArrayList<Integer> actorMarkers = new ArrayList<>();
        for (Long id : actors.keySet())
        {
            actorMarkers.add(actors.get(id).getMarker());
        }

        int foundMarker = renderer.getNearestMarkerExcluding(mark0, actorMarkers);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    private int getNearestNonPlayerMarkerExcluding(int mark0, ArrayList<Integer> marksX)
    {
        for (Actor actor : actors.values())
        {
            marksX.add(actor.getMarker());
        }

        int foundMarker = renderer.getNearestMarkerExcluding(mark0, marksX);

        if (foundMarker > -1)
        {
            return foundMarker;
        }

        return -1;
    }

    short getWall(long room0, long room1)
    {
        return getWall(rooms.get(room0), rooms.get(room1));
    }

    short getWall(Room room0, Room room1)
    {
        int mark0 = room0.getMarker();
        int mark1 = room1.getMarker();

        float angle0 = renderer.getAngleBetweenMarkers(mark0, mark1);

        short wall0 = getWallFromAngle(angle0);

        return wall0;
    }

    public boolean getValidPath(long room0, long room1)
    {
        return getValidPath(rooms.get(room0), rooms.get(room1));
    }

    public boolean getValidPath(Room room0, Room room1)
    {
        Pair<Integer, Integer> room0Pos = map.getPosition(room0.getId());
        short                  room0Rot = map.getRoomRotation(room0Pos.first, room0Pos.second);
        Pair<Integer, Integer> room1Pos = map.getPosition(room1.getId());
        short                  room1Rot = map.getRoomRotation(room1Pos.first, room1Pos.second);

        if (map.checkAdjacent(room0.getId(), room1.getId()) < 0)
        {
            return false;
        }

        Room.E_WALL_TYPE wallType0;
        Room.E_WALL_TYPE wallType1;

        if (room0Pos.second > room1Pos.second)
        {
            //room1 is north of room0
            wallType0 = room0.getWallType(room0Rot);
            wallType1 = room1.getWallType((short) ((room1Rot + 2) % 4));
        }
        else if (room0Pos.first < room1Pos.first)
        {
            //room1 is east of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 1) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 3) % 4));
        }
        else if (room0Pos.second < room1Pos.second)
        {
            //room1 is south of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 2) % 4));
            wallType1 = room1.getWallType(room1Rot);
        }
        else
        {
            //room1 is west of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 3) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 1) % 4));
        }

        if ((wallType0 == DOOR_UNLOCKED || wallType0 == DOOR_OPEN) &&
            (wallType1 == DOOR_UNLOCKED || wallType1 == DOOR_OPEN))
        {
            return true;
        }

        return false;
    }

    public boolean getValidAdjacency(long room0, long room1)
    {
        return getValidAdjacency(rooms.get(room0), rooms.get(room1));
    }

    public boolean getValidAdjacency(Room room0, Room room1)
    {
        int mark0 = room0.getMarker();
        int mark1 = room1.getMarker();

        float angle0 = renderer.getAngleBetweenMarkers(mark0, mark1);
        float angle1 = renderer.getAngleBetweenMarkers(mark1, mark0);

        short wall0 = getWallFromAngle(angle0);
        short wall1 = getWallFromAngle(angle1);

        Room.E_WALL_TYPE wallType0 = room0.getWallType(wall0);
        Room.E_WALL_TYPE wallType1 = room1.getWallType(wall1);

        if (wallType0 != wallType1)
        {
            return false;
        }

        Pair<Integer, Integer> proposedPositon = map.getProposedPositon(room0.getId(), wall0);
        short proposedRotation =
            map.getProposedRotation(room0.getId(), wall0, wall1);

        System.out.println(
            "Proposed new room position and rotation: " + proposedPositon.first + ", " +
            proposedPositon.second + ", " + proposedRotation);

        for (int i = 0; i < 4; ++i)
        {
            long testRoom =
                map.getRoomFromPositionInDirection(proposedPositon.first, proposedPositon.second,
                                                   (short) i);
            if (testRoom > -1 && testRoom != room0.getId())
            {
                System.out.println("Testing against room: " + testRoom);

                if (!getValidAdjacencyProposedRoom(proposedPositon.first,
                                                   proposedPositon.second,
                                                   proposedRotation,
                                                   rooms.get(testRoom),
                                                   room1))
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean getValidAdjacencyExistingRooms(long room0, long room1)
    {
        return getValidAdjacencyExistingRooms(rooms.get(room0), rooms.get(room1));
    }

    public boolean getValidAdjacencyProposedRoom(int x, int y, short rot, long room0, long room1)
    {
        return getValidAdjacencyProposedRoom(x, y, rot, rooms.get(room0), rooms.get(room1));
    }

    public boolean getValidAdjacencyProposedRoom(int x, int y, short rot, Room room0, Room room1)
    {
        Pair<Integer, Integer> room0Pos = map.getPosition(room0.getId());
        short                  room0Rot = map.getRoomRotation(room0Pos.first, room0Pos.second);
        Pair<Integer, Integer> room1Pos = new Pair<>(x, y);
        short                  room1Rot = rot;

        Room.E_WALL_TYPE wallType0;
        Room.E_WALL_TYPE wallType1;

        if (room0Pos.second > room1Pos.second)
        {
            //room1 is north of room0
            wallType0 = room0.getWallType(room0Rot);
            wallType1 = room1.getWallType((short) ((room1Rot + 2) % 4));
        }
        else if (room0Pos.first < room1Pos.first)
        {
            //room1 is east of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 1) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 3) % 4));
        }
        else if (room0Pos.second < room1Pos.second)
        {
            //room1 is south of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 2) % 4));
            wallType1 = room1.getWallType(room1Rot);
        }
        else
        {
            //room1 is west of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 3) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 1) % 4));
        }

        if (wallType0 == wallType1)
        {
            return true;
        }

        return false;
    }

    public boolean getValidAdjacencyExistingRooms(Room room0, Room room1)
    {
        Pair<Integer, Integer> room0Pos = map.getPosition(room0.getId());
        short                  room0Rot = map.getRoomRotation(room0Pos.first, room0Pos.second);
        Pair<Integer, Integer> room1Pos = map.getPosition(room1.getId());
        short                  room1Rot = map.getRoomRotation(room1Pos.first, room1Pos.second);

        if (map.checkAdjacent(room0.getId(), room1.getId()) < 0)
        {
            return false;
        }

        Room.E_WALL_TYPE wallType0;
        Room.E_WALL_TYPE wallType1;

        if (room0Pos.second > room1Pos.second)
        {
            //room1 is north of room0
            wallType0 = room0.getWallType(room0Rot);
            wallType1 = room1.getWallType((short) ((room1Rot + 2) % 4));
        }
        else if (room0Pos.first < room1Pos.first)
        {
            //room1 is east of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 1) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 3) % 4));
        }
        else if (room0Pos.second < room1Pos.second)
        {
            //room1 is south of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 2) % 4));
            wallType1 = room1.getWallType(room1Rot);
        }
        else
        {
            //room1 is west of room0
            wallType0 = room0.getWallType((short) ((room0Rot + 3) % 4));
            wallType1 = room1.getWallType((short) ((room1Rot + 1) % 4));
        }

        if (wallType0 == wallType1)
        {
            return true;
        }

        return false;
    }

    public short getWallFromAngle(float angle)
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

    @Override
    public void newMarker(int marker)
    {

    }

    private void performPlayerSpecial(Actor actor, Special special)
    {
        Room             room    = rooms.get(actor.getRoom());
        ArrayList<Actor> targets = new ArrayList<>();

        switch (special.getTargetingType())
        {
            case SINGLE_PLAYER:
            {
                targets = getActorsInRoom(room, true);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else if (targets.size() == 1)
                {
                    actor.performSpecial(special, targets.get(0));
                }
                else
                {
                    //TODO display target selection
                    Toast.makeText(getApplicationContext(),
                                   "Multiple possible targets, not yet implemented",
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case SINGLE_NON_PLAYER:
            {
                targets = getActorsInRoom(room, false);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else if (targets.size() == 1)
                {
                    if (!actor.performSpecial(special, targets.get(0)))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                else
                {
                    //TODO display target selection
                    Toast.makeText(getApplicationContext(),
                                   "Multiple possible targets, not yet implemented",
                                   Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case AOE_PLAYER:
            {
                targets = getActorsInRoom(room, true);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!actor.performSpecial(special, targets))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            case AOE_NON_PLAYER:
            {
                targets = getActorsInRoom(room, false);
                if (targets.size() < 1)
                {
                    Toast.makeText(getApplicationContext(),
                                   "Nothing to use special on here",
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (!actor.performSpecial(special, targets))
                    {
                        Toast.makeText(getApplicationContext(),
                                       "Not enough SP",
                                       Toast.LENGTH_SHORT).show();
                    }
                }
                break;
            }
            default:
                break;
        }
    }

    private ArrayList<Actor> getActorsInRoom(Room room, boolean getPlayers)
    {
        ArrayList<Actor> res = new ArrayList<>();

        for (Long actorId : room.getResidentActors())
        {
            Actor actor = actors.get(actorId);
            if ((getPlayers && actor.isPlayer()) || (!getPlayers && !actor.isPlayer()))
            {
                res.add(actor);
            }
        }

        return res;
    }

    @Override
    public void handleRead(String msg)
    {
        Toast.makeText(this, "Read: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleStatus(String msg)
    {
        Toast.makeText(this, "Status: " + msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void handleError(String msg)
    {
        Toast.makeText(this, "Error: " + msg, Toast.LENGTH_SHORT).show();
    }

    /*private class GameMaster extends Thread {
        boolean running = false;
        int state = 0;

        GameMaster() {
            running = true;
        }

        public void run() {
            while (running) {
                switch (state) {
                    case 0:
                        break;
                    default:
                        break;
                }
            }
        }
    }*/
}