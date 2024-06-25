import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PacketReceiver {

    public static Map<String, String> extractMessageFromDatagram(String datagram) {
        Map<String, String> datagramMap = new HashMap<>();
        datagram = datagram.replace(" ", "").toUpperCase();

        // Sender IP takes place from the 24th to the 32nd characters in the datagram
        String ipHex = datagram.substring(24, 32);
        byte[] ipBytes = new byte[ipHex.length() / 2];

        for (int i = 0; i < ipHex.length(); i += 2) {
            int value = Integer.parseInt(ipHex.substring(i, i + 2), 16);
            ipBytes[i / 2] = (byte) value;
        }
        String ipString = "";
        for(int i = 0; i < ipBytes.length; i++) {
            if(i < ipBytes.length - 1) {
                ipString += ipBytes[i] + ".";
            } else {
                ipString += ipBytes[i];
            }
        }
        datagramMap.put("senderIp", ipString);

        
        // Header length is 40 characters, begin payload parse after that
        String payloadHex = datagram.substring(40);
        byte[] payloadBytes = new byte[payloadHex.length() / 2];

        for (int i = 0; i < payloadHex.length(); i += 2) {
            int value = Integer.parseInt(payloadHex.substring(i, i + 2), 16);
            payloadBytes[i / 2] = (byte) value;
        }
        datagramMap.put("payload", new String(payloadBytes, StandardCharsets.UTF_8));
        datagramMap.put("payloadLength", payloadBytes.length + "");

        return datagramMap;
    }

    private static boolean verifyChecksum(String datagram) {
        String[] hexValues = datagram.split(" ");
        // Verify header fields (first 10 hex values) add up to FFFF, or rather that FFFF - sum = 0
        // I.e. we are resuing the checksum computation logic from PacketSender which already performs the 1s complement
        String sum = PacketSender.computeChecksum(Arrays.copyOfRange(hexValues, 0, 10));
        int sumInt = Integer.parseInt(sum, 16);
        return sumInt == 0x0000;
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Server Listening on 8888");

        try (ServerSocket serverSocket = new ServerSocket(8888)) {
            boolean socketOpen = true;
            
            // Set server timeout 60 minutes and wait for client connection
            Socket server = serverSocket.accept();
            serverSocket.setSoTimeout(1000 * 60 * 60);

            // Input stream for messages from client
            DataInputStream in = new DataInputStream(server.getInputStream());

            while(socketOpen) {
                String msg = in.readUTF();

                Map<String, String> datagramMap = extractMessageFromDatagram(msg);

                // Close server connection if exit command
                if(datagramMap.get("payload").toLowerCase().equals("exit")) {
                    System.out.println("Closing server");
                    socketOpen = false;
                    server.close();
                    break;
                }

                boolean checksumCorrect = false;
                if(verifyChecksum(msg)) {
                    // Checksum correct, print message later
                    checksumCorrect = true;
                } else {
                    // Checksum incorrect
                    System.out.println("\nThe verification of the checksum demonstrates that the packet received is corrupted. Packet Discarded!");
                    break;
                }

                // Data
                System.out.print("\nThe data received from " + datagramMap.get("senderIp"));
                System.out.println(" is " + datagramMap.get("payload"));
                
                // Data length
                int numBytes = Integer.parseInt(datagramMap.get("payloadLength"));
                System.out.print("The data has " + (numBytes * 8) + " bits or " + numBytes + " bytes. ");
                System.out.println("Total length of the packet is " + (numBytes + 20) + " bytes.");

                // Checksum correct
                if(checksumCorrect) {
                    System.out.println("The verification of the checksum demonstrates that the packet received is correct.");
                }
            }
        }
    }
}

