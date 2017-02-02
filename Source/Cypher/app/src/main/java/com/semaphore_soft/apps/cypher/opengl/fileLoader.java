package com.semaphore_soft.apps.cypher.opengl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Created by ceroj on 2/1/2017.
 */

public class fileLoader
{
    String filename = null;
    Vector<Float> vertices = null;
    Vector<Float> colors = null;
    Vector<Short> indices= null;
    Vector<Float> texCoord= null;

    public fileLoader(){ }
    public fileLoader(String filename){ this.filename = filename;}

    public void setName(String filename) {this.filename = filename;}

    public void getData(String filename) {
        if (filename != null)
        {
            BufferedReader br = null;
            FileReader     fr = null;
            try
            {
                fr = new FileReader(filename);
                br = new BufferedReader(fr);

                String sCurrentLine;

                br = new BufferedReader(new FileReader(filename));

                while ((sCurrentLine = br.readLine()) != null)
                {
                    getToken(sCurrentLine);
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
                        br.close();

                    if (fr != null)
                        fr.close();
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

    public void getToken(String sCurrentLine) {
        StringTokenizer st = new StringTokenizer(sCurrentLine, " ");
        String tempString;
        while (st.hasMoreTokens()) {
            tempString = st.nextToken();
            if (tempString == "v"){
                vertices.add(Float.parseFloat(st.nextToken()));
                vertices.add(Float.parseFloat(st.nextToken()));
                vertices.add(Float.parseFloat(st.nextToken()));
            }
            else if (tempString == "f"){
                StringTokenizer faceToken = new StringTokenizer(tempString, "/");
                while (faceToken.hasMoreTokens()) {
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                }
                tempString = st.nextToken();
                faceToken = new StringTokenizer(tempString, "/");
                while (faceToken.hasMoreTokens()) {
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                }
                tempString = st.nextToken();
                faceToken = new StringTokenizer(tempString, "/");
                while (faceToken.hasMoreTokens()) {
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                    indices.add(Short.parseShort(faceToken.nextToken()));
                }
            }
        }
    }

    public Vector<Float> getVerts(){
        return vertices;
    }
    public Vector<Short> getIndices(){
        return indices;
    }
}
