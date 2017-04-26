package com.semaphore_soft.apps.cypher.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.semaphore_soft.apps.cypher.PortalActivity;
import com.semaphore_soft.apps.cypher.utils.CollectionManager;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;
import com.semaphore_soft.apps.cypher.utils.Logger;
import com.semaphore_soft.apps.cypher.utils.Lottery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import static com.semaphore_soft.apps.cypher.utils.Lottery.performLottery;

/**
 * {@link GameMaster game.GameMaster} is a coordinator class intended to define
 * interaction with the game state {@link Model}, including game state status
 * checks, game state updating, and the performing of game action updates on
 * the game state {@link Model} as a whole.
 * <p>
 * Used by classes at all levels.
 *
 * @author scorple
 * @see Model
 */
public class GameMaster
{
    /**
     * Creates a new {@link Room} with the next available logical reference ID
     * and not associated with an AR marker, and add it to the {@link Model}.
     * <p>
     * The {@link Room} will host 0-3 random non-player {@link Actor Actors}
     * and 1-4 doors in a random configuration.
     * <p>
     * WARNING: Players will not be able to interact with or connect with the
     * rest of the game map a {@link Room} which is not associated with an AR
     * marker.
     *
     * @param context Context: The application context. Used for loading in
     *                any stats needed during {@link Room} generation.
     *
     * @return {@link Room}: The created {@link Room} object.
     *
     * @see Room
     * @see Actor
     * @see Model
     */
    public static Room generateRoom(final Context context, final Model model)
    {
        return generateRoom(context, model, CollectionManager.getNextID(model.getRooms()));
    }

    /**
     * Creates a new {@link Room} with a given logical reference ID and not
     * associated with an AR marker, and add it to the {@link Model}.
     * <p>
     * The {@link Room} will host 0-3 random non-player {@link Actor Actors}
     * and 1-4 doors in a random configuration.
     * <p>
     * WARNING: Players will not be able to interact with or connect with the
     * rest of the game map a {@link Room} which is not associated with an AR
     * marker.
     *
     * @param context Context: The application context. Used for loading in
     *                any stats needed during {@link Room} generation.
     * @param id      int: The logical reference ID to use for the created
     *                {@link Room}.
     *
     * @return {@link Room}: The created {@link Room} object.
     *
     * @see Room
     * @see Actor
     * @see Model
     */
    public static Room generateRoom(final Context context, final Model model, final int id)
    {
        return generateRoom(context, model, id, -1);
    }

    /**
     * Creates a new {@link Room} with a given logical reference ID and
     * associated with a given AR marker ID, and add it to the {@link Model}.
     * <p>
     * The {@link Room} will host 0-3 random non-player {@link Actor Actors}
     * and 1-4 doors in a random configuration.
     *
     * @param context Context: The application context. Used for loading in
     *                any stats needed during {@link Room} generation.
     * @param id      int: The logical reference ID to use for the created
     *                {@link Room}.
     * @param mark    int: The reference ID of the AR marker the created {@link
     *                Room} will associate with.
     *
     * @return {@link Room}: The created {@link Room} object.
     *
     * @see Room
     * @see Actor
     * @see Model
     */
    @Nullable
    public static Room generateRoom(final Context context,
                                    final Model model,
                                    final int id,
                                    final int mark)
    {
        Log.d("GameMaster", "Generating room with id " + id + " at mark " + mark);

        Room room = new Room(id, mark);

        if (getNumRooms(model) < 15)
        {
            int numEnemies = (int) (Math.random() * 4);
            if (numEnemies > 0)
            {
                ArrayList<String> enemyList = GameStatLoader.getList(context, "enemies");

                HashMap<String, Integer> enemyPrevalenceMap =
                    GameStatLoader.getTagsMembers(context, "actors.xml", enemyList, "prevalence");

                if (enemyPrevalenceMap != null)
                {
                    /*HashMap<String, Pair<Integer, Integer>> enemySpawnRanges = new HashMap<>();

                    int totalPrevalence = 0;

                    for (String name : enemyPrevalenceMap.keySet())
                    {
                        int prevalence = enemyPrevalenceMap.get(name);
                        Pair<Integer, Integer> range =
                            new Pair<>(totalPrevalence, totalPrevalence + prevalence);
                        enemySpawnRanges.put(name, range);
                        totalPrevalence += prevalence;
                    }*/

                    for (int i = 0; i < numEnemies; ++i)
                    {
                        String enemyName = performLottery(enemyPrevalenceMap);

                        Actor enemy = new Actor(CollectionManager.getNextID(model.getActors()),
                                                id,
                                                enemyName);

                        GameStatLoader.loadActorStats(enemy,
                                                      enemyName,
                                                      model.getSpecials(),
                                                      context);

                        HashMap<String, Integer> itemPrevalence =
                            GameStatLoader.getItemPrevalence(context, enemyName);

                        String itemName = Lottery.performLottery(itemPrevalence);

                        Logger.logI("chosen item for <" + enemyName + "> is <" + itemName + ">");

                        Item item = null;

                        if (itemName == null || itemName.equals("none"))
                        {

                        }
                        else if (itemName.equals("random"))
                        {
                            ArrayList<String> items = GameStatLoader.getList(context, "items");

                            if (items != null)
                            {
                                int itemId = (int) (Math.random() * items.size());

                                String selectedItem = items.get(itemId);

                                Logger.logI("randomly selected item:<" + selectedItem + ">");

                                item = GameStatLoader.loadItemStats(selectedItem,
                                                                    model.getItems(),
                                                                    model.getSpecials(),
                                                                    context);
                            }
                        }
                        else
                        {
                            item = GameStatLoader.loadItemStats(itemName,
                                                                model.getItems(),
                                                                model.getSpecials(),
                                                                context);
                        }

                        if (item != null)
                        {
                            enemy.addItem(item);
                        }

                        model.addActor(enemy.getId(), enemy);
                        room.addActor(enemy.getId());

                        /*int spawn = (int) (Math.random() * totalPrevalence);

                        for (String name : enemySpawnRanges.keySet())
                        {
                            Pair<Integer, Integer> range = enemySpawnRanges.get(name);

                            if (spawn >= range.first && spawn < range.second)
                            {
                                Actor enemy =
                                    new Actor(CollectionManager.getNextID(model.getActors()),
                                              id,
                                              name);
                                GameStatLoader.loadActorStats(enemy,
                                                              name,
                                                              model.getSpecials(),
                                                              context);

                                model.addActor(enemy.getId(), enemy);
                                room.addActor(enemy.getId());
                            }
                        }*/
                    }
                }
            }
        }
        else if (getNumRooms(model) == 16)
        {
            ArrayList<String> bossList = GameStatLoader.getList(context, "bosses");
            if (bossList != null)
            {
                Collections.shuffle(bossList);
                String bossName = bossList.get(0);

                Actor boss =
                    new Actor(CollectionManager.getNextID(model.getActors()), id, bossName, true);
                GameStatLoader.loadActorStats(boss, bossName, model.getSpecials(), context);

                model.addActor(boss.getId(), boss);
                room.addActor(boss.getId());
            }
        }
        else
        {
            return null;
        }

        ArrayList<Short> walls = new ArrayList<>();
        for (short i = 0; i < 4; ++i)
        {
            walls.add(i);
        }
        Collections.shuffle(walls);
        int numDoors = (int) (Math.random() * 4);
        int wall     = 0;
        for (short i : walls)
        {
            if (wall <= numDoors)
            {
                room.setWallType(i, Room.E_WALL_TYPE.DOOR_UNLOCKED);
                ++wall;
            }
            else
            {
                room.setWallType(i, Room.E_WALL_TYPE.NO_DOOR);
            }
        }

        model.addRoom(id, room);

        return room;
    }

