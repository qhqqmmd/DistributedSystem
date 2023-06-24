/*--------------------------------------------------------

1. Name Zirui Huang/ Date 09/25/2022:

2. Java version used (java -version), if not the official version for the class:18.0.2.1

3. Precise command-line compilation examples / instructions:


> javac JokeServer.java
> java JokeServer


4. Precise examples / instructions to run this program:

In separate shell windows:

> java JokeServer
> java JokeClient
> java JokeClientAdmin

5. List of files needed for running the program.

 a. checklist.html
 b. JokeServer.java
 c. JokeClient.java
 d. JokeClientAdmin.java

5. Notes:

----------------------------------------------------------*/
import javax.management.modelmbean.ModelMBean;
import java.io.*;//input the io packge
import java.net.*;//input net package

class Joker extends Thread {//define the joke class
    static int num = 0;//to count time of go through the random
    static int Mode=0;//define mode 0 is joke, 1 is  proverb

    Socket sock;//define socket as sock

    Joker(Socket a) {sock = a;}// for later to allocate

    static String name;//define name

    public static String[] joke() {//make 4 jokes in a String array
        String[] joke = new String[4];
        String j1 = "JA "+name+": What’s one way we know the ocean is friendly? It waves.";
        String j2 = "JB "+name+": What’s one animal you’ll always find at a baseball game? A bat.";
        String j3 = "JC "+name+": What do you call a fish with no eyes? A fsh.";
        String j4 = "JD "+name+": What is Forrest Gump’s email password? 1forrest1.";
        joke[0] = j1;
        joke[1] = j2;
        joke[2] = j3;
        joke[3] = j4;
        return joke;
    }

    public static String[] proverb() {//just same as joke!! make proverb
        String[] proverb = new String[4];//create 4 string for proverb
        String j1 = "PA "+name+": Every cloud has a silver lining.";
        String j2 = "PB "+name+": If at first you don’t succeed, try, try again.Don’t beat your head against a stone wall.";
        String j3 = "PC "+name+": Necessity is the mother of invention.";
        String j4 = "PD "+name+": Two wrongs don’t make a right.";
        proverb[0] = j1;
        proverb[1] = j2;
        proverb[2] = j3;
        proverb[3] = j4;
        return proverb;
    }

    public void run(){
        PrintStream output= null;
        BufferedReader input = null;

            try {//try to find name from put in "getInputStream"
                input = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                name = input.readLine();
                System.out.println("Called by " + name);
            } catch (IOException x) {//catch IO error
                System.out.println("x");
            }

        try{//send joke or proverb!
            output = new PrintStream(sock.getOutputStream());
            output.println(Mode);//get current Mode!!!
                for(String str : joke()) {//use for each loop to out put joke to client!!
                    output.println(str);
                }
                for(String str : proverb()){// same thing out put Proverb
                    output.println(str);
                }
        }catch (IOException x){
            System.out.println(x);
        }
    }
}

class Admin extends Thread{// the thread of Admin client!
    Socket sock;
    Admin(Socket s){sock=s;}//define the function for later use
    PrintStream out= null;
    BufferedReader in = null;

    public void run(){

        System.out.println("Port:5050");
        System.out.println("You are in Admin Tread! Mode:"+Joker.Mode+";0 is joke，1 is proverb");
        try{
            String order;// define the "order" of Admin
            in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
            out=new PrintStream(sock.getOutputStream());
            out.println(Joker.Mode);
            order=in.readLine();

            if(order.indexOf("0")>=0){//if order is 0 or include 0
                Joker.Mode=0;
                System.out.println("mode to 0（joke）");//change Mode to Joke mode
            } else if (order.indexOf("1")>=0) {//order is
                Joker.Mode=1;
                System.out.println("mode to 1（proverb）");// change to proverbMode
            }

        }catch(IOException x){System.out.println(x);}



    }
}

class Looper implements Runnable {// make a runnable class ;to make asynchronous;

    public void run() {
        Socket sock;
        ServerSocket Adminsock;

        try{
            Adminsock= new ServerSocket(5050,6);//define sock port and backlog number;

            while (true){// make a loop to allocate admin class continuously
                System.out.println("Admin ready");
                sock=Adminsock.accept();
                new Admin(sock).start();
            }

        }catch (IOException ioe){System.out.println(ioe);}

    }
}

public class JokeServer {


    public static void main(String[] srg) throws IOException {// main function of server
        int q_len = 6; //define length of line
        int port = 4545;//define port
        Socket sock;
        ServerSocket server = new ServerSocket(port, q_len);


        System.out.println("Mode is "+ Joker.Mode+"\n0 is joke, 1 is proverb");
        System.out.println("JokeServer!!! in port 4545.\n");

        Looper L=new Looper();//allocate Adminlooper
        Thread t=new Thread(L);
        t.start();

        while(true) {
            sock = server.accept(); // accept next connection
            new Joker(sock).start(); // continuing use worker with new client
        }
    }
}

