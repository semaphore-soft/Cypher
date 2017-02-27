package com.semaphore_soft.apps.cypher.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.util.Pair;

import com.semaphore_soft.apps.cypher.utils.CollectionManager;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;
import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

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
    private static Model model;

    /**
     * Set the {@link Model} which describes the game state.
     *
     * @param model {@link Model}: The {@link Model} which describes game
     *              state.
     */
    public static void setModel(final Model model)
    {
        GameMaster.model = model;
    }

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
    public static Room generateRoom(final Context context)
    {
        return generateRoom(context, CollectionManager.getNextID(model.getRooms()));
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
    public static Room generateRoom(final Context context, final int id)
    {
        return generateRoom(context, id, -1);
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
    public static Room generateRoom(final Context context, final int id, final int mark)
    {
        Log.d("GameMaster", "Generating room with id " + id + " at mark " + mark);

        Room room = new Room(id, mark);

        int numEnemies = (int) (Math.random() * 4);
        if (numEnemies > 0)
        {
            ArrayList<String> enemyList = GameStatLoader.getList(context, "enemies");
            if (enemyList != null)
            {
                for (int i = 0; i < numEnemies; ++i)
                {
                    Collections.shuffle(enemyList);
                    String enemyName = enemyList.get(0);

                    Actor enemy =
                        new Actor(CollectionManager.getNextID(model.getActors()), id, enemyName);
                    GameStatLoader.loadActorStats(enemy, enemyName, model.getSpecials(), context);

                    model.addActor(enemy.getId(), enemy);
                    room.addActor(enemy.getId());
                }
            }
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
    public static Room getRoom(final int id)
    {
        return model.getRooms().get(id);
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
    public static int getRoomMarkerId(final int id)
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
    public static int getRoomIdByMarkerId(final int markerId)
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
    public static ArrayList<Integer> getPlacedRoomMarkerIds()
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
     */
    @NonNull
    public static ArrayList<Integer> getAdjacentRoomIds(final int startRoomId)
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
     */
    public static short getSideOfRoomFrom(final int startRoomId, final int endRoomId)
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
    public static int getPlayersInRoom(final int roomId)
    {
        int  res  = 0;
        Room room = model.getRooms().get(roomId);

        for (int id : room.getResidentActors())
        {
            if (model.getActors().get(id).isPlayer())
            {
                ++res;
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
    public static int getEnemiesInRoom(final int roomId)
    {
        int  res  = 0;
        Room room = model.getRooms().get(roomId);

        for (int id : room.getResidentActors())
        {
            if (!model.getActors().get(id).isPlayer())
            {
                ++res;
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
    public static Actor getActor(final int id)
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
    public static int getActorMakerId(final int id)
    {
        return model.getActors().get(id).getMarker();
    }

    /**
     * Get the logical reference ID of the {@link Room} a given {@link Actor}
     * associates with, or considers itself to be a resident of.
     *
     * @param id int The logical reference ID of the desired {@link Actor}.
     *
     * @return int: The logical reference ID of the {@link Room} the given
     * {@link Actor} associates with, or considers itself to be a resident of.
     *
     * @see Actor
     * @see Room
     */
    public static int getActorRoomId(final int id)
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
    public static Room getActorRoom(final int id)
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
    public static boolean getActorIsPlayer(final int id)
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
    public static int getMarkerAttachment(final int markId)
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
    public static int getIdByMarker(final int markId)
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
     */
    public static int moveActor(final int actorId, final int endRoomId)
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

        int pathRes = getValidPath(actor.getRoom(), endRoomId);

        switch (pathRes)
        {
            case 0:
                Room startRoom = model.getRooms().get(actor.getRoom());
                startRoom.removeActor(actorId);

                endRoom.addActor(actorId);

                actor.setRoom(endRoomId);

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

    /**
     * Check whether or not there is a valid path between two given {@link Room
     * Rooms} and return a code describing if the path is valid or, if not, why
     * not.
     * <p>
     * A path between two {@link Room Rooms} is considered valid if and only if
     * there is an open door in the wall each connecting each {@link Room} with
     * the opposite {@link Room}.
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
     */
    public static int getValidPath(final int startRoomId, final int endRoomId)
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
     * given the side, or wall, of each {@link Room} the door should be in and
     * return a code describing success or failure.
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
     */
    public static int openDoor(final int startRoomId,
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
            if (getValidAdjacency(startRoomId, endRoomId, sideOfStartRoom, sideOfEndRoom) == 0)
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
        }

        return -1;
    }

    /**
     * Check whether or not two given walls of two given {@link Room Rooms}
     * form a valid adjacency and return a code describing the validity.
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
     */
    private static int getValidAdjacency(final int startRoomId,
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
                if (getValidAdjacencyProposedRoom(proposedEndRoomPosition.first,
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
     */
    private static int getValidAdjacencyProposedRoom(final int proposedRoomPositionX,
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

    public static ConcurrentHashMap<Integer, Actor> getPlayerTargets(final int actorId)
    {
        Actor                             actor   = model.getActors().get(actorId);
        Room                              room    = model.getRooms().get(actor.getRoom());
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

    public static ConcurrentHashMap<Integer, Actor> getNonPlayerTargets(final int actorId)
    {
        Actor                             actor   = model.getActors().get(actorId);
        Room                              room    = model.getRooms().get(actor.getRoom());
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

    public static ArrayList<Integer> getPlayerTargetIds(final int actorId)
    {
        Actor              actor   = model.getActors().get(actorId);
        Room               room    = model.getRooms().get(actor.getRoom());
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

    public static ArrayList<Integer> getNonPlayerTargetIds(final int actorId)
    {
        Actor              actor   = model.getActors().get(actorId);
        Room               room    = model.getRooms().get(actor.getRoom());
        ArrayList<Integer> targets = new ArrayList<>();

        Logger.logD("looking for player targets for: " + actorId);
        Logger.logD("looking for player targets in room: " + actor.getRoom());

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

        return targets;
    }

    public static ConcurrentHashMap<Integer, Special> getSpecials(final int actorId)
    {
        Logger.logI("looking for specials for actor: " + actorId, 1);
        Actor actor = model.getActors().get(actorId);
        for (int id : actor.getSpecials().keySet())
        {
            Logger.logI("found special: " + actor.getSpecials().get(id).getName(), 1);
        }
        return actor.getSpecials();
    }

    public static void setActorState(final int id, final Actor.E_STATE state)
    {
        model.getActors().get(id).setState(state);
    }

    //returns
    //1: success, enemy dead
    //0: success, enemy still alive
    public static int attack(final int attackerId, final int defenderId)
    {
        Actor attacker = model.getActors().get(attackerId);
        Actor defender = model.getActors().get(defenderId);

        attacker.setState(Actor.E_STATE.ATTACK);

        attacker.attack(defender);

        if (defender.getHealthCurrent() <= 0)
        {
            return 1;
        }

        return 0;
    }

    public static void removeActor(final int actorId)
    {
        getActorRoom(actorId).removeActor(actorId);
        model.getActors().remove(actorId);
    }

    public static void removeDeadActors()
    {
        Logger.logD("enter trace");

        ConcurrentHashMap<Integer, Actor> actors           = model.getActors();
        ArrayList<Integer>                markedForRemoval = new ArrayList<>();
        for (int id : actors.keySet())
        {
            if (actors.get(id).getHealthCurrent() <= 0)
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

    //returns
    //1: success, killed one or more targets
    //0: success
    //-1: failure, not enough energy
    //-2: failure, no targets in room
    public static int special(final int sourceId, final int specialId)
    {
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

                for (Actor target : targets)
                {
                    boolean kill = false;

                    if (target.getHealthCurrent() <= 0)
                    {
                        getActorRoom(target.getId()).removeActor(target.getId());

                        kill = true;
                    }

                    if (kill)
                    {
                        return 1;
                    }
                }

                return 0;
            }

            return -1;
        }

        return -2;
    }

    //returns
    //1: success, killed target
    //0: success
    //-1: failure, not enough energy
    //-2: failure, target not valid, not in same room
    public static int special(final int sourceId, final int targetId, final int specialId)
    {
        Actor   source  = model.getActors().get(sourceId);
        Actor   target  = model.getActors().get(targetId);
        Special special = model.getSpecials().get(specialId);

        if (source.getRoom() == target.getRoom())
        {
            if (source.performSpecial(special, target))
            {
                source.setState(Actor.E_STATE.SPECIAL);

                if (target.getHealthCurrent() <= 0)
                {
                    getActorRoom(target.getId()).removeActor(target.getId());

                    return 1;
                }

                return 0;
            }

            return -1;
        }

        return -2;
    }

    public static String getSpecialType(final int id)
    {
        ArrayList<Effect.E_EFFECT> specialEffects = model.getSpecials().get(id).getEffects();

        if (specialEffects.contains(Effect.E_EFFECT.HEAL))
        {
            return "help";
        }
        else if (specialEffects.contains(Effect.E_EFFECT.ATTACK))
        {
            return "harm";
        }

        return "help";
    }
}
