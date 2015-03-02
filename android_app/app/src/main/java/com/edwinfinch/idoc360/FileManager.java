package com.edwinfinch.idoc360;

import android.app.Activity;
import android.content.Context;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

/**
 * Created by edwinfinch on 15-02-10.
 */
public class FileManager {
    public static String DEVICE_FILE = "device.txt";

    public static String getDeviceName(Context context){
        if(getDeviceAddress(context) == null){
            return null;
        }
        return readFromFile(context, DEVICE_FILE).substring(readFromFile(context, DEVICE_FILE).length()-5).replace(":", "");
    }

    public static String getDeviceAddress(Context context){
        return readFromFile(context, DEVICE_FILE);
    }

    public static boolean writeDevice(Context context, String deviceID){
        return writeToFile(context, deviceID, DEVICE_FILE);
    }

    public static String getDeviceStatus(Activity activity, boolean connected){
        if(connected)
            return "iDoc " + FileManager.getDeviceName(activity.getApplicationContext()) + " " + activity.getResources().getString(R.string.ex_con);
        else
            return "iDoc " + FileManager.getDeviceName(activity.getApplicationContext()) + " " + activity.getResources().getString(R.string.ex_dis);
    }

    public static boolean deleteDevice(Context context){
        File devFile = new File(context.getFilesDir() + File.separator + DEVICE_FILE);
        return devFile.delete();
    }

    private static boolean writeToFile(Context context, String data, String filename){
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(context.getFilesDir() + File.separator + filename)));
            bufferedWriter.write(data);
            bufferedWriter.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String readFromFile(Context context, String fileName) {
        StringBuilder builder;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(new File(context.getFilesDir() + File.separator + fileName)));
            String read;
            builder = new StringBuilder("");

            while ((read = bufferedReader.readLine()) != null) {
                builder.append(read);
            }
            bufferedReader.close();
        }
        catch(Exception e){
            e.printStackTrace();
            return null;
        }
        String ret = builder.toString();
        return ret;
    }
}
