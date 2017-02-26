package com.semaphore_soft.apps.cypher.game;

import android.util.Pair;

import com.semaphore_soft.apps.cypher.utils.Logger;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Scorple on 1/27/2017.
 */

public class Map
{
    public static final short NORTH = 0;
    public static final short EAST  = 1;
    public static final short SOUTH = 2;
    public static final short WEST  = 3;

    private int[][]   roomIDMap;
    private short[][] roomRotationMap;

    public Map()
    {
        roomIDMap = new int[31][31];
        roomRotationMap = new short[31][31];
        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                roomIDMap[i][j] = -1;
                roomRotationMap[i][j] = -1;
            }
        }
    }

    public void init(int id)
    {
        roomIDMap[15][15] = id;
        roomRotationMap[15][15] = 0;
    }

    public int getRoomID(int x, int y)
    {
        return roomIDMap[x][y];
    }

    public void setRoomID(int x, int y, int id)
    {
        roomIDMap[x][y] = id;
    }

    public short getRotation(int id)
    {
        Pair<Integer, Integer> pos = getPosition(id);

        return getRotation(pos.first, pos.second);
    }

    public short getRotation(int x, int y)
    {
        return roomRotationMap[x][y];
    }

    public void setRoomRotation(int x, int y, short rot)
    {
        roomRotationMap[x][y] = rot;
    }

    public Pair<Integer, Integer> getPosition(int id)
    {
        Pair<Integer, Integer> res = new Pair<>(-1, -1);

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                if (roomIDMap[i][j] == id)
                {
                    return new Pair<>(i, j);
                }
            }
        }

        return res;
    }

    public void insert(int idA, short sideOfA, int idB, short sideOfB)
    {
        if (idA < 0 || idB < 0)
        {
            return;
        }

        boolean foundA = false;
        int     ax     = -1;
        int     ay     = -1;

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                if (roomIDMap[i][j] == idA)
                {
                    ax = i;
                    ay = j;
                    foundA = true;
                    break;
                }
            }
        }

        if (!foundA)
        {
            return;
        }

        short rotA = roomRotationMap[ax][ay];
        short rotB = -1;

        if ((sideOfA + 2) % 4 == sideOfB)
        {
            rotB = rotA;
        }
        else if ((sideOfA + 1) % 4 == sideOfB)
        {
            rotB = (short) ((3 + rotA) % 4);
        }
        else if (sideOfA == sideOfB)
        {
            rotB = (short) ((2 + rotA) % 4);
        }
        else if ((sideOfA + 3) % 4 == sideOfB)
        {
            rotB = (short) ((1 + rotA) % 4);
        }

        Logger.logI("idA: " + idA, 2);
        Logger.logI("sideOfA: " + sideOfA, 2);
        Logger.logI("rotA: " + rotA, 2);
        Logger.logI("idB: " + idB, 2);
        Logger.logI("sideOfB: " + sideOfB, 2);
        Logger.logI("rotB: " + rotB, 2);

        if ((rotA == 0 && sideOfA == 0) || (rotA == 1 && sideOfA == 1) ||
            (rotA == 2 && sideOfA == 2) || (rotA == 3 && sideOfA == 3))
        {
            //b is north of a
            roomIDMap[ax][ay - 1] = idB;
            roomRotationMap[ax][ay - 1] = rotB;
        }
        else if ((rotA == 0 && sideOfA == 1) || (rotA == 1 && sideOfA == 2) ||
                 (rotA == 2 && sideOfA == 3) || (rotA == 3 && sideOfA == 0))
        {
            //b is east of a
            roomIDMap[ax + 1][ay] = idB;
            roomRotationMap[ax + 1][ay] = rotB;
        }
        else if ((rotA == 0 && sideOfA == 2) || (rotA == 1 && sideOfA == 3) ||
                 (rotA == 2 && sideOfA == 0) || (rotA == 3 && sideOfA == 1))
        {
            //b is south of a
            roomIDMap[ax][ay + 1] = idB;
            roomRotationMap[ax][ay + 1] = rotB;
        }
        else
        {
            //b is west of a
            roomIDMap[ax - 1][ay] = idB;
            roomRotationMap[ax - 1][ay] = rotB;
        }
    }

    public Pair<Integer, Integer> getProposedPosition(int idA, short sideOfA)
    {
        Pair<Integer, Integer> res = new Pair<>(-1, -1);

        if (idA < 0)
        {
            return res;
        }

        boolean foundA = false;
        int     ax     = -1;
        int     ay     = -1;

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                if (roomIDMap[i][j] == idA)
                {
                    ax = i;
                    ay = j;
                    foundA = true;
                    break;
                }
            }
        }

        if (!foundA)
        {
            return res;
        }

        short rotA = roomRotationMap[ax][ay];

        if ((rotA == 0 && sideOfA == 0) || (rotA == 1 && sideOfA == 1) ||
            (rotA == 2 && sideOfA == 2) || (rotA == 3 && sideOfA == 3))
        {
            //b is north of a
            res = new Pair<>(ax, ay - 1);
        }
        else if ((rotA == 0 && sideOfA == 1) || (rotA == 1 && sideOfA == 2) ||
                 (rotA == 2 && sideOfA == 3) || (rotA == 3 && sideOfA == 0))
        {
            //b is east of a
            res = new Pair<>(ax + 1, ay);
        }
        else if ((rotA == 0 && sideOfA == 2) || (rotA == 1 && sideOfA == 3) ||
                 (rotA == 2 && sideOfA == 0) || (rotA == 3 && sideOfA == 1))
        {
            //b is south of a
            res = new Pair<>(ax, ay + 1);
        }
        else
        {
            //b is west of a
            res = new Pair<>(ax - 1, ay);
        }

        return res;
    }

    public short getProposedRotation(int idA, short sideOfA, short sideOfB)
    {
        if (idA < 0)
        {
            return -1;
        }

        boolean foundA = false;
        int     ax     = -1;
        int     ay     = -1;

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                if (roomIDMap[i][j] == idA)
                {
                    ax = i;
                    ay = j;
                    foundA = true;
                    break;
                }
            }
        }

        if (!foundA)
        {
            return -1;
        }

        short rotA = roomRotationMap[ax][ay];
        short rotB = -1;

        if ((sideOfA + 2) % 4 == sideOfB)
        {
            rotB = rotA;
        }
        else if ((sideOfA + 1) % 4 == sideOfB)
        {
            rotB = (short) ((3 + rotA) % 4);
        }
        else if (sideOfA == sideOfB)
        {
            rotB = (short) ((2 + rotA) % 4);
        }
        else if ((sideOfA + 3) % 4 == sideOfB)
        {
            rotB = (short) ((1 + rotA) % 4);
        }

        return rotB;
    }

    public int getRoomFromPositionInDirection(int x, int y, short dir)
    {
        int res = -1;

        switch (dir)
        {
            case 0:
                res = roomIDMap[x][y - 1];
                break;
            case 1:
                res = roomIDMap[x + 1][y];
                break;
            case 2:
                res = roomIDMap[x][y + 1];
                break;
            case 3:
                res = roomIDMap[x - 1][y];
                break;
            default:
                break;
        }

        return res;
    }

    public short checkAdjacent(int a, int b)
    {
        if (a < 0 || b < 0)
        {
            return -1;
        }

        boolean foundA = false;
        boolean foundB = false;
        int     ax     = -1;
        int     ay     = -1;
        int     bx     = -1;
        int     by     = -1;

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                if (roomIDMap[i][j] == a)
                {
                    ax = i;
                    ay = j;
                    foundA = true;
                }
                else if (roomIDMap[i][j] == b)
                {
                    bx = i;
                    by = j;
                    foundB = true;
                }
                if (foundA && foundB)
                {
                    break;
                }
            }
        }

        if (ax == bx && ay == (by - 1))
        {
            return 0; //a is north of b
        }
        if (ay == by && ax == (bx + 1))
        {
            return 1; //a is east of b
        }
        if (ax == bx && ay == (by + 1))
        {
            return 2; //a is south of b
        }
        if (ay == by && ax == (bx - 1))
        {
            return 3; //a is west of b
        }

        return -1;
    }

    public ArrayList<Integer> getAdjacentRooms(int idA)

    {
        ArrayList<Integer>     res = new ArrayList<>();
        Pair<Integer, Integer> pos = getPosition(idA);

        for (int i = 0; i < 4; ++i)
        {
            int testRoom = getRoomFromPositionInDirection(pos.first, pos.second, (short) i);
            if (testRoom > -1)
            {
                res.add(testRoom);
            }
        }

        return res;
    }

    public ConcurrentHashMap<Integer, Pair<Short, Short>> getAdjacentRoomsAndWalls(int idA)
    {
        ConcurrentHashMap<Integer, Pair<Short, Short>> res = new ConcurrentHashMap<>();
        Pair<Integer, Integer>                         pos = getPosition(idA);
        short                                          rot = getRotation(pos.first, pos.second);

        for (int i = 0; i < 4; ++i)
        {
            int testRoom = getRoomFromPositionInDirection(pos.first, pos.second, (short) i);
            if (testRoom > -1)
            {
                Pair<Integer, Integer> testPos = getPosition(testRoom);
                Pair<Short, Short> walls = new Pair<>((short) ((rot + i) % 4),
                                                      (short) ((getRotation(testPos.first,
                                                                            testPos.second) +
                                                                i + 2) % 4));
                res.put(testRoom, walls);
            }
        }

        return res;
    }

    public short getDirectionOfAdjacentRoom(int idA, int idB)
    {
        Pair<Integer, Integer> roomAPos = getPosition(idA);
        Pair<Integer, Integer> roomBPos = getPosition(idB);

        if (roomAPos.second > roomBPos.second)
        {
            //room B is north of room A
            return 0;
        }
        else if (roomAPos.first < roomBPos.first)
        {
            //room B is east of room A
            return 1;
        }
        else if (roomAPos.second < roomBPos.second)
        {
            //room B is south of room A
            return 2;
        }
        else if (roomAPos.first > roomBPos.first)
        {
            //room B is west of room A
            return 3;
        }

        return -1;
    }

    public Pair<Short, Short> getWallsBetweenAdjacentRooms(int idA, int idB)
    {
        short roomARot = getRotation(idA);
        short roomBRot = getRotation(idB);

        short dir = getDirectionOfAdjacentRoom(idA, idB);

        return new Pair<>((short) ((roomARot + dir) % 4), (short) ((roomBRot + dir + 2) % 4));
    }

    public void print()
    {
        String output = "";

        for (int i = 0; i < 31; ++i)
        {
            for (int j = 0; j < 31; ++j)
            {
                output += roomIDMap[j][i] + ":" + roomRotationMap[j][i] + " ";
            }
            output += "\n";
        }

        Logger.logI(output, 2);
    }
}
