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
                        int markerID = getPlayerMarker();

                        if (markerID > -1)
                        {
                            playerMarkerID = markerID;
                            if (!actors.containsKey(playerID))
                            {
                                actors.put(playerID, new Actor(playerID));
                            }
                            Actor actor = actors.get(playerID);
                            actor.setTag(markerID);
                            actor.setChar(characterID);
                            actor.setRoom(-1);
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
                spnCharacter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id)
                    {
                        characterID = position;
                        boolean foundCharacter = false;
                        for (Long actorID : actors.keySet())
                        {
                            if (actors.get(actorID).getChar() == characterID)
                            {
                                playerID = actorID;
                                foundCharacter = true;
                            }
                        }
                        if (!foundCharacter)
                        {
                            playerID = (long) actors.size();
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
                    {

                    }
                });
                break;
            }
            case 2:
            {
                overlay = inflater.inflate(R.layout.overlay_2, null, false);
                overlay_layout.addView(overlay);
                TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                txtStatus.setText("Player marker: " + playerMarkerID);
                Button btnReselect = (Button) findViewById(R.id.btnReselect);
                btnReselect.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int markerID = getPlayerMarker();

                        if (markerID > -1)
                        {
                            playerMarkerID = markerID;
                            if (!actors.containsKey(playerID))
                            {
                                actors.put(playerID, new Actor(playerID));
                            }
                            Actor actor = actors.get(playerID);
                            actor.setTag(markerID);
                            actor.setChar(characterID);
                            actor.setRoom(-1);
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
                Button btnEndTurn = (Button) findViewById(R.id.btnEndTurn);
                btnEndTurn.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int nearestRoomID = renderer.getNearestMarker(playerMarkerID);
                        if (nearestRoomID > -1)
                        {
                            actors.get(playerID).setRoom(nearestRoomID);
                            renderer.setCharacterRoom(characterID, nearestRoomID);
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText("Player is in room: " + nearestRoomID);
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
                spnCharacter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
                {
                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view,
                                               int position,
                                               long id)
                    {
                        characterID = position;

                        boolean foundCharacter = false;
                        for (Long actorID : actors.keySet())
                        {
                            if (actors.get(actorID).getChar() == characterID)
                            {
                                playerID = actorID;
                                foundCharacter = true;
                            }
                        }
                        if (!foundCharacter)
                        {
                            playerID = (long) actors.size();

                            playerMarkerID = -1;
                            TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                            txtStatus.setText(
                                "Player marker: -");
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText("Player is in room: -");
                        }
                        else
                        {
                            playerMarkerID = actors.get(playerID).getTag();
                            TextView txtStatus = (TextView) findViewById(R.id.txtStatus);
                            txtStatus.setText(
                                "Player marker: " + playerMarkerID);
                            TextView txtStatus2 = (TextView) findViewById(R.id.txtStatus2);
                            txtStatus2.setText(
                                "Player is in room: " + ((actors.get(playerID).getRoom() >
                                                          -1) ? actors.get(playerID)
                                                                      .getRoom() : "-"));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent)
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
            if (actors.get(actorID).getTag() == foundMarker)
            {
                return -1;
            }
        }

        //player marker cannot be a room with a character in it
        for (long roomID : rooms.keySet())
        {
            if (rooms.get(roomID).getTag() == foundMarker)
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