    /**
     * Get the {@link Room} object associated with a given logical reference
     * ID.
     *
     * @param id int: The logical reference ID of the desired {@link Room}.
     *
     * @return {@link Room}: The {@link Room} object associated ith the
     * provided logical reference ID in the {@link Model}, or <code>null</code>
     * if the given logical reference ID does not correspond to a {@link Room}.
     *
     * @see Room
     * @see Model
     */
    @Nullable
    public static Room getRoom(final Model model, final int id)
    {
        return model.getRooms().get(id);
    }

    public static int getNumRooms(final Model model)
    {
        return model.getRooms().size();
    }

    /**
     * Get the reference ID of the AR marker associated with by a given {@link
     * Room}.
     * <p>
     * To be used for checking the AR marker the given {@link Room} is anchored
     * to and referencing it in a graphical ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) context ONLY.
     *
     * @param id int: The logical reference ID of the desired {@link Room}.
     *
     * @return int: The AR marker ID associated with by the given {@link Room},
     * or a flag (-1) if the {@link Room} does not associated with an AR
     * marker.
     *
     * @see Room
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public static int getRoomMarkerId(final Model model, final int id)
    {
        return model.getRooms().get(id).getMarker();
    }

    /**
     * Get the logical reference ID of the {@link Room} which associates with
     * a given AR marker reference ID.
     * <p>
     * To be used for getting game state information about a {@link Room} from
     * and for use in a graphical ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) context ONLY.
     *
     * @param markerId int: The reference ID of the AR marker associated with
     *                 by the desired {@link Room}.
     *
     * @return int: The logical reference ID of the {@link Room} which
     * associates with, or is anchored to, the AR marker indexed by the given
     * ID, or a flag (-1) if no {@link Room} in the {@link Model} associates
     * with the given AR marker ID.
     *
     * @see Room
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public static int getRoomIdByMarkerId(final Model model, final int markerId)
    {
        for (Room room : model.getRooms().values())
        {
            if (room.getMarker() == markerId)
            {
                return room.getId();
            }
        }

        return -1;
    }

    /**
     * Get a list of the logical reference IDs of every {@link Room} in the
     * {@link Model} which is considered to be 'placed', accessible, and part
     * of the game {@link Map}.
     *
     * @return ArrayList: A list containing the logical reference IDs of every
     * {@link Room} in the {@link Model} which is considered to be 'placed'.
     *
     * @see Room
     * @see Model
     * @see Map
     */
    @NonNull
    public static ArrayList<Integer> getPlacedRoomMarkerIds(final Model model)
    {
        ArrayList<Integer> placedRoomMarkers = new ArrayList<>();
        for (Room room : model.getRooms().values())
        {
            if (room.isPlaced())
            {
                placedRoomMarkers.add(room.getMarker());
            }
        }
        return placedRoomMarkers;
    }

    /**
     * Get a list of the logical reference IDs of every {@link Room} which is
     * adjacent to a given {@link Room}.
     *
     * @param startRoomId int: The logical reference ID of the desired {@link
     *                    Room} to search for {@link Room Rooms} adjacent to.
     *
     * @return ArrayList: A list containing the logical reference IDs of every
     * {@link Room} which is adjacent to the given start {@link Room}.
     *
     * @see Room
     * @see Map
     * @see Map#getAdjacentRooms(int)
     */
    @NonNull
    public static ArrayList<Integer> getAdjacentRoomIds(final Model model, final int startRoomId)
    {
        return model.getMap().getAdjacentRooms(startRoomId);
    }

    /**
     * Get the short index of the wall of an end {@link Room} connecting it to
     * a start {@link Room}. Assumes the given {@link Room Rooms} are adjacent.
     *
     * @param startRoomId int: The logical reference ID of the desired start
     *                    {@link Room}.
     * @param endRoomId   int: The logical reference ID of the desired end
     *                    {@link Room}.
     *
     * @return short: The index of the end {@link Room} wall which connects it
     * to the start {@link Room}.
     *
     * @see Room
     * @see Map
     * @see Map#getWallsBetweenAdjacentRooms(int, int)
     */
    public static short getSideOfRoomFrom(final Model model,
                                          final int startRoomId,
                                          final int endRoomId)
    {
        return model.getMap().getWallsBetweenAdjacentRooms(startRoomId, endRoomId).second;
    }

    /**
     * Get the number of player {@link Actor Actors} in a given {@link Room}.
     *
     * @param roomId int: The logical reference ID of the desired {@link Room}.
     *
     * @return int: The count of player controlled {@link Actor Actors} in the
     * given {@link Room}. 0 if there are no player controlled {@link Actor
     * Actors} in the given {@link Room}.
     *
     * @see Room
     * @see Actor
     */
    public static int getPlayersInRoom(final Model model, final int roomId)
    {
        int  res  = 0;
        Room room = model.getRooms().get(roomId);

        if (room != null)
        {
            for (int id : room.getResidentActors())
            {
                if (model.getActors().get(id).isPlayer())
                {
                    ++res;
                }
            }
        }

        return res;
    }

    /**
     * Get the number of non-player {@link Actor Actors} in a given {@link
     * Room}.
     *
     * @param roomId int: The logical reference ID of the desired {@link Room}.
     *
     * @return int: The count of player controlled {@link Actor Actors} in the
     * given {@link Room}. 0 if there are no non-player controlled {@link Actor
     * Actors} in the given {@link Room}.
     *
     * @see Room
     * @see Actor
     */
    public static int getEnemiesInRoom(final Model model, final int roomId)
    {
        int  res  = 0;
        Room room = model.getRooms().get(roomId);

        if (room != null)
        {
            for (int id : room.getResidentActors())
            {
                if (!model.getActors().get(id).isPlayer())
                {
                    ++res;
                }
            }
        }

        return res;
    }

