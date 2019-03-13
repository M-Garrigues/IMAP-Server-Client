package src;

import java.io.*;
import java.net.*;
import java.util.Observable;

public class Client extends Observable {


    private DatagramSocket ds;
    private int portToSend;
    private InetAddress ip;
    private int port;
    private int nextFileId = 0;

    private Socket socket;

    private BufferedReader reader;
    private BufferedWriter writer;

    private String host;
    private int hostPort;


    public Client(String ip, int port)
    {
        setIP(ip);
        this.port = port;
    }

    private static byte[] constructWRQ(String fileName ) {
        byte[] request = new byte[516];
        byte[] upCode = new byte[2];
        upCode[0]= 0;
        upCode[1] = (byte)2; //code of WRQ
        byte[] space = new byte[1];
        space[0]=0;
        byte[] byte_fileName = fileName.getBytes();
        byte[] mode = "octet".getBytes();

        int pos = 0;
        System.arraycopy(upCode, 0, request, pos, upCode.length);
        pos +=2;
        System.arraycopy(byte_fileName, 0, request, pos, byte_fileName.length);
        pos += byte_fileName.length;
        System.arraycopy(space, 0, request, pos, 1);
        pos +=1;
        System.arraycopy(mode, 0, request, pos, mode.length);
        pos += mode.length;
        System.arraycopy(space, 0, request, pos, 1);

        return request;
    }

    private byte[] constructDataPackage(short blockNumber, byte[] data)
    {
        byte[] byte_blockNumber=new byte[]{(byte)(blockNumber>>>8),(byte)(blockNumber&0xFF)};
        byte[] output = new byte[516];
        //Creating the header
        output[0] = (byte) 0;
        output[1] = (byte) 3;
        output[2] = byte_blockNumber[0];
        output[3] = byte_blockNumber[1];
        //Adding the data to the package
        for (int k=0;k<data.length;k++){
            output[k+4] = data[k];
        }
        return output;
    }

    private void analyseError(int error){
        switch (error){
            case 0 :
                setChanged();
                notifyObservers(new String[] {"error", "Erreur non définie"});
                break;
            case 1 :
                setChanged();
                notifyObservers(new String[] {"error", "Fichier non trouvé."});
                break;
            case 2 :
                setChanged();
                notifyObservers(new String[] {"error", "Violation de l'accès."});
                break;
            case 3 :
                setChanged();
                notifyObservers(new String[] {"error", "Disque plein ou dépassement de l'espace alloué."});
                break;
            case 4 :
                setChanged();
                notifyObservers(new String[] {"error", "Opération TFTP illégale."});
                break;
            case 5 :
                setChanged();
                notifyObservers(new String[] {"error", "Transfert ID inconnu."});
                break;
            case 6 :
                setChanged();
                notifyObservers(new String[] {"error", "Le fichier existe déjà."});
                break;
            case 7 :
                setChanged();
                notifyObservers(new String[] {"error", "Utilisateur inconnu."});
                break;

        }
    }

