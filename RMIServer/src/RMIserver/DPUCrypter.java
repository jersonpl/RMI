package RMIserver;

import java.awt.List;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.DigestInputStream;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

public class DPUCrypter {

    public static int CryptFileUsingAES(boolean encrypt, String key, File inputFile, File outputFile, String checkSum) {
        try {
            if(key.length()<16){                
                key=String.format("%16s", key).replace(' ', '0');
            }
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            Cipher cipher = Cipher.getInstance("AES");
            if (encrypt) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
            }

            FileInputStream inputStream = new FileInputStream(inputFile);
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            FileOutputStream outputStream = new FileOutputStream(outputFile);
            outputStream.write(outputBytes);

            inputStream.close();
            outputStream.close();
            if (checkSum != null) {
                try {
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    String checkSumOfDecryptedFile = DPUCrypter.checksum(outputFile, md);
                    if (!(checkSum.equals(checkSumOfDecryptedFile))) {
                        return -2;
                    }
                } catch (Exception err) {

                }
            }
            return 0;

        } catch (NoSuchPaddingException | NoSuchAlgorithmException
                | BadPaddingException
                | IllegalBlockSizeException | IOException e) {

            //e.printStackTrace();
            return -1;
        } catch (InvalidKeyException e) {

            //e.printStackTrace();
            return -2;
        }
    }

    public static String CrackFile(long fromIndex, long toIndex, File inputFile, File outputFile, String checkSum, RMIServer rmiServer) {
        int begin = (int) fromIndex;
        int end = (int) toIndex;
        System.out.println("begin: "+begin+" en: "+end);
        File archivo = null;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            archivo = new File (new File ("").getAbsolutePath()+"\\output2.txt");            
            fr = new FileReader (archivo);
            br = new BufferedReader(fr);
            String linea;
            java.util.List<String> dic = new ArrayList<>();
            while((linea=br.readLine())!=null){
                dic.add(linea.replace(" ", ""));
            }
            Random rand = new Random();
            String filename = inputFile.getName();
            for (int currentIndex = begin; currentIndex <= end; currentIndex++) {                
                if (!rmiServer.seguirBuscando) {
                    break;
                }
                int randomNum = rand.nextInt(end + 1 - currentIndex) + begin;
                if(currentIndex % 100 == 0){
                    System.out.println("Current key: " + dic.get(randomNum) + " / Pending tries: " + (end - currentIndex));
                }
                int internalReturn = CryptFileUsingAES(false, dic.get(randomNum), inputFile, outputFile, checkSum);
                if (internalReturn == 0) {
                    Date endDate = new Date();
                    System.out.println("The key is: " + dic.get(randomNum) + " / ended at " + endDate.toString());
                    return dic.get(randomNum);
                }
                dic.remove(randomNum);

            }
        } catch (IOException e) {
        }
        return "NOT FOUND";

    }

    public static String checksum(File filepath, MessageDigest md) throws IOException {

        // file hashing with DigestInputStream
        try (DigestInputStream dis = new DigestInputStream(new FileInputStream(filepath), md)) {
            while (dis.read() != -1) ; //empty loop to clear the data
            md = dis.getMessageDigest();
        }

        // bytes to hex
        StringBuilder result = new StringBuilder();
        for (byte b : md.digest()) {
            result.append(String.format("%02x", b));
        }
        return result.toString();

    }

}