    /**
     * Get the {@link Actor} object associated with a given logical reference
     * ID.
     *
     * @param id int: The logical reference ID of the desired {@link Actor}.
     *
     * @return {@link Actor}: The {@link Actor} object associated ith the
     * provided logical reference ID in the {@link Model}, or <code>null</code>
     * if the given logical reference ID does not correspond to an {@link
     * Actor}.
     *
     * @see Actor
     * @see Model
     */
    @Nullable
    public static Actor getActor(final Model model, final int id)
    {
        return model.getActors().get(id);
    }

    /**
     * Get the reference ID of the AR marker associated with by a given {@link
     * Actor}.
     * <p>
     * To be used for checking the AR marker the given {@link Actor} associates
     * with and referencing it in a graphical ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) context ONLY.
     *
     * @param id int: The logical reference ID of the desired {@link Actor}.
     *
     * @return int: The AR marker ID associated with by the given {@link
     * Actor}, or a flag (-1) if the {@link Actor} does not associated with an
     * AR marker.
     *
     * @see Actor
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     */
    public static int getActorMakerId(final Model model, final int id)
    {
        return model.getActors().get(id).getMarker();
    }

    /**
     * Get the logical reference ID of the {@link Room} a given {@link Actor}
     * associates with, or considers itself to be a resident of.
     * <p>
     * To be used for referencing the {@link Room} in a game state context
     * (associating with {@link Actor Actors}, checking path validity, etc.)
     * ONLY.
     *
     * @param id int The logical reference ID of the desired {@link Actor}.
     *
     * @return int: The logical reference ID of the {@link Room} the given
     * {@link Actor} associates with, or considers itself to be a resident of.
     *
     * @see Actor
     * @see Room
     */
    public static int getActorRoomId(final Model model, final int id)
    {
        return model.getActors().get(id).getRoom();
    }

    /**
     * Get the {@link Room} object a given {@link Actor} associates with, or
     * considers itself to be a reference of.
     *
     * @param id int: The logical reference ID of the desired {@link Actor}.
     *
     * @return {@link Room}: The {@link Room} object the given {@link Actor}
     * associates with, or considers itself to be a resident of.
     *
     * @see Actor
     * @see Room
     */
    @Nullable
    public static Room getActorRoom(final Model model, final int id)
    {
        return model.getRooms().get(model.getActors().get(id).getRoom());
    }

    /**
     * Check whether or not a given {@link Actor} is a considered to be player
     * controlled.
     *
     * @param id int: The logical reference ID of the desired {@link Actor}.
     *
     * @return boolean:
     * <ul>
     * <li>True if the given {@link Actor} is considered to be player
     * controlled.</li>
     * <li>False otherwise.</li>
     * </ul>
     *
     * @see Actor
     */
    public static boolean getActorIsPlayer(final Model model, final int id)
    {
        return model.getActors().get(id).isPlayer();
    }

    /**
     * Get the type of object a given AR marker reference ID is associated
     * with.
     *
     * @param markId int: The reference ID of the desired AR marker.
     *
     * @return int:
     * <ul>
     * <li>1: The given AR marker ID is associated with a {@link Room}.</li>
     * <li>0: The given AR marker ID is associated with an {@link Actor}.</li>
     * <li>-1: The given AR marker ID is not associated with a game
     * object.</li>
     * </ul>
     *
     * @see Actor
     * @see Room
     */
    public static int getMarkerAttachment(final Model model, final int markId)
    {
        for (Actor actor : model.getActors().values())
        {
            if (actor.getMarker() == markId)
            {
                return 0;
            }
        }

        for (Room room : model.getRooms().values())
        {
            if (room.getMarker() == markId)
            {
                return 1;
            }
        }

        return -1;
    }

    /**
     * Get the logical reference ID of the game object associated with a given
     * AR marker reference ID, regardless of what type of game object it is.
     * <p>
     * To be used for referencing an object in a game state context (checking
     * game object associations, stats, etc.) ONLY.
     *
     * @param markId int: The reference ID of the desired AR marker.
     *
     * @return int: The logical reference ID of the {@link Actor} or {@link
     * Room} associated with the given AR marker reference ID, or a flag (-1)
     * if the given AR marker reference ID is not associated with a game
     * object.
     *
     * @see Actor
     * @see Room
     */
    public static int getIdByMarker(final Model model, final int markId)
    {
        for (Actor actor : model.getActors().values())
        {
            if (actor.getMarker() == markId)
            {
                return actor.getId();
            }
        }

        for (Room room : model.getRooms().values())
        {
            if (room.getMarker() == markId)
            {
                return room.getId();
            }
        }

        return -1;
    }

    /**
     * Attempt to move a given {@link Actor} to a given {@link Room} and return
     * a code describing the success or failure of the move.
     *
     * @param actorId   int: The logical reference ID of the {@link Actor}
     *                  attempting to move.
     * @param endRoomId int: The logical reference ID of the {@link Room} the
     *                  given {@link Actor} is attempting to move to.
     *
     * @return int:
     * <ul>
     * <li>2: Success, start {@link Room} established, {@link Actor} moves to
     * start {@link Room}.</li>
     * <li>1: Success, {@link Actor} remains in current {@link Room}.</li>
     * <li>0: Success, {@link Actor} moves from previous {@link Room}to new
     * {@link Room}.</li>
     * <li>-1: Failure, invalid path, bad connection between {@link Room
     * Rooms}.</li>
     * <li>-2: Failure, invalid path, {@link Room Rooms} not adjacent.</li>
     * <li>-3: Failure, destination {@link Room} is not placed.</li>
     * <li>-4: Failure, unknown.</li>
     * </ul>
     *
     * @see Room
     * @see PortalActivity#onActorMove(int, int)
     */
    public static int moveActor(final Model model, final int actorId, final int endRoomId)
    {
        Actor actor   = model.getActors().get(actorId);
        Room  endRoom = model.getRooms().get(endRoomId);

        if (!endRoom.isPlaced())
        {
            return -3;
        }

        if (actor.getRoom() < 0)
        {
            actor.setRoom(endRoomId);
            endRoom.addActor(actorId);

            return 2;
        }

        if (actor.getRoom() == endRoomId)
        {
            return 1;
        }

        int pathRes = getValidPath(model, actor.getRoom(), endRoomId);

        switch (pathRes)
        {
            case 0:
                Room startRoom = model.getRooms().get(actor.getRoom());
                startRoom.removeActor(actorId);

                endRoom.addActor(actorId);

                actor.setRoom(endRoomId);
                actor.setProposedRoomId(-1);

                return 0;
            case -1:
                return -1;
            case -2:
                return -2;
            default:
                break;
        }

        return -4;
    }

