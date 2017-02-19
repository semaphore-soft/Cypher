package com.semaphore_soft.apps.cypher.game;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.semaphore_soft.apps.cypher.utils.CollectionManager;
import com.semaphore_soft.apps.cypher.utils.GameStatLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

/**
 * Created by Scorple on 2/18/2017.
 */

public class GameMaster
{
    private static Model model;

    public static void setModel(Model model)
    {
        GameMaster.model = model;
    }

    public static Room generateRoom(Context context)
    {
        return generateRoom(context, CollectionManager.getNextID(model.getRooms()));
    }

    public static Room generateRoom(Context context, int id)
    {
        return generateRoom(context, id, -1);
    }

    public static Room generateRoom(Context context, int id, int mark)
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

        model.addRoom(id, room);

        return room;
    }

    public static Room getRoom(int id)
    {
        return model.getRooms().get(id);
    }

    public static int getRoomMarkerId(int id)
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

    public static ArrayList<Integer> getAdjacentRoomIds(int startRoomId)
    {
        return model.getMap().getAdjacentRooms(startRoomId);
    }

    public static Actor getActor(int id)
    {
        return model.getActors().get(id);
    }

    public static int getActorMakerId(int id)
    {
        return model.getActors().get(id).getMarker();
    }

    public static int getActorRoomId(int id)
    {
        return model.getActors().get(id).getRoom();
    }

    public static Room getActorRoom(int id)
    {
        return model.getRooms().get(model.getActors().get(id).getRoom());
    }

    public static boolean getActorIsPlayer(int id)
    {
        return model.getActors().get(id).isPlayer();
    }

    //returns
    //1: marker attached to a room
    //0: marker attached to a player
    //-1: marker not attached to anything
    public static int getMarkerAttachment(int markId)
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

    public static int getIdByMarker(int markId)
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
    public static int moveActor(int actorId, int endRoomId)
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
    private static int getValidPath(int startRoomId, int endRoomId)
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

    public static int openDoor(int startRoomId,
                               int endRoomId,
                               short sideOfStartRoom,
                               short sideOfEndRoom)
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

                Hashtable<Integer, Pair<Short, Short>>
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

    private static int getValidAdjacency(int startRoomId,
                                         int endRoomId,
                                         short sideOfStartRoom,
                                         short sideOfEndRoom)
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

    private static int getValidAdjacencyProposedRoom(int proposedRoomPositionX,
                                                     int proposedRoomPositionY,
                                                     short proposedRoomRotation,
                                                     int testRoomId,
                                                     int proposedRoomId)
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

    public static Hashtable<Integer, Actor> getEnemyTargets(int actorId)
    {
        Actor                     actor   = model.getActors().get(actorId);
        Room                      room    = model.getRooms().get(actor.getRoom());
        Hashtable<Integer, Actor> targets = new Hashtable<>();

        System.out.println("looking for enemy targets in room: " + actor.getRoom());

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

    public static Hashtable<Integer, Actor> getPlayerTargets(int actorId)
    {
        Actor                     actor   = model.getActors().get(actorId);
        Room                      room    = model.getRooms().get(actor.getRoom());
        Hashtable<Integer, Actor> targets = new Hashtable<>();

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

    public static ArrayList<Integer> getPlayerTargetIds(int actorId)
    {
        Actor              actor   = model.getActors().get(actorId);
        Room               room    = model.getRooms().get(actor.getRoom());
        ArrayList<Integer> targets = new ArrayList<>();

        System.out.println("looking for player targets for : " + actorId);
        System.out.println("looking for player targets in room: " + actor.getRoom());

        for (int targetId : room.getResidentActors())
        {
            Actor target = model.getActors().get(targetId);
            if (target.isPlayer())
            {
                System.out.println("found valid target: " + target.getId());
                targets.add(targetId);
            }
        }

        return targets;
    }

    public static Hashtable<Integer, Special> getSpecials(int actorId)
    {
        System.out.println("looking for specials for actor: " + actorId);
        Actor actor = model.getActors().get(actorId);
        for (int id : actor.getSpecials().keySet())
        {
            System.out.println("found special: " + actor.getSpecials().get(id).getName());
        }
        return actor.getSpecials();
    }

    public static void setActorState(int id, Actor.E_STATE state)
    {
        model.getActors().get(id).setState(state);
    }

    //returns
    //1: success, enemy dead
    //0: success, enemy still alive
    public static int attack(int attackerId, int defenderId)
    {
        Actor attacker = model.getActors().get(attackerId);
        Actor defender = model.getActors().get(defenderId);

        attacker.setState(Actor.E_STATE.ATTACK);

        attacker.attack(defender);

        if (defender.getHealthCurrent() <= 0)
        {
            getActorRoom(defenderId).removeActor(defenderId);
            model.getActors().remove(defenderId);

            return 1;
        }

        return 0;
    }

    //returns
    //1: success, killed one or more targets
    //0: success
    //-1: failure, not enough energy
    //-2: failure, no targets in room
    public static int special(int sourceId, int specialId)
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
    public static int special(int sourceId, int targetId, int specialId)
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
}
