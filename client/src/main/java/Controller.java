import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

public class Controller implements Initializable, Runnable {

    public Button send;
    public ListView<String> listView;
    private List<File> clientFileList;
    public static Socket socket;
    private DataInputStream is;
    private DataOutputStream os;
    public TextField textField;         // changed name "text"
    private  byte [] buffer = new byte[1024];

    String clientPath;

    public void sendCommand(ActionEvent actionEvent) {

/*        while (true) {
            try {
                String command = is.readUTF();
                if (command.equals("./upload")) {               // command identification
                    String fileName = is.readUTF();             // read fileName
                    System.out.println("fileName: " + fileName);
                    long fileLength = is.readLong();            // read fileLength
                    System.out.println("    fileLength: " + fileLength);
                    File file = new File(clientPath + "/" + fileName); // create path
                    if (!file.exists()) {
                        file.createNewFile();                                           // create pacifier
                    }
                    try(FileOutputStream fos = new FileOutputStream(file)) {
                        for (long i = 0; i < (fileLength / 1024 == 0 ? 1 : fileLength / 1024); i++) {
                            int bytesRead = is.read(buffer);
                            fos.write(buffer, 0, bytesRead);            // writing
                        }
                    }
                    os.writeUTF("OK");                                  // response
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
*/        try {
            os.writeUTF("./download");
            os.writeUTF(textField.getText());
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // TODO: 7/21/2020 init connect to server
        try{
            socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread.sleep(1000);
            clientFileList = new ArrayList<>();
            clientPath = "./client/src/main/resources/";
            File dir = new File(clientPath);
            if (!dir.exists()) {
                throw new RuntimeException("directory resource not exists on client");
            }
            for (File file : Objects.requireNonNull(dir.listFiles())) {
                clientFileList.add(file);
                listView.getItems().add(file.getName() /*+ " : " + file.length()*/);
            }
            listView.setOnMouseClicked(a -> {
                if (a.getClickCount() == 2) {
                    String fileName = listView.getSelectionModel().getSelectedItem(); // file name
                    File currentFile = findFileByName(fileName);                        // find file by name
                    if (currentFile != null) {                                          // write file
                        try {
                            os.writeUTF("./upload");
                            os.writeUTF(fileName);
                            os.writeLong(currentFile.length());
                            FileInputStream fis = new FileInputStream(currentFile);
 //                           byte [] buffer = new byte[1024];
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
            });

          /*  String command = is.readUTF();
            if (command.equals("./upload")) {               // command identification
                String fileName = is.readUTF();             // read fileName
                System.out.print("fileName: " + fileName);
                long fileLength = is.readLong();            // read fileLength
                System.out.println("    fileLength: " + fileLength);
                File file = new File(clientPath + "/" + fileName); // create path
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
            }*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File findFileByName(String fileName) {
        for (File file : clientFileList) {
            if (file.getName().equals(fileName)){
                return file;
            }
        }
        return null;
    }

    @Override
    public void run() {
        try{
            String command = is.readUTF();
            if (command.equals("./upload")) {               // command identification
                String fileName = is.readUTF();             // read fileName
                System.out.print("fileName: " + fileName);
                long fileLength = is.readLong();            // read fileLength
                System.out.println("    fileLength: " + fileLength);
                File file = new File(clientPath + "/" + fileName); // create path
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
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
