/*MIME tell the browser what kind of data is coming!
by use some type of MIME:
text data : text/plain text/html...
picture data:image/gif, image/jpeg...
audio data: audio/midi, audio/mpeg...
application data: application/xml...
when the browser read the content type and reade the type and sub-type like something upon

how to return the html type content?
first step we need return the content type ->text/html
second step we just return the html format of data to the browser.
third change the content length of browser view in order to view all data we want

how to return the text content?(just like html)
first step we need return the content type ->text/plain
second step we just return the text format of data to the browser.
third change the content length of browser view in order to view all data we want
*/


import java.io.*;//io package
import java.net.*;// net package

class ListenWorker extends Thread {//make new work thread
    Socket sock;
    ListenWorker (Socket s) {sock = s;}

    public void run(){
        PrintStream out = null;
        BufferedReader in = null;
        try {
            String line;
            out = new PrintStream(sock.getOutputStream());
            in = new BufferedReader
                    (new InputStreamReader(sock.getInputStream()));

            line = in.readLine();//initial the first line of 'get information'.
            int lentext=0;//initial the length of len-text
            String text="";//initial text
            if(line.length()>51){
                //System.out.println(line.length());
                String[] line1 = line.split("&",3);//use split to cut text to 3 different parts
                String name=line1[0].split("=")[1];//get string of->name
                String num1=line1[1].split("=")[1];//get string of->num1
                String num2=line1[2].substring(5,line1[2].length()-9);//get string of->num2
                //System.out.println(name+num1+num2);

                int sum = Integer.valueOf(num1)+Integer.valueOf(num2);//count sum of num1 and num2
                //System.out.println(lentext);
                text="<p>name:"+name+"<p>sum:"+sum+"<p>";//string of html of text result
                lentext=text.length();
            }
            //aad the webadd in to the new html webpage
            String webadd="<form method=\"GET\" action=\"http://localhost:2540/WebAdd.fake-cgi\">\n" +
                    "\n" +
                    "Enter your name and two numbers. My program will return the sum:<p>\n" +
                    "\n" +
                    "<input type=\"text\" name=\"person\" size=\"20\" value=\"YourName\"></p><p>\n" +
                    "\n" +
                    "<input type=\"text\" name=\"num1\" size=\"5\" value=\"4\"> <br>\n" +
                    "<input type=\"text\" name=\"num2\" size=\"5\" value=\"5\"> </p><p>\n" +
                    "\n" +
                    "<input type=\"submit\" value=\"Submit Numbers\">";

            System.out.println("Sending the HTML Reponse now: " +
                    Integer.toString(MiniWebserver.i) + "\n" );//return html response time
            String HTMLResponse = "<html> <h1> The result! " +
                    Integer.toString(MiniWebserver.i++) +  "</h1> <p><p> <hr> <p>";//response the topic of html
            //header
            out.println("HTTP/1.1 200 OK");//put out the html version and connect
            out.println("Connection: open");
            int Len = HTMLResponse.length()+lentext+webadd.length()+3;//length of html
            out.println("Content-Length: " + Integer.toString(Len));//the length of html view
            out.println("Content-Type: text/html \r\n\r\n");// the type of the browser recognized

            out.println(HTMLResponse);
            out.println(text);
            out.println(webadd);



            out.println("</html>");

            sock.close();
        } catch (IOException x) {
            System.out.println("Error: Connetion reset. Listening again...");
        }
    }
}

public class MiniWebserver {

    static int i = 0;

    public static void main(String a[]) throws IOException {
        int q_len = 6;//define the length of the socket line
        int port = 2540;// define the port of the socket
        Socket sock;

        ServerSocket servsock = new ServerSocket(port, q_len);

        System.out.println("MiniWebserver running at 2540.");
        System.out.println("Point Firefox browser to http://localhost:2540/abc.\n");
        while (true) {
            sock = servsock.accept();
            new ListenWorker (sock).start();
        }
    }
}
