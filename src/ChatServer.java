import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ChatServer {
    private final ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private final ServerSocket serverSocket;

    public ChatServer(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void StartServer() {
        while (!serverSocket.isClosed()) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("A new Client is connected");
            ClientHandler clientHandler = new ClientHandler(socket);
            clientHandlers.add(clientHandler);
            Thread thread = new Thread(clientHandler);
            thread.start();
        }
    }

    public void broadCastMessage(String message, String username) {
        for (ClientHandler clientHandler : clientHandlers) {
            if(!clientHandler.username.equals(username)){
                clientHandler.SendMessage(message);
            }
        }
    }
    public void unicastMessage(String message, String username){
        for (ClientHandler clientHandler : clientHandlers) {
            if(clientHandler.username.equals(username)){
                clientHandler.SendMessage(message);
            }
        }
    }
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("Waiting for client to connnect");
        ChatServer server = new ChatServer(serverSocket);
        server.StartServer();
    }
    public class ClientHandler implements Runnable{
        String username;
        public PrintWriter printWriter;
        public BufferedReader bufferedReader;
        public Socket socket;
        public ClientHandler(Socket socket){
            try{
                this.socket=socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.printWriter = new PrintWriter(socket.getOutputStream(),true);
                this.username = bufferedReader.readLine();
                broadCastMessage("SERVER : "+username+" has joined the chat", username);
                SendMessage("HEY WELCOME TO THE SERVER \n\t PLEASE FOLLOW THE FOLLOWING TIPS TO USE THE SERVER \n\t USE '/private username: msg ' TO SEND MSG TO A SINGLE CLIENT  \n\t USE '/exit:' to exit the chat ");
                SendMessage("Online Users ");
                if(!clientHandlers.isEmpty()){
                    for(ClientHandler clientHandler: clientHandlers){
                        SendMessage("\t>  "+clientHandler.username);
                    }
                }

                SendMessage("--------------------------------------------------------------------");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        public void SendMessage(String Message){
            printWriter.println(Message);
        }
        public void decipherCode(String message){
            String firstChar = message.substring(0, 1);
            if (firstChar.equals("/")) {
                int indexOfCodeEnd = message.indexOf(":");
                if (indexOfCodeEnd != -1) {
                    String codeAndUsername = message.substring(1, indexOfCodeEnd);
                    String[] codeAndUsernameArray = codeAndUsername.split(" ");
                    String code = codeAndUsernameArray[0];

                    switch (code.toLowerCase()) {
                        case "private" -> {
                            String messageToSend = "(Private) " + username + " : " + message.substring(indexOfCodeEnd + 1);
                            unicastMessage(messageToSend, codeAndUsernameArray[1]);
                        }
                        case "exit" -> {
                            try {
                                removeClientHandler();
                            } catch (IOException ioe) {
                                System.out.println(username + " Disconnected");
                            }
                        }
                        default -> SendMessage("SERVER : WRONG CODE");
                    }
                } else {
                    SendMessage("SERVER :  WRONG SYNTAX");
                }


            } else {
                broadCastMessage(username+" : "+message, username);
            }
        }
        public void removeClientHandler() throws IOException {
            SendMessage("exit");
            clientHandlers.remove(this);
            closeEverything(socket,bufferedReader,printWriter);
            broadCastMessage("SERVER : "+username+" has left the chat",username);
        }
        public void closeEverything(Socket socket, BufferedReader bufferedReader, PrintWriter printWriter){

            try{
                if(bufferedReader!= null)
                    bufferedReader.close();
                if(printWriter!=null)
                    printWriter.close();
                if(socket != null)
                    socket.close();
                System.out.println(username+" has left the chat ");
                clientHandlers.remove(this);
            }catch(IOException IOE){

                System.out.println(username + " Disconnected");
            }
        }
        @Override
        public void run() {
            String Messagge;
            Boolean isConnect = socket.isConnected();
            while(isConnect){

                try {
                    Messagge = bufferedReader.readLine();
                    decipherCode(Messagge);
                } catch (IOException e) {
                    isConnect = Boolean.FALSE;
                }
            }
        }
    }
}
