import java.io.DataInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class PacketReceiver {

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
                // Read from client with input stream
                System.out.println(msg);

                // Close server connection if exit command
                if(msg.toLowerCase().equals("exit")) {
                    System.out.println("Closing server");
                    socketOpen = false;
                    server.close();
                }
            }
        }
    }
}