    public static int simulateMove(final Model model, final int actorId, final int endRoomMarkerId)
    {
        Actor actor     = model.getActors().get(actorId);
        Room  startRoom = getRoom(model, actor.getRoom());
        int   endRoomId = getRoomIdByMarkerId(model, endRoomMarkerId);
        Room  endRoom   = model.getRooms().get(endRoomId);

        if (!endRoom.isPlaced())
        {
            if (startRoom != null)
            {
                startRoom.addActor(actorId);
            }

            return -3;
        }

        if (actor.getRoom() == endRoomId)
        {
            actor.setProposedRoomId(-1);

            if (startRoom != null)
            {
                startRoom.removeActor(actorId);
            }
            endRoom.addActor(actorId);

            return 1;
        }

        int pathRes = getValidPath(model, actor.getRoom(), endRoomId);

        switch (pathRes)
        {
            case 0:
                actor.setProposedRoomId(endRoomId);

                if (startRoom != null)
                {
                    startRoom.removeActor(actorId);
                }
                endRoom.addActor(actorId);

                return 0;
            case -1:
                return -1;
            case -2:
                return -2;
            default:
                break;
        }

        return -4;
    }

    public static int getRoomFull(final Model model, final int roomId)
    {
        int residentEnemies = 0;

        Room endRoom = getRoom(model, roomId);

        if (endRoom != null)
        {
            for (int actorId : endRoom.getResidentActors())
            {
                Actor actor = getActor(model, actorId);

                if (actor != null && !actor.isPlayer())
                {
                    ++residentEnemies;
                }
            }

            if (residentEnemies >= 4)
            {
                return -1;
            }

            return 0;
        }
        else
        {
            return -2;
        }
    }

    /**
     * Check whether or not there is a valid path between two given {@link Room
     * Rooms} and return a code describing if the path is valid or, if not, why
     * not.
     * <p>
     * A path between two {@link Room Rooms} is considered valid if and only if
     * there is an open door in the wall connecting each {@link Room} with the
     * opposite {@link Room}.
     *
     * @param startRoomId int: The logical reference ID of the {@link Room}
     *                    checking for path from.
     * @param endRoomId   int: The logical reference ID of the {@link Room}
     *                    checking for path to.
     *
     * @return int:
     * <ul>
     * <li>0: Valid path.</li>
     * <li>-1: Invalid path, bad connection between given {@link Room
     * Rooms}.</li>
     * <li>-2: Invalid path, given {@link Room Rooms} not adjacent.</li>
     * </ul>
     *
     * @see Room
     * @see #moveActor(Model, int, int)
     * @see ActorController#takeTurn(GameController, Model, int)
     * @see Room.E_WALL_TYPE
     */
    public static int getValidPath(final Model model, final int startRoomId, final int endRoomId)
    {
        Map map = model.getMap();

        if (map.checkAdjacent(startRoomId, endRoomId) < 0)
        {
            return -2;
        }

        Room startRoom = model.getRooms().get(startRoomId);
        Room endRoom   = model.getRooms().get(endRoomId);

        Pair<Short, Short> wallsBetweenAdjacentRooms =
            map.getWallsBetweenAdjacentRooms(startRoomId, endRoomId);

        Room.E_WALL_TYPE startRoomWall = startRoom.getWallType(wallsBetweenAdjacentRooms.first);
        Room.E_WALL_TYPE endRoomWall   = endRoom.getWallType(wallsBetweenAdjacentRooms.second);

        if (startRoomWall == Room.E_WALL_TYPE.DOOR_OPEN &&
            endRoomWall == Room.E_WALL_TYPE.DOOR_OPEN)
        {
            return 0;
        }

        return -1;
    }

    /**
     * Attempt to open a door connecting two given {@link Room Rooms} using the
     * given the side, or wall, of each {@link Room} the door should be in, add
     * the proposed end {@link Room} to the {@link Map} if door opening is
     * successful and return a code describing success or failure.
     * <p>
     * A door can be opened between two {@link Room Rooms} if and only if there
     * is an unlocked door in the given wall index of the start {@link Room}
     * and the end {@link Room} and a valid adjacency can be formed between the
     * end {@link Room} and all adjacent {@link Room Rooms}.
     *
     * @param startRoomId     int: The logical reference ID of the {@link Room}
     *                        opening door from.
     * @param endRoomId       int: The logical reference ID of the {@link Room}
     *                        opening door to.
     * @param sideOfStartRoom short: The index of the wall the door must be in
     *                        for the start {@link Room}.
     * @param sideOfEndRoom   short: The index of the wall the door must be in
     *                        for the end {@link Room}.
     *
     * @return int:
     * <ul>
     * <li>0: Success, door opened between given {@link Room Rooms}.</li>
     * <li>-1: Failure, door could not be opened between given {@link Room
     * Rooms}.</li>
     * </ul>
     *
     * @see Room
     * @see Map
     * @see #getValidAdjacency(Model, int, int, short, short)
     * @see #getValidAdjacencyProposedRoom(Model, int, int, short, int, int)
     * @see PortalActivity#openDoor()
     * @see Room.E_WALL_TYPE
     */
    public static int openDoor(final Model model, final int startRoomId,
                               final int endRoomId,
                               final short sideOfStartRoom,
                               final short sideOfEndRoom)
    {
        Room startRoom = model.getRooms().get(startRoomId);
        Room endRoom   = model.getRooms().get(endRoomId);

        if (startRoom.getWallType(sideOfStartRoom) != Room.E_WALL_TYPE.DOOR_UNLOCKED ||
            endRoom.getWallType(sideOfEndRoom) !=
            Room.E_WALL_TYPE.DOOR_UNLOCKED)
        {
            return -1;
        }

        {
            if (getValidAdjacency(model, startRoomId, endRoomId, sideOfStartRoom, sideOfEndRoom) ==
                0)
            {
                endRoom.setPlaced(true);

                startRoom.setWallType(sideOfStartRoom, Room.E_WALL_TYPE.DOOR_OPEN);
                endRoom.setWallType(sideOfEndRoom, Room.E_WALL_TYPE.DOOR_OPEN);

                Map map = model.getMap();
                map.insert(startRoomId, sideOfStartRoom, endRoomId, sideOfEndRoom);

                //Pair<Integer, Integer> endRoomPosition = map.getPosition(endRoomId);
                map.print();

                ConcurrentHashMap<Integer, Pair<Short, Short>>
                    adjacentRoomsAndWalls = map.getAdjacentRoomsAndWalls(endRoomId);

                for (int id : adjacentRoomsAndWalls.keySet())
                {
                    if (id != startRoomId)
                    {
                        Room               testRoom = model.getRooms().get(id);
                        Pair<Short, Short> wallPair = adjacentRoomsAndWalls.get(id);

                        if (endRoom.getWallType(wallPair.first) == Room.E_WALL_TYPE.DOOR_UNLOCKED &&
                            testRoom.getWallType(wallPair.second) ==
                            Room.E_WALL_TYPE.DOOR_UNLOCKED)
                        {
                            endRoom.setWallType(wallPair.first, Room.E_WALL_TYPE.DOOR_OPEN);
                            testRoom.setWallType(wallPair.second, Room.E_WALL_TYPE.DOOR_OPEN);
                        }
                    }
                }

                return 0;
            }
            else
            {
                return -2;
            }
        }

        //return -1;
    }