    public void sendFile(String path)
    {
        setChanged();
        int fileId = getNextFileId();
        notifyObservers(new String[] {"addFileTransfer", Integer.toString(fileId), path});

        Thread sendThread = new Thread(() -> {
            DatagramPacket wrq;
            DatagramPacket ack;

            FileInputStream file;

            int i =0;
            int j = 0;
            int ttl = 0;


            try {


                //ouverture socket
                ds = new DatagramSocket();

                file = new FileInputStream(path);

                File fileTemp = new File(path);
                long fileSize;
                fileSize = fileTemp.length();

                //envoi WRQ
                byte[] request = constructWRQ(path.substring(path.lastIndexOf("/")+1));

                wrq= new DatagramPacket(request, request.length, ip, port);
                ds.send(wrq);

                //reception de la reponse
                byte[] answer = new byte[4];
                ack = new DatagramPacket(answer, answer.length);


                if(port != 69){
                    ds.setSoTimeout(2000);
                }

                ds.receive(ack);



                //Analyse de la réponse
                if (answer[1] == 4) {

                    portToSend = ack.getPort();



                    //int blockNumber = 1;
                    DatagramPacket ACK;
                    short blockNumber = 1;

                    byte[] data = new byte[512];
                    byte[] dataPackage;

                    while (file.read(data) > 0) {

                        dataPackage = constructDataPackage(blockNumber, data);


                        //Tant que l'ACK n'est pas le bon en envoi le paquet
                        // ou que ce n'est pas un ACK
                        int verifAck = -1;
                        DatagramPacket donnees = new DatagramPacket(dataPackage, dataPackage.length, ip, portToSend);
                        while(verifAck != (i+1) || answer[1] != 4) {

                            //On envoie le paquet
                            ds.send(donnees);

                            answer = new byte[516];

                            //Réception ack

                            ACK = new DatagramPacket(answer, answer.length);

                            ds.receive(ACK);

                            verifAck = answer[3];

                            if(verifAck < 0){
                                verifAck = 256 - Math.abs(answer[3]);
                            }

                            ttl++;

                        }

                        ttl = 0;
                        i++;
                        blockNumber++;
                        setChanged();
                        notifyObservers(new String[] {"fileTransferStatus", Integer.toString(fileId), Integer.toString(blockNumber*512), Long.toString(fileSize)});
                        if (i == 255) {
                            j++;
                            i = -1;
                            //Laisser -1 absolument sinon les paquets
                            //multiples de 256 ne sont pas envoyÈs
                        }

                        data= new byte[512];
                    }

                    //Envoi d'un dernier paquet vide pour les fichier multiple de 512
                    dataPackage = new byte[4];
                    dataPackage[0] = (byte) 0;
                    dataPackage[1] = (byte) 3;
                    dataPackage[2] = (byte) j;
                    dataPackage[3] = (byte) (i+1);
                    DatagramPacket donneesDernierPacket = new DatagramPacket(dataPackage, dataPackage.length, ip, portToSend);
                    ds.send(donneesDernierPacket);

                }

                else if (answer[1] == 5)
                {
                    analyseError(answer[3]);
                    return;
                }
                file.close();
            } catch (FileNotFoundException e) //Fichier non trouvÈ ou accËs refusÈ.
            {
                setChanged();
                notifyObservers(new String[] {"error", "Fichier introuvable"});
                return;
            }

            catch (UnknownHostException e) {
                setChanged();
                notifyObservers(new String[] {"error", "IP inconnue"});
                return;
            }
            catch (SocketException e1) {
                setChanged();
                notifyObservers(new String[] {"error", "Erreur socket (création/accès) "});
                return;
            }
            catch (SocketTimeoutException e){
                setChanged();
                notifyObservers(new String[] {"error", "Hôte ou port invalide"});
                return;
            }
            catch (IOException e1) {
                setChanged();
                notifyObservers(new String[] {"error", "Erreur réseau"});
                return;
            }
            catch (Exception e) {

                setChanged();
                notifyObservers(new String[] {"error", "Erreur indéterminée "});
                return;
            } finally {
                ds.close();
            }
            return;
        });
        sendThread.start();
    }


    private int getNextFileId()
    {
        nextFileId++;
        return nextFileId-1;
    }


    public void setPort(int port)
    {
        this.port = port;
    }

    public void setIP(String ip)
    {
        try {
            this.ip = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private static int portScanner() {
        int portLibre = 0;
        for (int port = 2000; port <= 4000; port++) {
            try {
                DatagramSocket server = new DatagramSocket(port);
                if(portLibre == 0)
                    portLibre = port;
                server.close();
            } catch (SocketException ex) {
            }
        }
        return portLibre;
    }

    public boolean connectToHost(String host, int port) throws IOException{

        socket = new Socket();

        socket.connect(new InetSocketAddress(host, port));

        reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

        return true;
    }

    public boolean isConnected(){
         return socket != null && socket.isConnected();
    }

    public boolean sendAPOP(String username, String pwd){

        try{
            writer.write("APOP "+username+" "+pwd);
        }catch (Exception e){
            System.out.println("error wrinting sendAPOP");
        }
        return true;
    }



    public void sendLIST(){

    }

    public void sendSTAT(){

    }

    public String retrieveMessage(int id){

        return new String();
    }
}
