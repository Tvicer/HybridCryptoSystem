package com.example.cryptocoursework;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Arrays;

public class ClientSocketAssistant implements Closeable {
    private final Socket socket;
    private final BufferedReader reader;
    private final BufferedWriter writer;
    public byte[] data;

    public ClientSocketAssistant(Socket client) {
        try {
            this.socket = client;
            this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
            this.writer = new BufferedWriter(new OutputStreamWriter(this.socket.getOutputStream()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendMessage(byte[] data) {
        try {
            writer.write(Arrays.toString(data));
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public void sendMessage(String data) {
        try {
            writer.write(data);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public BigInteger[] getBigIntegerMessage() {
        try {
            String str = reader.readLine();
            String[] arrStr = str.substring(1, str.length() - 1).split(", ");
            BigInteger[] res = new BigInteger[arrStr.length];
            for (int i = 0; i < arrStr.length; i++)
                res[i] = new BigInteger(arrStr[i]);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] getByteMessage() {
        try {
            String str = reader.readLine();
            String[] arrStr = str.substring(1, str.length() - 1).split(", ");
            byte[] res = new byte[arrStr.length];
            for (int i = 0; i < arrStr.length; i++)
                res[i] = Byte.parseByte(arrStr[i]);
            return res;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public String getStringMessage() {
        try {
            return reader.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws IOException {
        reader.close();
        writer.close();
        socket.close();
    }
}
