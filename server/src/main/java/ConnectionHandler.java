import java.io.*;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ConnectionHandler implements Runnable {
    private DataInputStream is;
    private DataOutputStream os;
    private List<File> serverFileList;
    String serverPath;

    public ConnectionHandler(Socket socket) throws IOException, InterruptedException {
        System.out.println("Connection accepted");
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        Thread.sleep(2000);
    }



    @Override
    public void run() {
        byte [] buffer = new byte[1024];
        serverFileList = new ArrayList<>();
        serverPath = "./server/src/main/resources/";
        File dir = new File(serverPath);
        if (!dir.exists()) {
            throw new RuntimeException("directory resource not exists on client");
        }
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            serverFileList.add(file);
        }

        while (true) {
            try {
                String command = is.readUTF();
                if (command.equals("./download")){
                    System.out.println("./download");
                    String fileName = is.readUTF();             // read fileName
//                    System.out.println("fileName: " + fileName);
                    File currentFile = findFileByName(fileName);                        // find file by name
                    if (currentFile != null) {                                          // write file
                        try {
                            os.writeUTF("./upload");
                            os.writeUTF(fileName);
                            os.writeLong(currentFile.length());
                            FileInputStream fis = new FileInputStream(currentFile);
                            while (fis.available() > 0) {
                                int bytesRead = fis.read(buffer);
                                os.write(buffer, 0, bytesRead);
                            }
                            os.flush();                                                 // flush buffer
                            String response = is.readUTF();                             // wait response
                            System.out.println(response);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
               else if (command.equals("./upload")) {               // command identification
                    String fileName = is.readUTF();             // read fileName
                    System.out.print("fileName: " + fileName);
                    long fileLength = is.readLong();            // read fileLength
                    System.out.println("    fileLength: " + fileLength);
                    File file = new File(Server.serverPath + "/" + fileName); // create path
                    if (!file.exists()) {
                        file.createNewFile();                                           // create pacifier
                    }
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                            int bytesRead = is.read(buffer);
                            fos.write(buffer, 0, bytesRead);            // writing -> file
                        }
                    }
                    os.writeUTF("OK");                                  // response
                }
/*
               if(command.equals("close")) {       // ПОКА  НЕ ПРИДУМАЛ КАК ОБРАБОТАТЬ ЗАКРЫВАНИЕ ОКНА ЮЗЕРА ЕСЛИ ИДЕТ ЗАПИСЬ ДАННЫХ
                    is.close();
                    os.close();
                    throw new InterruptedException("Client lives");
                }
*/
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private File findFileByName(String fileName) {
        for (File file : serverFileList) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }
}
