package com.semaphore_soft.apps.cypher.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.semaphore_soft.apps.cypher.game.Actor;
import com.semaphore_soft.apps.cypher.game.Effect;
import com.semaphore_soft.apps.cypher.game.Item;
import com.semaphore_soft.apps.cypher.game.ItemConsumable;
import com.semaphore_soft.apps.cypher.game.ItemDurable;
import com.semaphore_soft.apps.cypher.game.Special;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import static com.semaphore_soft.apps.cypher.utils.CollectionManager.getNextID;

/**
 * Created by Scorple on 2/5/2017.
 */

public class GameStatLoader
{
    public static ArrayList<String> getList(Context context, String listName)
    {
        try
        {
            XmlPullParserFactory factory    = XmlPullParserFactory.newInstance();
            XmlPullParser        listParser = factory.newPullParser();

            AssetManager assetManager    = context.getAssets();
            InputStream  listInputStream = assetManager.open("lists.xml");
            listParser.setInput(listInputStream, null);

            boolean foundList    = false;
            boolean finishedList = false;

            System.out.println("loading list");
            System.out.println("list name is: " + listName);

            ArrayList<String> res = new ArrayList<>();

            int event = listParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedList)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (listName.equals(listParser.getName()))
                        {
                            foundList = true;
                            System.out.println("found list");
                        }
                        else if (foundList && listParser.getName().equals("name"))
                        {
                            listParser.next();
                            res.add(listParser.getText());
                            System.out.println("added list member: " + listParser.getText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (listName.equals(listParser.getName()))
                        {
                            finishedList = true;
                            System.out.println("finished list");
                        }
                        break;
                    default:
                        break;
                }
                event = listParser.next();
            }
            System.out.println("finished loading list");
            return res;
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public static void loadActorStats(Actor actor,
                                      int characterID,
                                      ConcurrentHashMap<Integer, Special> specials,
                                      Context context)
    {
        String actorName;

        switch (characterID)
        {
            case 0:
                actorName = "knight";
                break;
            case 1:
                actorName = "soldier";
                break;
            case 2:
                actorName = "ranger";
                break;
            case 3:
                actorName = "wizard";
                break;
            default:
                return;
        }

        loadActorStats(actor, actorName, specials, context);
    }

    public static void loadActorStats(Actor actor,
                                      String actorName,
                                      ConcurrentHashMap<Integer, Special> specials,
                                      Context context)
    {
        try
        {
            XmlPullParserFactory factory     = XmlPullParserFactory.newInstance();
            XmlPullParser        actorParser = factory.newPullParser();

            AssetManager assetManager     = context.getAssets();
            InputStream  actorInputStream = assetManager.open("actors.xml");
            actorParser.setInput(actorInputStream, null);

            boolean foundActor       = false;
            boolean finishedActor    = false;
            boolean foundSpecials    = false;
            boolean finishedSpecials = false;

            ArrayList<String> actorSpecials = new ArrayList<>();

            System.out.println("loading actor");
            System.out.println("actor name is: " + actorName);

            int event = actorParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedActor)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (actorName.equals(actorParser.getName()))
                        {
                            foundActor = true;
                            System.out.println("found actor");
                        }
                        else if (foundActor)
                        {
                            if (actorParser.getName().equals("specials"))
                            {
                                foundSpecials = true;
                                System.out.println("found actor specials");
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                actorParser.next();
                                actorSpecials.add(actorParser.getText());
                                System.out.println("found special: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("healthMaximum"))
                            {
                                actorParser.next();
                                actor.setHealthMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setHealthCurrent(actor.getHealthMaximum());
                                System.out.println(
                                    "found health maximum: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("attackRating"))
                            {
                                actorParser.next();
                                actor.setAttackRating(Integer.parseInt(actorParser.getText()));
                                System.out.println("found attack rating: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("specialMaximum"))
                            {
                                actorParser.next();
                                actor.setSpecialMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setSpecialCurrent(actor.getSpecialMaximum());
                                System.out.println(
                                    "found special maximum: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("specialRating"))
                            {
                                actorParser.next();
                                actor.setSpecialRating(Integer.parseInt(actorParser.getText()));
                                System.out.println(
                                    "found special rating: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("defenceRating"))
                            {
                                actorParser.next();
                                actor.setDefenceRating(Integer.parseInt(actorParser.getText()));
                                System.out.println(
                                    "found defence rating: " + actorParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundActor)
                        {
                            if (actorName.equals(actorParser.getName()))
                            {
                                finishedActor = true;
                                System.out.println("finished actor");
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                if (actorParser.getName().equals("specials"))
                                {
                                    finishedSpecials = true;
                                    System.out.println("finished actor specials");
                                }
                            }
                        }
                        break;
                }
                event = actorParser.next();
            }

            if (!foundActor)
            {
                System.out.println("failed to load actor: actor not found");
                return;
            }

            System.out.println("loading actor specials");
            for (String specialName : actorSpecials)
            {
                Special special = loadSpecialStats(specialName, specials, context);
                if (special != null)
                {
                    actor.addSpecial(special);
                }
            }
            System.out.println("finished loading actor specials");
            System.out.println("finished loading actor");
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public static Special loadSpecialStats(String specialName,
                                           ConcurrentHashMap<Integer, Special> specials,
                                           Context context)
    {
        System.out.println("loading special");
        System.out.println("special name is: " + specialName);

        for (int specialId : specials.keySet())
        {
            Special special = specials.get(specialId);
            if (specialName.equals(special.getName()))
            {
                System.out.println("special already exists in special table");
                System.out.println("finished loading special");
                return special;
            }
        }

        try
        {
            XmlPullParserFactory factory      = XmlPullParserFactory.newInstance();
            AssetManager         assetManager = context.getAssets();

            XmlPullParser specialParser      = factory.newPullParser();
            InputStream   specialInputStream = assetManager.open("specials.xml");
            specialParser.setInput(specialInputStream, null);

            boolean foundSpecial    = false;
            boolean finishedSpecial = false;
            boolean foundEffects    = false;
            boolean finishedEffects = false;

            int               cost          = -1;
            int               duration      = -1;
            String            targetingType = "";
            ArrayList<String> effects       = new ArrayList<>();

            int event = specialParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedSpecial)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (specialName.equals(specialParser.getName()))
                        {
                            foundSpecial = true;
                            System.out.println("found special");
                        }
                        else if (foundSpecial)
                        {
                            if (specialParser.getName().equals("effects"))
                            {
                                foundEffects = true;
                                System.out.println("found special effects");
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                specialParser.next();
                                effects.add(specialParser.getText());
                                System.out.println(
                                    "found effect: " + specialParser.getText());
                            }
                            else if (specialParser.getName().equals("cost"))
                            {
                                specialParser.next();
                                cost = Integer.parseInt(specialParser.getText());
                                System.out.println(
                                    "found cost: " + specialParser.getText());
                            }
                            else if (specialParser.getName().equals("duration"))
                            {
                                specialParser.next();
                                duration = Integer.parseInt(specialParser.getText());
                                System.out.println(
                                    "found duration: " + specialParser.getText());
                            }
                            else if (specialParser.getName().equals("targetingType"))
                            {
                                specialParser.next();
                                targetingType = specialParser.getText();
                                System.out.println(
                                    "found targeting type: " + specialParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundSpecial)
                        {
                            if (specialName.equals(specialParser.getName()))
                            {
                                finishedSpecial = true;
                                System.out.println("finished special");
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                if (specialParser.getName().equals("effects"))
                                {
                                    finishedEffects = true;
                                    System.out.println("finished special effects");
                                }
                            }
                        }
                        break;
                }
                event = specialParser.next();
            }

            if (!foundSpecial)
            {
                System.out.println("failed to load special: special not found");
                return null;
            }

            Special.E_TARGETING_TYPE specialTargetingType;

            switch (targetingType)
            {
                case "SINGLE_PLAYER":
                    specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_PLAYER;
                    break;
                case "SINGLE_NON_PLAYER":
                    specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
                    break;
                case "AOE_PLAYER":
                    specialTargetingType = Special.E_TARGETING_TYPE.AOE_PLAYER;
                    break;
                case "AOE_NON_PLAYER":
                    specialTargetingType = Special.E_TARGETING_TYPE.AOE_NON_PLAYER;
                    break;
                default:
                    specialTargetingType = Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
                    break;
            }

            if (finishedSpecial)
            {
                Special special = new Special(getNextID(specials),
                                              specialName,
                                              cost,
                                              duration,
                                              specialTargetingType);

                for (String effect : effects)
                {
                    switch (effect)
                    {
                        case "HEAL":
                            special.addEffect(Effect.E_EFFECT.HEAL);
                            break;
                        case "ATTACK":
                            special.addEffect(Effect.E_EFFECT.ATTACK);
                            break;
                        case "HEALTH_MAXIMUM_UP":
                            special.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_UP);
                            break;
                        case "HEALTH_MAXIMUM_DOWN":
                            special.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_DOWN);
                            break;
                        case "ATTACK_RATING_UP":
                            special.addEffect(Effect.E_EFFECT.ATTACK_RATING_UP);
                            break;
                        case "ATTACK_RATING_DOWN":
                            special.addEffect(Effect.E_EFFECT.ATTACK_RATING_DOWN);
                            break;
                        case "SPECIAL_MAXIMUM_UP":
                            special.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_UP);
                            break;
                        case "SPECIAL_MAXIMUM_DOWN":
                            special.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_DOWN);
                            break;
                        case "SPECIAL_RATING_UP":
                            special.addEffect(Effect.E_EFFECT.SPECIAL_RATING_UP);
                            break;
                        case "SPECIAL_RATING_DOWN":
                            special.addEffect(Effect.E_EFFECT.SPECIAL_RATING_DOWN);
                            break;
                        case "DEFENCE_RATING_UP":
                            special.addEffect(Effect.E_EFFECT.DEFENCE_RATING_UP);
                            break;
                        case "DEFENCE_RATING_DOWN":
                            special.addEffect(Effect.E_EFFECT.DEFENCE_RATING_DOWN);
                            break;
                        default:
                            Special linkedSpecial = loadSpecialStats(effect, specials, context);
                            if (linkedSpecial != null)
                            {
                                for (Effect.E_EFFECT specialEffect : linkedSpecial.getEffects())
                                {
                                    special.addEffect(specialEffect);
                                }
                            }
                            else
                            {
                                System.out.println("failed to add special effect: " + effect);
                            }
                    }
                }

                specials.put(special.getId(), special);
                System.out.println("special added to specials table");
                System.out.println("finished loading special");
                return special;
            }
        }
        catch (XmlPullParserException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return null;
    }

    public static Item loadItemStats(String itemName,
                                     ConcurrentHashMap<Integer, Item> items,
                                     ConcurrentHashMap<Integer, Special> specials,
                                     Context context)
    {
        System.out.println("loading item");
        System.out.println("item name is: " + itemName);

        for (Integer itemId : items.keySet())
        {
            Item item = items.get(itemId);
            if (itemName.equals(item.getName()))
            {
                System.out.println("item already exists in item table");
                System.out.println("borrowing item stats");
                Item newItem = null;
                if (item instanceof ItemConsumable)
                {
                    newItem = new ItemConsumable(getNextID(items),
                                                 itemName,
                                                 item.getEffectRating(),
                                                 ((ItemConsumable) item).getDuration(),
                                                 ((ItemConsumable) item).getTargetingType());
                }
                else if (item instanceof ItemDurable)
                {
                    newItem = new ItemDurable(getNextID(items),
                                              itemName,
                                              item.getEffectRating());
                }
                else
                {
                    System.out.println("error: bad item in table, removing");
                    items.remove(item.getID());
                }
                if (newItem != null)
                {
                    items.put(newItem.getID(), newItem);
                    System.out.println("finished loading item");
                    return newItem;
                }
            }
        }

        try
        {
            XmlPullParserFactory factory      = XmlPullParserFactory.newInstance();
            AssetManager         assetManager = context.getAssets();

            XmlPullParser itemParser      = factory.newPullParser();
            InputStream   itemInputStream = assetManager.open("items.xml");
            itemParser.setInput(itemInputStream, null);

            boolean foundItem       = false;
            boolean finishedItem    = false;
            boolean foundEffects    = false;
            boolean finishedEffects = false;

            int               effectRating  = 0;
            int               duration      = 0;
            String            targetingType = null;
            ArrayList<String> effects       = new ArrayList<>();

            int event = itemParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedItem)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (itemName.equals(itemParser.getName()))
                        {
                            foundItem = true;
                            System.out.println("found item");
                        }
                        else if (foundItem)
                        {
                            if (itemParser.getName().equals("effects"))
                            {
                                foundEffects = true;
                                System.out.println("found item effects");
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                itemParser.next();
                                effects.add(itemParser.getText());
                                System.out.println("found effect: " + itemParser.getText());
                            }
                            else if (itemParser.getName().equals("effectRating"))
                            {
                                itemParser.next();
                                effectRating = Integer.parseInt(itemParser.getText());
                                System.out.println("found effect rating: " + itemParser.getText());
                            }
                            else if (itemParser.getName().equals("duration"))
                            {
                                itemParser.next();
                                duration = Integer.parseInt(itemParser.getText());
                                System.out.println("found duration: " + itemParser.getText());
                            }
                            else if (itemParser.getName().equals("targetingType"))
                            {
                                itemParser.next();
                                targetingType = itemParser.getText();
                                System.out.println(
                                    "found targeting type: " + itemParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundItem)
                        {
                            if (itemName.equals(itemParser.getName()))
                            {
                                finishedItem = true;
                                System.out.println("finished item");
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                if (itemParser.getName().equals("effect"))
                                {
                                    finishedEffects = true;
                                    System.out.println("finished item effects");
                                }
                            }
                        }
                        break;
                }
                event = itemParser.next();
            }

            if (!foundItem)
            {
                System.out.println("failed to load item: item not found");
                return null;
            }

            Item item;

            if (targetingType != null)
            {
                ItemConsumable.E_TARGETING_TYPE itemTargetingType;

                switch (targetingType)
                {
                    case "SINGLE_PLAYER":
                        itemTargetingType = ItemConsumable.E_TARGETING_TYPE.SINGLE_PLAYER;
                        break;
                    case "SINGLE_NON_PLAYER":
                        itemTargetingType = ItemConsumable.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
                        break;
                    case "AOE_PLAYER":
                        itemTargetingType = ItemConsumable.E_TARGETING_TYPE.AOE_PLAYER;
                        break;
                    case "AOE_NON_PLAYER":
                        itemTargetingType = ItemConsumable.E_TARGETING_TYPE.AOE_NON_PLAYER;
                        break;
                    default:
                        itemTargetingType = ItemConsumable.E_TARGETING_TYPE.SINGLE_PLAYER;
                        break;
                }

                item = new ItemConsumable(getNextID(items),
                                          itemName,
                                          effectRating,
                                          duration,
                                          itemTargetingType);
                System.out.println("created new consumable item");
            }
            else
            {
                item = new ItemDurable(getNextID(items), itemName, effectRating);
                System.out.println("created new durable item");
            }

            for (String effect : effects)
            {
                switch (effect)
                {
                    case "HEAL":
                        item.addEffect(Effect.E_EFFECT.HEAL);
                        break;
                    case "ATTACK":
                        item.addEffect(Effect.E_EFFECT.ATTACK);
                        break;
                    case "HEALTH_MAXIMUM_UP":
                        item.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_UP);
                        break;
                    case "HEALTH_MAXIMUM_DOWN":
                        item.addEffect(Effect.E_EFFECT.HEALTH_MAXIMUM_DOWN);
                        break;
                    case "ATTACK_RATING_UP":
                        item.addEffect(Effect.E_EFFECT.ATTACK_RATING_UP);
                        break;
                    case "ATTACK_RATING_DOWN":
                        item.addEffect(Effect.E_EFFECT.ATTACK_RATING_DOWN);
                        break;
                    case "SPECIAL_MAXIMUM_UP":
                        item.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_UP);
                        break;
                    case "SPECIAL_MAXIMUM_DOWN":
                        item.addEffect(Effect.E_EFFECT.SPECIAL_MAXIMUM_DOWN);
                        break;
                    case "SPECIAL_RATING_UP":
                        item.addEffect(Effect.E_EFFECT.SPECIAL_RATING_UP);
                        break;
                    case "SPECIAL_RATING_DOWN":
                        item.addEffect(Effect.E_EFFECT.SPECIAL_RATING_DOWN);
                        break;
                    case "DEFENCE_RATING_UP":
                        item.addEffect(Effect.E_EFFECT.DEFENCE_RATING_UP);
                        break;
                    case "DEFENCE_RATING_DOWN":
                        item.addEffect(Effect.E_EFFECT.DEFENCE_RATING_DOWN);
                        break;
                    default:
                        Special special = loadSpecialStats(effect, specials, context);
                        if (special != null)
                        {
                            for (Effect.E_EFFECT specialEffect : special.getEffects())
                            {
                                item.addEffect(specialEffect);
                            }
                        }
                        else
                        {
                            System.out.println("failed to add item effect: " + effect);
                        }
                }
            }

            items.put(item.getID(), item);
            System.out.println("item added to items table");
            System.out.println("finished loading item");
            return item;
        }
        catch (XmlPullParserException e)
        {

        }
        catch (IOException e)
        {

        }

        return null;
    }
}