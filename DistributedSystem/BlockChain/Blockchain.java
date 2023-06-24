import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import com.google.gson.*;

class PublicKeyObj{//creat a new class of pub lic key
    String publickey;
    int processID;

    public int getProcessID() {
        return processID;
    }

    public String getPublickey() {
        return publickey;
    }

    public void setProcessID(int processID) {
        this.processID = processID;
    }

    public void setPublickey(String publickey) {
        this.publickey = publickey;
    }

}
class Block1 {//define a block object

    String Fname;
    String Lname;
    String SSNum;
    String DOB;
    String Diag;
    String Treat;
    String Rx;
    String RandomSeed;
    String SignedHash;
    String VerificationProcessID;
    String BlockID;
    public int Blocknum;
    public String hash;
    public String previousHash;
    public String data;


    private String timeStamp;

    public String getSignedHash() {
        return SignedHash;
    }

    public void setSignedHash(String signedHash) {
        SignedHash = signedHash;
    }

    public void setVerificationProcessID(String verificationProcessID) {
        VerificationProcessID = verificationProcessID;
    }

    public String getVerificationProcessID() {
        return VerificationProcessID;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getBlockID() {return BlockID;}
    public void setBlockID (String ud){this.BlockID = ud;}
    public String getSSNum() {return SSNum;}
    public void setSSNum (String SS){this.SSNum = SS;}

    public String getDOB() {return DOB;}
    public void setDOB (String RS){this.DOB = RS;}

    public String getDiag() {return Diag;}
    public void setDiag (String D){this.Diag = D;}

    public String getTreat() {return Treat;}
    public void setTreat (String Tr){this.Treat = Tr;}

    public String getRx() {return Rx;}
    public void setRx (String Rx){this.Rx = Rx;}

    public String getRandomSeed() {return RandomSeed;}
    public void setRandomSeed (String RS){this.RandomSeed = RS;}
    public void setBlocknum(int blocknum) {
        Blocknum = blocknum;
    }

    public int getBlocknum() {
        return Blocknum;
    }

    public String getLname() {return Lname;}
    public void setLname (String LN){this.Lname = LN;}

    public String getFname() {return Fname;}
    public void setFname (String FN){this.Fname = FN;}

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getPreviousHash() {
        return previousHash;
    }
    public Block1(){}
}


class Ports{//de fined the port base of these service and worker
    public static int KeyServerPortBase = 4710;
    public static int UnverifiedBlockServerPortBase = 4820;
    public static int BlockchainServerPortBase = 4930;
    public static int P2ServerPortBase = 4740;
    public static int KeyServerPort;
    public static int UnverifiedBlockServerPort;
    public static int BlockchainServerPort;
    public static int P2ServerPort;

    public void setPorts(){//set the port of socket
        KeyServerPort = KeyServerPortBase+(Blockchain.pnum * 1000);
        UnverifiedBlockServerPort = UnverifiedBlockServerPortBase + (Blockchain.pnum * 1000);
        BlockchainServerPort = BlockchainServerPortBase + (Blockchain.pnum * 1000);
        P2ServerPort = P2ServerPortBase + (Blockchain.pnum * 1000);
    }
}


/*
some code below thses is modified by me from the Bc.java
it is all about the worker and the server. p2 just need receive a String signal
and take it to main function ,to start the whole thing

the other --key, UVB and BC is all about change the String to json and send to the other ports
and the worker read this line as json formate and as themselfes class
 */
class P2Worker extends Thread{// woker of P2 command
    Socket sock;
    String signal;
    P2Worker (Socket s) {this.sock = s;}

    public void run(){
        //String signal="waitP2";
        //System.out.println("In Unverified Block Worker");
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String signal= in.readLine();//read the signal from p2
            System.out.println("signal:"+signal);
            if(signal.equals("start")) Blockchain.P2signal="start";
        } catch (Exception x){x.printStackTrace();}
    }
}
class P2Server implements Runnable {// Server of reseive p2 command