    /**
     * Check whether or not the placement of a proposed end {@link Room}
     * rotated such that its given wall connects to a given wall of the given
     * start {@link Room} forms a valid adjacency with that {@link Room} and
     * all other {@link Room Rooms} which would be adjacent to the proposed
     * {@link Room}, and return a code describing the validity.
     * <p>
     * A proposed adjacency between two {@link Room Rooms} is valid if and only
     * if the wall connecting the start {@link Room} to the end {@link Room} is
     * the same type as the wall connecting the end {@link Room} to the start
     * {@link Room}.
     *
     * @param startRoomId     int: The logical reference ID of the {@link Room}
     *                        checking validity from.
     * @param endRoomId       int: The logical reference ID of the {@link Room}
     *                        checking validity to.
     * @param sideOfStartRoom short: The index of the wall of the start {@link
     *                        Room} to check adjacency with.
     * @param sideOfEndRoom   short: The index of the wall of the end {@link
     *                        Room} to check adjacency with.
     *
     * @return int:
     * <ul>
     * <li>0: Valid adjacency between given {@link Room Rooms}.</li>
     * <li>-1: Invalid adjacency between given {@link Room Rooms}.</li>
     * </ul>
     *
     * @see Room
     * @see Map
     * @see #getValidAdjacencyProposedRoom(Model, int, int, short, int, int)
     * @see #openDoor(Model, int, int, short, short)
     * @see Room.E_WALL_TYPE
     */
    private static int getValidAdjacency(final Model model, final int startRoomId,
                                         final int endRoomId,
                                         final short sideOfStartRoom,
                                         final short sideOfEndRoom)
    {
        Room startRoom = model.getRooms().get(startRoomId);
        Room endRoom   = model.getRooms().get(endRoomId);

        if (startRoom.getWallType(sideOfStartRoom) != endRoom.getWallType(sideOfEndRoom))
        {
            return -1;
        }

        Map map = model.getMap();

        Pair<Integer, Integer> proposedEndRoomPosition =
            map.getProposedPosition(startRoomId, sideOfStartRoom);
        short proposedEndRoomRotation =
            map.getProposedRotation(startRoomId, sideOfStartRoom, sideOfEndRoom);

        for (short i = 0; i < 4; ++i)
        {
            int testRoomId = map.getRoomFromPositionInDirection(proposedEndRoomPosition.first,
                                                                proposedEndRoomPosition.second,
                                                                i);

            if (testRoomId > -1 && testRoomId != startRoomId)
            {
                if (getValidAdjacencyProposedRoom(model, proposedEndRoomPosition.first,
                                                  proposedEndRoomPosition.second,
                                                  proposedEndRoomRotation,
                                                  testRoomId,
                                                  endRoomId) == -1)
                {
                    return -1;
                }
            }
        }

        return 0;
    }

    /**
     * Check whether or not a given {@link Room} at proposed X and Y position
     * with proposed rotation in the {@link Map} forms a valid adjacency with
     * another given {@link Room} and return a code describing the validity or
     * invalidity of the adjacency.
     * <p>
     * A proposed adjacency between two {@link Room Rooms} is valid if and only
     * if the wall connecting the start {@link Room} to the end {@link Room} is
     * the same type as the wall connecting the end {@link Room} to the start
     * {@link Room}.
     *
     * @param proposedRoomPositionX int: The proposed X position of the
     *                              proposed {@link Room} in the {@link Map}.
     * @param proposedRoomPositionY int: The proposed Y position of the
     *                              proposed {@link Room} in the {@link Map}.
     * @param proposedRoomRotation  int: The proposed rotation of the proposed
     *                              {@link Room} in the {@link Map}.
     * @param testRoomId            int: The logical reference ID of the {@link
     *                              Room} to test the proposed {@link Room
     *                              Room's} adjacency with.
     * @param proposedRoomId        int: The logical reference ID of a proposed
     *                              {@link Room} to add to the {@link Map}.
     *
     * @return int:
     * <ul>
     * <li>0: Valid adjacency between given {@link Room Rooms}.</li>
     * <li>-1: Invalid adjacency between given {@link Room Rooms}.</li>
     * </ul>
     *
     * @see Room
     * @see Map
     * @see #getValidAdjacency(Model, int, int, short, short)
     * @see #openDoor(Model, int, int, short, short)
     * @see Room.E_WALL_TYPE
     */
    private static int getValidAdjacencyProposedRoom(final Model model,
                                                     final int proposedRoomPositionX,
                                                     final int proposedRoomPositionY,
                                                     final short proposedRoomRotation,
                                                     final int testRoomId,
                                                     final int proposedRoomId)
    {
        Room testRoom     = model.getRooms().get(testRoomId);
        Room proposedRoom = model.getRooms().get(proposedRoomId);

        Pair<Integer, Integer> testRoomPosition = model.getMap().getPosition(testRoom.getId());
        short testRoomRotation =
            model.getMap().getRotation(testRoomPosition.first, testRoomPosition.second);
        Pair<Integer, Integer> proposedRoomPosition =
            new Pair<>(proposedRoomPositionX, proposedRoomPositionY);

        Room.E_WALL_TYPE wallType0;
        Room.E_WALL_TYPE wallType1;

        if (testRoomPosition.second > proposedRoomPosition.second)
        {
            //room1 is north of room0
            wallType0 = testRoom.getWallType(testRoomRotation);
            wallType1 = proposedRoom.getWallType((short) ((proposedRoomRotation + 2) % 4));
        }
        else if (testRoomPosition.first < proposedRoomPosition.first)
        {
            //room1 is east of room0
            wallType0 = testRoom.getWallType((short) ((testRoomRotation + 1) % 4));
            wallType1 = proposedRoom.getWallType((short) ((proposedRoomRotation + 3) % 4));
        }
        else if (testRoomPosition.second < proposedRoomPosition.second)
        {
            //room1 is south of room0
            wallType0 = testRoom.getWallType((short) ((testRoomRotation + 2) % 4));
            wallType1 = proposedRoom.getWallType(proposedRoomRotation);
        }
        else
        {
            //room1 is west of room0
            wallType0 = testRoom.getWallType((short) ((testRoomRotation + 3) % 4));
            wallType1 = proposedRoom.getWallType((short) ((proposedRoomRotation + 1) % 4));
        }

        if (wallType0 == wallType1)
        {
            return 0;
        }

        return -1;
    }

