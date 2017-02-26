package com.semaphore_soft.apps.cypher.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.semaphore_soft.apps.cypher.R;

/**
 * Created by Scorple on 2/19/2017.
 * UI class for {@code CharacterSelectActivity}.
 * @see com.semaphore_soft.apps.cypher.CharacterSelectActivity
 */

public class UICharacterSelect extends UIBase
{
    private TextView txtStatus;
    private Button   btnStart;

    private ImageView imgKnight;
    private ImageView imgSoldier;
    private ImageView imgRanger;
    private ImageView imgWizard;

    private ImageButton imgBtnKnight;
    private ImageButton imgBtnSoldier;
    private ImageButton imgBtnRanger;
    private ImageButton imgBtnWizard;

    private String selected = "";

    public UICharacterSelect(Context context)
    {
        super(context);
    }

    public UICharacterSelect(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public UICharacterSelect(Context context, AttributeSet attrs, int defStyleAttr)
    {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void createUI()
    {
        makeView(R.layout.character_select);

        txtStatus = (TextView) findViewById(R.id.txtStatus);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                notifyListener("cmd_btnStart");
            }
        });

        imgKnight = (ImageView) findViewById(R.id.imgKnight);
        imgSoldier = (ImageView) findViewById(R.id.imgSoldier);
        imgRanger = (ImageView) findViewById(R.id.imgRanger);
        imgWizard = (ImageView) findViewById(R.id.imgWizard);

        imgBtnKnight = (ImageButton) findViewById(R.id.imgBtnKnight);
        imgBtnKnight.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSelection("knight");
            }
        });

        imgBtnSoldier = (ImageButton) findViewById(R.id.imgBtnSoldier);
        imgBtnSoldier.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSelection("soldier");
            }
        });

        imgBtnRanger = (ImageButton) findViewById(R.id.imgBtnRanger);
        imgBtnRanger.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSelection("ranger");
            }
        });

        imgBtnWizard = (ImageButton) findViewById(R.id.imgBtnWizard);
        imgBtnWizard.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setSelection("wizard");
            }
        });
    }

    /**
     * Set ready status of all players.
     *
     * @param status status of players
     */
    public void setStatus(String status)
    {
        txtStatus.setText(status);
    }

    /**
     * Set if game can be continued by the host.
     * @param enabled whether or not button is enabled
     */
    public void setStartEnabled(boolean enabled)
    {
        btnStart.setEnabled(enabled);
    }

    /**
     * Update UI to show player's selection.
     * @param selection Player's selection
     */
    private void setSelection(String selection)
    {
        if (selected.equals(selection))
        {
            clearSelection();
        }
        else
        {
            selected = selection;

            switch (selection)
            {
                case "knight":
                    imgKnight.setVisibility(VISIBLE);
                    imgSoldier.setVisibility(GONE);
                    imgRanger.setVisibility(GONE);
                    imgWizard.setVisibility(GONE);
                    break;
                case "soldier":
                    imgKnight.setVisibility(GONE);
                    imgSoldier.setVisibility(VISIBLE);
                    imgRanger.setVisibility(GONE);
                    imgWizard.setVisibility(GONE);
                    break;
                case "ranger":
                    imgKnight.setVisibility(GONE);
                    imgSoldier.setVisibility(GONE);
                    imgRanger.setVisibility(VISIBLE);
                    imgWizard.setVisibility(GONE);
                    break;
                case "wizard":
                    imgKnight.setVisibility(GONE);
                    imgSoldier.setVisibility(GONE);
                    imgRanger.setVisibility(GONE);
                    imgWizard.setVisibility(VISIBLE);
                    break;
            }

            notifyListener(selection);
        }
    }

    /**
     * Update UI to clear player's selection
     */
    public void clearSelection()
    {
        selected = "";
        imgKnight.setVisibility(GONE);
        imgSoldier.setVisibility(GONE);
        imgRanger.setVisibility(GONE);
        imgWizard.setVisibility(GONE);
        notifyListener("clear");
    }

    /**
     * Set whether or not a character can be chosen by a player
     * @param selection Character to enable or disable
     * @param enabled Whether to enable or disable selection
     */
    public void setButtonEnabled(String selection, boolean enabled)
    {
        switch (selection)
        {
            case "knight":
                imgBtnKnight.setEnabled(enabled);
                if (enabled)
                {
                    imgBtnKnight.setImageAlpha(255);
                }
                else
                {
                    imgBtnKnight.setImageAlpha(80);
                }
                break;
            case "soldier":
                imgBtnSoldier.setEnabled(enabled);
                if (enabled)
                {
                    imgBtnSoldier.setImageAlpha(255);
                }
                else
                {
                    imgBtnSoldier.setImageAlpha(80);
                }
                break;
            case "ranger":
                imgBtnRanger.setEnabled(enabled);
                if (enabled)
                {
                    imgBtnRanger.setImageAlpha(255);
                }
                else
                {
                    imgBtnRanger.setImageAlpha(80);
                }
                break;
            case "wizard":
                imgBtnWizard.setEnabled(enabled);
                if (enabled)
                {
                    imgBtnWizard.setImageAlpha(255);
                }
                else
                {
                    imgBtnWizard.setImageAlpha(80);
                }
                break;
        }
    }
}