    public void run(){
        int q_len = 6;
        Socket sock;
        System.out.println("wait for the P2 signal " +
                Integer.toString(Ports.P2ServerPort));
        try{
            ServerSocket P2Server = new ServerSocket(Ports.P2ServerPort, q_len);
            while (true) {
                sock =P2Server.accept();
                new P2Worker(sock).start();
            }
        }catch (IOException ioe) {System.out.println(ioe);}
    }
}

class UnverifiedBlockWorker extends Thread {//the thread of UVB
    Socket sock;
    UnverifiedBlockWorker (Socket s) {this.sock = s;}

    public void run(){
        Gson gson=new Gson();
        //System.out.println("In Unverified Block Worker");
        try{
            BufferedReader unverifiedIn = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String json= unverifiedIn.readLine();

            Block1 data = gson.fromJson(json,Block1.class);
            Blockchain.PriorityQueue.add(data);

            sock.close();
        } catch (Exception x){x.printStackTrace();}
    }
}
class UnverifiedBlockServer implements Runnable {//from professor's bc.java

    public void run(){ // reiceive the massage of UVB from other process
        int q_len = 6;
        Socket sock;
        System.out.println("Starting the Unverified Block Server input thread using " +
                Integer.toString(Ports.UnverifiedBlockServerPort));
        try{
            ServerSocket UVBServer = new ServerSocket(Ports.UnverifiedBlockServerPort, q_len);//define the port we need
            while (true) {
                sock = UVBServer.accept(); //get a new UVB

                new UnverifiedBlockWorker(sock).start();//get start to receive it
            }
        }catch (IOException ioe) {System.out.println(ioe);}
    }
}

class PublicKeyWorker extends Thread { // Worker thread which can take incoming public keys
    Socket keySock;
    PublicKeyWorker (Socket s) {keySock = s;}
    public void run(){
        Gson gson=new Gson();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(keySock.getInputStream()));
            String data = in.readLine ();
            //System.out.println("Got key: " + data);
            PublicKeyObj pk = gson.fromJson(data,PublicKeyObj.class);
            Blockchain.publicKeyObjList.add(pk);
            keySock.close();
        } catch (IOException x){x.printStackTrace();}
    }
}

class PublicKeyServer implements Runnable {// the method to receive key object

    public void run(){
        int q_len = 6;
        Socket keySock;
        System.out.println("Starting Key Server input thread using " + Integer.toString(Ports.KeyServerPort));
        try{
            ServerSocket servsock = new ServerSocket(Ports.KeyServerPort, q_len);
            while (true) {
                keySock = servsock.accept();
                new PublicKeyWorker (keySock).start();
            }
        }catch (IOException ioe) {System.out.println(ioe);}
    }
}

class BlockchainWorker extends Thread { // the worker to take block chain
    Socket sock;
    public BlockchainWorker (Socket s) {this.sock = s;}
    public void run(){// it is just same to other thread of worker
        Gson gson=new Gson();
        try{
            BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
            String json=in.readLine();
            Block1[] blocklist=gson.fromJson(json,Block1[].class);//read blocklist from json
            Blockchain.BlockChain.clear();
            for (Block1 b:blocklist){
                Blockchain.BlockChain.add(b);

            }
            System.out.println("ID:"+blocklist[0].BlockID);
            System.out.println("num"+blocklist[0].Blocknum);
            if(Blockchain.pnum==0)Blockchain.writeToFile(Blockchain.BlockChain);
            sock.close();
        } catch (IOException x){x.printStackTrace();}
    }
}

class BlockchainServer implements Runnable {//the method to receive the block chain list
    public void run(){
        int q_len = 6;
        Socket sock;
        System.out.println("Starting the Blockchain server input thread using " + Integer.toString(Ports.BlockchainServerPort));
        try{
            ServerSocket servsock = new ServerSocket(Ports.BlockchainServerPort, q_len);
            while (true) {
                sock = servsock.accept();
                new BlockchainWorker (sock).start();
            }
        }catch (IOException ioe) {System.out.println(ioe);}
    }
}
/*
code below the line is the public class of the chain
 */

