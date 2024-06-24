import java.io.DataOutputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.Scanner;

public class PacketSender {

    private static int identificationNumber = 0;

    public synchronized static int getNextId() {
        // Can represent 65536 values with 2 bytes, cycle that for the id number
        if(identificationNumber > 65536) {
            identificationNumber = 0;
        }
        identificationNumber++;
        return identificationNumber;
    }

    public static String computeCheckSum(String... hexValues) {
        int checkSum = 0;
        for (String headerHex : hexValues) {
            checkSum += Integer.parseInt(headerHex, 16);
        }

        // Handle the carry by adding overflow back to the checksum
        while (checkSum > 0xFFFF) {
            // Extract most significant bit (MSB) with shift
            int carry = checkSum >> 16;
            // Add MSB and retain only least 4 significant bits
            checkSum = (checkSum & 0xFFFF) + carry;
        }

        // 1's complement
        checkSum = 0xFFFF - checkSum;

        return String.format("%04x", checkSum);
    }
    
    public static String convertStringToHex(String msg, String sourceIpStr, String destinationIpStr) {
        String ipv4AndTos = "4500";
        String iplengthInHex = String.format("%04x", msg.getBytes().length + 20);
        
        String identificationField = String.format("%04x", getNextId());
        
        String flagAndFragmentOffset = "4000";
        String ttlAndProtocol = "4006";

        // Break ip address strings into parts and parse the hex version
        String[] sourceIpParts = sourceIpStr.split("\\.");
        String sourceIp = "";
        for(String sourceIpPart : sourceIpParts) {
            int temp = Integer.parseInt(sourceIpPart);
            sourceIp += String.format("%02x", temp);
        }
        String[] destinationIpParts = destinationIpStr.split("\\.");
        String destinationIp = "";
        for(String destinationIpPart : destinationIpParts) {
            int temp = Integer.parseInt(destinationIpPart);
            destinationIp += String.format("%02x", temp);
        }

        String checkSum = computeCheckSum(
            ipv4AndTos, iplengthInHex, identificationField, flagAndFragmentOffset, ttlAndProtocol, 
            sourceIp.substring(0, sourceIp.length() / 2), sourceIp.substring(sourceIp.length() / 2, sourceIp.length()),
            destinationIp.substring(0, destinationIp.length() / 2), destinationIp.substring(destinationIp.length() / 2, destinationIp.length()));

        String payloadHex = String.format("%x", new BigInteger(1, msg.getBytes()));

        String datagram = 
            ipv4AndTos + iplengthInHex + identificationField + flagAndFragmentOffset + 
            ttlAndProtocol + checkSum + sourceIp + destinationIp + payloadHex;

        // Append 0s to datagram until length is divisible by 8
        while (datagram.length() % 8 != 0) {
            datagram = datagram + "0";
        }
        // Convert final string to uppercase (i.e. a0a0 to A0A0) and space into 2 byte blocks (4 chars)
        return datagram.replaceAll("(.{" + 4 + "})", "$1 ").trim().toUpperCase();
    }

    public static void main(String args[]) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            boolean socketOpen = true;
            
            // Client socket and output stream for server
            Socket client = new Socket("localhost",8888);
            DataOutputStream out = new DataOutputStream(client.getOutputStream());

            // Get relevant IP fields for datagram
            String sourceIp = client.getLocalAddress().getHostAddress().toString();
            String destinationIp = client.getInetAddress().getHostAddress().toString();
            
            while(socketOpen) {
                System.out.print("Enter a message (\"exit\" to stop): ");
                String msg = scanner.nextLine();

                out.writeUTF(convertStringToHex(msg, sourceIp, destinationIp));

                // Close client connection for exit command
                if(msg.toLowerCase().equals("exit")) {
                    System.out.println("Closing client");
                    client.close();
                    socketOpen = false;
                }
            }
        }
    }
}
