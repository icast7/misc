package omse555.LocationRecorder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import omse555.LocationRecorder.ResourceChecker.Resource;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;

public class ActivityLocationRecorder extends Activity {
    /** Called when the activity is first created. */
	/** Constants **/
	private static final int REQUEST_CODE = 10;
	
	/*Global Controls*/
	private EnumApplicationStatus appTransmitStatus;
	private EnumApplicationStatus startButtonStatus;
	private LocationManager mgr = null;
	private LocationRecorderSettings locRecSettings;
    WorkerThreadTransmitting swtTransmitting = null;
	WorkerThreadSampling swtSampling = null;
    private ScheduledThreadPoolExecutor stpeT;
    private ScheduledThreadPoolExecutor stpeS;
    private ScheduledFuture<?> stpeTFuture = null;
    private ScheduledFuture<?> stpeSFuture = null;
    private Location oldLocation = null;
    private Location newLocation = null;
    
	/*Activities*/
	private Intent settingsIntent;
    
    /*Main Controls*/
    private Button startButton, stopButton;
    private EditText latValue, longValue, routeidValue, routenameValue;
    private RadioButton transmittingValue;
    private WebView webview;
    
    /* Menus */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.main_menu, menu);
        return true;
    }
    /* Menu Item*/
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	
        switch (item.getItemId()) {
        case R.id.logout_menu_item:
        	TryLogout taskLogout = new TryLogout();
        	taskLogout.execute(new String[]{});
	     	return true;
        case R.id.newroute_menu_item:
        	stopSampling();
        	stopTransmitting();
        	createNewRoute();
            appTransmitStatus = EnumApplicationStatus.Init;
            startButtonStatus = EnumApplicationStatus.Init;
            startButton.setText(EnumApplicationStatus.Start.name());
            stopButton.setEnabled(false); // Initially disabled       
	     	return true;
        case R.id.settings_menu_item:
        	stopSampling();
        	stopTransmitting();
        	TrySettings taskSettings = new TrySettings();
        	taskSettings.execute(new String[]{});
        	return true;
        case R.id.renameroute_menu_item:
        	stopSampling();
        	stopTransmitting();
            renameRoute();
            appTransmitStatus = EnumApplicationStatus.Init;
            startButtonStatus = EnumApplicationStatus.Init;
            startButton.setText(EnumApplicationStatus.Start.name());
            stopButton.setEnabled(false); // Initially disabled   
        default:
            return super.onOptionsItemSelected(item);
        }
    }    
      
    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        setContentView(R.layout.main);
        
        locRecSettings = new LocationRecorderSettings(this.getApplicationContext());
        settingsIntent = new Intent(this, ActivitySettings.class);
        
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
        mgr = (LocationManager)getSystemService(LOCATION_SERVICE);
        
        startButton = (Button)findViewById(R.id.startButton);
        stopButton = (Button)findViewById(R.id.stopButton);
        routeidValue = (EditText)findViewById(R.id.routeidValue); 
        routenameValue = (EditText)findViewById(R.id.routenameValue); 
        latValue = (EditText)findViewById(R.id.latValue);     
        longValue = (EditText)findViewById(R.id.longValue);
        webview = (WebView)findViewById(R.id.webkit);
        transmittingValue = (RadioButton)findViewById(R.id.transmittingValue);
                    
		startButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				TryStart task = new TryStart();
				task.execute(new String[]{});
			}
		});		
		stopButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				TryStop task = new TryStop();
				task.execute(new String[]{});
			}
		});
		transmittingValue.setOnClickListener(new OnClickListener()
		{
			public void onClick(View view)
			{
				transmittingValue.setChecked(appTransmitStatus == EnumApplicationStatus.Start);
			}
		});
		//If the settings are not initialized show the settings screen
		if (!locRecSettings.LoadSettingsFromFile())
		{
		 	TrySettings task = new TrySettings();		 	
	     	task.execute(new String[]{});
		}	
		/// Initialize GPS values ///
        setDefaultValues();
        setLocationManager();
        swtSampling = new WorkerThreadSampling("SWT");
        swtTransmitting = new WorkerThreadTransmitting("TWT");
    }
	@Override
	public void onResume()
	{		
		super.onResume();
		new ResourceChecker(this).pass(new ResourceChecker.Pass() {
		     @Override public void pass() 
		     {}
		  }).check(Resource.GPS, Resource.NETWORK, Resource.BLUETOOTH);
	}	
	@Override
	public void onPause() 
	{
		super.onPause();
	}	
	@Override
	public void onDestroy() 
	{
		mgr.removeUpdates(onLocationChange);
		super.onDestroy();
	}
	@Override 
	public void onActivityResult(int requestCode, int resultCode, Intent data) 
	{     
	  super.onActivityResult(requestCode, resultCode, data);
	  locRecSettings.LoadSettingsFromFile();
	  setDefaultValues();
	  setLocationManager();
	}
	
	/**
	 * Reinitializes controls to its default value
	 */
	private void setDefaultValues() {
		//Initialize values
        appTransmitStatus = EnumApplicationStatus.Init;
        startButtonStatus = EnumApplicationStatus.Init;
        startButton.setText(EnumApplicationStatus.Start.name());
        stopButton.setEnabled(false); // Initially disabled       

        latValue.setKeyListener(null); // Set to read-only
        longValue.setKeyListener(null); // Set to read-only
        routeidValue.setKeyListener(null); // Set to read-only

        oldLocation = null;
        newLocation = null;
        
        int route = locRecSettings.getRoute();
        if (route != 0)
        {
        	routeidValue.setText(route+"");
        	String routeName = locRecSettings.getRouteName();
            routenameValue.setText(routeName);
        }
        webview.loadData("((( LOADED )))", "text/html", "UTF-8");
	}   