public class Blockchain {
    public static String P2signal = "wait";
    static int pnum;
    static String serverName = "localhost";
    public static PrivateKey privKey;
    public static LinkedList<Block1> BlockChain = new LinkedList<>();//make a list to save block chain
    public static List<PublicKeyObj> publicKeyObjList = new ArrayList<>();// Create a new Array list to put public key and process id
    public static List<Block1> UBVlist = new ArrayList<>();// create a new list to put UVB
    static String randString;
/*
thses code is from BlockInputG.java
to in put the txt file to java object
 */
    private static String FILENAME;
    //something input in this order!
    //later we will split the string from txt to json item
    private static final int iFNAME = 0;
    private static final int iLNAME = 1;
    private static final int iDOB = 2;
    private static final int iSSNUM = 3;
    private static final int iDIAG = 4;
    private static final int iTREAT = 5;
    private static final int iRX = 6;

    //input some unverified block from txt to json to java
    public static List<Block1> BlockInput(int pnum) {//use the method of BlockinputG.java
        List<Block1> recordList = new ArrayList<Block1>();

        switch (pnum) {//the  switch which distribute the file input to process
            case 1:
                FILENAME = "BlockInput1.txt";
                break;
            case 2:
                FILENAME = "BlockInput2.txt";
                break;
            default:
                FILENAME = "BlockInput0.txt";
                break;
        }
        System.out.println("Using input file: " + FILENAME);
        try {
            BufferedReader br = new BufferedReader(new FileReader(FILENAME));

            String[] tokens = new String[10];
            String InputLineStr;
            String suuid;
            UUID idA;
            Block1 tempRec;

            StringWriter sw = new StringWriter();
            int n = 0;//count mathod
            while ((InputLineStr = br.readLine()) != null) {
                Block1 BR = new Block1(); // Careful
                try {
                    Thread.sleep(1001);
                } catch (InterruptedException e) {
                }
                Date date = new Date();
                String T1 = String.format("%1$s %2$tF.%2$tT", "", date);//define the format of data and save to t1
                String TimeStampString = T1 + "." + pnum;
                System.out.println("Timestamp: " + TimeStampString);
                BR.setTimeStamp(TimeStampString);
                BR.setBlocknum(n);
                suuid = new String(UUID.randomUUID().toString());
                BR.setBlockID(suuid);//sign theuuid by privkey
                byte[] signature = signData(suuid.getBytes(), privKey);//should process after define the private key
                String signaturedid = Base64.getEncoder().encodeToString(signature);
                BR.setSignedHash(signaturedid);//sign the hash

                tokens = InputLineStr.split(" +"); //take the input to the BR object
                BR.setFname(tokens[iFNAME]);
                BR.setLname(tokens[iLNAME]);
                BR.setSSNum(tokens[iSSNUM]);
                BR.setDOB(tokens[iDOB]);
                BR.setDiag(tokens[iDIAG]);
                BR.setTreat(tokens[iTREAT]);
                BR.setRx(tokens[iRX]);

                recordList.add(BR);
                n++;
            }

        } catch (Exception e) {
            System.out.println(e);
        }
        return recordList;
    }

    public static Comparator<Block1> TimeComparator = new Comparator<Block1>() {

        public int compare(Block1 o1, Block1 o2) {//a compare mathod in order to use to the priority queue
            String t1 = o1.getTimeStamp();
            String t2 = o2.getTimeStamp();
            if (t1 == t2) {
                return 0;
            } else if (t1 == null) {
                return -1;
            } else if (t2 == null) {
                return 1;
            } else return t1.compareTo(t2);
        }
    };
    public static Queue<Block1> PriorityQueue = new PriorityQueue<>(4, TimeComparator);
/*
some code below this line,is just for the format of different block in work ;
we need transform some byte to String in order to make the signature
 */
    public static String ByteArrayToString(byte[] ba) {//take the array to the String
        StringBuilder hex = new StringBuilder(ba.length * 2);
        for (int i = 0; i < ba.length; i++) {
            hex.append(String.format("%02X", ba[i]));
        }
        return hex.toString();
    }

    public static String randomAlphaNumeric(int count) {//create a random numaric
        StringBuilder builder = new StringBuilder();
        while (count-- != 0) {
            int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
            builder.append(ALPHA_NUMERIC_STRING.charAt(character));
        }
        return builder.toString();
    }

    private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    static String someText = "one two three";

