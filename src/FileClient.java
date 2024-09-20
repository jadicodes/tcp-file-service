import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

public class FileClient {
    public static void main(String[] args) throws IOException {
        if(args.length != 2){
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        String command;
        do {
            System.out.println("\nPlease enter a command.");
            Scanner keyboard = new Scanner(System.in);
            command = keyboard.nextLine();
            switch(command){
                case "D": // Delete
                    // Send command and file name to server
                    System.out.println("Enter the name of the file you want to delete.");
                    String fileName = keyboard.nextLine();
                    ByteBuffer request = ByteBuffer.wrap((command + fileName).getBytes());
                    SocketChannel channel = SocketChannel.open();
                    channel.connect(new InetSocketAddress(args[0], serverPort));
                    channel.write(request);
                    channel.shutdownOutput();

                    // Receive the status code from server and tell the user if success
                    ByteBuffer reply = ByteBuffer.allocate(1);
                    channel.read(reply);
                    channel.close();
                    reply.flip();
                    byte[] a = new byte[1];
                    reply.get(a);
                    String code = new String(a);
                    if(code.equals("S")){
                        System.out.println("File successfully deleted.");
                    }
                    else if(code.equals("F")){
                        System.out.println("Failed to delete file.");
                    }
                    else{
                        System.out.println("Invalid server code received.");
                }
                    break;
                case "L": // List
                    break;
                case "R": // Rename
                    System.out.println("Enter the name of the file you would like to rename.");
                    String fileRename = keyboard.nextLine();
                    System.out.println("Enter what you would like to rename this to.");
                    String newName = keyboard.nextLine();
                    ByteBuffer renameRequest = ByteBuffer.wrap((command + fileRename + newName).getBytes());
                    break;
                case "U": // Upload
                    break;
                case "G": // Download
                    break;
                default:
                    if (!command.equals("Q")) {
                        System.out.println("Invalid command");
                    }
            }
        }while(!command.equals("Q"));
    }
}