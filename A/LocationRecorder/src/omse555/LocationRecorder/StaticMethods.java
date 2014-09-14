package omse555.LocationRecorder;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.Log;
import android.widget.Toast;

public final class StaticMethods 
{
    private StaticMethods() {}
  
    
    
    public static String GetAllXMLValues(String separator, String xmlString) //throws XmlPullParserException, IOException
    {
    	String result = "";
    	String startTag = "", endTag = "", textValue = "";
	    try{
	    	
	    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
	    	factory.setNamespaceAware(true);
			XmlPullParser xpp = factory.newPullParser();
			xpp.setInput(new StringReader (xmlString) );
			int eventType = xpp.getEventType();
			while (eventType != XmlPullParser.END_DOCUMENT) {
				     if(eventType == XmlPullParser.START_DOCUMENT) {
				   	  Log.v("Start document","");
				     } else if(eventType == XmlPullParser.START_TAG) {
				    	 startTag = xpp.getName(); 
				    	 Log.v("Start tag ",startTag);
				     } else if(eventType == XmlPullParser.END_TAG) {
				    	 endTag =  xpp.getName();
				    	 Log.v("End tag ",xpp.getName());
				     } else if(eventType == XmlPullParser.TEXT) {
				    	textValue = xpp.getText();
				    	if (textValue != "")
				    	{
				    		if ((textValue.trim().length() > 0))
				    		{
				    			textValue = textValue.trim() +  separator;
				    			if (textValue.startsWith("fault"))
				    				textValue ="\n" + textValue;
				    			result += textValue;
				    		}				    		
				    	}
				    	Log.v("Text ", textValue);
				     }
				     eventType = xpp.next();
			    }
			Log.v("End document","");
	    }
	    catch (Exception e)
	    {    	
	    }
			return result;
	    }
	
    public static boolean tryParseInt(String value)  
    {  
         try  
         {  
             Integer.parseInt(value.trim());  
             return true;  
          } catch(NumberFormatException nfe)  
          {  
              return false;  
          }  
    }
    
    public static boolean tryParseDouble(String value)  
    {  
         try  
         {  
        	 Double.parseDouble(value.trim());  
             return true;  
          } catch(NumberFormatException nfe)  
          {  
              return false;  
          }  
    }
    
    public static boolean tryParseLong(String value)  
    {  
         try  
         {  
        	 Long.parseLong(value.trim());  
             return true;  
          } catch(NumberFormatException nfe)  
          {  
              return false;  
          }  
    }
       
