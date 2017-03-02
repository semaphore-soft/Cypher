package com.semaphore_soft.apps.cypher;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.FrameLayout;

import com.semaphore_soft.apps.cypher.networking.ClientService;
import com.semaphore_soft.apps.cypher.networking.NetworkConstants;
import com.semaphore_soft.apps.cypher.networking.ResponseReceiver;
import com.semaphore_soft.apps.cypher.ui.UIListener;
import com.semaphore_soft.apps.cypher.ui.UIPortalActivity;
import com.semaphore_soft.apps.cypher.ui.UIPortalOverlay;

import org.artoolkit.ar.base.ARActivity;
import org.artoolkit.ar.base.rendering.ARRenderer;

/**
 * Created by rickm on 3/1/2017.
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
    private static Handler handler      = new Handler();

    private static int    playerId;
    private static String characterName;

    private static boolean turn;

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
    }

    @Override
    protected void onStart()
    {
        // Bind to ClientService
        Intent intent = new Intent(this, ClientService.class);
        bindService(intent, mClientConnection, Context.BIND_AUTO_CREATE);
    }

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

    @Override
    protected FrameLayout supplyFrameLayout()
    {
        return (FrameLayout) this.findViewById(R.id.portal_frame);
    }

    @Override
    public void onCommand(String cmd)
    {

    }

    @Override
    public void handleRead(String msg, int readFrom)
    {

    }

    @Override
    public void handleStatus(String msg, int readFrom)
    {

    }

    @Override
    public void handleError(String msg, int readFrom)
    {

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
