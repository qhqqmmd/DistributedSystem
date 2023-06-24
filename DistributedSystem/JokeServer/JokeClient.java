/*--------------------------------------------------------

1. Name Zirui Huang/ Date 09/25/2022:

2. Java version used (java -version), if not the official version for the class:18.0.2.1

3. Precise command-line compilation examples / instructions:


> java JokeClient.java



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
import javax.net.ssl.SNIServerName;
import java.io.*;//IO packages
import java.net.*;//networking packages
public class JokeClient {
    static boolean flag=true;//make sure just out put once name!!
    static String text[];
    static String Mode;
    static int[] orderj;//create a random order for joke
    static int[] orderp;
    //static int[] orderp = random();//for proverb
    public static void main (String args[]) {
        String serverName;
        serverName = "127.0.0.1";

        System.out.println("Now communicating with:" + serverName + ", port 4545\n");
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));//read something you type in
        try{
            String name,str;

            System.out.println("please Enter your name:");
            System.out.flush();//clean up immediately
            name=in.readLine();//input name from type
            getText(name,serverName);
            System.out.println("Mode is "+Mode+"(0 is Joke, 1 is Proverb)");
            str ="Get start? or enter";
            System.out.println(str+" 'quit'");
            str=in.readLine();//wait for enter

            orderj=random();//initial
            orderp=random();//joke and proverbs' order

            int numj=0;
            int nump=0;
            while(name.indexOf("quit")<0 && str.indexOf("quit")<0){//check quit
                if(Integer.parseInt(Mode)==1){//print Proverb
                    System.out.println(text[orderp[nump]+4]);
                    nump++;
                } else if (Integer.parseInt(Mode)==0) {//print Joke
                    System.out.println(text[orderj[numj]]);
                    numj++;
                }

                if(numj>3) {//check cycle down of Joke
                    System.out.println("One joke cycle is done");
                    orderj=random();//reinitialize joke
                    numj=0;
                }
                if(nump>3) {//check cycle down of Proverb
                    System.out.println("One proverb cycle is done");
                    orderp=random();//reinitialize proverb
                    nump=0;
                }
                str = in.readLine();//input new enter case to stream
                getText(name,serverName);//check Mode
            }

            System.out.println("finish!");
        }catch(IOException x){x.printStackTrace();}

    }
    public static int[] random() {//create a random int array and in order take jokes!
        int[] random = new int[4];
        int i = 0;
        while (i < random.length) {
            random[i] = (int) (Math.random() * 4);
            for (int j = 0; j < i; j++) {
                if (random[i] == random[j]) {//avoid duplication
                    i--;
                }
            }
            i++;
        }
        return random;
    }

    static void getText (String name, String servername){//get text from server !!
        Socket sock;
        BufferedReader come;
        PrintStream go;
        String textFromServer;
        text=new String[8];//create a string array to save joke and Proverb
        try{

            sock = new Socket(servername, 4545);//choose port number
            come = //get information from buffer
                    new BufferedReader(new InputStreamReader(sock.getInputStream()));
            go = new PrintStream(sock.getOutputStream());//send IP or name to server

            go.println(name);//send name
            go.flush();//clean immediately
            textFromServer=come.readLine();//record Mode
            Mode=textFromServer;//get mode number
            for(int i=0;i<8;i++){//putin 8 string
                textFromServer = come.readLine();//get joke !
                text[i]=textFromServer;//save to array
            }

            sock.close();

        } catch (IOException c) {
            System.out.println (c);
        }

    }
}