    /**
     * Get a map of the valid player {@link Actor} targets for a given {@link
     * Actor}.
     * <p>
     * A target is valid if and only if it resides within the same {@link Room}
     * as the given {@link Actor}.
     * <p>
     * Map may include the given {@link Actor}.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @return ConcurrentHashMap: A map containing valid player {@link Actor}
     * targets indexed by their logical reference IDs.
     *
     * @see Actor
     * @see Room
     */
    @NonNull
    public static ConcurrentHashMap<Integer, Actor> getPlayerTargets(final Model model,
                                                                     final int actorId)
    {
        Actor actor          = model.getActors().get(actorId);
        int   proposedRoomId = actor.getProposedRoomId();
        Room room =
            model.getRooms().get(proposedRoomId > -1 ? proposedRoomId : actor.getRoom());
        ConcurrentHashMap<Integer, Actor> targets = new ConcurrentHashMap<>();

        Logger.logI("looking for player targets in room: " + actor.getRoom(), 1);

        for (int targetId : room.getResidentActors())
        {
            Actor target = model.getActors().get(targetId);
            if (target.isPlayer())
            {
                Logger.logI("found valid target: " + target.getId(), 1);
                targets.put(targetId, target);
            }
        }

        return targets;
    }

    /**
     * Get a map of the valid non-player {@link Actor} targets for a given
     * {@link Actor}.
     * <p>
     * A target is valid if and only if it resides within the same {@link Room}
     * as the given {@link Actor}.
     * <p>
     * Map may include the given {@link Actor}.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @return ConcurrentHashMap: A map containing valid non-player {@link
     * Actor} targets indexed by their logical reference IDs.
     *
     * @see Actor
     * @see Room
     */
    @NonNull
    public static ConcurrentHashMap<Integer, Actor> getNonPlayerTargets(final Model model,
                                                                        final int actorId)
    {
        Actor actor          = model.getActors().get(actorId);
        int   proposedRoomId = actor.getProposedRoomId();
        Room room =
            model.getRooms().get(proposedRoomId > -1 ? proposedRoomId : actor.getRoom());
        ConcurrentHashMap<Integer, Actor> targets = new ConcurrentHashMap<>();

        Logger.logI("looking for non-player targets in room: " + actor.getRoom(), 1);

        for (int targetId : room.getResidentActors())
        {
            Actor target = model.getActors().get(targetId);
            if (!target.isPlayer())
            {
                Logger.logI("found valid target: " + target.getId(), 1);
                targets.put(targetId, target);
            }
        }

        return targets;
    }

    /**
     * Get a list of the logical reference IDs of valid player {@link Actor}
     * targets for a given {@link Actor}.
     * <p>
     * A target is valid if and only if it resides within the same {@link Room}
     * as the given {@link Actor}.
     * <p>
     * Will not include the logical reference ID of the given {@link Actor}.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @return ArrayList: A list containing the logical reference IDs of valid
     * player {@link Actor} targets for a given {@link Actor}.
     *
     * @see Actor
     * @see Room
     */
    @NonNull
    public static ArrayList<Integer> getPlayerTargetIds(final Model model, final int actorId)
    {
        Actor actor          = model.getActors().get(actorId);
        int   proposedRoomId = actor.getProposedRoomId();
        Room room =
            model.getRooms().get(proposedRoomId > -1 ? proposedRoomId : actor.getRoom());
        ArrayList<Integer> targets = new ArrayList<>();

        Logger.logD("looking for player targets for: " + actorId);
        Logger.logD("looking for player targets in room: " + actor.getRoom());

        for (int targetId : room.getResidentActors())
        {
            if (targetId != actorId)
            {
                Actor target = model.getActors().get(targetId);
                if (target.isPlayer())
                {
                    Logger.logD("found valid target: " + target.getId());
                    targets.add(targetId);
                }
            }
        }

        return targets;
    }

    /**
     * Get a list of the logical reference IDs of valid non-player {@link
     * Actor} targets for a given {@link Actor}.
     * <p>
     * A target is valid if and only if it resides within the same {@link Room}
     * as the given {@link Actor}.
     * <p>
     * Will not include the logical reference ID of the given {@link Actor}.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @return ArrayList: A list containing the logical reference IDs of valid
     * non-player {@link Actor} targets for a given {@link Actor}.
     *
     * @see Actor
     * @see Room
     */
    @NonNull
    public static ArrayList<Integer> getNonPlayerTargetIds(final Model model, final int actorId)
    {
        Actor actor          = model.getActors().get(actorId);
        int   proposedRoomId = actor.getProposedRoomId();
        Room room =
            model.getRooms().get(proposedRoomId > -1 ? proposedRoomId : actor.getRoom());
        ArrayList<Integer> targets = new ArrayList<>();

        Logger.logD("looking for player targets for: " + actorId);
        Logger.logD("looking for player targets in room: " + actor.getRoom());

        try
        {
            for (int targetId : room.getResidentActors())
            {
                if (targetId != actorId)
                {
                    Actor target = model.getActors().get(targetId);
                    if (!target.isPlayer())
                    {
                        Logger.logD("found valid target: " + target.getId());
                        targets.add(targetId);
                    }
                }
            }
        }
        catch (Exception e)
        {
            return getNonPlayerTargetIds(model, actorId);
        }

        return targets;
    }

    /**
     * Get a map of the {@link Special} abilities associated with, or known by,
     * a given {@link Actor}.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @return ConcurrentHashMap: A map containing the {@link Special}
     * abilities associated with, or known by, a given {@link Actor}, indexed
     * by logical reference ID.
     *
     * @see Actor
     * @see Actor#getSpecials()
     * @see Special
     */
    public static ConcurrentHashMap<Integer, Special> getSpecials(final Model model,
                                                                  final int actorId)
    {
        Logger.logI("looking for specials for actor: " + actorId, 1);
        Actor actor = model.getActors().get(actorId);
        for (int id : actor.getSpecials().keySet())
        {
            Logger.logI("found special: " + actor.getSpecials().get(id).getName(), 1);
        }
        return actor.getSpecials();
    }

    /**
     * Update the {@link Actor.E_STATE state} of a given {@link Actor},
     * reflecting the most recent action taken by that {@link Actor}.
     *
     * @param id    int: The logical reference ID of the desired {@link Actor}.
     * @param state {@link Actor.E_STATE}: The new {@link Actor.E_STATE state}
     *              of the given {@link Actor}, reflecting the most recent
     *              action taken by that {@link Actor}.
     *
     * @see Actor
     * @see Actor.E_STATE
     * @see Actor#setState(Actor.E_STATE)
     */
    public static void setActorState(final Model model, final int id, final Actor.E_STATE state)
    {
        model.getActors().get(id).setState(state);
    }