//	/**
//	 * Sets the location provider(s) configuration (GPS, Wireless networks, etc.)
//	 */
	private void setLocationManager()
	{
		final Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setCostAllowed(true);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        final String bestProvider = mgr.getBestProvider(criteria, true);

        if (bestProvider != null && bestProvider.length() > 0)
        {
                mgr.requestLocationUpdates(bestProvider, 0, 0, onLocationChange);
        }
        else
        {
        	final List<String> providers = mgr.getProviders(true);

            for (final String provider : providers)
            {
            	mgr.requestLocationUpdates(provider, 0, 0, onLocationChange);
            }
        }
	}

	private LocationListener onLocationChange = new LocationListener() 
	{
		public void onLocationChanged(Location location)
		{
			// required for interface, not used
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
		}
	};
	
	public class WorkerThreadSampling implements Runnable
	{
	        private String  threadName = null;
	        public WorkerThreadSampling(String threadName)
	        {
	                this.threadName = threadName;
	        }
	        public void run()
	        {
				TrySampleLocation s = new TrySampleLocation();
				s.execute(new String[]{});
			}
	}
	
	public class WorkerThreadTransmitting implements Runnable
	{
	        private String  threadName = null;
	        public WorkerThreadTransmitting(String threadName)
	        {
	                this.threadName = threadName;
	        }
	        public void run()
	        {
	    		TrySendCoord t = new TrySendCoord();
				t.execute(new String[]{});
	        }
	}
	
	//Commands
    private class TryGetNewRoute extends AsyncTask<String, Void, LocationRecorderResult>
    {
    	@Override
	    protected void onPreExecute()
	    {}
	    @Override
	    protected LocationRecorderResult doInBackground(String... parms) 
	    {
	    	LocationRecorderResult response = StaticMethods.HttpGetGetNewRouteCommand(locRecSettings);
			if (response.getResult())
			{
				locRecSettings.setRoute(response.getCode());
				locRecSettings.SaveSettingsToFile();
			}
	    	return response;
	    }    	
		@Override
		protected void onPostExecute(LocationRecorderResult result)
		{	
			int route = locRecSettings.getRoute();
			if (route != 0)
			{	
	        	String newRouteName = locRecSettings.getRouteName();
				routenameValue.setText(newRouteName);
				routeidValue.setText(route+"");
				StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.NEW_ROUTE+ " "+ newRouteName +"("+ result.getText() +")");
			}
			else
			{
				StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.NEW_ROUTE_FAILED);
	        	locRecSettings.setRouteName("");
	        	locRecSettings.SaveSettingsToFile();
				routenameValue.setText("");
				routeidValue.setText("");
			}
		}
    }
    
    private class TryRenameRoute extends AsyncTask<String, Void, LocationRecorderResult>
    {
    	@Override
	    protected void onPreExecute()
	    {}
	    @Override
	    protected LocationRecorderResult doInBackground(String... parms) 
	    {
	    	String newName = parms[0];
	    	LocationRecorderResult response = StaticMethods.HttpRenameRouteCommand(locRecSettings, newName);
			if (response.getResult())
			{
				locRecSettings.setRouteName(response.getText());
				locRecSettings.SaveSettingsToFile();
			}
	    	return response;
	    }    	
		@Override
		protected void onPostExecute(LocationRecorderResult result)
		{	
        	String routeName = result.getText();
			routenameValue.setText(routeName);
			int routeId = locRecSettings.getRoute();
			if (result.getResult())
			{	
				StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.RENAME_ROUTE+ " "+ routeName +"("+ routeId +")");
			}
			else
			{
				StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.RENAME_ROUTE_FAILED);
			}
		}
    }

    private class TryLogout extends AsyncTask<String, Void, String>
    {
	    @Override
	    protected String doInBackground(String... parms) 
	    {
	    	String response = StaticMethods.WSLogoutCommand(locRecSettings);    	
	    	return response;	    
	    }    	
		@Override
		protected void onPostExecute(String result)
		{
			
			locRecSettings.ClearFile();
			StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.LOGOUT);
			webview.loadData(result, "text/html", "UTF-8");
			settingsIntent = new Intent(getBaseContext(), ActivitySettings.class);
			startActivityForResult(settingsIntent, REQUEST_CODE);
		}
    }

    private class TrySampleLocation extends AsyncTask<String, Void, Location>
    {	
	    @Override
	    protected Location doInBackground(String... parms) 
	    {
		    if (appTransmitStatus == EnumApplicationStatus.Stop)
			{
		    	return null;
			}
		    
		    // Get current location
		    List<String> providerList =  mgr.getAllProviders();
			Location lastLocation =  null;
			//Try getting the Location from the providers
			for (String provider : providerList)
			{
				lastLocation = mgr.getLastKnownLocation(provider);
				if (lastLocation != null)
						break;
			}
			//Create one manually
			if (lastLocation == null)
			{
				lastLocation = new Location(providerList.get(0));
			}
			
		    //If transmitting location deltas keep last and second to last Location
		    if (locRecSettings.getTransUnits() == EnumTransmissionUnits.meters)
		    {
			    newLocation = lastLocation;
		    }
	    	return lastLocation;
	    }    	
		@Override
		protected void onPostExecute(Location result)
		{	
		    if (appTransmitStatus == EnumApplicationStatus.Stop)
			{
		    	return;
			}

		    latValue.setText(result.getLatitude()+"");
			longValue.setText(result.getLongitude()+"");
			
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = new Date();			
			System.out.println(StaticMethods.Constants.SAMPLED + "@" + dateFormat.format(date));
			webview.loadData(StaticMethods.Constants.SAMPLED + "@" + dateFormat.format(date), "text/html", "UTF-8");
			
			
        	if (locRecSettings.getTransUnits() == EnumTransmissionUnits.meters)
        	{
        		float distance = -1;
        		if (newLocation != null)
	        	{
        			distance = locRecSettings.getTransmissionInterval();
        			
        			if (oldLocation != null)
        			{
        				distance = oldLocation.distanceTo(newLocation);
        			}        			
	        	}
        		if (distance >= locRecSettings.getTransmissionInterval())
        		{
    			    oldLocation = newLocation;
    	    		TrySendCoord t = new TrySendCoord();
    				t.execute(new String[]{});
        		}	        		
        	}		
		}
    }
    
    private class TrySendCoord extends AsyncTask<String, Void, LocationRecorderResult>
    {	
	    @Override
	    protected void onPreExecute()
	    {}
	    @Override
	    protected LocationRecorderResult doInBackground(String... parms) 
	    {
		    if (appTransmitStatus == EnumApplicationStatus.Stop)
			{
		    	return null;
			}
	    	String session = locRecSettings.getSession();
	    	if ((session == "")||(session == null))
	    	{
	    		TrySettings taskSettings = new TrySettings();
	    	    taskSettings.execute(new String[]{});    		
	    	}
	    	String route =  routeidValue.getText().toString();
	    	if (route.isEmpty())
	    	{
	    		int routeNumber = locRecSettings.getRoute();
	    		if (routeNumber == 0)
		    	{
	    			createNewRoute();
		    	}
	    	}    	
	    	LocationRecorderResult response = new LocationRecorderResult();
		    String latitude = latValue.getText().toString();
		    String longitude = longValue.getText().toString();
		    if (latitude.isEmpty() || longitude.isEmpty())
		    {
		    	response.setText(StaticMethods.Constants.INVALID_COORD);
		    	return response;
		    }
		    //If it is transmitting
		    response = StaticMethods.HttpGetPostCoordinateCommand(locRecSettings, latitude, longitude);
		    return response;
	    }    	
		@Override
		protected void onPostExecute(LocationRecorderResult result)
		{
			if (appTransmitStatus == EnumApplicationStatus.Stop)
			{
				return;
			}
			String route = locRecSettings.getRoute()+"";
			routeidValue.setText(route);

			//If it is transmitting
			if (result.getText() == StaticMethods.Constants.INVALID_COORD)
	    	{
	    		StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.INVALID_COORD);
	    	}
	    	else
	    	{
	    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	    		Date date = new Date();
	    		if (result.getResult())
	    		{
	    			System.out.println(StaticMethods.Constants.SENT + "@" + dateFormat.format(date));
	    			webview.loadData(StaticMethods.Constants.SENT + "@" + dateFormat.format(date), "text/html", "UTF-8");
	    		}
	    		else
	    		{
	    			webview.loadData(result.getText() + "@" + dateFormat.format(date), "text/html", "UTF-8");
	    		}
	    	}
		}
    }
       
    private class TrySettings extends AsyncTask<String, Void, String>
    {
	    @Override
	    protected void onPreExecute()
	    {
				webview.loadData("loading...","text/html", "UTF-8");
	    }
	    @Override
	    protected String doInBackground(String... parms) 
	    {
	    	settingsIntent = new Intent(getBaseContext(), ActivitySettings.class);
			startActivityForResult(settingsIntent, REQUEST_CODE);
	    	return "";	    
	    }    	
		@Override
		protected void onPostExecute(String result)
		{
			webview.loadData(result, "text/html", "UTF-8");
		}
    }
    
    private class TryStart extends AsyncTask<String, Void, String>
    {
	    @Override
	    protected void onPreExecute()
	    {
	    	//Invalid route
	    	String route =  routeidValue.getText().toString();
	    	if (route.isEmpty())
	    	{
	    		int routeNumber = locRecSettings.getRoute();
	    		if (routeNumber == 0)
		    	{
		    		StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.INVALID_ROUTE);
	    			appTransmitStatus = EnumApplicationStatus.Stop;
	    			this.cancel(true);
		    		return;
		    	}
	    	}	
	    	
	    }
	    @Override
	    protected String doInBackground(String... parms) 
	    {
	    	//Invalid session
	    	if (locRecSettings.getSession()=="")
			{
				StaticMethods.LongToast(getApplicationContext(), StaticMethods.Constants.INVALID_SESSION);
		     	TrySettings taskSettings = new TrySettings();
		     	taskSettings.execute(new String[]{});	    		     	
			}
	    	
	    	switch (startButtonStatus) 
	    	{
	    		case Init: 
	    		{
	    	        stpeT = new ScheduledThreadPoolExecutor(1);
	    	        stpeS = new ScheduledThreadPoolExecutor(1);
	    			appTransmitStatus = EnumApplicationStatus.Start;
					startButtonStatus = EnumApplicationStatus.Pause;
	    		}
	    		break;
	    		
	    		case Pause:
	    		{ 
	    			appTransmitStatus = EnumApplicationStatus.Stop;
	    			startButtonStatus = EnumApplicationStatus.Resume;
	    			if (locRecSettings.getTransUnits() ==  EnumTransmissionUnits.seconds)
					{
	    				stpeTFuture.cancel(true);
					}
	    			stpeSFuture.cancel(true);	    		
	    		}	    		
	    		break;
	    		
	    		case Resume:
	    		case Start:
	    		{
	    	        stpeT = new ScheduledThreadPoolExecutor(1);
	    	        stpeS = new ScheduledThreadPoolExecutor(1);
	    	        appTransmitStatus = EnumApplicationStatus.Start;
	    			startButtonStatus = EnumApplicationStatus.Pause;
	    		}
	    		break;
	    		
	    		default: startButtonStatus = EnumApplicationStatus.Start;
	    		break;
	    	}
	    	return startButtonStatus.name();
	    }    	
		@Override
		protected void onPostExecute(String result)
		{
			startButton.setText(result);
			stopButton.setEnabled(true);
			
			if (appTransmitStatus == EnumApplicationStatus.Start)
			{
				//Set the samplingTimer
				int gpsSamplingInterval = locRecSettings.getSamplingInterval();
				long secGpsSamplingInterval = gpsSamplingInterval;
				stpeSFuture = stpeS.scheduleWithFixedDelay(swtSampling, 0, secGpsSamplingInterval, TimeUnit.SECONDS);

				//Set the transmittingTimer
				if (locRecSettings.getTransUnits() ==  EnumTransmissionUnits.seconds)
				{
					int gpsTransmittingInterval = locRecSettings.getTransmissionInterval();
					long secGpsTransmittingInterval = gpsTransmittingInterval;				
					// Wait for at least one sampling interval + 1 second to start transmitting to make sure valid coordinates are available
					long initialDelay = secGpsSamplingInterval/2;
					stpeTFuture = stpeT.scheduleWithFixedDelay(swtTransmitting, initialDelay , secGpsTransmittingInterval, TimeUnit.SECONDS);
				}				
				transmittingValue.setChecked(true);
			}
			else 
			{
				transmittingValue.setChecked(false);
			}
		}
    }    
    
    private class TryStop extends AsyncTask<String, Void, String>
    {
	    @Override
	    protected void onPreExecute()
	    {}    	
	    @Override
	    protected String doInBackground(String... parms) 
	    {
	    	startButtonStatus = EnumApplicationStatus.Start;
	    	appTransmitStatus = EnumApplicationStatus.Stop;
	    	return startButtonStatus.name();	    
	    }    	
		@Override
		protected void onPostExecute(String result)
		{			
			stopSampling();
			if (locRecSettings.getTransUnits() ==  EnumTransmissionUnits.seconds)
			{
				stopTransmitting();	
			}
			startButton.setText(result);
			stopButton.setEnabled(false);
		    transmittingValue.setChecked(appTransmitStatus == EnumApplicationStatus.Start);
		}

    }

    private void stopTransmitting() {
    	if (stpeT != null)
	{
		    //Cancel scheduled but not started task, and avoid new ones
    		try {    		
    			//This try/catch handles empty futures
    			stpeTFuture.cancel(true);
			} catch (Exception e) {
				e.printStackTrace();
			}
    		stpeT.remove(swtTransmitting);
			stpeT.purge();
		    stpeT.shutdown();
		    //Wait for the running tasks 
		    try {
				stpeT.awaitTermination(1, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    //Interrupt the threads and shutdown the scheduler
		    stpeT.shutdownNow();
		}
	}
    		
    private void stopSampling() {
	if  (stpeS != null)
	{
		try {
			//This try/catch handles empty futures
		stpeSFuture.cancel(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		stpeS.remove(swtSampling);
		stpeS.purge();		
		stpeS.shutdown();
		try {
			stpeS.awaitTermination(1, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		stpeS.shutdownNow();
		}
	}
   //End Commands
	/**
	 * 
	 */
	private void createNewRoute() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("New Route");
		alert.setMessage("Enter new route name:");
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setFocusable(true);
		input.requestFocus();
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  // Do something with value!
			String newRouteName = input.getText().toString();
			locRecSettings.setRouteName(newRouteName);
			locRecSettings.SaveSettingsToFile();
			
        	TryGetNewRoute taskGetNewRoute = new TryGetNewRoute();
        	taskGetNewRoute.execute(new String[]{});        	
		}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		alert.show();
	}
	
	private void renameRoute() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Rename Route");
		alert.setMessage("Enter new route name:");
		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		input.setFocusable(true);
		input.requestFocus();
		alert.setView(input);

		alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  // Do something with value!
			String newName = input.getText().toString();
        	TryRenameRoute taskRenameRoute = new TryRenameRoute();
        	taskRenameRoute.execute(new String[]{newName});
		}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		alert.show();
	}

}