package OMSE555.HTTPGetPost;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Scanner;

public class TestConsoleJava {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		try
		{
			URL url;
			HttpURLConnection conn;
			//if you are using https, make sure to import java.net.HttpsURLConnection
			url=new URL("http://weather.noaa.gov/mgetmetar.php");

			//you need to encode ONLY the values of the parameters
			String param="cccc=" + URLEncoder.encode("KPDX","UTF-8");

			conn=(HttpURLConnection)url.openConnection();
			//set the output to true, indicating you are outputting(uploading) POST data
			conn.setDoOutput(true);
			//once you set the output to true, you don't really need to set the request method to post, but I'm doing it anyway
			conn.setRequestMethod("POST");

			//Android documentation suggested that you set the length of the data you are sending to the server, BUT
			// do NOT specify this length in the header by using conn.setRequestProperty("Content-Length", length);
			//use this instead.
			conn.setFixedLengthStreamingMode(param.getBytes().length);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			//send the POST out
			PrintWriter out = new PrintWriter(conn.getOutputStream());
			out.print(param);
			out.close();
			//build the string to store the response text from the server
			//start listening to the stream
			Scanner inStream = new Scanner(conn.getInputStream());
			//process the stream and store it in StringBuilder
			while(inStream.hasNextLine())
			{
				System.out.println(inStream.nextLine());
			}
			System.exit(0);
			}
			//catch some error
			catch(MalformedURLException e)
			{  
				//Toast.makeText(this, e.toString(), 1 ).show();
				System.out.println(e.toString());
			}
			catch (IOException e)
			{
				//Toast.makeText(this,e.toString(),1).show();
				System.out.println(e.toString());
			}
	}

}
