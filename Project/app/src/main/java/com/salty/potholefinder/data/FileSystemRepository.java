package com.salty.potholefinder.data;


import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class FileSystemRepository<T> {

    private static String DATA_PATH = "/Data/";
    private static File filesDirectory = null;

    private Context context = null;

    public FileSystemRepository(Context c) {
        context = c;
        filesDirectory = new File(context.getFilesDir() + DATA_PATH);
    }

    public void save(String objectID, T object) {
        if (!filesDirectory.exists())
            filesDirectory.mkdir();

        try {
            FileOutputStream fileOut = new FileOutputStream(filesDirectory + "/" + objectID + ".obj");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(object);
            out.close();
            fileOut.close();
        } catch(IOException i) {
            i.printStackTrace();
        }
    }

    public T get(String objectID) {
        T result = null;

        try {
            FileInputStream fileOut = new FileInputStream(filesDirectory + "/" + objectID + ".obj");
            ObjectInputStream in = new ObjectInputStream(fileOut);
            result = (T)in.readObject();
            in.close();
            fileOut.close();
        } catch(ClassNotFoundException e) {
            e.printStackTrace();
        } catch(IOException i) {
            i.printStackTrace();
        }

        return result;
    }

    public List<T> getAll() {
        ArrayList<T> result = new ArrayList<T>();
        File[] files = filesDirectory.listFiles();

        for(File file : files) {
            String objectID = file.getName().substring(0, file.getName().length() - 4);
            result.add(this.get(objectID));
        }

        return result;
    }

    public void delete(String objectID) {
        File file = null;
        try {
            file = new File(filesDirectory + "/" + objectID + ".obj");
            file.delete();
        } catch (Exception e) {
            // Rien
        }
    }

    public void deleteAll() {
        File[] files = filesDirectory.listFiles();

        for(File file : files) {
            String objectID = file.getName().substring(0, file.getName().length() - 4);
            this.delete(objectID);
        }
    }
}
