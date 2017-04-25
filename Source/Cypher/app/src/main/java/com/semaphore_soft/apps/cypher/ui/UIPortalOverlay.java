package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.support.v4.util.Pair;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;
import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;

/**
 * Created by Scorple on 2/17/2017.
 */

public class UIPortalOverlay extends UIBase
{
    private int healthMax;
    private int energyMax;

    private float healthBarMaxLength;
    private float energyBarMaxLength;

    private int lastHealth;
    private int lastEnergy;

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

        setupHealthAndEnergyBars(healthMax, energyMax);

        setHealth(healthCurrent);
        setEnergy(energyCurrent);
    }

    public void overlayAction(final int healthMax,
                              final int healthCurrent,
                              final int energyMax,
                              final int energyCurrent)
    {
        makeView(R.layout.overlay_action);

        ImageButton btnItems = (ImageButton) findViewById(R.id.btnItems);
        btnItems.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnItems");
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

        setupHealthAndEnergyBars(healthMax, energyMax);

        setHealth(healthCurrent);
        setEnergy(energyCurrent);
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
            layoutParams.setMargins(80, 80, 80, 160);
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

    public void overlaySelect(ArrayList<Pair<String, String>> options, boolean left, boolean bottom)
    {
        if (bottom)
        {
            makeView(R.layout.overlay_select);
        }
        else
        {
            makeView(R.layout.overlay_select_top);
        }

        setupHealthAndEnergyBars(healthMax, energyMax);

        setHealth(lastHealth);
        setEnergy(lastEnergy);

        LinearLayout      lloParent  = (LinearLayout) findViewById(R.id.lloParent);
        LinearLayout      lloOptions = (LinearLayout) findViewById(R.id.lloOptions);
        ArrayList<String> names      = new ArrayList<>();

        RelativeLayout.LayoutParams parentLayoutParams =
            new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                            ViewGroup.LayoutParams.WRAP_CONTENT);

        LinearLayout.LayoutParams optionsLayoutParams =
            new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                          LayoutParams.WRAP_CONTENT);

        if (left)
        {
            parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START);
            if (bottom)
            {
                parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
            else
            {
                parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            lloParent.setLayoutParams(parentLayoutParams);
        }
        else
        {
            parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
            if (bottom)
            {
                parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
            }
            else
            {
                parentLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
            }
            lloParent.setLayoutParams(parentLayoutParams);
            lloParent.setGravity(Gravity.END);
            optionsLayoutParams.gravity = Gravity.END;
        }

        lloParent.setLayoutParams(parentLayoutParams);

        for (final Pair<String, String> option : options)
        {
            Button btnTarget = new Button(getContext());
            String name      = getName(option.first, 1, names);
            names.add(name);
            optionsLayoutParams.setMargins(0, 20, 0, 20);
            btnTarget.setLayoutParams(optionsLayoutParams);
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

    private void setupHealthAndEnergyBars(final int healthMax, final int energyMax)
    {
        this.healthMax = healthMax;
        this.energyMax = energyMax;

        if (healthMax > energyMax)
        {
            healthBarMaxLength = getResources().getDimension(R.dimen.bar_length_max);
            energyBarMaxLength = getResources().getDimension(R.dimen.bar_length_min) +
                                 ((getResources().getDimension(R.dimen.bar_length_max) -
                                   getResources().getDimension(R.dimen.bar_length_min)) *
                                  ((float) energyMax / (float) healthMax));
        }
        else
        {
            energyBarMaxLength = getResources().getDimension(R.dimen.bar_length_max);
            healthBarMaxLength = getResources().getDimension(R.dimen.bar_length_min) +
                                 ((getResources().getDimension(R.dimen.bar_length_max) -
                                   getResources().getDimension(R.dimen.bar_length_min)) *
                                  ((float) healthMax / (float) energyMax));
        }

        RelativeLayout.LayoutParams healthBarBackLayoutParams =
            new RelativeLayout.LayoutParams((int) healthBarMaxLength,
                                            (int) getResources().getDimension(R.dimen.bar_height));

        healthBarBackLayoutParams.topMargin =
            (int) getResources().getDimension(R.dimen.bar_back_offset);
        healthBarBackLayoutParams.leftMargin =
            (int) getResources().getDimension(R.dimen.bar_back_offset);

        healthBarBackLayoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.imgHealth);

        ImageView imgHealthBack = (ImageView) findViewById(R.id.imgHealthBack);
        imgHealthBack.setLayoutParams(healthBarBackLayoutParams);
        imgHealthBack.requestLayout();

        RelativeLayout.LayoutParams energyBarBackLayoutParams =
            new RelativeLayout.LayoutParams((int) energyBarMaxLength,
                                            (int) getResources().getDimension(R.dimen.bar_height));

        energyBarBackLayoutParams.topMargin =
            (int) getResources().getDimension(R.dimen.bar_back_offset);
        energyBarBackLayoutParams.leftMargin =
            (int) getResources().getDimension(R.dimen.bar_back_offset);

        energyBarBackLayoutParams.addRule(RelativeLayout.ALIGN_TOP, R.id.imgEnergy);

        ImageView imgEnergyBack = (ImageView) findViewById(R.id.imgEnergyBack);
        imgEnergyBack.setLayoutParams(energyBarBackLayoutParams);
        imgEnergyBack.requestLayout();
    }

    public void setHealth(final int healthCurrent)
    {
        lastHealth = healthCurrent;

        ImageView imgHealth = (ImageView) findViewById(R.id.imgHealth);
        TextView  lblHealth = (TextView) findViewById(R.id.lblHealth);

        if (imgHealth != null && lblHealth != null)
        {
            float healthBarLength = getResources().getDimension(R.dimen.bar_length_min);

            Logger.logI("health bar min length is <" + healthBarLength + ">");

            healthBarLength += ((float) healthCurrent / (float) healthMax) *
                               (healthBarMaxLength -
                                getResources().getDimension(R.dimen.bar_length_min));

            Logger.logI("health bar final length is is <" + healthBarLength + ">");

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) healthBarLength,
                (int) getResources().getDimension(R.dimen.bar_height));

            layoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.bar_margin);

            imgHealth.setLayoutParams(layoutParams);

            imgHealth.requestLayout();
            lblHealth.setText("" + healthCurrent);
        }
    }

    public void setEnergy(final int energyCurrent)
    {
        lastEnergy = energyCurrent;

        ImageView imgEnergy = (ImageView) findViewById(R.id.imgEnergy);
        TextView  lblEnergy = (TextView) findViewById(R.id.lblEnergy);

        if (imgEnergy != null && lblEnergy != null)
        {
            float energyBarLength = getResources().getDimension(R.dimen.bar_length_min);

            Logger.logI("energy bar min length is <" + energyBarLength + ">");

            energyBarLength += ((float) energyCurrent / (float) energyMax) *
                               (energyBarMaxLength -
                                getResources().getDimension(R.dimen.bar_length_min));

            Logger.logI("energy bar final length is <" + energyBarLength + ">");

            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                (int) energyBarLength,
                (int) getResources().getDimension(R.dimen.bar_height));

            layoutParams.addRule(RelativeLayout.BELOW, R.id.imgHealth);

            imgEnergy.setLayoutParams(layoutParams);

            imgEnergy.requestLayout();
            lblEnergy.setText("" + energyCurrent);
        }
    }

    public void overlayWinCondition()
    {
        makeView(R.layout.overlay_win_condition);
    }

    public void overlayLoseCondition()
    {
        makeView(R.layout.overlay_lose_condition);
    }
}
