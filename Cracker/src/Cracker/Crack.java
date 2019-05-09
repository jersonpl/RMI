package Cracker;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import RMIserver.RMIServerInterface;

public class Crack extends Thread {

    public RMIServerInterface stub;
    public String fileName;
    public CrackerUI cracker;
    public byte[] fileBytes;
    public String checkSum;
    public long start;
    public long end;

    public Crack(String ip, CrackerUI cracker, String fileName, byte[] fileBytes, String checkSum, long start, long end) {
        this.checkSum = checkSum;
        this.end = end;
        this.fileBytes = fileBytes;
        this.fileName = fileName;
        this.start = start;
        this.cracker = cracker;
        try {
            Registry registry = LocateRegistry.getRegistry(ip, 1099);
            stub = (RMIServerInterface) registry.lookup("RMIServerInterface");
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            String response = stub.crackearArchivo(fileName, fileBytes, checkSum, start, end);
            if (!"NOT FOUND".equals(response)) {
                cracker.DetenerLosDemasNodos(response);
            }
        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
