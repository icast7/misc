package omse555.LocationRecorder;

public class LocationRecorderResult 
{
    //Set default value
	private boolean result = false;
    private String text = "";
    private String xml = "";
    private int code = -1;
	   
    public LocationRecorderResult() 
    {
    }

    public LocationRecorderResult(boolean result, int code, String text, String xml) 
    {
    	this.result = result;
    	this.code = code;
    	this.text = text;
    	this.xml = xml;    			
    }
          
    public String getText() 
    {
    	return this.text;
    }
    public void setText(String text) 
    {        	
    	this.text = text;
    }
    
    public String getXml() 
    {
    	return this.xml;
    }
    public void setXml(String xml) 
    {        	
    	this.xml = xml;
    }
    
    public int getCode() 
    {
    	return this.code;
    }
    public void setCode(int code) 
    {        	
    	this.code = code;
    }
    
    public boolean getResult() 
    {
    	return this.result;
    }
    public void setResult(boolean result) 
    {        	
    	this.result = result;
    }
}
