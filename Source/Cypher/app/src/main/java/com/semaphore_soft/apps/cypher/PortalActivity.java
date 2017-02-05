package com.semaphore_soft.apps.cypher;

import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.Effect;
import com.semaphore_soft.apps.cypher.game.Entity;
import com.semaphore_soft.apps.cypher.game.Item;
import com.semaphore_soft.apps.cypher.game.Map;
import com.semaphore_soft.apps.cypher.game.Room;
import com.semaphore_soft.apps.cypher.game.Special;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_OPEN;
import static com.semaphore_soft.apps.cypher.game.Room.E_WALL_TYPE.DOOR_UNLOCKED;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity implements PortalRenderer.NewMarkerListener
{
    //TODO this class is too large, some methods should be exported

    public static final short OVERLAY_PLAYER_MARKER_SELECT = 0;
    public static final short OVERLAY_START_MARKER_SELECT  = 1;
    public static final short OVERLAY_ACTION               = 2;
    public static final short OVERLAY_OPEN_DOOR            = 3;
    public static final short OVERLAY_WAITING_FOR_HOST     = 4;
    public static final short OVERLAY_WAITING_FOR_PLAYERS  = 5;

    boolean host;

    Long playerID;
    int  characterID;

    PortalRenderer renderer;
    int            overlayID;
    FrameLayout    overlay_layout;

    private int playerMarkerID = -1;

    private Hashtable<Long, Room>    rooms;
    private Hashtable<Long, Actor>   actors;
    private Hashtable<Long, Special> specials;
    private Hashtable<Long, Entity>  entities;
    private Hashtable<Long, Item>    items;
    private Map                      map;

    //GameMaster gameMaster;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        overlay_layout = (FrameLayout) findViewById(R.id.overlay_frame);

        host = getIntent().getBooleanExtra("host", false);

        playerID = getIntent().getLongExtra("player", 0);
        characterID = getIntent().getIntExtra("character", 0);

        renderer = new PortalRenderer();

        rooms = new Hashtable<>();
        actors = new Hashtable<>();
        specials = new Hashtable<>();
        entities = new Hashtable<>();
        items = new Hashtable<>();

        map = new Map();

        setOverlay2(OVERLAY_PLAYER_MARKER_SELECT);

        //gameMaster = new GameMaster();
        //gameMaster.start();
    }

    /*private void setOverlay(int id)
    {
        overlay_layout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        View overlay;

        switch (id)
        {
            case 1:
            {
                overlay = inflater.inflate(R.layout.overlay_1, null, false);
                overlay_layout.addView(overlay);
                Button btnSelect = (Button) findViewById(R.id.btnSelect);
                btnSelect.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //look for a valid player marker
                        int markerID = getFirstUnreservedMarker();

                        if (markerID > -1)
                        {
                            playerMarkerID = markerID;
                            //if there is not an actor associated with this playerID,
                            //generate a new one
                            if (!actors.containsKey(playerID))
                            {
                                actors.put(playerID, new Actor(playerID));
                            }
                            //update the marker and character of the actor associated with this player
                            Actor actor = actors.get(playerID);
                            actor.setMarker(markerID);
                            actor.setChar(characterID);
                            //if the actor was previously in a room, remove it from that room's actor list
                            if (actor.getRoom() > -1)
                            {
                                rooms.get(actor.getRoom()).removeActor(playerID);
                            }
                            //clear the actor's room
                            actor.setRoom(-1);

                            //notify the renderer of the character/marker change
                            renderer.setCharacterMarker(characterID, markerID);

                            Toast.makeText(getApplicationContext(),
                                           "Marker selected",
                                           Toast.LENGTH_SHORT)
                                 .show();
                            setOverlay(2);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "No marker found",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });

                //re-initialize the character select drop-down
                ArrayList<String> characterNamesList = new ArrayList<>();
                characterNamesList.add("White");
                characterNamesList.add("Red");
                characterNamesList.add("Green");
                characterNamesList.add("Purple");
                Spinner spnCharacter =
                    (Spinner) findViewById(R.id.spnCharacter);
                ArrayAdapter<String> characterNameAdapter = new ArrayAdapter<>(
                    getApplicationContext(),
                    R.layout.text_spinner,
                    characterNamesList);
                spnCharacter.setAdapter(characterNameAdapter);
                spnCharacter.setSelection(characterID);

                //handler for character select
                spnCharacter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id)
                    {
                        //characters are listed in the spinner in character id order, so we may
                        //use selected item position to get character id
                        characterID = position;

                        //check if the character is already associated with an actor/player
                        boolean foundCharacter = false;
                        for (Long actorID : actors.keySet())
                        {
                            //if the selected character is not yet associated with an actor,
                            //get a new playerID/actor id and clear any associated marker
                            if (actors.get(actorID).getChar() == characterID)
                            {
                                playerID = actorID;
                                foundCharacter = true;
                            }
                        }
                        if (!foundCharacter)
                        {
                            //if the selected character is not yet associated with an actor,
                            //get a new playerID/actor id and clear any associated marker
                            playerID = getNextID(actors);
                            playerMarkerID = -1;
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });
                break;
            }
            //primary interaction/debug overlay
            case 2:
            {
                //generate overlay_2, currently the primary interaction/debug overlay
                overlay = inflater.inflate(R.layout.overlay_2, null, false);
                overlay_layout.addView(overlay);

                //update debug interface
                TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                txtStatus.setText("Player marker: " + playerMarkerID);

                //"reselect" button
                //for resetting current character/player marker or setting a marker for a new
                //character/player
                Button btnReselect = (Button) findViewById(R.id.btnReselect);
                btnReselect.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        //look for a valid player marker
                        int markerID = getFirstUnreservedMarker();

                        if (markerID > -1)
                        {
                            playerMarkerID = markerID;
                            //if there is not an actor associated with this playerID,
                            //generate a new one
                            if (!actors.containsKey(playerID))
                            {
                                actors.put(playerID, new Actor(playerID));
                            }
                            //update the marker and character of the actor associated with this player
                            Actor actor = actors.get(playerID);
                            actor.setMarker(markerID);
                            actor.setChar(characterID);
                            //if the actor was previously in a room, remove it from that room's actor list
                            if (actor.getRoom() > -1)
                            {
                                rooms.get(actor.getRoom()).removeActor(playerID);
                            }
                            //clear the actor's room
                            actor.setRoom(-1);

                            //notify the renderer of the character/marker change
                            renderer.setCharacterMarker(characterID, markerID);

                            Toast.makeText(getApplicationContext(),
                                           "Marker selected",
                                           Toast.LENGTH_SHORT)
                                 .show();
                            setOverlay(2);
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "No marker found",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });

                //"end turn" button
                //currently exists only to update player room
                Button btnEndTurn = (Button) findViewById(R.id.btnEndTurn);
                btnEndTurn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int nearestMarkerID = getNearestNonPlayerMarker(playerMarkerID);
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
                            if (!foundRoom)
                            {
                                //if there is not an existing room attached to the nearest marker,
                                //generate a new one
                                nearestRoomID = getNextID(rooms);
                                Room newRoom = new Room(nearestRoomID, nearestMarkerID);

                                //attach three random entities with type 0 or 1 to the new room
                                for (int i = 0; i < 3; ++i)
                                {
                                    long newEntityID =
                                        getNextID(entities);
                                    int newType = ((Math.random() > 0.3) ? 0 : 1);

                                    Entity newEntity = new Entity(newEntityID, newType);
                                    entities.put(newEntityID, newEntity);
                                    newRoom.addEntity(newEntityID);
                                }

                                //add the new room to the room collection
                                rooms.put(nearestRoomID, newRoom);
                            }

                            //if the actor was previously in a room, remove it from that room
                            if (actors.get(playerID).getRoom() > -1)
                            {
                                rooms.get(actors.get(playerID).getRoom()).removeActor(playerID);
                            }

                            //update the actor's room and the new room's actor list
                            actors.get(playerID).setRoom(nearestRoomID);
                            rooms.get(nearestRoomID).addActor(playerID);

                            //notify the renderer of the actor's new location
                            renderer.setCharacterRoom(characterID, nearestMarkerID);

                            //update the debug interface
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText("Player is in room: " + nearestMarkerID);
                            Toast.makeText(getApplicationContext(),
                                           "Updated player room",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Couldn't find room",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                    }
                });

                //debug/status screen button
                Button btnStatus = (Button) findViewById(R.id.btnStatus);
                btnStatus.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(3);
                    }
                });

                //re-initialize the character select drop-down
                ArrayList<String> characterNamesList = new ArrayList<>();
                characterNamesList.add("White");
                characterNamesList.add("Red");
                characterNamesList.add("Green");
                characterNamesList.add("Purple");
                Spinner spnCharacter =
                    (Spinner) findViewById(R.id.spnCharacter);
                ArrayAdapter<String> characterNameAdapter = new ArrayAdapter<>(
                    getApplicationContext(),
                    R.layout.text_spinner,
                    characterNamesList);
                spnCharacter.setAdapter(characterNameAdapter);
                spnCharacter.setSelection(characterID);

                //handler for character select
                spnCharacter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id)
                    {
                        //characters are listed in the spinner in character id order, so we may
                        //use selected item position to get character id
                        characterID = position;

                        //check if the character is already associated with an actor/player
                        boolean foundCharacter = false;
                        for (Long actorID : actors.keySet())
                        {
                            if (actors.get(actorID).getChar() == characterID)
                            {
                                //if it is, set this player id to the actor id of the existing actor
                                //(playerID can be thought of as "this player's actorID")
                                playerID = actorID;
                                foundCharacter = true;
                            }
                        }
                        if (!foundCharacter)
                        {
                            //if the selected character is not yet associated with an actor,
                            //get a new playerID/actor id and clear any associated marker
                            playerID = getNextID(actors);
                            playerMarkerID = -1;

                            //update debug interface
                            TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                            txtStatus.setText(
                                "Player marker: -");
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText("Player is in room: -");
                        }
                        else
                        {
                            //if the selected character is associated with an actor,
                            //get that actor's associated marker
                            playerMarkerID = actors.get(playerID).getMarker();

                            //update debug interface
                            TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                            txtStatus.setText(
                                "Player marker: " + playerMarkerID);
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText(
                                "Player is in room: " + ((actors.get(playerID).getRoom() >
                                                          -1) ? rooms.get(actors.get(playerID)
                                                                                .getRoom())
                                                                     .getMarker() : "-"));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });

                Button btnHeading = (Button) findViewById(R.id.btnMarkerHeading);
                btnHeading.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int markerID = renderer.getFirstMarker();
                        if (markerID > -1)
                        {
                            float  res    = renderer.getMarkerDirection(markerID);
                            String output = "Heading: " + res;

                            int nearestMarkerID = getNearestNonPlayerMarker(markerID);
                            if (nearestMarkerID > -1)
                            {
                                float res2 =
                                    renderer.getAngleBetweenMarkers(markerID, nearestMarkerID);
                                output += "\nAngle: " + res2;
                            }

                            Toast.makeText(PortalActivity.this,
                                           output,
                                           Toast.LENGTH_SHORT).show();
                        }

                        //System.out.println("Heading: " + heading);
                        //Toast.makeText(getApplicationContext(), "Heading: " + heading, Toast.LENGTH_SHORT);
                    }
                });
                break;
            }
            case 3:
            {
                overlay = inflater.inflate(R.layout.overlay_3, null, false);
                overlay_layout.addView(overlay);

                Actor actor = actors.get(playerID);
                Room  room  = ((actor.getRoom() > -1) ? rooms.get(actor.getRoom()) : null);
                String output = "Player ID: " + playerID
                                + "\nPlayer Character ID: " + actor.getChar()
                                + "\nPlayer Marker ID: " + actor.getMarker()
                                + "\nPlayer Room ID: " +
                                ((actor.getRoom() > -1) ? actor.getRoom() : "-")
                                + "\nRoom Marker ID: " + ((room != null) ? room.getMarker() : "-");
                if (room != null)
                {
                    output += "\nRoom Resident Actors:";
                    for (Long residentActorID : room.getResidentActors())
                    {
                        output +=
                            " " + residentActorID + ":" + actors.get(residentActorID).getChar();
                    }
                    output += "\nRoom Resident Entities:";
                    for (Long residentEntityID : room.getResidentEntities())
                    {
                        output +=
                            " " + residentEntityID + ":" + entities.get(residentEntityID).getType();
                    }
                }

                TextView txtStatus = (TextView) findViewById(R.id.txtOut);
                txtStatus.setText(output);

                Button btnStatusReturn = (Button) findViewById(R.id.btnStatusReturn);
                btnStatusReturn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(2);
                    }
                });
                break;
            }
            case 4:
            {
                overlay = inflater.inflate(R.layout.overlay_4, null, false);
                overlay_layout.addView(overlay);
                Button btnMakeRoom = (Button) findViewById(R.id.btnMakeRoom);
                btnMakeRoom.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int mark = getFirstUnreservedMarker();
                        if (mark > -1)
                        {
                            long roomID = getNextID(rooms);
                            Room room   = new Room(roomID, mark);
                            rooms.put(roomID, room);
                            renderer.createRoom(room);
                        }
                    }
                });
                Button btnOpenDoor = (Button) findViewById(R.id.btnOpenDoor);
                btnOpenDoor.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (actors.get(playerID).getRoom() < 0)
                        {
                            Toast.makeText(getApplicationContext(),
                                           "Must enter room first",
                                           Toast.LENGTH_SHORT)
                                 .show();
                        }
                        else
                        {

                        }
                    }
                });
                Button btnMove = (Button) findViewById(R.id.btnMove);
                btnMove.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (actors.get(playerID).getRoom() < 0)
                        {
                            int nearestMarkerID = getNearestNonPlayerMarker(playerMarkerID);
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
                                if (!foundRoom)
                                {
                                    //if there is not an existing room attached to the nearest marker,
                                    //generate a new one
                                    nearestRoomID = getNextID(rooms);
                                    Room newRoom = new Room(nearestRoomID, nearestMarkerID);

                                    //attach three random entities with type 0 or 1 to the new room
                                    for (int i = 0; i < 3; ++i)
                                    {
                                        long newEntityID =
                                            getNextID(entities);
                                        int newType = ((Math.random() > 0.3) ? 0 : 1);

                                        Entity newEntity = new Entity(newEntityID, newType);
                                        entities.put(newEntityID, newEntity);
                                        newRoom.addEntity(newEntityID);
                                    }

                                    //add the new room to the room collection
                                    rooms.put(nearestRoomID, newRoom);
                                }

                                //if the actor was previously in a room, remove it from that room
                                if (actors.get(playerID).getRoom() > -1)
                                {
                                    rooms.get(actors.get(playerID).getRoom()).removeActor(playerID);
                                }

                                //update the actor's room and the new room's actor list
                                actors.get(playerID).setRoom(nearestRoomID);
                                rooms.get(nearestRoomID).addActor(playerID);

                                //notify the renderer of the actor's new location
                                renderer.setCharacterRoom(characterID, nearestMarkerID);

                                Toast.makeText(getApplicationContext(),
                                               "Updated player room",
                                               Toast.LENGTH_SHORT)
                                     .show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(),
                                               "Couldn't find room",
                                               Toast.LENGTH_SHORT)
                                     .show();
                            }
                        }
                    }
                });
                break;
            }
            case 5:
            {
                overlay = inflater.inflate(R.layout.overlay_5, null, false);
                overlay_layout.addView(overlay);
                Button btnMakeRoom = (Button) findViewById(R.id.btnMakeRoom);
                btnMakeRoom.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int mark = getFirstUnreservedMarker();
                        if (mark > -1)
                        {
                            long roomID = getNextID(rooms);
                            Room room   = new Room(roomID, mark);
                            rooms.put(roomID, room);
                            renderer.createRoom(room);
                        }
                    }
                });
                Button btnConfirm = (Button) findViewById(R.id.btnConfirm);
                btnConfirm.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                    }
                });
                break;
            }
            default:
                break;
        }

        overlayID = id;
    }
    */

    private void setOverlay2(int id)
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
                            Actor actor = new Actor(playerID, characterID, mark);
                            loadActorStats(actor, characterID);
                            actors.put(playerID, actor);
                            renderer.setCharacterMarker(characterID, mark);

                            Toast.makeText(getApplicationContext(),
                                           "Player Marker Set",
                                           Toast.LENGTH_SHORT)
                                 .show();

                            if (host)
                            {
                                setOverlay2(OVERLAY_START_MARKER_SELECT);
                            }
                            else
                            {
                                setOverlay2(OVERLAY_ACTION);
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

                            //place every actor in that room
                            for (Actor actor : actors.values())
                            {
                                actor.setRoom(roomID);
                            }

                            map.init(roomID);

                            Toast.makeText(getApplicationContext(),
                                           "Starting Room Established",
                                           Toast.LENGTH_SHORT)
                                 .show();

                            setOverlay2(OVERLAY_ACTION);
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
                                    //check for valid move
                                    if (getValidPath(actors.get(playerID).getRoom(), nearestRoomID))
                                    {
                                        //if the actor was previously in a room, remove it from that room
                                        if (actors.get(playerID).getRoom() > -1)
                                        {
                                            rooms.get(actors.get(playerID).getRoom())
                                                 .removeActor(playerID);
                                        }

                                        //update the actor's room and the new room's actor list
                                        actors.get(playerID).setRoom(nearestRoomID);
                                        rooms.get(nearestRoomID).addActor(playerID);

                                        //notify the renderer of the actor's new location
                                        renderer.setCharacterRoom(characterID, nearestMarkerID);

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
                        setOverlay2(OVERLAY_OPEN_DOOR);
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
                        for (Long targetId : room.getResidentActors())
                        {
                            Actor target = actors.get(targetId);
                            if (!target.isPlayer())
                            {
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
                            actor.attack(targets.get(0));
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

                                    renderer.updateRoom(room0);
                                    renderer.updateRoom(room1);

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

                                            renderer.updateRoom(room1);
                                            renderer.updateRoom(rooms.get(id));
                                        }
                                    }

                                    Toast.makeText(getApplicationContext(),
                                                   "Door Opened",
                                                   Toast.LENGTH_SHORT)
                                         .show();

                                    setOverlay2(OVERLAY_ACTION);
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
                        setOverlay2(OVERLAY_ACTION);
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

    public long getNextID(Hashtable<Long, ?> hashtable)
    {
        return ((hashtable.size() > 0) ? Collections.max(hashtable.keySet()) + 1 : 0);
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

    private void loadActorStats(Actor actor, int characterID)
    {
        String actorName;

        switch (characterID)
        {
            case 0:
                actorName = "knight";
                break;
            case 1:
                actorName = "soldier";
                break;
            case 2:
                actorName = "ranger";
                break;
            case 3:
                actorName = "wizard";
                break;
            default:
                return;
        }

        loadActorStats(actor, actorName);
    }

    private void loadActorStats(Actor actor, String actorName)
    {
        try
        {
            XmlPullParserFactory factory     = XmlPullParserFactory.newInstance();
            XmlPullParser        actorParser = factory.newPullParser();

            AssetManager assetManager     = getAssets();
            InputStream  actorInputStream = assetManager.open("actors.xml");
            actorParser.setInput(actorInputStream, null);

            boolean foundCharacter    = false;
            boolean finishedCharacter = false;
            boolean foundSpecials     = false;
            boolean finishedSpecials  = false;

            ArrayList<String> actorSpecials = new ArrayList<>();

            System.out.println("character is: " + actorName);

            int event = actorParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedCharacter)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (actorName.equals(actorParser.getName()))
                        {
                            foundCharacter = true;
                            System.out.println("found character");
                        }
                        else if (foundCharacter)
                        {
                            if (actorParser.getName().equals("specials"))
                            {
                                foundSpecials = true;
                                System.out.println("found character specials");
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                actorParser.next();
                                actorSpecials.add(actorParser.getText());
                                System.out.println("found special: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("healthMaximum"))
                            {
                                actorParser.next();
                                actor.setHealthMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setHealthCurrent(actor.getHealthMaximum());
                                System.out.println(
                                    "found health maximum: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("attackRating"))
                            {
                                actorParser.next();
                                actor.setAttackRating(Integer.parseInt(actorParser.getText()));
                                System.out.println("found attack rating: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("specialMaximum"))
                            {
                                actorParser.next();
                                actor.setSpecialMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setSpecialCurrent(actor.getSpecialMaximum());
                                System.out.println(
                                    "found special maximum: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("specialRating"))
                            {
                                actorParser.next();
                                actor.setSpecialRating(Integer.parseInt(actorParser.getText()));
                                System.out.println(
                                    "found special rating: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("defenceRating"))
                            {
                                actorParser.next();
                                actor.setDefenceRating(Integer.parseInt(actorParser.getText()));
                                System.out.println(
                                    "found defence rating: " + actorParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundCharacter)
                        {
                            if (actorName.equals(actorParser.getName()))
                            {
                                finishedCharacter = true;
                                System.out.println("finished character");
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                if (actorParser.getName().equals("specials"))
                                {
                                    finishedSpecials = true;
                                    System.out.println("finished character specials");
                                }
                            }
                        }
                        break;
                }
                event = actorParser.next();
            }

            for (String specialName : actorSpecials)
            {
                System.out.println("special is: " + specialName);

                boolean specialLoaded = false;

                for (Long specialId : specials.keySet())
                {
                    Special special = specials.get(specialId);
                    if (specialName.equals(special.getName()))
                    {
                        actor.addSpecial(special);
                        specialLoaded = true;
                        break;
                    }
                }

                if (!specialLoaded)
                {
                    XmlPullParser specialParser      = factory.newPullParser();
                    InputStream   specialInputStream = assetManager.open("specials.xml");
                    specialParser.setInput(specialInputStream, null);

                    boolean foundSpecial    = false;
                    boolean finishedSpecial = false;
                    boolean foundEffects    = false;
                    boolean finishedEffects = false;

                    int               cost          = -1;
                    int               duration      = -1;
                    String            targetingType = "";
                    ArrayList<String> effects       = new ArrayList<>();

                    event = specialParser.getEventType();
                    while (event != XmlPullParser.END_DOCUMENT && !finishedSpecial)
                    {
                        switch (event)
                        {
                            case XmlPullParser.START_TAG:
                                if (specialName.equals(specialParser.getName()))
                                {
                                    foundSpecial = true;
                                    System.out.println("found special");
                                }
                                else if (foundSpecial)
                                {
                                    if (specialParser.getName().equals("effects"))
                                    {
                                        foundEffects = true;
                                        System.out.println("found special effects");
                                    }
                                    else if (foundEffects && !finishedEffects)
                                    {
                                        specialParser.next();
                                        effects.add(specialParser.getText());
                                        System.out.println(
                                            "found effect: " + specialParser.getText());
                                    }
                                    else if (specialParser.getName().equals("cost"))
                                    {
                                        specialParser.next();
                                        cost = Integer.parseInt(specialParser.getText());
                                        System.out.println(
                                            "found cost: " + specialParser.getText());
                                    }
                                    else if (specialParser.getName().equals("duration"))
                                    {
                                        specialParser.next();
                                        duration = Integer.parseInt(specialParser.getText());
                                        System.out.println(
                                            "found duration: " + specialParser.getText());
                                    }
                                    else if (specialParser.getName().equals("targetingType"))
                                    {
                                        specialParser.next();
                                        targetingType = specialParser.getText();
                                        System.out.println(
                                            "found targeting type: " + specialParser.getText());
                                    }
                                }
                                break;
                            case XmlPullParser.END_TAG:
                                if (foundSpecial)
                                {
                                    if (specialName.equals(specialParser.getName()))
                                    {
                                        finishedSpecial = true;
                                        System.out.println("finished special");
                                    }
                                    else if (foundEffects && !finishedEffects)
                                    {
                                        if (specialParser.getName().equals("effects"))
                                        {
                                            finishedEffects = true;
                                            System.out.println("finished special effects");
                                        }
                                    }
                                }
                                break;
                        }
                        event = specialParser.next();
                    }

                    Special.E_TARGETING_TYPE specialTargetingType;

                    switch (targetingType)
                    {
                        case "SINGLE_PLAYER":
                            specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_PLAYER;
                            break;
                        case "SINGLE_NON_PLAYER":
                            specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
                            break;
                        case "AOE_PLAYER":
                            specialTargetingType = Special.E_TARGETING_TYPE.AOE_PLAYER;
                            break;
                        case "AOE_NON_PLAYER":
                            specialTargetingType = Special.E_TARGETING_TYPE.AOE_NON_PLAYER;
                            break;
                        default:
                            specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
                            break;
                    }

                    if (finishedSpecial)
                    {
                        Special special = new Special(getNextID(specials),
                                                      specialName,
                                                      cost,
                                                      duration,
                                                      specialTargetingType);

                        for (String effect : effects)
                        {
                            switch (effect)
                            {
                                case "HEAL":
                                    special.addEffect(Effect.E_EFFECT.HEAL);
                                    break;
                                case "ATTACK":
                                    special.addEffect(Effect.E_EFFECT.ATTACK);
                                    break;
                                case "HEALTH_MAXIMUM_UP":
                                    special.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_UP);
                                    break;
                                case "HEALTH_MAXIMUM_DOWN":
                                    special.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_DOWN);
                                    break;
                                case "ATTACK_RATING_UP":
                                    special.addEffect(Effect.E_EFFECT.ATTACK_RATING_UP);
                                    break;
                                case "ATTACK_RATING_DOWN":
                                    special.addEffect(Effect.E_EFFECT.ATTACK_RATING_DOWN);
                                    break;
                                case "SPECIAL_MAXIMUM_UP":
                                    special.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_UP);
                                    break;
                                case "SPECIAL_MAXIMUM_DOWN":
                                    special.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_DOWN);
                                    break;
                                case "SPECIAL_RATING_UP":
                                    special.addEffect(Effect.E_EFFECT.SPECIAL_RATING_UP);
                                    break;
                                case "SPECIAL_RATING_DOWN":
                                    special.addEffect(Effect.E_EFFECT.SPECIAL_RATING_DOWN);
                                    break;
                                case "DEFENCE_RATING_UP":
                                    special.addEffect(Effect.E_EFFECT.DEFENCE_RATING_UP);
                                    break;
                                case "DEFENCE_RATING_DOWN":
                                    special.addEffect(Effect.E_EFFECT.DEFENCE_RATING_DOWN);
                                    break;
                            }
                        }

                        specials.put(special.getId(), special);
                        actor.addSpecial(special);
                    }
                }
            }
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
