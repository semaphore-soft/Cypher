package com.semaphore_soft.apps.cypher;

import android.os.Bundle;
import android.widget.FrameLayout;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

/**
 * Created by rickm on 11/9/2016.
 */

public class PortalActivity extends ARActivity {

    PortalRenderer renderer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState); //Calls ARActivity's ctor, abstract class of ARBaseLib
        setContentView(R.layout.main_portal);

        renderer = new PortalRenderer();
        renderer.setCharacter(getIntent().getExtras().getInt("character"));
    }

    @Override
    protected ARRenderer supplyRenderer() {
        return renderer;
    }

    @Override
    protected FrameLayout supplyFrameLayout() {
        return (FrameLayout) this.findViewById(R.id.portal_frame);
    }
}
