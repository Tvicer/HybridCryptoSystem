package com.example.cryptocoursework;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Random;
import java.util.ResourceBundle;


public class AppController implements Initializable {
    @FXML
    TextField textFieldFilePath;
    @FXML
    RadioButton rdbtnSend;
    @FXML
    RadioButton rdbtnDownload;
    @FXML
    RadioButton rdbtnCBC;
    @FXML
    RadioButton rdbtnCFB;
    @FXML
    RadioButton rdbtnCTR;
    @FXML
    RadioButton rdbtnECB;
    @FXML
    RadioButton rdbtnOFB;
    @FXML
    RadioButton rdbtnRD;
    @FXML
    RadioButton rdbtnRDH;
    @FXML
    VBox vboxMessages;
    XTR.PublicKey XTROpenKey;
    int[] serpentKey;
    String path = "";
    byte[] IV;
    XTR xtr;

    String getPath() {
        return this.path;
    }

    void setPath(String path) {
        this.path = path;
    }

    private static Random randomizer = new Random(LocalDateTime.now().getNano());
    SymmetricCrypto symmetricCrypto;
    ClientSocketAssistant socketAssistant;
    Modes mode;

    boolean connectStatus = false;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        writeMessage("Client started");
        this.mode = Modes.CBC;
        rdbtnCBC.setSelected(true);
        writeMessage("Mode CBC set");
    }

    @FXML
    protected void connect(ActionEvent event) {
        if (!this.connectStatus) {
            try {
                this.socketAssistant = new ClientSocketAssistant(new Socket("localhost", 1234));
            } catch (IOException e) {
                e.printStackTrace();
            }

            BigInteger[] pKey = socketAssistant.getBigIntegerMessage();

            writeMessage("Client connected to server");

            writeMessage("Public XTR key received");

            this.XTROpenKey = new XTR.PublicKey(pKey[0], pKey[1], new GFP2(pKey[0], pKey[2], pKey[3]), new GFP2(pKey[0], pKey[4], pKey[5]));

            this.xtr = new XTR(Entities.TestMode.MILLER_RABIN, 128);

            this.serpentKey = generateKey(4);
            System.out.println(Arrays.toString(this.serpentKey));

            this.IV = new byte[16];
            randomizer.nextBytes(IV);

            writeMessage("All keys generated");

            socketAssistant.sendMessage(xtr.encryptKey(intArrayToByte(this.serpentKey), this.XTROpenKey, pKey[6]));
            socketAssistant.sendMessage(this.mode.toString());
            socketAssistant.sendMessage(IV);

            writeMessage("Encrypted Serpent key sent");

            this.symmetricCrypto = new SymmetricCrypto(this.mode, new Serpent(this.serpentKey), this.IV);
            this.connectStatus = true;

            writeMessage("Ready to work");
        }
    }

    private void writeMessage(String str) {
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setPadding(new Insets(5, 5, 5, 5));
        TextFlow textFlow = new TextFlow(new Text(str));
        textFlow.setStyle("-fx-background-color: rgb(233,233,235);" + " -fx-background-radius: 20px;");
        textFlow.setPadding(new Insets(5, 10, 5, 10));

        hBox.getChildren().add(textFlow);
        vboxMessages.getChildren().add(hBox);
    }

    @FXML
    protected void choosingFile(ActionEvent event) {
        textFieldFilePath.clear();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(null);
        setPath(file.getAbsolutePath());
        textFieldFilePath.appendText(path);
        writeMessage("File " + file.getName() + " selected");
    }

    @FXML
    protected void closeAssistant(ActionEvent event) {
        try {
            socketAssistant.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.connectStatus = false;
        writeMessage("Connection closed");
    }

    @FXML
    protected void updateMode(ActionEvent event) {
        if (!connectStatus) {
            if (rdbtnCBC.isSelected())
                this.mode = Modes.CBC;
            else if (rdbtnCFB.isSelected())
                this.mode = Modes.CFB;
            else if (rdbtnCTR.isSelected())
                this.mode = Modes.CTR;
            else if (rdbtnECB.isSelected())
                this.mode = Modes.ECB;
            else if (rdbtnOFB.isSelected())
                this.mode = Modes.OFB;
            else if (rdbtnRD.isSelected())
                this.mode = Modes.RD;
            else if (rdbtnRDH.isSelected())
                this.mode = Modes.RDH;
            writeMessage("Mode " + this.mode.toString() + " selected");
        }
        else writeMessage("You can't select mode after connecting to server");
    }


    @FXML
    protected void Run() {
        try {
            if (rdbtnSend.isSelected()) {
                writeMessage("Sending started");

                socketAssistant.sendMessage("Send");

                File file = new File(path);

                socketAssistant.sendMessage(file.getName());

                socketAssistant.sendMessage(symmetricCrypto.encryptFile(new FileInputStream(file), file.length()));

                writeMessage("File " + file.getName() + " sent");


            } else if (rdbtnDownload.isSelected()) {
                writeMessage("Downloading started");

                socketAssistant.sendMessage("Download");

                socketAssistant.sendMessage(path);

                String curPath = socketAssistant.getStringMessage();

                byte[] byteData = socketAssistant.getByteMessage();

                writeMessage("Encrypted file received");

                File file = new File(curPath);
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(symmetricCrypto.decryptData(byteData));
                fileOutputStream.close();

                writeMessage("File decrypted and saved");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public int[] generateKey(int len) {  // len = 4/6/8
        int[] key = new int[len];
        for (int i = 0; i < len; i++) {
            key[i] = randomizer.nextInt();
        }
        return key;
    }

    public static byte[] intArrayToByte(int[] array) {
        ByteBuffer buffer = ByteBuffer.allocate(array.length * 4);
        for (int j : array) {
            buffer.putInt(j);
        }
        return buffer.array();
    }

    public static int[] byteArrayToInt(byte[] array) {
        int[] res = new int[array.length / 4];
        for (int i = 0; i < array.length / 4; i++)
            for (int j = 0; j < 4; j++)
                res[i] = (res[i] << 8) + (array[i * 4 + j] & 0xFF);
        return res;
    }


}