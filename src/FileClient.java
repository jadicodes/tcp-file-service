import java.io.FileInputStream;
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
                    ByteBuffer listR = ByteBuffer.wrap((command).getBytes());
                    SocketChannel listC = SocketChannel.open();
                    listC.connect(new InetSocketAddress(args[0], serverPort));
                    listC.write(listR);
                    listC.shutdownOutput();

                    ByteBuffer listReply = ByteBuffer.allocate(1024);
                    listC.read(listReply);
                    listC.close();
                    listR.flip();

                    byte[] listBytes = new byte[listReply.remaining()];
                    listReply.get(listBytes);
                    String filesList = new String(listBytes);
                    System.out.println("Files:\n" + filesList);
                    break;
                case "R": // Rename
                    // Get input from user
                    System.out.println("Enter the name of the file you would like to rename.");
                    String fileRename = keyboard.nextLine();
                    int fileLength = fileRename.length();

                    System.out.println("Enter what you would like to rename this to.");
                    String newName = keyboard.nextLine();

                    // Send request to server
                    ByteBuffer renameRequest = ByteBuffer.wrap((command + fileRename + fileLength + newName).getBytes());
                    break;
                case "U": // Upload
                    System.out.println("Please enter the path of the file you would like to upload.");
                    String uploadFileName = keyboard.nextLine();
                    SocketChannel uploadChannel = SocketChannel.open();
                    FileInputStream fis = new FileInputStream(uploadFileName);
                    byte[] data = new byte[1024];
                    int bytesRead = 0;
                    while((bytesRead=fis.read(data)) != -1) {
                        ByteBuffer buffer = ByteBuffer.wrap(data, 0, bytesRead);
                        uploadChannel.write(buffer);
                    }
                    ByteBuffer uploadReply = ByteBuffer.allocate(1);
                    uploadChannel.read(uploadReply);
                    uploadChannel.close();
                    uploadReply.flip();
                    byte[] b = new byte[1];
                    uploadReply.get(b);
                    String uploadCode = new String(b);
                    if(uploadCode.equals("S")){
                        System.out.println("File successfully deleted.");
                    }
                    else if(uploadCode.equals("F")){
                        System.out.println("Failed to delete file.");
                    }
                    else{
                        System.out.println("Invalid server code received.");
                    }
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