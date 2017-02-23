package com.semaphore_soft.apps.cypher.game;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.semaphore_soft.apps.cypher.utils.CollectionManager;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;
import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scorple on 2/18/2017.
 */

public class GameMaster
{
    private static Model model;

    public static void setModel(final Model model)
    {
        GameMaster.model = model;
    }

    public static Room generateRoom(final Context context)
    {
        return generateRoom(context, CollectionManager.getNextID(model.getRooms()));
    }

    public static Room generateRoom(final Context context, final int id)
    {
        return generateRoom(context, id, -1);
    }

    public static Room generateRoom(final Context context, final int id, final int mark)
    {
        Log.d("GameMaster", "Generating room with id " + id + " at mark " + mark);

        Room room = new Room(id, mark);

        ArrayList<String> enemyList = GameStatLoader.getList(context, "enemies");
        if (enemyList != null)
        {
            int numEnemies = (int) (Math.random() * 4);
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

    public static Room getRoom(final int id)
    {
        return model.getRooms().get(id);
    }

    public static int getRoomMarkerId(final int id)
    {
        return model.getRooms().get(id).getMarker();
    }

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

    public static ArrayList<Integer> getAdjacentRoomIds(final int startRoomId)
    {
        return model.getMap().getAdjacentRooms(startRoomId);
    }

    public static short getSideOfRoomFrom(final int startRoomId, final int endRoomId)
    {
        return model.getMap().getWallsBetweenAdjacentRooms(startRoomId, endRoomId).second;
    }

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

    public static Actor getActor(final int id)
    {
        return model.getActors().get(id);
    }

    public static int getActorMakerId(final int id)
    {
        return model.getActors().get(id).getMarker();
    }

    public static int getActorRoomId(final int id)
    {
        return model.getActors().get(id).getRoom();
    }

    public static Room getActorRoom(final int id)
    {
        return model.getRooms().get(model.getActors().get(id).getRoom());
    }

    public static boolean getActorIsPlayer(final int id)
    {
        return model.getActors().get(id).isPlayer();
    }

    //returns
    //1: marker attached to a room
    //0: marker attached to a player
    //-1: marker not attached to anything
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

    //returns
    //2: success, start room established
    //1: success, remain in room
    //0: success, actor moves from old room to new room
    //-1: failure, invalid path, bad connection
    //-2: failure, invalid path, rooms not adjacent
    //-3: failure, room not placed
    //-4: failure, unknown
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

    //returns
    //0: success, valid path
    //-1: failure, invalid path, bad connection
    //-2: failure, invalid path, rooms not adjacent
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

        System.out.println("looking for player targets in room: " + actor.getRoom());

        for (int targetId : room.getResidentActors())
        {
            Actor target = model.getActors().get(targetId);
            if (target.isPlayer())
            {
                System.out.println("found valid target: " + target.getId());
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

        System.out.println("looking for non-player targets in room: " + actor.getRoom());

        for (int targetId : room.getResidentActors())
        {
            Actor target = model.getActors().get(targetId);
            if (!target.isPlayer())
            {
                System.out.println("found valid target: " + target.getId());
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
        System.out.println("looking for specials for actor: " + actorId);
        Actor actor = model.getActors().get(actorId);
        for (int id : actor.getSpecials().keySet())
        {
            System.out.println("found special: " + actor.getSpecials().get(id).getName());
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
