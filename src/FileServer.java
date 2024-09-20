import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class FileServer {
    public static void main(String[] args) throws Exception{
        int port = 3000;
        ServerSocketChannel listenChannel = ServerSocketChannel.open();
        listenChannel.bind(new InetSocketAddress(port));
        while(true){
            SocketChannel serveChannel = listenChannel.accept();
            ByteBuffer request = ByteBuffer.allocate(1024);
            int numBytes = serveChannel.read(request);
            request.flip();
            byte[] a = new byte[1];
            request.get(a);
            String command = new String(a);
            switch (command){
                case "D": // Delete
                    byte[] b = new byte[request.remaining()];
                    request.get(b);
                    String fileName = new String(b);

                    System.out.println("File to delete" + fileName);
                    File file = new File("ServerFiles/" + fileName);
                    boolean success = false;
                    if(file.exists()){
                        success = file.delete();
                    }
                    if(success){
                        System.out.println("File deleted successfully.");
                        ByteBuffer code = ByteBuffer.wrap("S".getBytes());
                        serveChannel.write(code);
                    }
                    else{
                        System.out.println("Unable to delete file.");
                        ByteBuffer code = ByteBuffer.wrap("F".getBytes());
                        serveChannel.write(code);
                    }
                    serveChannel.close();
                    break;
                case "L": // List
                    break;
                case "R": // Rename
                    break;
                case "U": // Upload
                    break;
                case "G": // Download
                    break;
                default:
                    System.out.println("Invalid input!");
            }
        }
    }
}
