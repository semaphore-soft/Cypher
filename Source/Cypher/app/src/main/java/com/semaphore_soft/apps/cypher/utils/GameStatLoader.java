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
import java.util.HashMap;
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

            Logger.logI("loading list <" + listName + ">");

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
                            Logger.logI("found list", 1);
                        }
                        else if (foundList && listParser.getName().equals("name"))
                        {
                            listParser.next();
                            res.add(listParser.getText());
                            Logger.logI("added list member: " + listParser.getText(), 1);
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (listName.equals(listParser.getName()))
                        {
                            finishedList = true;
                            Logger.logI("finished list", 1);
                        }
                        break;
                    default:
                        break;
                }
                event = listParser.next();
            }

            if (foundList)
            {
                Logger.logI("finished loading list <" + listName + ">");
            }
            else
            {
                Logger.logI("list <" + listName + "> not found");
            }

            return res;
        }
        catch (XmlPullParserException e)
        {
            Logger.logD("error parsing list <" + listName + ">");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Logger.logD("list file error");
            e.printStackTrace();
        }
        return null;
    }

    public static HashMap<String, Integer> getTagsMembers(Context context,
                                                          String file,
                                                          ArrayList<String> tags,
                                                          String memberName)
    {
        try
        {
            XmlPullParserFactory factory    = XmlPullParserFactory.newInstance();
            XmlPullParser        listParser = factory.newPullParser();

            AssetManager assetManager    = context.getAssets();
            InputStream  listInputStream = assetManager.open(file);
            listParser.setInput(listInputStream, null);

            Logger.logI("loading tags with member <" + memberName + ">");

            HashMap<String, Integer> res = new HashMap<>();

            int event = listParser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        for (String tag : tags)
                        {
                            if (tag.equals(listParser.getName()))
                            {
                                int[] member = new int[1];

                                if (getIntMember(listParser, tag, memberName, member))
                                {
                                    res.put(tag, member[0]);

                                    Logger.logI(
                                        "found tag <" + tag + "> with member <" + memberName +
                                        ">:<" + member[0] + ">");
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }

                if (tags.size() == res.size())
                {
                    return res;
                }

                event = listParser.next();
            }

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

    private static boolean getIntMember(final XmlPullParser parser,
                                        final String parentName,
                                        final String memberName,
                                        final int[] member)
    {
        Logger.logD("enter trace");

        try
        {
            int event = parser.getEventType();

            while (event != XmlPullParser.END_DOCUMENT)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (memberName.equals(parser.getName()))
                        {
                            parser.next();
                            member[0] = Integer.parseInt(parser.getText());

                            Logger.logD("exit trace");

                            return true;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (parentName.equals(parser.getName()))
                        {
                            Logger.logD("exit trace");

                            return false;
                        }
                        break;
                    default:
                        break;
                }

                event = parser.next();
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

        Logger.logD("exit trace");

        return false;
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
            String  actorBehavior    = null;

            ArrayList<String> actorSpecials = new ArrayList<>();

            Logger.logI("loading actor <" + actorName + ">");

            int event = actorParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedActor)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (actorName.equals(actorParser.getName()))
                        {
                            foundActor = true;
                            Logger.logI("found actor", 1);
                        }
                        else if (foundActor)
                        {
                            if (actorParser.getName().equals("specials"))
                            {
                                foundSpecials = true;
                                Logger.logI("found actor specials", 1);
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                actorParser.next();
                                actorSpecials.add(actorParser.getText());
                                Logger.logI("found special: " + actorParser.getText(), 1);
                            }
                            else if (actorParser.getName().equals("displayName"))
                            {
                                actorParser.next();
                                actor.setDisplayName(actorParser.getText());
                                Logger.logI("found actor display name: " + actorParser.getText(),
                                            1);
                            }
                            else if (actorParser.getName().equals("healthMaximum"))
                            {
                                actorParser.next();
                                actor.setHealthMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setHealthCurrent(actor.getHealthMaximum());
                                Logger.logI(
                                    "found health maximum: " + actorParser.getText(), 1);
                            }
                            else if (actorParser.getName().equals("attackRating"))
                            {
                                actorParser.next();
                                actor.setAttackRating(Integer.parseInt(actorParser.getText()));
                                Logger.logI("found attack rating: " + actorParser.getText());
                            }
                            else if (actorParser.getName().equals("specialMaximum"))
                            {
                                actorParser.next();
                                actor.setSpecialMaximum(Integer.parseInt(actorParser.getText()));
                                actor.setSpecialCurrent(actor.getSpecialMaximum());
                                Logger.logI(
                                    "found special maximum: " + actorParser.getText(), 1);
                            }
                            else if (actorParser.getName().equals("specialRating"))
                            {
                                actorParser.next();
                                actor.setSpecialRating(Integer.parseInt(actorParser.getText()));
                                Logger.logI(
                                    "found special rating: " + actorParser.getText(), 1);
                            }
                            else if (actorParser.getName().equals("defenceRating"))
                            {
                                actorParser.next();
                                actor.setDefenceRating(Integer.parseInt(actorParser.getText()));
                                Logger.logI(
                                    "found defence rating: " + actorParser.getText(), 1);
                            }
                            else if (actorParser.getName().equals("behavior"))
                            {
                                actorParser.next();
                                actorBehavior = actorParser.getText();
                                Logger.logI("found behavior: " + actorBehavior, 1);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundActor)
                        {
                            if (actorName.equals(actorParser.getName()))
                            {
                                finishedActor = true;
                                Logger.logI("finished actor", 1);
                            }
                            else if (foundSpecials && !finishedSpecials)
                            {
                                if (actorParser.getName().equals("specials"))
                                {
                                    finishedSpecials = true;
                                    Logger.logI("finished actor specials", 1);
                                }
                            }
                        }
                        break;
                }
                event = actorParser.next();
            }

            if (!foundActor)
            {
                Logger.logI("actor <" + actorName + "> not found");
                return;
            }

            Logger.logI("loading actor specials", 1);
            for (String specialName : actorSpecials)
            {
                Special special = loadSpecialStats(specialName, specials, context);
                if (special != null)
                {
                    actor.addSpecial(special);
                }
            }
            Logger.logI("finished loading actor specials", 1);

            if (actorBehavior != null)
            {
                Logger.logI("loading actor behavior", 1);
                loadBehavior(actor, actorBehavior, context);
                Logger.logI("finished loading actor behavior", 1);
            }
            else
            {
                Logger.logI("actor behavior not defined", 1);
            }

            Logger.logI("finished loading actor <" + actorName + ">");
        }
        catch (XmlPullParserException e)
        {
            Logger.logD("error parsing actor <" + actorName + ">");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Logger.logD("actors file error");
            e.printStackTrace();
        }
    }

    public static HashMap<String, Integer> getItemPrevalence(final Context context,
                                                             final String actorName)
    {
        HashMap<String, Integer> ret = new HashMap<>();

        XmlPullParserFactory factory = null;
        try
        {
            factory = XmlPullParserFactory.newInstance();
            AssetManager assetManager = context.getAssets();

            XmlPullParser actorItemsParser      = factory.newPullParser();
            InputStream   actorItemsInputStream = assetManager.open("actors.xml");
            actorItemsParser.setInput(actorItemsInputStream, null);

            boolean foundActor    = false;
            boolean foundItems    = false;
            boolean finishedItems = false;
            boolean foundItem     = false;

            int event = actorItemsParser.getEventType();

            String itemName       = "none";
            int    itemPrevalence = 0;

            while (event != XmlPullParser.END_DOCUMENT && !finishedItems)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        //Logger.logD(actorItemsParser.getName());
                        if (!foundActor && actorName.equals(actorItemsParser.getName()))
                        {
                            foundActor = true;
                        }
                        else if (!foundItems && actorItemsParser.getName().equals("items"))
                        {
                            foundItems = true;
                        }
                        else if (!foundItem && actorItemsParser.getName().equals("item"))
                        {
                            foundItem = true;
                        }
                        else if (foundItem)
                        {
                            if (actorItemsParser.getName().equals("name"))
                            {
                                actorItemsParser.next();
                                itemName = actorItemsParser.getText();
                            }
                            else if (actorItemsParser.getName().equals("prevalence"))
                            {
                                actorItemsParser.next();
                                itemPrevalence = Integer.parseInt(actorItemsParser.getText());
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        //Logger.logD(actorItemsParser.getName());
                        if (foundItem && actorItemsParser.getName().equals("item"))
                        {
                            foundItem = false;
                            ret.put(itemName, itemPrevalence);
                        }
                        else if ((foundItems && actorItemsParser.getName().equals("items")) ||
                                 actorItemsParser.getName().equals(actorName))
                        {
                            finishedItems = true;
                        }
                        break;
                    default:
                        break;
                }

                event = actorItemsParser.next();
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

        return ret;
    }

    public static Special loadSpecialStats(String specialName,
                                           ConcurrentHashMap<Integer, Special> specials,
                                           Context context)
    {
        Logger.logI("loading special <" + specialName + ">");

        for (int specialId : specials.keySet())
        {
            Special special = specials.get(specialId);
            if (specialName.equals(special.getName()))
            {
                Logger.logI("special already exists in special table", 1);
                Logger.logI("finished loading special");
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

            int               cost               = 1;
            int               duration           = -1;
            float             scalar             = 1;
            String            targetingType      = "";
            ArrayList<String> effects            = new ArrayList<>();
            String            specialDisplayName = null;

            int event = specialParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedSpecial)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (specialName.equals(specialParser.getName()))
                        {
                            foundSpecial = true;
                            Logger.logI("found special", 1);
                        }
                        else if (foundSpecial)
                        {
                            if (specialParser.getName().equals("effects"))
                            {
                                foundEffects = true;
                                Logger.logI("found special effects", 1);
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                specialParser.next();
                                effects.add(specialParser.getText());
                                Logger.logI(
                                    "found effect: " + specialParser.getText(), 1);
                            }
                            else if (specialParser.getName().equals("name"))
                            {
                                specialParser.next();
                                specialDisplayName = specialParser.getText();
                                Logger.logI("found display name: " + specialParser.getText(), 1);
                            }
                            else if (specialParser.getName().equals("cost"))
                            {
                                specialParser.next();
                                cost = Integer.parseInt(specialParser.getText());
                                Logger.logI(
                                    "found cost: " + specialParser.getText(), 1);
                            }
                            else if (specialParser.getName().equals("duration"))
                            {
                                specialParser.next();
                                duration = Integer.parseInt(specialParser.getText());
                                Logger.logI(
                                    "found duration: " + specialParser.getText(), 1);
                            }
                            else if (specialParser.getName().equals("scalar"))
                            {
                                specialParser.next();
                                scalar = Float.parseFloat(specialParser.getText());
                                Logger.logI(
                                    "found scalar: " + specialParser.getText(), 1);
                            }
                            else if (specialParser.getName().equals("targetingType"))
                            {
                                specialParser.next();
                                targetingType = specialParser.getText();
                                Logger.logI(
                                    "found targeting type: " + specialParser.getText(), 1);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundSpecial)
                        {
                            if (specialName.equals(specialParser.getName()))
                            {
                                finishedSpecial = true;
                                Logger.logI("finished special", 1);
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                if (specialParser.getName().equals("effects"))
                                {
                                    finishedEffects = true;
                                    Logger.logI("finished special effects", 1);
                                }
                            }
                        }
                        break;
                }
                event = specialParser.next();
            }

            if (!foundSpecial)
            {
                Logger.logI("special <" + specialName + "> not found");
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
                                              scalar,
                                              specialTargetingType);

                if (specialDisplayName != null)
                {
                    special.setDisplayName(specialDisplayName);
                }

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
                                Logger.logI("failed to add special effect: " + effect, 1);
                            }
                    }
                }

                specials.put(special.getId(), special);
                Logger.logI("special added to specials table", 1);
                Logger.logI("finished loading special <" + specialName + ">");
                return special;
            }
        }
        catch (XmlPullParserException e)
        {
            Logger.logD("error parsing special <" + specialName + ">");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Logger.logD("specials file error");
            e.printStackTrace();
        }

        return null;
    }

    public static void loadBehavior(Actor actor, String behaviorName, Context context)
    {
        Logger.logI("loading behavior <" + behaviorName + ">");

        try
        {
            XmlPullParserFactory factory      = XmlPullParserFactory.newInstance();
            AssetManager         assetManager = context.getAssets();

            XmlPullParser behaviorParser      = factory.newPullParser();
            InputStream   behaviorInputStream = assetManager.open("behaviors.xml");
            behaviorParser.setInput(behaviorInputStream, null);

            boolean foundBehavior    = false;
            boolean finishedBehavior = false;

            int event = behaviorParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedBehavior)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (behaviorName.equals(behaviorParser.getName()))
                        {
                            foundBehavior = true;
                            Logger.logI("found behavior", 1);
                        }
                        else if (foundBehavior)
                        {
                            if (behaviorParser.getName().equals("attack"))
                            {
                                behaviorParser.next();
                                actor.setAttackTickets(Integer.parseInt(behaviorParser.getText()));
                                Logger.logI("found attack tickets: " + behaviorParser.getText(), 1);
                            }
                            else if (behaviorParser.getName().equals("defend"))
                            {
                                behaviorParser.next();
                                actor.setDefendTickets(Integer.parseInt(behaviorParser.getText()));
                                Logger.logI("found defend tickets: " + behaviorParser.getText(), 1);
                            }
                            else if (behaviorParser.getName().equals("special"))
                            {
                                behaviorParser.next();
                                actor.setSpecialTickets(Integer.parseInt(behaviorParser.getText()));
                                Logger.logI("found special tickets: " + behaviorParser.getText(),
                                            1);
                            }
                            else if (behaviorParser.getName().equals("move"))
                            {
                                behaviorParser.next();
                                actor.setMoveTickets(Integer.parseInt(behaviorParser.getText()));
                                Logger.logI("found move tickets: " + behaviorParser.getText(), 1);
                            }
                            else if (behaviorParser.getName().equals("item"))
                            {
                                behaviorParser.next();
                                actor.setUseItemTickets(Integer.parseInt(behaviorParser.getText()));
                                Logger.logI("found use item tickets: " + behaviorParser.getText(),
                                            1);
                            }
                            else if (behaviorParser.getName().equals("seek"))
                            {
                                behaviorParser.next();
                                actor.setSeeker(behaviorParser.getText().equals("1"));
                                Logger.logI("found seek flag: " + behaviorParser.getText(), 1);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundBehavior)
                        {
                            if (behaviorName.equals(behaviorParser.getName()))
                            {
                                finishedBehavior = true;
                                Logger.logI("finished behavior", 1);
                            }
                        }
                        break;
                }
                event = behaviorParser.next();
            }

            Logger.logI("finished loading behavior <" + behaviorName + ">");
        }
        catch (XmlPullParserException e)
        {
            Logger.logD("error parsing behavior <" + behaviorName + ">");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Logger.logD("behavior file error");
            e.printStackTrace();
        }

    }

    public static Item loadItemStats(String itemName,
                                     ConcurrentHashMap<Integer, Item> items,
                                     ConcurrentHashMap<Integer, Special> specials,
                                     Context context)
    {
        Logger.logI("loading item <" + itemName + ">");

        for (Integer itemId : items.keySet())
        {
            Item item = items.get(itemId);
            if (itemName.equals(item.getName()))
            {
                Logger.logI("item already exists in item table", 1);
                Logger.logI("borrowing item stats", 1);
                Item newItem = null;
                if (item instanceof ItemConsumable)
                {
                    newItem = new ItemConsumable(getNextID(items),
                                                 itemName,
                                                 item.getEffectRating(),
                                                 ((ItemConsumable) item).getDuration(),
                                                 ((ItemConsumable) item).getTargetingType());
                    newItem.setDisplayName(item.getDisplayName());
                }
                else if (item instanceof ItemDurable)
                {
                    newItem = new ItemDurable(getNextID(items),
                                              itemName,
                                              item.getEffectRating());
                    newItem.setDisplayName(item.getDisplayName());
                }
                else
                {
                    Logger.logI("error: bad item in table, removing", 1);
                    items.remove(item.getId());
                }
                if (newItem != null)
                {
                    items.put(newItem.getId(), newItem);
                    Logger.logI("finished loading item");
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

            String itemDisplayName = null;

            int event = itemParser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT && !finishedItem)
            {
                switch (event)
                {
                    case XmlPullParser.START_TAG:
                        if (itemName.equals(itemParser.getName()))
                        {
                            foundItem = true;
                            Logger.logI("found item", 1);
                        }
                        else if (foundItem)
                        {
                            if (itemParser.getName().equals("effects"))
                            {
                                foundEffects = true;
                                Logger.logI("found item effects", 1);
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                itemParser.next();
                                effects.add(itemParser.getText());
                                Logger.logI("found effect: " + itemParser.getText(), 1);
                            }
                            else if (itemParser.getName().equals("name"))
                            {
                                itemParser.next();
                                itemDisplayName = itemParser.getText();
                                Logger.logI("found display name: " + itemParser.getText(), 1);
                            }
                            else if (itemParser.getName().equals("effectRating"))
                            {
                                itemParser.next();
                                effectRating = Integer.parseInt(itemParser.getText());
                                Logger.logI("found effect rating: " + itemParser.getText(), 1);
                            }
                            else if (itemParser.getName().equals("duration"))
                            {
                                itemParser.next();
                                duration = Integer.parseInt(itemParser.getText());
                                Logger.logI("found duration: " + itemParser.getText(), 1);
                            }
                            else if (itemParser.getName().equals("targetingType"))
                            {
                                itemParser.next();
                                targetingType = itemParser.getText();
                                Logger.logI(
                                    "found targeting type: " + itemParser.getText(), 1);
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        if (foundItem)
                        {
                            if (itemName.equals(itemParser.getName()))
                            {
                                finishedItem = true;
                                Logger.logI("finished item", 1);
                            }
                            else if (foundEffects && !finishedEffects)
                            {
                                if (itemParser.getName().equals("effect"))
                                {
                                    finishedEffects = true;
                                    Logger.logI("finished item effects", 1);
                                }
                            }
                        }
                        break;
                }
                event = itemParser.next();
            }

            if (!foundItem)
            {
                Logger.logI("item <" + itemName + "> not found");
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
                Logger.logI("created new consumable item", 1);
            }
            else
            {
                item = new ItemDurable(getNextID(items), itemName, effectRating);
                Logger.logI("created new durable item", 1);
            }

            if (itemDisplayName != null)
            {
                item.setDisplayName(itemDisplayName);
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
                            Logger.logI("failed to add item effect: " + effect, 1);
                        }
                }
            }

            items.put(item.getId(), item);
            Logger.logI("item added to items table", 1);
            Logger.logI("finished loading item <" + itemName + ">");
            return item;
        }
        catch (XmlPullParserException e)
        {
            Logger.logD("error parsing item <" + itemName + ">");
            e.printStackTrace();
        }
        catch (IOException e)
        {
            Logger.logD("items file error");
        }

        return null;
    }
}