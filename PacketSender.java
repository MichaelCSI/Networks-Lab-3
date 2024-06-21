import java.math.BigInteger;

public class PacketSender {

    private static int identificationNumber = 0;

    public synchronized int getNextId() {
        // Can represent 65536 values with 2 bytes, cycle that for the id number
        int id = (identificationNumber + 1) % 65536;
        identificationNumber++;
        return id;
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
    
    public static String convertStringToHex(String msg) {
        String ipv4AndTos = "4500";
        String iplengthInHex = String.format("%04x", msg.getBytes().length + 20);
        
        // TODO: Change this later, currently set to fixed value for testing
        String identificationField = "1C46";
        // String identificationField = String.format("%04x", identificationNumber);
        
        String flagAndFragmentOffset = "4000";
        String ttlAndProtocol = "4006";

        // TODO: GET ACTUAL IPs HERE, currently set to fixed values for testing
        String sourceIpPart1 = "C0A8";
        String sourceIpPart2 = "0003";
        String destinationIpPart1 = "C0A8";
        String destinationIpPart2 = "0001";

        String checkSum = computeCheckSum(
            ipv4AndTos, iplengthInHex, identificationField, flagAndFragmentOffset,
            ttlAndProtocol, sourceIpPart1, sourceIpPart2, destinationIpPart1, destinationIpPart2);

        String payloadHex = String.format("%x", new BigInteger(1, msg.getBytes()));

        String datagram = 
            ipv4AndTos + iplengthInHex + identificationField + flagAndFragmentOffset + ttlAndProtocol +
            checkSum + sourceIpPart1 + sourceIpPart2 + destinationIpPart1 + destinationIpPart2 + payloadHex;

        // Append 0s to datagram until length is divisible by 8
        while (datagram.length() % 8 != 0) {
            datagram = datagram + "0";
        }
        // Convert final string to uppercase (i.e. a0a0 to A0A0) and space into 2 byte blocks (4 chars)
        return datagram.replaceAll("(.{" + 4 + "})", "$1 ").trim().toUpperCase();
    }

    public static void main(String args[]) {
        String msg = "COLOMBIA 2 - MESSI 0";
        System.out.println("\n");
        System.out.println(convertStringToHex(msg));
        System.out.println("\n");
    }
}
