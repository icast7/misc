package omse555.LocationRecorder;

import android.content.Context;
import android.content.SharedPreferences;

public class LocationRecorderSettings
{
    //Set default value
	private String webAppURL;
    private String username;
    private String password;
    private String session;
	private String route_name;
    private int route;
    private int samplingInterval;
    private int transmissionInterval;
    private EnumTransmissionUnits tranUnits;
	
    SharedPreferences settingsFile;
       
    public LocationRecorderSettings(Context context) 
    {
    	settingsFile = context.getSharedPreferences(StaticMethods.Constants.PREFS_NAME, 0);
    }

    public boolean LoadSettingsFromFile()
    {
    	this.route_name = settingsFile.getString("RouteName","");
    	this.route = settingsFile.getInt("Route", 0);
    	this.session =  settingsFile.getString("Session","");
    	this.webAppURL =  settingsFile.getString("WebAppURL","http://web.cecs.pdx.edu/~jwater/");
    	this.username = settingsFile.getString("Username", "");
    	this.password = settingsFile.getString("Password", "");
    	this.samplingInterval = settingsFile.getInt("SamplingInterval", 10);
    	this.transmissionInterval = settingsFile.getInt("TransmissionInterval", 10);
    	this.tranUnits = EnumTransmissionUnits.valueOf(settingsFile.getString("TransUnits", EnumTransmissionUnits.seconds.name()));       			
    	
    	return ((!this.webAppURL.isEmpty())&&(!this.username.isEmpty())&&
    			(!this.password.isEmpty())&&(this.samplingInterval>0)&&(this.transmissionInterval>0));
    }
    
    public boolean SaveSettingsToFile()
    {
    	SharedPreferences.Editor editor = settingsFile.edit();
    	editor.putString("RouteName", this.route_name);
    	editor.putInt("Route", this.route);
    	editor.putString("Session", this.session);
    	editor.putString("WebAppURL", this.webAppURL);
        editor.putString("Username", this.username);
        editor.putString("Password", this.password);
        editor.putInt("SamplingInterval", this.samplingInterval);
        editor.putInt("TransmissionInterval", this.transmissionInterval);
        editor.putString("TransUnits", this.tranUnits.name());
        boolean result = editor.commit();
        return result;
    }
    
    public boolean ClearFile()
    {
    	SharedPreferences.Editor editor = settingsFile.edit();
    	editor.putString("RouteName","");
    	editor.putInt("Route", 0);
    	editor.putString("Session", "");
    	editor.putString("WebAppURL", this.webAppURL);
        editor.putString("Username", "");
        editor.putString("Password", "");
        editor.putInt("SamplingInterval", this.samplingInterval);
        editor.putInt("TransmissionInterval", this.transmissionInterval);
        editor.putString("TransUnits", this.tranUnits.name());
        boolean result = editor.commit();
        return result;
    }
    
    public String getSession() 
    {
    	return this.session;
    }
    public void setSession(String newValue) 
    {        	
    	this.session = newValue;
    }
    
    public int getRoute() 
    {
    	return this.route;
    }
    public void setRoute(int newValue) 
    {        	
    	this.route = newValue;
    }
    
    public String getWebAppURL() 
    {
    	return this.webAppURL;
    }
    public void setWebAppURL(String newValue) 
    {        	
    	this.webAppURL = newValue;
    }
    
    public String getUsername() 
    {
    	return this.username;
    }
    public void setUsername(String newValue) 
    {        	
    	this.username = newValue;
    }

    public String getPassword() 
    {
    	return this.password;
    }               
    public void setPassword(String newValue) 
    {        	
    	this.password = newValue;
    }

    public int getSamplingInterval() 
    {
    	return this.samplingInterval;
    }               
    public void setSamplingInterval(int newValue) 
    {        	
    	this.samplingInterval = newValue;
    }
    
    public int getTransmissionInterval() 
    {
    	return this.transmissionInterval;
    }               
    public void setTransmissionInterval(int newValue) 
    {        	
    	this.transmissionInterval = newValue;
    }
    
    public EnumTransmissionUnits getTransUnits() 
    {
    	return this.tranUnits;
    }               
    public void setTransUnits(EnumTransmissionUnits newValue) 
    {        	
    	this.tranUnits = newValue;
    }

    public void setRouteName(String newValue) {
    	this.route_name = newValue;
    }
	public String getRouteName() {
		return this.route_name;
	}
 }