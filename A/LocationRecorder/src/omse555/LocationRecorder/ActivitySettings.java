package omse555.LocationRecorder;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.InputFilter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class ActivitySettings extends Activity{

	/*Settings Variables*/
	private LocationRecorderSettings locRecSettings;
    private String sessionID;

	/*Settings Controls*/
    private Button saveButton, cancelButton;
    private EditText loginEditText, passwordEditText, urlEditText, intervalEditText, transEditText;
    private Spinner transUnitsSpinner;
    
    @Override
    public void onCreate(Bundle icicle) 
    {
        super.onCreate(icicle);
        setContentView(R.layout.settings);       
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        cancelButton = (Button)findViewById(R.id.cancelButton);
        saveButton = (Button)findViewById(R.id.saveButton);      
        
        loginEditText = (EditText)findViewById(R.id.loginValue);
        passwordEditText = (EditText)findViewById(R.id.passwordValue);
        urlEditText = (EditText)findViewById(R.id.urlValue);
        intervalEditText = (EditText)findViewById(R.id.intervalValue);
        transEditText = (EditText)findViewById(R.id.transValue);        
        transUnitsSpinner = (Spinner)findViewById(R.id.unitsSpinner);    

        locRecSettings = new LocationRecorderSettings(this.getApplicationContext());
		//settingsFile =  this.getSharedPreferences(StaticMethods.Constants.PREFS_NAME, MODE_WORLD_WRITEABLE);
        loadSettingsFile();
    	    	
        cancelButton.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View view)
            {
            	TryCancel task = new TryCancel();
            	task.execute(new String[]{});
            }  
        });
        saveButton.setOnClickListener(new OnClickListener() 
        {
            public void onClick(View view)
            {
            	TrySave task = new TrySave();
            	task.execute(new String[]{});
            }  
        });
    }
    
    // Private Methods
	private void loadSettingsFile() {
		locRecSettings.LoadSettingsFromFile();
		this.sessionID = locRecSettings.getSession();
    	loginEditText.setText(locRecSettings.getUsername());
    	passwordEditText.setText(locRecSettings.getPassword());
    	intervalEditText.setText(String.valueOf(locRecSettings.getSamplingInterval()));
    	intervalEditText.setFilters(new InputFilter[] {new StaticMethods.InputFilterMinMax("0","3600")} );
    	
    	transEditText.setText(String.valueOf(locRecSettings.getTransmissionInterval()));
    	
    	//Set WebApplication value
    	String waUrl = locRecSettings.getWebAppURL().toString();
    	if (waUrl == "")
    		waUrl = StaticMethods.Constants.DEFAULT_WA_URL;
    	urlEditText.setText(waUrl);
        	
    	// Set Spinner value
    	String transUnits = locRecSettings.getTransUnits().name();
    	String[] unitsArray = getResources().getStringArray(R.array.inter_units);
    	List<String> list = Arrays.asList(unitsArray);
    	int selection = list.indexOf(transUnits);
    	transUnitsSpinner.setSelection(selection);
	}
	
	private void saveSettingsFile() {
		locRecSettings.setUsername(loginEditText.getText().toString());
		locRecSettings.setPassword(passwordEditText.getText().toString());	
		locRecSettings.setWebAppURL(urlEditText.getText().toString());
		locRecSettings.setSamplingInterval(Integer.parseInt(intervalEditText.getText().toString()));
		
		String units = transUnitsSpinner.getSelectedItem().toString();
		locRecSettings.setTransUnits(EnumTransmissionUnits.valueOf(units));
		locRecSettings.setTransmissionInterval(Integer.parseInt(transEditText.getText().toString()));
    	    	
    	locRecSettings.SaveSettingsToFile();
	}
	
    //Finish Activity
    @Override
	public void finish() {
		Intent data = new Intent();
		// Return session id value
		data.putExtra("SessionID",this.sessionID);
		setResult(RESULT_OK, data);
		super.finish();
	}

    private class TrySave extends AsyncTask<String, Void, LocationRecorderResult>
    {    
    	@Override
	    protected void onPreExecute()
    	{
	    }
	    @Override
	    protected LocationRecorderResult doInBackground(String... url) 
	    {
	    	String selectedUnits = transUnitsSpinner.getSelectedItem().toString();    	
    		if (selectedUnits.equalsIgnoreCase(EnumTransmissionUnits.seconds.name()))
    		{
    			String samplingInterval =  intervalEditText.getText().toString();
    			String transmitInterval = transEditText.getText().toString();
    			if (StaticMethods.tryParseInt(samplingInterval) && StaticMethods.tryParseInt(transmitInterval))
    			{
    				int numSamplingInterval = Integer.parseInt(samplingInterval);
    				int numTransmitInterval = Integer.parseInt(transmitInterval);
    				if (numTransmitInterval < numSamplingInterval)
    				{
    					LocationRecorderResult result = new LocationRecorderResult();
    					result.setResult(false);
    					result.setText(StaticMethods.Constants.INVALID_SAMPLING_INTERVAL);
    					return result;
    				}
    			}
    		}	    	
	    	String username = loginEditText.getText().toString();
	    	String password = passwordEditText.getText().toString();
	    	String waUrl = urlEditText.getText().toString();    	
	    	return StaticMethods.HttpGetLoginCommand(locRecSettings, username, password, waUrl);
	    }	    	
		@Override
		protected void onPostExecute(LocationRecorderResult result)
		{	
			if (result.getResult())
			{
				locRecSettings.setSession(result.getText());
				saveSettingsFile();	
				String msg = "Login succesful\n\n" + StaticMethods.Constants.VALID_SESSION + result.getText();
				StaticMethods.LongToast(getApplicationContext(), msg);
				finish();
			}			
			else
			{
				if (result.getCode() == -2)
				{
					result.setText(StaticMethods.Constants.AUTH_FAILED + "\n" + result.getText());
				}
				StaticMethods.LongToast(getApplicationContext(), result.getText());
			}		
		}
    }
    
    private class TryCancel extends AsyncTask<String, Void, String>
    {  	
	    @Override
	    protected String doInBackground(String... url) 
	    {   
	    	return StaticMethods.Constants.SETTINGS_RELOAD;
	    }	    	
		@Override
		protected void onPostExecute(String result)
		{
			String msg;
	    	loadSettingsFile();
	    	if  (locRecSettings.getSession().isEmpty())
	    	{
	    		msg = result + "\n\n" + StaticMethods.Constants.INVALID_SESSION;
	    		StaticMethods.LongToast(getApplicationContext(), msg);
	    	}
	    	else 
	    	{
	    		msg = result + "\n\n" + StaticMethods.Constants.VALID_SESSION +locRecSettings.getSession() ;
	    		StaticMethods.LongToast(getApplicationContext(), msg);
	        	finish();	    		
	    	}
		}
    }
}