    /**
     * Perform an attack originating from a given attacker {@link Actor}
     * directed at a given defender {@link Actor}, update the {@link Actor
     * attacker} {@link Actor.E_STATE state} accordingly and return a code
     * describing the outcome of the attack.
     *
     * @param attackerId int: The logical reference ID of the attacking {@link
     *                   Actor}.
     * @param defenderId int: The logical reference ID of the target {@link
     *                   Actor}.
     *
     * @return int:
     * <ul>
     * <li>1: Attack success, target {@link Actor} killed.</li>
     * <li>0: Attack success, target {@link Actor} still alive.</li>
     * </ul>
     *
     * @see Actor
     * @see Actor.E_STATE
     * @see Actor#attack(Actor)
     * @see Actor#receiveAttack(int)
     * @see PortalActivity#postAttackResults(int, int, int)
     */
    public static int attack(final Model model, final int attackerId, final int defenderId)
    {
        int ret = 0;

        Actor attacker = model.getActors().get(attackerId);
        Actor defender = model.getActors().get(defenderId);

        attacker.setState(Actor.E_STATE.ATTACK);

        attacker.attack(defender);

        if (defender.getHealthCurrent() <= 0)
        {
            if (defender.isBoss())
            {
                ret = 2;
            }
            else if (defender.isPlayer())
            {
                ret = 3;

                if (areAllPlayersDead(model))
                {
                    ret = 4;
                }
            }
            else
            {
                ret = 1;

                Room room = getRoom(model, defender.getRoom());

                if (room != null)
                {
                    for (int itemId : defender.getItems().keySet())
                    {
                        room.addItem(itemId);
                        Logger.logI("item <" + itemId + "> added to room <" + room.getId() + ">");
                        defender.removeItem(itemId);

                        ret = 5;
                    }
                }
            }
        }

        int proposedRoom = model.getActors().get(attackerId).getProposedRoomId();

        if (proposedRoom > -1)
        {
            moveActor(model, attackerId, proposedRoom);
        }

        return ret;
    }

    /**
     * Remove a given {@link Actor} from the {@link Model}, including any
     * {@link Room} which associated with it.
     *
     * @param actorId int: The logical reference ID of the desired {@link
     *                Actor}.
     *
     * @see Actor
     * @see Room
     * @see Model
     */
    public static void removeActor(final Model model, final int actorId)
    {
        Room room = getActorRoom(model, actorId);
        if (room != null)
        {
            room.removeActor(actorId);
        }
        model.getActors().remove(actorId);
    }

    public static void addActorToRoom(final Model model, final int actorId, final int roomId)
    {
        Room room = getRoom(model, roomId);
        if (room != null)
        {
            room.addActor(actorId);
        }
    }

    public static void removeActorFromRoom(final Model model, final int actorId, final int roomId)
    {
        Room room = getRoom(model, roomId);
        if (room != null)
        {
            room.removeActor(actorId);
        }
    }

    /**
     * Remove all {@link Actor Actors} with a current health value less than or
     * equal to 0 from the {@link Model} and from any {@link Room Rooms} which
     * associated with them.
     * <p>
     * Excludes player-controlled {@link Actor Actors}.
     *
     * @see Actor
     * @see Room
     * @see Model
     */
    public static void removeDeadActors(final Model model)
    {
        Logger.logD("enter trace");

        ConcurrentHashMap<Integer, Actor> actors           = model.getActors();
        ArrayList<Integer>                markedForRemoval = new ArrayList<>();
        for (int id : actors.keySet())
        {
            if (!actors.get(id).isPlayer() && actors.get(id).getHealthCurrent() <= 0)
            {
                markedForRemoval.add(id);
                Logger.logD("marked " + id + " for removal");
            }
        }
        for (int id : markedForRemoval)
        {
            Room room = model.getRooms().get(actors.get(id).getRoom());
            room.removeActor(id);
            Logger.logD("removed " + id + " from room " + room.getId());
            actors.remove(id);
            Logger.logD("removed " + id + " from actors list");
        }

        Logger.logD("exit trace");
    }

    /**
     * Perform a given {@link Special} ability originating from a given source
     * {@link Actor} without a specific target, update the source {@link Actor}
     * {@link Actor.E_STATE state} if the {@link Special} was performed
     * successfully and return a code describing the outcome of the {@link
     * Special} performance.
     *
     * @param sourceId  int: The logical reference ID of the desired source
     *                  {@link Actor}.
     * @param specialId int: The logical reference ID of the {@link Special}
     *                  being performed.
     *
     * @return int:
     * <ul>
     * <li>1: {@link Special} performance success, one or more target {@link
     * Actor Actors} killed by {@link Special}.</li>
     * <li>0: {@link Special} performance success, no significant outcome to
     * report.</li>
     * <li>-1: {@link Special} performance failure, source {@link Actor} does
     * not have enough {@link Special} energy.</li>
     * <li>-2: {@link Special} performance failure, no valid targets for {@link
     * Special} ability.</li>
     * </ul>
     *
     * @see Actor
     * @see Actor.E_STATE
     * @see Actor#performSpecial(Special, ArrayList)
     * @see PortalActivity#postSpecialResult(int, int, int, int)
     * @see Special
     */
    public static int special(final Model model, final int sourceId, final int specialId)
    {
        int ret;

        Actor            source  = model.getActors().get(sourceId);
        Special          special = model.getSpecials().get(specialId);
        ArrayList<Actor> targets = new ArrayList<>();

        for (Actor actor : model.getActors().values())
        {
            if (actor.getRoom() == source.getRoom())
            {
                if (special.getTargetingType() == Special.E_TARGETING_TYPE.AOE_PLAYER &&
                    actor.isPlayer())
                {
                    targets.add(actor);
                }
                else if (
                    special.getTargetingType() == Special.E_TARGETING_TYPE.AOE_NON_PLAYER &&
                    !actor.isPlayer())
                {
                    targets.add(actor);
                }
            }
        }

        if (targets.size() > 0)
        {
            if (source.performSpecial(special, targets))
            {
                source.setState(Actor.E_STATE.SPECIAL);

                ret = 0;

                for (Actor target : targets)
                {
                    boolean kill       = false;
                    boolean playerKill = false;
                    boolean teamKill   = false;

                    if (target.getHealthCurrent() <= 0)
                    {
                        ret = 1;

                        if (target.isBoss())
                        {
                            return 2;
                        }

                        if (target.isPlayer())
                        {
                            playerKill = true;
                        }
                        else
                        {
                            Room room = getRoom(model, target.getRoom());

                            if (room != null)
                            {
                                for (int itemId : target.getItems().keySet())
                                {
                                    room.addItem(itemId);
                                    Logger.logI(
                                        "item <" + itemId + "> added to room <" + room.getId() +
                                        ">");
                                    target.removeItem(itemId);

                                    ret = 6;
                                }
                            }
                        }
                    }

                    if (playerKill)
                    {
                        ret = 3;

                        if (areAllPlayersDead(model))
                        {
                            ret = 4;
                        }
                    }
                }

                int proposedRoom = model.getActors().get(sourceId).getProposedRoomId();

                if (proposedRoom > -1)
                {
                    moveActor(model, sourceId, proposedRoom);
                }

                return ret;
            }

            return -1;
        }

        return -2;
    }

