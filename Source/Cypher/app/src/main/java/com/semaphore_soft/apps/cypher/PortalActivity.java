package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity
{

    PortalRenderer renderer;
    int            overlayID;
    FrameLayout    overlay_layout;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        overlay_layout = (FrameLayout) findViewById(R.id.overlay_frame);

        setOverlay(1);

        renderer = new PortalRenderer();
        renderer.setCharacter(getIntent().getExtras().getInt("character"));
    }

    private void setOverlay(int id)
    {
        overlay_layout.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(getApplicationContext());

        View overlay;
        Button btnOverlay;

        switch (id)
        {
            case 1:
                overlay = inflater.inflate(R.layout.overlay_1, null, false);
                overlay_layout.addView(overlay);
                btnOverlay = (Button) findViewById(R.id.btnOverlay);
                btnOverlay.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(2);
                    }
                });
                break;
            case 2:
                overlay = inflater.inflate(R.layout.overlay_2, null, false);
                overlay_layout.addView(overlay);
                btnOverlay = (Button) findViewById(R.id.btnOverlay);
                btnOverlay.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        setOverlay(1);
                    }
                });
                break;
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
}
