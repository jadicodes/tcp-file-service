import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.io.FileOutputStream;

public class FileClient {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Please specify <serverIP> and <serverPort>");
            return;
        }
        int serverPort = Integer.parseInt(args[1]);
        String command;
        Scanner keyboard = new Scanner(System.in);

        do {
            System.out.println("\nPlease enter a command.");
            System.out.println("D (Delete), L (List), R (Rename), U (Upload), G (Download), Q (Quit)");
            command = keyboard.nextLine().toUpperCase();

            switch (command) {
                case "D" -> { // Delete
                    System.out.println("Enter the name of the file you want to delete:");
                    String fileName = keyboard.nextLine();
                    sendCommandToServer(command + fileName, serverPort, args[0]);
                }
                case "L" -> // List
                        sendCommandToServer(command, serverPort, args[0]);
                case "R" -> { // Rename
                    System.out.println("Enter the name of the file you would like to rename:");
                    String oldName = keyboard.nextLine();
                    System.out.println("Enter what you would like to rename this to:");
                    String newName = keyboard.nextLine();
                    sendCommandToServer("R" + oldName + "\0" + newName, serverPort, args[0]);
                }
                case "U" -> { // Upload
                    System.out.println("Please enter the path of the file you would like to upload:");
                    String uploadFileName = keyboard.nextLine();
                    uploadFile(uploadFileName, serverPort, args[0]);
                }
                case "G" -> { // Download
                    System.out.println("Enter the name of the file you would like to download:");
                    String downloadable = keyboard.nextLine();
                    downloadFile("G" + downloadable, serverPort, args[0]);
                }
                default -> {
                    if (!command.equals("Q")) {
                        System.out.println("Invalid command");
                    }
                }
            }
        } while (!command.equals("Q"));
        keyboard.close();
    }

    private static void sendCommandToServer(String command, int port, String serverIp) {
        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverIp, port));
            ByteBuffer request = ByteBuffer.wrap(command.getBytes());
            channel.write(request);
            channel.shutdownOutput();

            ByteBuffer reply = ByteBuffer.allocate(1024);
            channel.read(reply);
            reply.flip();

            byte[] responseBytes = new byte[reply.remaining()];
            reply.get(responseBytes);
            String response = new String(responseBytes);
            System.out.println("Server response:\n" + response);
        } catch (IOException e) {
            System.out.println("Error communicating with server: " + e.getMessage());
        }
    }

    private static void uploadFile(String filePath, int port, String serverIp) {
        try (SocketChannel channel = SocketChannel.open();
             FileInputStream fis = new FileInputStream(filePath)) {
            channel.connect(new InetSocketAddress(serverIp, port));

            // Send the command and filename first
            String filename = new File(filePath).getName();
            channel.write(ByteBuffer.wrap(("U" + filename).getBytes()));

            byte[] data = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(data)) != -1) {
                channel.write(ByteBuffer.wrap(data, 0, bytesRead));
            }
            // Indicate end of file transfer
            channel.shutdownOutput();

            // Read the response from the server
            ByteBuffer reply = ByteBuffer.allocate(1);
            channel.read(reply);
            reply.flip();
            byte[] responseBytes = new byte[1];
            reply.get(responseBytes);
            String response = new String(responseBytes);
            System.out.println(response.equals("S") ? "File successfully uploaded." : "Failed to upload file.");
        } catch (IOException e) {
            System.out.println("Error uploading file: " + e.getMessage());
        }
    }
    private static void downloadFile(String gName, int port, String serverIp) {
        String fileName = gName.substring(1);  // Extract file name from command
        String downloadDir = "ClientFiles";  // Directory to save downloaded files

        // Ensure the directory exists
        File dir = new File(downloadDir);
        if (!dir.exists()) {
            dir.mkdirs();  // Create the directory if it doesn't exist
        }

        try (SocketChannel channel = SocketChannel.open()) {
            channel.connect(new InetSocketAddress(serverIp, port));

            // Send the download command "G" followed by the file name
            channel.write(ByteBuffer.wrap("G".getBytes()));
            channel.write(ByteBuffer.wrap(fileName.getBytes()));

            // Buffer to hold the initial server response (success or failure)
            ByteBuffer initialResponse = ByteBuffer.allocate(1);
            int bytesRead = channel.read(initialResponse);

            if (bytesRead == -1) {
                System.out.println("No response from the server.");
                return;
            }

            initialResponse.flip();
            if (initialResponse.remaining() >= 1) {
                byte[] responseBytes = new byte[1];
                initialResponse.get(responseBytes);
                String response = new String(responseBytes);

                if (response.equals("F")) {
                    // Server responded with "F", indicating the file does not exist
                    System.out.println("The file '" + fileName + "' does not exist on the server.");
                    return;
                }
            }

            // If the server responds with success, proceed to download the file
            try (FileOutputStream fos = new FileOutputStream(downloadDir + "/" + fileName)) {
                ByteBuffer buffer = ByteBuffer.allocate(1024);

                // Read the file data in chunks
                while ((bytesRead = channel.read(buffer)) > 0) {
                    buffer.flip();
                    fos.write(buffer.array(), 0, bytesRead);
                    buffer.clear();
                }

                System.out.println("File downloaded successfully: " + fileName + " to " + downloadDir);
            }

        } catch (IOException e) {
            System.out.println("Error downloading file: " + e.getMessage());
        }
    }
}