    /**
     * Perform a given {@link Special} ability originating from a given source
     * {@link Actor} with a specific target {@link Actor}, update the source
     * {@link Actor} {@link Actor.E_STATE state} if the {@link Special} was
     * performed successfully and return a code describing the outcome of the
     * {@link Special} performance.
     *
     * @param sourceId  int: The logical reference ID of the desired source
     *                  {@link Actor}.
     * @param targetId  int: The logical reference ID of the desired target
     *                  {@link Actor}.
     * @param specialId int: The logical reference ID of the {@link Special}
     *                  being performed.
     *
     * @return int:
     * <ul>
     * <li>1: {@link Special} performance success, target {@link Actor Actors}
     * killed by {@link Special}.</li>
     * <li>0: {@link Special} performance success, no significant outcome to
     * report.</li>
     * <li>-1: {@link Special} performance failure, source {@link Actor} does
     * not have enough {@link Special} energy.</li>
     * <li>-2: {@link Special} performance failure, no valid targets for {@link
     * Special} ability.</li>
     * </ul>
     *
     * @see Actor
     * @see Actor.E_STATE
     * @see Actor#performSpecial(Special, ArrayList)
     * @see PortalActivity#postSpecialResult(int, int, int, int)
     * @see Special
     */
    public static int special(final Model model,
                              final int sourceId,
                              final int targetId,
                              final int specialId)
    {
        int ret = 0;

        Actor   source  = model.getActors().get(sourceId);
        Actor   target  = model.getActors().get(targetId);
        Special special = model.getSpecials().get(specialId);

        if (source.getRoom() == target.getRoom())
        {
            if (source.performSpecial(special, target))
            {
                source.setState(Actor.E_STATE.SPECIAL);

                ret = 0;

                if (target.getHealthCurrent() <= 0)
                {
                    ret = 1;

                    if (target.isBoss())
                    {
                        ret = 2;
                    }
                    else if (target.isPlayer())
                    {
                        ret = 3;

                        if (areAllPlayersDead(model))
                        {
                            ret = 4;
                        }
                    }
                    else
                    {
                        Room room = getRoom(model, target.getRoom());

                        if (room != null)
                        {
                            for (int itemId : target.getItems().keySet())
                            {
                                room.addItem(itemId);
                                Logger.logI(
                                    "item <" + itemId + "> added to room <" + room.getId() + ">");
                                target.removeItem(itemId);

                                ret = 5;
                            }
                        }
                    }
                }

                int proposedRoom = model.getActors().get(sourceId).getProposedRoomId();

                if (proposedRoom > -1)
                {
                    moveActor(model, sourceId, proposedRoom);
                }

                return ret;
            }

            return -1;
        }

        return -2;
    }

    /**
     * Get a String descriptor of the type of a given {@link Special}.
     * <p>
     * To be used for presentation ({@link
     * com.semaphore_soft.apps.cypher.PortalRenderer}) purposes ONLY.
     *
     * @param id int: The logical reference ID of the desired {@link Special}.
     *
     * @return String:
     * <ul>
     * <li>"help": The given {@link Special} has a helping effect, e.g.
     * positive stat modifier, buff, heal.</li>
     * <li>"harm": The given {@link Special} has a harming effect, e.g.
     * negative stat modifier, de-buff, damage.</li>
     * </ul>
     *
     * @see Special
     * @see Special#getEffects()
     * @see com.semaphore_soft.apps.cypher.PortalRenderer
     * @see PortalActivity#postSpecialResult(int, int, int, int)
     */
    public static String getSpecialTypeDescriptor(final Model model, final int id)
    {
        ArrayList<Effect.E_EFFECT> specialEffects = model.getSpecials().get(id).getEffects();

        if (specialEffects.contains(Effect.E_EFFECT.HEAL) ||
            specialEffects.contains(Effect.E_EFFECT.ATTACK_RATING_UP) ||
            specialEffects.contains(Effect.E_EFFECT.DEFENCE_RATING_UP) ||
            specialEffects.contains(Effect.E_EFFECT.HEALTH_MAXIMUM_UP) ||
            specialEffects.contains(Effect.E_EFFECT.SPECIAL_MAXIMUM_UP) ||
            specialEffects.contains(Effect.E_EFFECT.SPECIAL_RATING_UP))
        {
            return "help";
        }

        return "harm";
    }

    /**
     * Get the {@link Special.E_TARGETING_TYPE} of a given {@link Special}
     * ability.
     *
     * @param id int: The logical reference ID of the desired {@link Special}
     *           ability.
     *
     * @return {@link Special.E_TARGETING_TYPE}: The {@link
     * Special.E_TARGETING_TYPE type} of the given {@link Special} ability.
     */
    public static Special.E_TARGETING_TYPE getSpecialTargetingType(final Model model, final int id)
    {
        Special special = model.getSpecials().get(id);

        if (special != null)
        {
            return special.getTargetingType();
        }

        return Special.E_TARGETING_TYPE.SINGLE_NON_PLAYER;
    }

    @NonNull
    public static ArrayList<Integer> getPlayerActorIds(final Model model)
    {
        ArrayList<Integer> playerActorIds = new ArrayList<>();

        for (Actor actor : model.getActors().values())
        {
            if (actor.isPlayer())
            {
                playerActorIds.add(actor.getId());
            }
        }

        return playerActorIds;
    }

    public static boolean areAllPlayersDead(final Model model)
    {
        ArrayList<Integer> playersIds     = getPlayerActorIds(model);
        int                numPlayers     = playersIds.size();
        int                numDeadPlayers = 0;

        for (int id : playersIds)
        {
            Actor actor = getActor(model, id);

            if (actor != null && actor.getHealthCurrent() <= 0)
            {
                ++numDeadPlayers;
            }
        }

        return numDeadPlayers >= numPlayers;
    }

    @Nullable
    public static Item getItem(final Model model, final int itemId)
    {
        return model.getItems().get(itemId);
    }

    public static void removeItem(final Model model, final int itemId)
    {
        model.getItems().remove(itemId);
    }

    @Nullable
    public static Special getSpecial(final Model model, final int specialId)
    {
        return model.getSpecials().get(specialId);
    }
}
