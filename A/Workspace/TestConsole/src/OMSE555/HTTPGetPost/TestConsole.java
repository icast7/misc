package OMSE555.HTTPGetPost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TestConsole 
{
	public static void main (String args[]) throws IOException 
	{
		try
		{
		System.out.println ("---------------- Trying POST... ----------------");
		HttpClient httpclient = new DefaultHttpClient();
		//HttpPost httppost= new HttpPost("http://weather.noaa.gov/mgetmetar.php");

		String postURL = "http://web.cecs.pdx.edu/~jwater/index.php";
		System.out.println ("POST URL: "+  postURL);
		
		HttpPost httppost= new HttpPost(postURL);
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		nameValuePairs.add(new BasicNameValuePair("action","user_login"));
		nameValuePairs.add(new BasicNameValuePair("username","me"));
		nameValuePairs.add(new BasicNameValuePair("password","me"));
		
		System.out.println ("POST VALUE PAIRS: ");
		for (NameValuePair n : nameValuePairs)
			System.out.println ("|NAME|"+ n.getName() +"|VALUE|"+ n.getValue() + "|");
		
		
		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		System.out.println ("POST ENTITY: "+  httppost.getEntity().toString());
		
		
		
		HttpResponse response = httpclient.execute(httppost);
		int status =  response.getStatusLine().getStatusCode();
		System.out.println ("STATUS CODE: "+ status);
		
		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
		String line = "";
		while((line=rd.readLine())!=null)
		{
			System.out.println (line);
		}
		System.out.println ("-------------------------------------------------------------------------");
		
		System.out.println ("---------------- Trying GET... ----------------");	
		HttpClient httpclient2 = new DefaultHttpClient();
		String getURL = "http://web.cecs.pdx.edu/~jwater/index.php?action=user_login&username=me&password=me";
		System.out.println ("GET URL: "+ getURL);
		
		HttpGet httpget= new HttpGet(getURL);
		HttpResponse response2 = httpclient2.execute(httpget);
		int status2 =  response2.getStatusLine().getStatusCode();
		System.out.println ("STATUS CODE: "+ status2);
		
		
		BufferedReader rd2 = new BufferedReader(new InputStreamReader(response2.getEntity().getContent()));
		String line2 = "";
		while((line2 = rd2.readLine())!=null)
		{
			System.out.println(line2);
		}
		
//		System.out.println("---------------------------");
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//		   //get current date time with Date()
//		   Date date = new Date();
//		   System.out.println(dateFormat.format(date));
//		   System.out.println(date.toString());
//		   System.out.println(System.nanoTime() + "" );
//		   
//		   System.out.println("---------------------------");
//		   //get current date time with Calendar()
//		   Calendar cal = Calendar.getInstance();
//		   System.out.println(dateFormat.format(cal.getTime()));
//		  System.out.println("---------------------------");
		  //System.out.println(dateFormat.format(cal.getTime().toString()));
		   
		   
		
		System.exit(0);
		}		
		catch (ClientProtocolException e)
		{
			System.out.println(e.toString());
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
	}
}