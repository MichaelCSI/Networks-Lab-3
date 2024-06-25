## How to run the code

We wrote our program in java, so classes must be compiled and ran individually

Steps
- Navigate to the project directory under ```.../Networks Lab 3```
- Compile both java files: ```javac PacketSender.java``` and ```javac PacketReceiver.java```
- Start the server (PacketReceiver): ```java PacketReceiver```
- In a new terminal window under the same project directory, start the client (PacketSender): ```java PacketSender```

In the client terminal window you will be prompted for input to send to the server, you can see the server response in the server terminal window. The program continues accepting client input and printing server output until the "exit" command is sent to the server, shutting down both the server and the client.

Note that some IDEs such as VS Code have built in functionality for running java programs, which may be used as an alternative to these steps.
