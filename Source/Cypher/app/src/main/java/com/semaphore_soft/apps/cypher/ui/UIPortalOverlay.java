package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;

import java.util.ArrayList;

/**
 * Created by Scorple on 2/17/2017.
 */

public class UIPortalOverlay extends UIBase
{
    public UIPortalOverlay(Context context)
    {
        super(context);
    }

    public UIPortalOverlay(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public UIPortalOverlay(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createUI()
    {
        makeView(R.layout.overlay_loading);
    }

    public void overlayPlayerMarkerSelect()
    {
        makeView(R.layout.overlay_player_marker_select);

        Button btnPlayerMarkerSelect = (Button) findViewById(R.id.btnPlayerMarkerSelect);
        btnPlayerMarkerSelect.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnPlayerMarkerSelect");
            }
        });
    }

    public void overlayStartMarkerSelect()
    {
        makeView(R.layout.overlay_start_marker_select);

        Button btnStartMarkerSelect = (Button) findViewById(R.id.btnStartMarkerSelect);
        btnStartMarkerSelect.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnStartMarkerSelect");
            }
        });
    }

    public void overlayWaitingForClients()
    {
        makeView(R.layout.overlay_waiting_for_clients);
    }

    public void overlayWaitingForHost()
    {
        makeView(R.layout.overlay_waiting_for_host);
    }

    public void overlayWaitingForTurn(final int healthMax,
                                      final int healthCurrent,
                                      final int energyMax,
                                      final int energyCurrent)
    {
        makeView(R.layout.overlay_waiting_for_turn);

        setHealth(healthMax, healthCurrent);
        setEnergy(energyMax, energyCurrent);
    }

    public void overlayAction(final int healthMax,
                              final int healthCurrent,
                              final int energyMax,
                              final int energyCurrent)
    {
        makeView(R.layout.overlay_action);

        Button btnEndTurn = (Button) findViewById(R.id.btnEndTurn);
        btnEndTurn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnEndTurn");
            }
        });

        Button btnGenerateRoom = (Button) findViewById(R.id.btnGenerateRoom);
        btnGenerateRoom.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnGenerateRoom");
            }
        });

        ImageButton btnOpenDoor = (ImageButton) findViewById(R.id.btnOpenDoor);
        btnOpenDoor.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnOpenDoor");
            }
        });

        ImageButton btnAttack = (ImageButton) findViewById(R.id.btnAttack);
        btnAttack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnAttack");
            }
        });

        ImageButton btnDefend = (ImageButton) findViewById(R.id.btnDefend);
        btnDefend.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnDefend");
            }
        });

        ImageButton btnSpecial = (ImageButton) findViewById(R.id.btnSpecial);
        btnSpecial.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnSpecial");
            }
        });

        setHealth(healthMax, healthCurrent);
        setEnergy(energyMax, energyCurrent);
    }

    public void overlaySelect(ArrayList<Pair<String, String>> options)
    {
        makeView(R.layout.overlay_select);
        LinearLayout      lloOptions = (LinearLayout) findViewById(R.id.lloOptions);
        ArrayList<String> names      = new ArrayList<>();

        for (final Pair<String, String> option : options)
        {
            Button btnTarget = new Button(getContext());
            String name      = getName(option.first, 1, names);
            names.add(name);
            LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                              LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(160, 160, 160, 160);
            btnTarget.setLayoutParams(layoutParams);
            btnTarget.setText(name);
            btnTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    notifyListener(option.second);
                }
            });
            lloOptions.addView(btnTarget);
        }

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnCancel");
            }
        });
    }

    private String getName(String originalName, int testNum, ArrayList<String> takenNames)
    {
        if (!takenNames.contains(originalName))
        {
            return originalName;
        }
        else if (testNum == 1 || takenNames.contains(originalName + " " + testNum))
        {
            return getName(originalName, testNum + 1, takenNames);
        }
        else
        {
            return originalName + " " + testNum;
        }
    }

    public void setHealth(final int healthMax, final int healthCurrent)
    {
        ImageView imgHealth = (ImageView) findViewById(R.id.imgHealth);
        TextView  lblHealth = (TextView) findViewById(R.id.lblHealth);

        if (imgHealth != null && lblHealth != null)
        {
            float healthBarLength = 50f;

            healthBarLength += ((float) healthCurrent / (float) healthMax) * 150f;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                healthBarLength,
                                                getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                24,
                                                getResources().getDisplayMetrics()));

            imgHealth.setLayoutParams(layoutParams);

            //imgHealth.getLayoutParams().width = (int) healthBarLength;

            imgHealth.requestLayout();
            lblHealth.setText("" + healthCurrent);
            //lblHealth.requestLayout();
        }
    }

    public void setEnergy(final int energyMax, final int energyCurrent)
    {
        ImageView imgEnergy = (ImageView) findViewById(R.id.imgEnergy);
        TextView  lblEnergy = (TextView) findViewById(R.id.lblEnergy);

        if (imgEnergy != null && lblEnergy != null)
        {
            float energyBarLength = 50f;

            energyBarLength += ((float) energyCurrent / (float) energyMax) * 150f;

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                energyBarLength,
                                                getResources().getDisplayMetrics()),
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                24,
                                                getResources().getDisplayMetrics()));
            layoutParams.addRule(RelativeLayout.BELOW, R.id.imgHealth);

            imgEnergy.setLayoutParams(layoutParams);

            //imgEnergy.getLayoutParams().width = (int) energyBarLength;

            imgEnergy.requestLayout();
            lblEnergy.setText("" + energyCurrent);
            //lblEnergy.requestLayout();
        }
    }
}
