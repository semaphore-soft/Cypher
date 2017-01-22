package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity
{
    Long playerID;
    int  characterID;

    PortalRenderer renderer;
    int            overlayID;
    FrameLayout    overlay_layout;

    private int playerMarkerID = -1;

    private Hashtable<Long, Actor>  actors;
    private Hashtable<Long, Room>   rooms;
    private Hashtable<Long, Entity> entities;

    //GameMaster gameMaster;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        overlay_layout = (FrameLayout) findViewById(R.id.overlay_frame);

        setOverlay(1);

        renderer = new PortalRenderer();

        playerID = getIntent().getExtras().getLong("player", 0);
        characterID = getIntent().getExtras().getInt("character", 0);

        actors = new Hashtable<>();
        rooms = new Hashtable<>();
        entities = new Hashtable<>();

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
                        int markerID = getPlayerMarker();

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
                            playerID = (long) actors.size();
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
                        int markerID = getPlayerMarker();

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
                        int nearestMarkerID = renderer.getNearestMarker(playerMarkerID);
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
                                nearestRoomID = rooms.size();
                                Room newRoom = new Room(nearestRoomID, nearestMarkerID);

                                //attach three random entities with type 0 or 1 to the new room
                                for (int i = 0; i < 3; ++i)
                                {
                                    long newEntityID = entities.size();
                                    int  newType     = ((Math.random() > 0.3) ? 0 : 1);

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
                            playerID = (long) actors.size();
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
            }
            default:
                break;
        }

        overlayID = id;
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

    private int getPlayerMarker()
    {
        int foundMarker = renderer.getFirstMarker();

        //player marker cannot already be a character's marker
        for (long actorID : actors.keySet())
        {
            if (actors.get(actorID).getMarker() == foundMarker)
            {
                return -1;
            }
        }

        //player marker cannot be a room with a character in it
        for (long roomID : rooms.keySet())
        {
            if (rooms.get(roomID).getMarker() == foundMarker)
            {
                return -1;
            }
        }

        if (foundMarker > -1)
        {
            playerMarkerID = foundMarker;
            return foundMarker;
        }

        return -1;
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