    public static String concat(Block1 block) {//concat
        String cat = block.getBlockID() +
                block.getFname() +
                block.getLname() +
                block.getBlocknum() +
                block.getPreviousHash() +
                block.getHash() +
                block.getTimeStamp() +
                block.getDiag() +
                block.getDOB() +
                block.getRx() +
                block.getTreat() +
                block.getSSNum();
        return cat;
    }
/*
we can control the difficalty by chang the length of randomnumber; if the number is big; it will be harder
for anothor we canchange the worker number ,if workder number small we need make more calculate
so it is more slowly
 */
    public static Block1 WorkA(Block1 block) {//work for solve puzzle
        String concatData = "";

        String hash = "";
        int workNumber = 0;
        block.setPreviousHash(Blockchain.BlockChain.get(0).getPreviousHash());
        block.setBlocknum(Blockchain.BlockChain.get(0).getBlocknum() + 1);
        block.setVerificationProcessID(Integer.toString(Blockchain.pnum));
        String data = concat(block);
        try {
            while (true) {
                randString = randomAlphaNumeric(5);//if random string very long work will be harder
                concatData = data + randString;
                //make the data to the sha 256 algorithm
                MessageDigest MD = MessageDigest.getInstance("SHA-256");
                byte[] bytesHash = MD.digest(concatData.getBytes("UTF-8"));
                hash = ByteArrayToString(bytesHash);
                System.out.println("Hash is: " + hash);

                //get the signd hash
                workNumber = Integer.parseInt(hash.substring(0, 4), 16);
                System.out.println("First 16 bits in Hex and Decimal: " + hash.substring(0, 4) + " and " + workNumber);
                if (!(workNumber < 20000)) {
                    System.out.format("%d is not less than 20,000 so we did not solve the puzzle\n\n", workNumber);
                }
                if (workNumber < 20000) {//I can set the worker number smaller make it harder
                    System.out.println("block verified !");
                    System.out.format("%d IS less than 20,000 so puzzle solved!\n", workNumber);
                    System.out.println("The seed (puzzle answer) was: " + randString);
                    block.setRandomSeed(randString);
                    block.setHash(hash);
                    byte[] signedhash = signData(bytesHash, privKey);
                    String signedString = Base64.getEncoder().encodeToString(signedhash);
                    block.setSignedHash(signedString);
                    break;
                }
                for (Block1 b : BlockChain) {
                    if (b.getBlockID() == block.getBlockID()) {
                        System.out.println("Abandon block");
                        Block1 abandonBlock = new Block1();
                        abandonBlock.setBlockID("Abandon");
                        return abandonBlock;
                    }
                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return block;
    }

    public static String applySha256(String input) {//method which use to make the hash
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            //make the sha 256 as input
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer(); // it will savethe  hash hexidecimal
            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);//define the length of hash
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void initialBlockchain() {//initial the first block0

        LinkedList<Block1> bc = new LinkedList<>();

        Block1 block0 = new Block1();
        String suuid = new String(UUID.randomUUID().toString());
        block0.setBlockID(suuid);
        block0.setBlocknum(0);
        try {
            Thread.sleep(1000);
        } catch (InterruptedException E) {
        }
        Date date = new Date();
        String T1 = String.format("%1$s %2$tF.%2$tT", "", date);
        String TimeStampString = T1 + "." + Blockchain.pnum;
        block0.setTimeStamp(TimeStampString);
        block0.setVerificationProcessID("0");
        block0.setPreviousHash("0");
        block0.setFname("li");
        block0.setLname("baba");
        block0.setDOB("2000.01.01");
        block0.setSSNum("888-88-9999");
        block0.setDiag("break");
        block0.setTreat("reset");
        block0.setRx("aaa pill");
        block0.setRandomSeed("12345678");
        block0.setHash(applySha256(concat(block0) + block0.getRandomSeed()));
        bc.add(block0);
        BlockChain = bc;
        //writeToFile(BlockChain);
    }

    public static void writeToFile(LinkedList<Block1> BlockChain) {//write the block chain to json
        UUID BinaryUUID = UUID.randomUUID();
        String suuid = BinaryUUID.toString();//create a uuid to the chain
        System.out.println("Unique Block ID: " + suuid + "\n");//print it
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        //String json = gson.toJson(BlockChain.get(0));
        String json;
        json = "[";
        for (Block1 block : BlockChain) {//take all block to json for the format we want
            json += gson.toJson(block);
            if (BlockChain.indexOf(block) == BlockChain.size() - 1) break;
            json += ",";
        }
        json += "]";
        //try (FileWriter writer = new FileWriter("block"+block.getBlockID()+".json")) {
        try (FileWriter writer = new FileWriter("BlockchainLedger" + ".json", false)) {
            //gson.toJson(json, writer);
            writer.write(json);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    public static PublicKeyObj readKey(String name) {//the function to read key from json
        Gson gson = new Gson();
        PublicKeyObj in = new PublicKeyObj();//but for now it is useless
        try (Reader reader = new FileReader(name)) {

            PublicKeyObj KeyIn = gson.fromJson(reader, PublicKeyObj.class);

            //System.out.println(KeyIn.processID);

            //System.out.println(KeyIn.publickey);

            in.setPublickey(KeyIn.publickey);
            in.setProcessID(KeyIn.processID);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return in;
    }

    public static void ReadJSON(String name) {
        System.out.println("\n=========> In ReadJSON <=========\n");

        Gson gson = new Gson();

        try (Reader reader = new FileReader(name)) {//the function of read json

            Block1 blockRecordIn = gson.fromJson(reader, Block1.class);

            System.out.println(blockRecordIn);



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PublicKeyObj initialPublickey(int pnum) throws Exception {//initilize the public key with process
        Random ran = new Random();
        long ranNum = ran.nextInt(1000);
        //System.out.println(ranNum);
        KeyPair keyPair = generateKeyPair(ranNum);//get the key pair with the random num!!
        privKey = keyPair.getPrivate();
        byte[] bytePubkey = keyPair.getPublic().getEncoded();//get the byte formate key!
        System.out.println("Key in Byte[] form: " + bytePubkey);

        String stringKey = Base64.getEncoder().encodeToString(bytePubkey);
        System.out.println("Key in String form: " + stringKey);
        PublicKeyObj pk = new PublicKeyObj();
        pk.setPublickey(stringKey);
        pk.setProcessID(pnum);
        return pk;
    }

    public static KeyPair generateKeyPair(long seed) throws Exception {// get the key pair of a seed
        KeyPairGenerator keyGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom rng = SecureRandom.getInstance("SHA1PRNG", "SUN");
        rng.setSeed(seed);//made a randomseed
        keyGenerator.initialize(1024, rng);

        //and create a pare of public key and privatekey
        return (keyGenerator.generateKeyPair());
    }

    //just sign the data by privkey
    public static byte[] signData(byte[] data, PrivateKey key) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initSign(key);//use signature method to sign data by private key
        signer.update(data);
        return (signer.sign());
    }

    //verify the if the data sign by our public key
    public static boolean verifySig(byte[] data, PublicKey key, byte[] sig) throws Exception {
        Signature signer = Signature.getInstance("SHA1withRSA");
        signer.initVerify(key);
        signer.update(data);//the method to verify the key
        // if it can verify return true

        return (signer.verify(sig));
    }

    public static String bctojson(LinkedList<Block1> bc) {
        Gson gson = new GsonBuilder().create();
        String json;
        json = "[";
        for (Block1 block : BlockChain) {
            json += gson.toJson(block);//turn the block chan to gson format
            if (BlockChain.indexOf(block) == BlockChain.size() - 1) break;
            json += ",";
        }
        json += "]";
        return json;
    }

    public void MuticastBC(LinkedList<Block1> bc) {//multicast for blockchain
        Socket sock;
        PrintStream toServer;
        Gson gson = new GsonBuilder().create();
        String json = bctojson(bc);
        try {
            for (int i = 0; i < 3; i++) {
                sock = new Socket(serverName, Ports.BlockchainServerPortBase + (i * 1000));//try to connect with every port 
                toServer = new PrintStream(sock.getOutputStream());
                toServer.println(json);
                toServer.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void MulticastPK(PublicKeyObj pk) {//send publickeyobj to all process
        Socket sock;
        PrintStream toServer;
        Gson gson = new GsonBuilder().create();
        String json = gson.toJson(pk);

        try {

            for (int i = 0; i < 3; i++) {// Send our public key to all servers.
                if (i != pnum) {
                    //System.out.println(i+"process");
                    sock = new Socket(serverName, Ports.KeyServerPortBase + (i * 1000));//try to connect with every port 
                    toServer = new PrintStream(sock.getOutputStream());
                    //toServer.println("I get public key from" +pk.processID+" key:"+ pk.getPublickey());
                    toServer.println(json);
                    toServer.flush();
                    sock.close();
                }
            }
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void MulticastUVB(Block1 block) {//send the unveriied block to all processes
        Socket sock;
        PrintStream toServer;
        try {
            Gson gson = new Gson();

            String json = gson.toJson(block);
            for (int i = 0; i < 3; i++) {
                sock = new Socket(serverName, Ports.UnverifiedBlockServerPortBase + (i * 1000));//try to connect with every port 
                toServer = new PrintStream(sock.getOutputStream());
                toServer.println(json);
                toServer.flush();
            }
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void MulticastP2() {//send the message of p2 online
        Socket sock;
        PrintStream toServer;
        try {
            for (int i = 0; i < 3; i++) {
                sock = new Socket(serverName, Ports.P2ServerPortBase + (i * 1000));
                toServer = new PrintStream(sock.getOutputStream());
                toServer.println("start");
                toServer.flush();
            }
        } catch (IOException e) {
        }
    }


    public static void main(String[] args) throws Exception {
        if (args[0].equals("0")) pnum = 0;
        else if (args[0].equals("1")) pnum = 1;
        else if (args[0].equals("2")) pnum = 2;
        else System.err.println("please enter0,1 or 2");
        System.out.println("come from process[" + pnum + "]");
        initialBlockchain();//initial a block0

        new Thread(new P2Server()).start();//wait p2 online

        //ReadJSON("block3.json");
        System.out.println("---------------------------------------------------------------------");

        Blockchain bc = new Blockchain();

        bc.run(args);

    }

    public void run(String args[]) throws Exception {
        new Ports().setPorts();
        if (pnum == 2) {//if it is p2 multicast to other process
            MulticastP2();
        }
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
        }
        if (!P2signal.equals("start")) {
            System.err.println("there are no process 2!");
            System.exit(0);//if no p2 process done
        }
        PublicKeyObj pk = new PublicKeyObj();//create a new public key class

        try {
            pk = initialPublickey(pnum);//initial publickey;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //writeKey(pk);
        //put after the initial key,it will use private key to sign ID
        UBVlist = BlockInput(pnum);//read unverified block from txt file

        new Thread(new PublicKeyServer()).start();
        new Thread(new BlockchainServer()).start();
        new Thread(new UnverifiedBlockServer()).start();


        WorkA(BlockChain.get(0));//work for the first block0
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        System.out.println("--------------work with the first block0--------------");


        MulticastPK(pk);
        try {
            Thread.sleep(4000);
        } catch (Exception e) {
        }
        for (PublicKeyObj key : publicKeyObjList) {//print other processes's public key
            System.out.println("Proccess ID:" + key.processID + "Public key:" + key.publickey);
            System.out.println("-------------------the public key received-------------");
        }

        MuticastBC(BlockChain);
        try {
            Thread.sleep(2000);
        } catch (Exception e) {
        }

        for (Block1 block : UBVlist) {//print input order
            new Blockchain().MulticastUVB(block);
            System.out.println("Name input:" + block.getFname() + " " + block.getLname());
        }

        try {
            Thread.sleep(3000);
        } catch (Exception e) {
        }
        for (Block1 block : PriorityQueue) {//name order in priority queue
            System.out.println("Name in PriorityQueue order:" + block.getFname() + " " + block.getLname() + "time:" + block.getTimeStamp());
        }
        System.out.println("---------------------try to verifyed these blocks-------------------");
        //for (Block1 block : PriorityQueue) {
        // WorkA(block);
        //try{Thread.sleep(5000);}catch (Exception e){}
        //}//just for fun!!
        writeToFile(BlockChain);


    }
}
