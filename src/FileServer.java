import java.io.File;
import java.io.FileOutputStream;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception {
        int port = 3000;
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));
        System.out.println("Server started on port " + port);

        while (true) {
            try (SocketChannel serveChannel = listenChannel.accept()) {
                ByteBuffer request = ByteBuffer.allocate(1024);
                serveChannel.read(request);
                request.flip();
                byte[] commandBytes = new byte[1];
                request.get(commandBytes);
                String command = new String(commandBytes);

                switch (command) {
                    case "D": // Delete
                        handleDelete(serveChannel, request);
                        break;
                    case "L": // List
                        handleList(serveChannel);
                        break;
                    case "R": // Rename
                        // Implement rename logic here
                        break;
                    case "U": // Upload
                        handleUpload(serveChannel);
                        break;
                    case "G": // Download
                        // Implement download logic here
                        break;
                    default:
                        System.out.println("Invalid input!");
                }
            }
        }
    }

    private static void handleDelete(SocketChannel serveChannel, ByteBuffer request) throws Exception {
        byte[] fileNameBytes = new byte[request.remaining()];
        request.get(fileNameBytes);
        String fileName = new String(fileNameBytes);
        File file = new File("ServerFiles/" + fileName);
        boolean success = file.delete();
        ByteBuffer response = ByteBuffer.wrap(success ? "S".getBytes() : "F".getBytes());
        serveChannel.write(response);
        System.out.println(success ? "File deleted successfully." : "Unable to delete file.");
    }

    private static void handleList(SocketChannel serveChannel) throws Exception {
        File folder = new File("ServerFiles");
        File[] listFiles = folder.listFiles();
        StringBuilder fileList = new StringBuilder();
        if (listFiles != null && listFiles.length > 0) {
            for (File f : listFiles) {
                if (f.isFile()) {
                    fileList.append(f.getName()).append("\n");
                }
            }
        } else {
            fileList.append("No files");
        }
        ByteBuffer listResponse = ByteBuffer.wrap(fileList.toString().getBytes());
        serveChannel.write(listResponse);
    }

    private static void handleUpload(SocketChannel serveChannel) throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try (FileOutputStream fos = new FileOutputStream("ServerFiles/uploaded_file")) {
            int bytesRead;
            while ((bytesRead = serveChannel.read(buffer)) != -1) {
                buffer.flip(); // Prepare buffer for reading
                fos.write(buffer.array(), 0, bytesRead);
                buffer.clear(); // Clear buffer for the next read
                System.out.println("Bytes read: " + bytesRead); // Debug print
            }
            ByteBuffer replyBuffer = ByteBuffer.wrap("S".getBytes());
            serveChannel.write(replyBuffer);
            System.out.println("File uploaded successfully.");
        } catch (Exception e) {
            System.out.println("Error during upload: " + e.getMessage());
            ByteBuffer replyBuffer = ByteBuffer.wrap("F".getBytes());
            serveChannel.write(replyBuffer);
        }
    }
}