	static LocationRecorderResult httpGetImplementation(String geturl) 
	{
		LocationRecorderResult result = new LocationRecorderResult();
		try
		{
			HttpParams httpParameters = new BasicHttpParams();
			// Set the timeout in milliseconds until a connection is established.
			int timeoutConnection = 3000;
			HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
			// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			int timeoutSocket =3000;
			HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

			DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
			HttpGet httpGet= new HttpGet(geturl);		
			
			HttpResponse response = httpClient.execute(httpGet);			

			BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
			String line = "";
			while((line = rd.readLine())!=null)
			{
				result.setText(result.getText() + line);
			}
			result.setXml(result.getText());
		}
		catch (ClientProtocolException e)
		{
			result.setText(e.getMessage());
		}
		catch (IOException e)
		{
			result.setText(e.getMessage());
		}
		if (!result.getText().startsWith("<?xml version=\"1.0\"?>"))
		{
			result.setText(StaticMethods.Constants.INVALID_RESPONSE +"\n" + result.getText());
			result.setXml(StaticMethods.Constants.INVALID_RESPONSE +"\n" + result.getText());
			result.setResult(false);		
		}
		else
		{
			if (result.getText().contains(StaticMethods.Constants.FAULT_TAG))
			{
				result.setResult(false);
				DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
				try {
					InputStream in = new ByteArrayInputStream(result.getText().getBytes("UTF-8"));
					DocumentBuilder builder = domFactory.newDocumentBuilder();
			        Document dDoc = builder.parse(in);
			        XPath xPath = XPathFactory.newInstance().newXPath();
			        Node node1 = (Node) xPath.evaluate("/methodResponse/fault/value/struct/member[1]/name", dDoc, XPathConstants.NODE);	   
			        String value1 = node1.getTextContent();
			        
			        Node node2 = (Node) xPath.evaluate("/methodResponse/fault/value/struct/member[1]/value/int", dDoc, XPathConstants.NODE);
			        String value2 = node2.getTextContent();
			        
			        if (StaticMethods.tryParseInt(value2))
			        {
			        	result.setCode(Integer.parseInt(value2));
			        }
			        	
			        Node node3 = (Node) xPath.evaluate("/methodResponse/fault/value/struct/member[2]/name", dDoc, XPathConstants.NODE);
			        String value3 = node3.getTextContent();
			        	        
			        Node node4 = (Node) xPath.evaluate("/methodResponse/fault/value/struct/member[2]/value/string", dDoc, XPathConstants.NODE);
			        String value4 = node4.getTextContent();
	
			        result.setText(value1 + " " + value2 + " " + value3 + " " + value4);      
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else 
			{
				result.setResult(true);
			}
		}
		return result;
	}	

	public final class Constants
	{
	    public static final String LOGIN = "LoginGetMethod";
	    public static final String POST_COORDINATE = "PostCoordinate";
	    public static final String DEFAULT_WA_URL = "";//"http://web.cecs.pdx.edu/~jwater/index.php";
	    
	    public static final String SETTINGS_RELOAD = "Reloaded settings from disk";
	    public static final String INVALID_SESSION = "Invalid session, please login to start using the application";
	    public static final String INVALID_COORD = "Invalid coordindates, please enter valid coordinates and retry";
	    public static final String INVALID_ROUTE = "Invalid route, please create a new route";
	    
	    
	    public static final String VALID_SESSION = "Session Id: ";
	    
	    public static final String VALUE_NOT_FOUND = "VALUE_NOT_FOUND";
	    public static final String INVALID_SAMPLING_INTERVAL = "Invalid sampling interval\nThe Transmission Interval cannot be shorter than the Sampling Interval";
	    public static final String FAULT_CODE = "faultCode";
	    public static final String FAULT_TAG = "<fault>";

	    public static final String AUTH_FAILED = "Unable to authenticate with the entered login and password";
	    public static final String NEW_ROUTE_FAILED = "Unable to create new route, please check the application settings";
	    public static final String RENAME_ROUTE_FAILED = "Unable to rename route, please check the application settings";
	    public static final String LOGOUT = "Logged out";
	    public static final String NEW_ROUTE = "New Route:";
	    public static final String RENAME_ROUTE = "New Route Name:";


	    
	    public static final String TO_RESUME = "RESUMMING...";
	    public static final String TO_STOP = "STOPPING...";
	    public static final String TO_START = "STARTING...";    
	    
	    public static final String FAILURE = "FAILURE";
	    public static final String SUCCESS = "SUCCESS";
	    public static final String SENT = "Sent";
	    public static final String SAMPLED = "Sampled";
	    public static final String FAILED = "Failed";
	    public static final String INVALID_RESPONSE = "Invalid server response, format cannot be recognized:";
	    
	    public static final String PREFS_NAME = "LocationRecorderFile";
	}
	
	public static class InputFilterMinMax implements InputFilter {
		 
		private int min, max;
	 
		public InputFilterMinMax(int min, int max) {
			this.min = min;
			this.max = max;
		}
	 
		public InputFilterMinMax(String min, String max) {
			this.min = Integer.parseInt(min);
			this.max = Integer.parseInt(max);
		}
	 
		//@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {	
			try {
				int input = Integer.parseInt(dest.toString() + source.toString());
				if (isInRange(min, max, input))
					return null;
			} catch (NumberFormatException nfe) { }		
			return "";
		}
	 
		private boolean isInRange(int a, int b, int c) {
			return b > a ? c >= a && c <= b : c >= b && c <= a;
		}
	}
	
	public static void LongToast(Context appContext, String msg)
	{
		Toast toast = Toast.makeText(appContext, msg, Toast.LENGTH_LONG);
		toast.show();		
	}
	
	/// Commands
	public static String WSLogoutCommand(LocationRecorderSettings locRecSettings)
	{	//LOGOUT http://<server>/<apppath>/index.php?action=user_login&sessionid=764efa883dda1e11db47671c4a3bbd9e
		String session = locRecSettings.getSession();
    	String waUrl = locRecSettings.getWebAppURL();   	
    	String request = waUrl + "?action=user_logout&sessionid=" + session;
    	String response = StaticMethods.httpGetImplementation(request).getText();
		return response;
	}
	
	public static String WSStartRouteCommand(LocationRecorderSettings locRecSettings)
	{	//START A ROUTE http://<server>/<apppath>/index.php?action=new_route&sessionid=764efa883dda1e11db47671c4a3bbd9e
		String session = locRecSettings.getSession();
    	String waUrl = locRecSettings.getWebAppURL();   	
    	String request = waUrl + "?action=new_route&sessionid=" + session;
    	String response = StaticMethods.httpGetImplementation(request).getText();
		return response;
	}
	
	public static String WSStartListRoutesCommand(LocationRecorderSettings locRecSettings)
	{	//LIST ROUTES http://<server>/<apppath>/index.php?action=get_route_list&sessionid=764efa883dda1e11db47671c4a3bbd9e
		String session = locRecSettings.getSession();
    	String waUrl = locRecSettings.getWebAppURL();   	
    	String request = waUrl + "?action=get_route_list&sessionid=" + session;
    	String response = StaticMethods.httpGetImplementation(request).getText();
		return response;
	}
		
	public static String WSGetRouteCommand(LocationRecorderSettings locRecSettings)
	{	//GET ROUTE http://<server>/<apppath>/index.php?action=get_route&route_id=12345&sessionid=764efa883dda1e11db47671c4a3bbd9e
		String session = locRecSettings.getSession();
		String route =  locRecSettings.getRoute()+"";
    	String waUrl = locRecSettings.getWebAppURL();
	    String request = waUrl + "?action=get_route&route_id="+ route + "&sessionid=" + session;
    	String response = StaticMethods.httpGetImplementation(request).getText();
		return response;
	}
	
	/// Commands
		public static LocationRecorderResult HttpGetLoginCommand(LocationRecorderSettings locRecSettings, String username, String password, String waUrl)
		{	//LOGIN http://<server>/<apppath>/index.php?action=user_login&username=USERNAME&password=PASSWORD
	    	String request = waUrl + "?action=user_login&username=" + username + "&password="+ password;
	    	LocationRecorderResult r = StaticMethods.httpGetImplementation(request);
	    	r.setText(StaticMethods.GetAllXMLValues("|", r.getXml()).replace('|',' ').trim());
			return r;
		}
		
		public static LocationRecorderResult HttpGetCommand(LocationRecorderSettings locRecSettings)
		{	//LOGOUT http://<server>/<apppath>/index.php?action=user_login&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();   	
	    	String request = waUrl + "?action=user_logout&sessionid=" + session;
	    	String response = StaticMethods.httpGetImplementation(request).getText();
	    	LocationRecorderResult result = new LocationRecorderResult(false, 0, "", response);
			return result;
		}
		
		public static LocationRecorderResult HttpGetStartRouteCommand(LocationRecorderSettings locRecSettings)
		{	//START A ROUTE http://<server>/<apppath>/index.php?action=new_route&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();   	
	    	String request = waUrl + "?action=new_route&sessionid=" + session;
	    	String response = StaticMethods.httpGetImplementation(request).getText();
	    	LocationRecorderResult result = new LocationRecorderResult(false, 0, "", response);
			return result;
		}
		
		public static LocationRecorderResult HttpGetStartListRoutesCommand(LocationRecorderSettings locRecSettings)
		{	//LIST ROUTES http://<server>/<apppath>/index.php?action=get_route_list&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();   	
	    	String request = waUrl + "?action=get_route_list&sessionid=" + session;
	    	String response = StaticMethods.httpGetImplementation(request).getText();
	    	LocationRecorderResult result = new LocationRecorderResult(false, 0, "", response);
			return result;
		}
			
		public static LocationRecorderResult HttpGetPostCoordinateCommand(LocationRecorderSettings locRecSettings, String latitude, String longitude)
		{	//POST COORD http://<server>/<apppath>/index.php?action=post_coord&route_id=5&lat=40.446195&lon=-79.948862&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();
	    	String route = locRecSettings.getRoute()+"";
		    String request = waUrl + "?action=post_coord&route_id="+ route + "&lat="+ latitude +"&lon=" + longitude + "&sessionid=" + session;
	    	
		    String xmlResponse = StaticMethods.httpGetImplementation(request).getText();
	    	String stringResponse = StaticMethods.GetAllXMLValues("|", xmlResponse).replace('|',' ').trim();
	    	boolean boolResponse = StaticMethods.tryParseInt(stringResponse) 
	    			&& !xmlResponse.contains(StaticMethods.Constants.FAULT_CODE);
	    	int intResponse = 0;
	    	if (boolResponse)
	    	{	
	    		intResponse = Integer.parseInt(stringResponse);
	    	}
	    	
	    	LocationRecorderResult result = new LocationRecorderResult(boolResponse, intResponse, stringResponse, xmlResponse);
			return result;
		}
		
		public static LocationRecorderResult HttpGetGetRouteCommand(LocationRecorderSettings locRecSettings)
		{	//GET ROUTE http://<server>/<apppath>/index.php?action=get_route&route_id=12345&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
			String route =  locRecSettings.getRoute()+"";
	    	String waUrl = locRecSettings.getWebAppURL();
		    String request = waUrl + "?action=get_route&route_id="+ route + "&sessionid=" + session;
	    	String response = StaticMethods.httpGetImplementation(request).getText();
	    	LocationRecorderResult result = new LocationRecorderResult(false, 0, "", response);
			return result;
		}	
		
		public static LocationRecorderResult HttpGetGetNewRouteCommand(LocationRecorderSettings locRecSettings)
		{	//GET ROUTE http://<server>/<apppath>/index.php?action=get_route&route_id=12345&sessionid=764efa883dda1e11db47671c4a3bbd9e
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();
	    	String route_name = locRecSettings.getRouteName();
	    	String request = waUrl + "?action=new_route&sessionid=" + session + "&route_name=" + route_name;
	    	String xmlResponse = StaticMethods.httpGetImplementation(request).getText();
			String stringResponse = StaticMethods.GetAllXMLValues("|", xmlResponse).replace('|',' ').trim();
			boolean boolResponse = StaticMethods.tryParseInt(stringResponse) 
					&& !xmlResponse.contains(StaticMethods.Constants.FAULT_CODE);
	    	int intResponse = 0;
	    	if (boolResponse)
	    	{	
	    		intResponse = Integer.parseInt(stringResponse);
	    	}
	    	
			LocationRecorderResult result = new LocationRecorderResult(boolResponse, intResponse, stringResponse, xmlResponse);
			return result;
		}	
		
		public static LocationRecorderResult HttpRenameRouteCommand(LocationRecorderSettings locRecSettings, String newName)
		{	//GET ROUTE http://<server>/<apppath>/index.php?action=get_route&route_id=12345&sessionid=764efa883dda1e11db47671c4a3bbd9e
			//RENAME ROUTE http://<server>/<apppath>/index.php?action=rename_route&sessionid=764efa883dda1e11db47671c4a3bbd9e&route_id=12&new_route_name=MyNewName
			
			String session = locRecSettings.getSession();
	    	String waUrl = locRecSettings.getWebAppURL();
	    	String new_route_name = newName;
	    	String route_id = locRecSettings.getRoute()+"";
	    	
	    	String request = waUrl + "?action=rename_route&sessionid=" + session + "&route_id="+ route_id +"&new_route_name=" + new_route_name;
	    	String xmlResponse = StaticMethods.httpGetImplementation(request).getText();
			String stringResponse = StaticMethods.GetAllXMLValues("|", xmlResponse).replace('|',' ').trim();
			boolean boolResponse = !xmlResponse.contains(StaticMethods.Constants.FAULT_CODE);	    	
			LocationRecorderResult result = new LocationRecorderResult(boolResponse, 0, stringResponse, xmlResponse);
			return result;
		}
}

//protected static class TryPost extends AsyncTask<String, Void, String>
//{
//	@Override
//  protected void onPreExecute() 
//	{
//	///	resulttext.setText("POSTING...");
//	///	webview.loadData("loading...","text/html", "UTF-8");
//	}
//	@Override
//	protected String doInBackground(String... parms) 
//	{
//		return httpPostImplementation(parms);
//	}
//	@Override
//  protected void onPostExecute(String result) 
//	{
//	///	resulttext.setText(result);
//	///	webview.loadData(result, "text/html", "UTF-8");
//  }    	
//}
//private static String httpPostImplementation(String... parms) 
//{
//	String result = "";
//	try
//	{
//		HttpClient httpclient = new DefaultHttpClient();
//		HttpPost httppost= new HttpPost(parms[0]);
//		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
//		nameValuePairs.add(new BasicNameValuePair("cccc",parms[1]));
//		httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
//		HttpResponse response = httpclient.execute(httppost);
//		//int status =  response.getStatusLine().getStatusCode();
//		BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
//		String line = "";
//		while((line=rd.readLine())!=null)
//		{
//			result += line;
//		}
//	}
//	catch (ClientProtocolException e)
//	{
//		result = e.getMessage();
//	}
//	catch (IOException e)
//	{
//		result = e.getMessage();
//	}
//	return result;
//}
// public static String GetXMLValuez(String xmlPath, String xmlString) //throws XmlPullParserException, IOException
//{
//	String result = StaticMethods.Constants.VALUE_NOT_FOUND;
//	String relativePath = "", startTag = "", endTag = "", textValue = "";
//try{
//	
//	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
//	factory.setNamespaceAware(true);
//	XmlPullParser xpp = factory.newPullParser();
//	xpp.setInput(new StringReader (xmlString) );
//	int eventType = xpp.getEventType();
//	while (eventType != XmlPullParser.END_DOCUMENT) {
//		     if(eventType == XmlPullParser.START_DOCUMENT) {
//		   	  Log.v("Start document","");
//		     } else if(eventType == XmlPullParser.START_TAG) {
//		    	 startTag = xpp.getName(); 
//		    	 relativePath += startTag + ".";
//		    	 Log.v("Start tag ",startTag);
//		     } else if(eventType == XmlPullParser.END_TAG) {
//		    	 endTag =  xpp.getName();
//		    	 Log.v("End tag ",xpp.getName());
//		     } else if(eventType == XmlPullParser.TEXT) {
//		    	textValue = xpp.getText();
//		    	 if (relativePath.compareToIgnoreCase(xmlPath+".")==0)
//		    	 {
//		    		 result = textValue;
//			    	 break;
//		    	 }
//		    	 Log.v("Text ", textValue);
//		     }
//		     eventType = xpp.next();
//	    }
//	Log.v("End document","");
//}
//catch (Exception e)
//{
//	
//}
//	return result;
//}
//public static String WSPostCoordinateCommand(LocationRecorderSettings locRecSettings, String latitude, String longitude)
//{	//POST COORD http://<server>/<apppath>/index.php?action=post_coord&route_id=5&lat=40.446195&lon=-79.948862&sessionid=764efa883dda1e11db47671c4a3bbd9e
//	String session = locRecSettings.getSession();
//	String waUrl = locRecSettings.getWebAppURL();
//	String route = locRecSettings.getRoute()+"";
//    String request = waUrl + "?action=post_coord&route_id="+ route + "&lat="+ latitude +"&lon=" + longitude + "&sessionid=" + session;
//	String response = StaticMethods.httpGetImplementation(request);
//	return response;
//}
//public static String WSGetNewRouteCommand(LocationRecorderSettings locRecSettings)
//{	//GET ROUTE http://<server>/<apppath>/index.php?action=get_route&route_id=12345&sessionid=764efa883dda1e11db47671c4a3bbd9e
//	String session = locRecSettings.getSession();
//	String waUrl = locRecSettings.getWebAppURL();
//    String request = waUrl + "?action=new_route&sessionid=" + session;
//	String response = StaticMethods.httpGetImplementation(request);
//	return response;
//}
//public static String WSLoginCommand(LocationRecorderSettings locRecSettings, String username, String password, String waUrl)
//{	//LOGIN 
//	//http://<server>/<apppath>/index.php?action=user_login&username=USERNAME&password=PASSWORD
//	String request = waUrl + "?action=user_login&username=" + username + "&password="+ password;
//	String response = StaticMethods.httpGetImplementation(request);
//	return response;
//}

