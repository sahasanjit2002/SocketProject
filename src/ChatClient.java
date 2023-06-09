import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ChatClient {
    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost",9999);


        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter printWriter = new PrintWriter(socket.getOutputStream(),true);
        System.out.print("Enter your username ");
        Scanner sc = new Scanner(System.in);
        Thread sender = new Thread(new Runnable() {
            @Override
            public void run() {
                while(socket.isConnected()){
                    String msg = sc.nextLine();
                    printWriter.println(msg);
                }
            }
        });
        Thread recieve = new Thread(new Runnable() {
            String msg;
            @Override
            public void run() {
                try {
                    while(socket.isConnected()){
                        msg = bufferedReader.readLine();
                        if(msg.equals("exit")){

                            bufferedReader.close();
                            printWriter.close();
                            System.out.println("SERVER : GOODBYE");
                            socket.close();
                            sender.interrupt();
                            break;
                        }else{
                            System.out.println(msg);
                        }
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        sender.start();
        recieve.start();
    }

}
