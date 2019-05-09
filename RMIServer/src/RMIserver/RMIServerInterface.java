package RMIserver;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RMIServerInterface extends Remote{

    public String crackearArchivo(String fileName, byte[] fileBytes, String checkSum, long start, long end) throws RemoteException;

    public void DetenerCracker() throws RemoteException;
}