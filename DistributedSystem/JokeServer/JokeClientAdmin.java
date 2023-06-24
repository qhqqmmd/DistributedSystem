/*--------------------------------------------------------

1. Name Zirui Huang/ Date 09/25/2022:

2. Java version used (java -version), if not the official version for the class:18.0.2.1

3. Precise command-line compilation examples / instructions:

> java JokeClientAdmin.java


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
import java.net.*;
import java.io.*;

public class JokeClientAdmin {
    static String Mode;
    static String server = "127.0.0.1";

    static void setMode(String mode){//set Mode and then get Mode to check; in put mode is new mode
        PrintStream out;
        BufferedReader in;
        Socket sock;

        try{
            sock=new Socket(server,5050);//set socket to port 5050
            out=new PrintStream(sock.getOutputStream());
            in=new BufferedReader(new InputStreamReader(sock.getInputStream()));
            Mode=in.readLine();//get last Mode
            System.out.println("change from Mode is:"+Mode);
            System.out.flush();
            out.println(mode);//send new mode to server
            out.flush();
            sock.close();
        }catch (IOException x){System.out.println(x);}
    }

    public static void main(String args[]){
        String enter="null";
        System.out.println("Here is Admin");
        System.out.println("name: "+server+", port: 5050");
        BufferedReader typein = new BufferedReader(new InputStreamReader(System.in));
        try {
            System.out.println("please Enter '0'(joke mode) or '1'(proverb mode):");
            enter = typein.readLine();//save what admin put in
            //System.out.println(enter);//check enter

        }catch (IOException x){System.out.println(x);}
        if(enter.indexOf("0")==0){setMode("0");}// judge the input to decided set to mode 1 or 0;
        else if (enter.indexOf("1")==0){setMode("1");}
        System.out.println("set done!");
    }
}
