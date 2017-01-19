package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

import java.util.ArrayList;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity
{
    int playerID;
    int characterID;

    PortalRenderer renderer;
    int            overlayID;
    FrameLayout    overlay_layout;

    private int playerMarkerID = -1;

    //GameMaster gameMaster;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        overlay_layout = (FrameLayout) findViewById(R.id.overlay_frame);

        setOverlay(1);

        renderer = new PortalRenderer();

        characterID = getIntent().getExtras().getInt("character");

        //renderer.setCharacter(getIntent().getExtras().getInt("character"));

        //gameMaster = new GameMaster();
        //gameMaster.start();
    }

    private void setOverlay(int id)
    {
        overlay_layout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        View   overlay;

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
                        if (setPlayerMarker())
                        {
                            //renderer.setPlayerMarkerID(playerMarkerID);
                            renderer.setCharacterMarker(characterID, playerMarkerID);
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
                        if (setPlayerMarker())
                        {
                            //renderer.setPlayerMarkerID(playerMarkerID);
                            renderer.setCharacterMarker(characterID, playerMarkerID);
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
                            //renderer.setPlayerRoomID(nearestRoomID);
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

    private boolean setPlayerMarker()
    {
        boolean ret = false;

        playerMarkerID = renderer.getFirstMarker();

        ret = (playerMarkerID > -1);

        return ret;
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
