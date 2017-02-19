package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.semaphore_soft.apps.cypher.R;
import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.Item;
import com.semaphore_soft.apps.cypher.game.Special;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by Scorple on 2/17/2017.
 */

public class UIPortalOverlay extends UIBase
{
    private Hashtable<Integer, Actor>   enemyTargets;
    private Hashtable<Integer, Actor>   playerTargets;
    private Hashtable<Integer, Special> specials;
    private Hashtable<Integer, Item>    items;

    public enum E_SELECT_MODE
    {
        NONE,
        ATTACK_TARGET,
        SPECIAL,
        SPECIAL_TARGET_PLAYER,
        SPECIAL_TARGET_ENEMY,
        ITEM
    }

    private E_SELECT_MODE selectMode = E_SELECT_MODE.NONE;

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

    public void overlayWaitingForHost()
    {

    }

    public void overlayWaitingForTurn()
    {

    }

    public void overlayAction()
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

        Button btnOpenDoor = (Button) findViewById(R.id.btnOpenDoor);
        btnOpenDoor.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnOpenDoor");
            }
        });

        Button btnAttack = (Button) findViewById(R.id.btnAttack);
        btnAttack.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnAttack");
            }
        });

        Button btnDefend = (Button) findViewById(R.id.btnDefend);
        btnDefend.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnDefend");
            }
        });

        Button btnSpecial = (Button) findViewById(R.id.btnSpecial);
        btnSpecial.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnSpecial");
            }
        });
    }

    public void setSelectMode(E_SELECT_MODE selectMode)
    {
        this.selectMode = selectMode;
    }

    public void setEnemyTargets(Hashtable<Integer, Actor> enemyTargets)
    {
        this.enemyTargets = new Hashtable<>();
        this.enemyTargets = enemyTargets;
    }

    public void overlayEnemyTargetSelect()
    {
        overlayEnemyTargetSelect(-1);
    }

    public void overlayEnemyTargetSelect(final int mod)
    {
        makeView(R.layout.overlay_select);
        LinearLayout      lloOptions = (LinearLayout) findViewById(R.id.lloOptions);
        ArrayList<String> names      = new ArrayList<>();

        for (final int id : enemyTargets.keySet())
        {
            Button btnTarget = new Button(getContext());
            String name      = getName(enemyTargets.get(id).getName(), 1, names);
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
                    switch (selectMode)
                    {
                        case ATTACK_TARGET:
                            notifyListener("cmd_attack:" + id);
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                        case SPECIAL_TARGET_ENEMY:
                            notifyListener("cmd_special:" + mod + ";target:" + id);
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                        default:
                            Toast.makeText(getContext(),
                                           "enemy target select error",
                                           Toast.LENGTH_SHORT).show();
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                    }
                    selectMode = E_SELECT_MODE.NONE;
                    overlayAction();
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
                overlayAction();
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

    public void setPlayerTargets(Hashtable<Integer, Actor> playerTargets)
    {
        this.playerTargets = new Hashtable<>();
        this.playerTargets = playerTargets;
    }

    public void overlayPlayerTargetSelect()
    {
        overlayPlayerTargetSelect(-1);
    }

    public void overlayPlayerTargetSelect(final int mod)
    {
        makeView(R.layout.overlay_select);
        LinearLayout lloOptions = (LinearLayout) findViewById(R.id.lloOptions);

        for (final int id : playerTargets.keySet())
        {
            Button btnTarget = new Button(getContext());
            btnTarget.setText(playerTargets.get(id).getName());
            btnTarget.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (selectMode)
                    {
                        case SPECIAL_TARGET_PLAYER:
                            notifyListener("cmd_special:" + mod + ";target:" + id);
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                        default:
                            Toast.makeText(getContext(),
                                           "player target select error",
                                           Toast.LENGTH_SHORT).show();
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                    }
                }
            });
            lloOptions.addView(btnTarget);
            System.out.println("added special option: " + playerTargets.get(id).getName());
        }

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                overlayAction();
            }
        });
    }

    public void setSpecials(Hashtable<Integer, Special> specials)
    {
        this.specials = new Hashtable<>();
        this.specials = specials;
    }

    public void overlaySpecialSelect()
    {
        makeView(R.layout.overlay_select);
        LinearLayout lloOptions = (LinearLayout) findViewById(R.id.lloOptions);

        for (final int id : specials.keySet())
        {
            Button btnSpecial = new Button(getContext());
            LinearLayout.LayoutParams layoutParams =
                new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                              LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(160, 160, 160, 160);
            btnSpecial.setLayoutParams(layoutParams);
            btnSpecial.setText(specials.get(id).getName());
            btnSpecial.setOnClickListener(new OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    switch (specials.get(id).getTargetingType())
                    {
                        case SINGLE_PLAYER:
                            selectMode = E_SELECT_MODE.SPECIAL_TARGET_PLAYER;
                            overlayPlayerTargetSelect(id);
                            break;
                        case SINGLE_NON_PLAYER:
                            selectMode = E_SELECT_MODE.SPECIAL_TARGET_ENEMY;
                            overlayEnemyTargetSelect(id);
                            break;
                        case AOE_PLAYER:
                            notifyListener("cmd_special:" + id);
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                        case AOE_NON_PLAYER:
                            notifyListener("cmd_special:" + id);
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                        default:
                            Toast.makeText(getContext(), "special select error", Toast.LENGTH_SHORT)
                                 .show();
                            selectMode = E_SELECT_MODE.NONE;
                            overlayAction();
                            break;
                    }
                }
            });
            lloOptions.addView(btnSpecial);
        }

        Button btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                overlayAction();
            }
        });
    }

    public void setItems(Hashtable<Integer, Item> items)
    {
        items = new Hashtable<>();
        this.items = items;
    }
}
