package omse555.HttpGetPost;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.webkit.WebView;
import android.widget.Toast;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class MyHttpGetPostActivity extends Activity {
    /** Called when the activity is first created. */
	private LocationManager mgr = null;
    private Button postButton, getButton;
    private EditText gettext, posttext, latValue, longValue, routeidValue, resulttext;
    private TextView latLabel, longLabel;
    private WebView webview;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
             
        //latLabel = (TextView)findViewById(R.id.latLabel);
        latValue = (EditText)findViewById(R.id.latValue);
        //longLabel = (TextView)findViewById(R.id.longLabel);
        longValue = (EditText)findViewById(R.id.longValue);
        
        routeidValue = (EditText)findViewById(R.id.routeidValue);
        
        getButton = (Button)findViewById(R.id.getButton);
        resulttext = (EditText)findViewById(R.id.resultText);                        
        webview = (WebView)findViewById(R.id.webkit);      

        routeidValue.setText("0");
        
        resulttext.setText("<<LOADED>>");
        
//        postURL.setText("http://weather.noaa.gov/mgetmetar.php");
        
        getButton.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View view)
            {
            	TryGet task = new TryGet();
            	task.execute(new String[]{});
            }  
        });
        
//        postButton.setOnClickListener(new OnClickListener() 
//        {
//            public void onClick(View view)
//            {
//            	TryPost task = new TryPost();
//            	task.execute(new String[]{postURL.getText().toString(), posttext.getText().toString() });
//            }
//        });

    }

	@Override
	public void onResume()
	{
		super.onResume();
		mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3600000, 1000, onLocationChange);
//		String sessionId = System.nanoTime() + "";// Add device identifier
//        String myresponse = httpGetImplementation("http://web.cecs.pdx.edu/~jwater/index.php?cmd=user_login&username=egor&password=Abcde123");       
//        resulttext.append(myresponse);
//        String myresponse2 = httpGetImplementation("http://web.cecs.pdx.edu/~jwater/index.php?cmd=new_route&sessionid=" + sessionId + "ICL");       
//        resulttext.append(myresponse2);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mgr.removeUpdates(onLocationChange);
	}
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
	}
	
	LocationListener onLocationChange = new LocationListener() {
	public void onLocationChanged(Location location) 
	{
		latValue.setText(location.getLatitude()+"");
		longValue.setText(location.getLongitude()+"");
		//location.getLatitude();
		//location.getLongitude();	
	}
		
		
	public void onProviderDisabled(String provider) 
	{
		// required for interface, not used
	}
		
	public void onProviderEnabled(String provider) 
	{
			// required for interface, not used
	}
		
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// required for interface, not used
	}};
	
    private class TryPost extends AsyncTask<String, Void, String>{
    	@Override
        protected void onPreExecute() {
			resulttext.setText("POSTING...");
			webview.loadData("loading...","text/html", "UTF-8");
    	}
    	@Override
    	protected String doInBackground(String... parms) 
    	{
    		return httpPostImplementation(parms);
		}
    	@Override
        protected void onPostExecute(String result) {
			resulttext.setText(result);
			webview.loadData(result, "text/html", "UTF-8");
        }    	
    }
	private String httpPostImplementation(String... parms) {
		String result = "";
		try
		{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost= new HttpPost(parms[0]);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
			nameValuePairs.add(new BasicNameValuePair("cccc",parms[1]));
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = httpclient.execute(httppost);
			int status =  response.getStatusLine().getStatusCode();
			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while((line=rd.readLine())!=null)
			{
				result += line;
			}
		}
		catch (ClientProtocolException e)
		{
			result = e.getMessage();
		}
		catch (IOException e)
		{
			result = e.getMessage();
		}
		return result;
	}
    
    private class TryGet extends AsyncTask<String, Void, String>{
    	@Override
        protected void onPreExecute() {
			resulttext.setText("GETTING...");
			webview.loadData("loading...","text/html", "UTF-8");
        }
    	@Override
    protected String doInBackground(String... parms) 
    {
    		String getlatlong = "http://web.cecs.pdx.edu/~jwater/index.php?action=post_coord&route_id="+ routeidValue.getText() +"&lat="+ latValue.getText() +"&lon=" + longValue.getText() + "&sessionid=764efa883dda1e11db47671c4a3bbd9e";
    		return httpGetImplementation(getlatlong);
    }
    	
	@Override
	protected void onPostExecute(String result)
	{
		resulttext.setText(result);
		webview.loadData(result, "text/html", "UTF-8");
	}
    }

    private String httpGetImplementation(String geturl) 
	{
		String result="";
		try
			{
				HttpClient httpclient = new DefaultHttpClient();
				HttpGet httpget= new HttpGet(geturl);
				HttpResponse response = httpclient.execute(httpget);
				int status =  response.getStatusLine().getStatusCode();
				BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while((line = rd.readLine())!=null)
				{
					result += line;
				}
			}
			catch (ClientProtocolException e)
			{
				result = e.getMessage();
			}
			catch (IOException e)
			{
				result = e.getMessage();
			}
			return result;
	}
}    

//protected void tryGet(String pwd)
//{   
//	try
//	{
//		resulttext.setText("");
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpGet httpget= new HttpGet("http://www.google.com");
//		
//		resulttext.setText("Attempting to connect...");
//		HttpResponse response = httpclient.execute(httpget);
//	
//		int status =  response.getStatusLine().getStatusCode();
//		resulttext.setText("STATUS:" + status);
//	
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		String line = "";
//		while((line = rd.readLine())!=null)
//		{
//			resulttext.append(line);
//		}
//		Toast.makeText(this,"DONE!",1).show();
//	}
//	catch (ClientProtocolException e)
//	{
//		Toast.makeText(this, "ClientProtocolException",1).show();
//		resulttext.append(e.toString());
//	}
//	catch (IOException e)
//	{
//		Toast.makeText(this, "IOException", 1).show();
//		resulttext.append(e.toString());
//	}
//}
//
//protected void tryPost(String pwd)
//{   
//	try
//	{
//		resulttext.setText("");
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpPost httppost= new HttpPost("http://weather.noaa.gov/mgetmetar.php");
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
//		nameValuePairs.add(new BasicNameValuePair("cccc","KPDX"));
//		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//		
//		resulttext.setText("Attempting to connect...");
//		HttpResponse response = httpclient.execute(httppost);
//		int status =  response.getStatusLine().getStatusCode();
//		resulttext.setText("STATUS:" + status);
//	
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		String line = "";
//		while((line=rd.readLine())!=null)
//		{
//			resulttext.append(line);
//		}
//		Toast.makeText(this,"DONE!",1).show();
//	}
//	catch (ClientProtocolException e)
//	{
//		Toast.makeText(this,"ClientProtocolException",1).show();
//		resulttext.append(e.toString());
//	}
//	catch (IOException e)
//	{
//		Toast.makeText(this, "IOException", 1).show();
//		resulttext.append(e.toString());
//	}
//}
//getButton.setOnClickListener(new OnClickListener() 
//{
//  public void onClick(View v) 
//  {
//      String   mGetText = gettext.getText().toString();
//      tryGet(mGetText);
//  }
//});
//
//postButton.setOnClickListener(new OnClickListener() 
//{
//  public void onClick(View v) 
//  {
//      String   mPostText = posttext.getText().toString();
//      tryPost(mPostText);
//  }
//});
//<WebView android:id="@+id/webkit" 
//	android:layout_width="match_parent" 
//	android:layout_height="wrap_content"/>