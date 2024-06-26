package com.dji.importSDKDemo;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

public class loggerr {
    private FileOutputStream fos;
    private OutputStreamWriter osw;

    public loggerr(String filename) throws IOException {
        fos = new FileOutputStream(filename, true); // true for append mode
        osw = new OutputStreamWriter(fos);
    }

    public void logData(float altitude, float yaw,float pitch, float roll, double lat, double lng) {
        try {
            String logEntry = "Altitude: " + altitude + ", Yaw: " + yaw + ", Pitch: "+ pitch + ", Roll: "+roll + ", Lat: "+ lat + ", Lng: "+lng + "\n";
            osw.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            osw.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
