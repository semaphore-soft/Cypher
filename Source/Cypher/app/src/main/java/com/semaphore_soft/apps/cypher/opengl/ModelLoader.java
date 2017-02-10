package com.semaphore_soft.apps.cypher.opengl;

import android.content.Context;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by ceroj on 2/1/2017.
 */

public class ModelLoader
{
    String filename = null;
    Context context;
    Vector<Float> vertices = null;
    Vector<Float> colors   = null;
    Vector<Short> indices  = null;
    Vector<Float> texCoord = null;

    public ModelLoader()
    {
    }

    public ModelLoader(String filename, Context context)
    {
        this.filename = filename;
        this.context = context;
        vertices = new Vector<>();
        colors = new Vector<>();
        indices = new Vector<>();
        texCoord = new Vector<>();
        getData(filename);
    }

    public void setName(String filename)
    {
        this.filename = filename;
    }

    public void getData(String filename)
    {
        if (filename != null)
        {
            BufferedReader br = null;
            FileReader     fr = null;
            try
            {
                //fr = new FileReader(filename);
                br = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));

                String sCurrentLine;

                //br = new BufferedReader(new FileReader(filename));

                while ((sCurrentLine = br.readLine()) != null)
                {
                    System.out.println(sCurrentLine);
                    getToken(sCurrentLine);
                }

                System.out.println("Number of verts: " + vertices.size() / 3);
                System.out.println("Number of tris: " + indices.size() / 3);
                for (int i = 0; i < indices.size(); i += 3)
                {
                    System.out.println(
                        indices.get(i) + " " + indices.get(i + 1) + " " + indices.get(i + 2));
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            finally
            {
                try
                {
                    if (br != null)
                    {
                        br.close();
                    }

                    if (fr != null)
                    {
                        fr.close();
                    }
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        else
        {
            vertices.add(1f);
            vertices.add(2f);
            vertices.add(1f);
            vertices.add(0f);
            vertices.add(1f);
            vertices.add(3f);
            vertices.add(1f);
            vertices.add(1f);
            vertices.add(1f);
            indices.add((short) 1);
            indices.add((short) 2);
            indices.add((short) 3);

        }
    }

    public void getToken(String sCurrentLine)
    {
        StringTokenizer st = new StringTokenizer(sCurrentLine, " ");
        String          tempString;
        while (st.hasMoreTokens())
        {
            tempString = st.nextToken();
            if (tempString.equals("v"))
            {
                vertices.add(Float.parseFloat(st.nextToken()));
                vertices.add(Float.parseFloat(st.nextToken()));
                vertices.add(Float.parseFloat(st.nextToken()));
            }
            else if (tempString.equals("f"))
            {
                tempString = st.nextToken();
                short           triad[]   = new short[3];
                StringTokenizer faceToken = new StringTokenizer(tempString, "/");
                triad[2] = (short) (Short.parseShort(faceToken.nextToken()) - 1);
                //indices.add((short) (Short.parseShort(faceToken.nextToken()) - 1));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                tempString = st.nextToken();
                faceToken = new StringTokenizer(tempString, "/");
                triad[1] = (short) (Short.parseShort(faceToken.nextToken()) - 1);
                //indices.add((short) (Short.parseShort(faceToken.nextToken()) - 1));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                tempString = st.nextToken();
                faceToken = new StringTokenizer(tempString, "/");
                triad[0] = (short) (Short.parseShort(faceToken.nextToken()) - 1);
                //indices.add((short) (Short.parseShort(faceToken.nextToken()) - 1));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                //indices.add(Short.parseShort(faceToken.nextToken()));
                for (int i = 0; i < 3; ++i)
                {
                    indices.add(triad[i]);
                }
            }
        }
    }

    public Vector<Float> getVerts()
    {
        return vertices;
    }

    public Vector<Short> getIndices()
    {
        return indices;
    }
}
