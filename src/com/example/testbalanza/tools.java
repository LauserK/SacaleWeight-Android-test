package com.example.testbalanza;

import java.io.File;
import java.net.NetworkInterface;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;

public class tools {
	final protected static char[] hexArray = "0123456789abcdef".toCharArray();
	public static String bytesToHex(byte[] bytes, int l) 
	{
	    char[] hexChars = new char[l * 2];
	    for ( int j = 0; j < l; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}	
	
	public static byte[] hexStringToByteArray(String s) 
	{
		    byte[] b = new byte[s.length() / 2];
		    for (int i = 0; i < b.length; i++) 
		    {
		      int index = i * 2;
		      int v = Integer.parseInt(s.substring(index, index + 2), 16);
		      b[i] = (byte) v;
		    }
		    return b;
	}	
	
	public static String cash(Float price){
		NumberFormat numberFormat  = new DecimalFormat("#0.00");
		String str = numberFormat.format(price);
		int l=str.length();
		str=str.replace(",", ".");
		if(l>6) str=str.substring(0,l-6)+","+str.substring(l-6);
		if(l>9) str=str.substring(0,l-9)+","+str.substring(l-9);
		str=str.replace("-,", "-");
	    return str;
    }

    public static String unit(Float quantity) {
        DecimalFormat df = new DecimalFormat("#0");
        return df.format(quantity);
    }

    public static String decimals(float d, int i)
    {
        NumberFormat numberFormat=null;
        if(i==0) numberFormat = new DecimalFormat("#0");
        if(i==1) numberFormat = new DecimalFormat("#0.0");
        if(i==2) numberFormat = new DecimalFormat("#0.00");
        if(i==3) numberFormat = new DecimalFormat("#0.000");
        String str = numberFormat.format(d);
        str=str.replace(",", ".");
        return str;
    }

    public static String zero(String s,int t)
    {
        String cero="000000000000000000000000000000";
        String tmp="";
        if( s.length() < t )
            tmp=cero.substring( 0, t-s.length() )+s;
        else
            tmp=s.substring( 0, t );
        return tmp;
    }

    public static String sp(String s, int n)
    {
        String str=s;
        String b="                                                                                                                                                      ";
        if( s.length()<n ) str=s+b.substring(0, n-s.length());
        return str.substring(0,n);
    }
    
    public static Bitmap LoadImage(String s) 
    {
   	    File imgFile = new File( s );
    	if(!imgFile.exists()) return null;
    	try
    	{
    		Bitmap bmp = BitmapFactory.decodeFile( imgFile.getAbsolutePath() );
    		return bmp;
    	} catch(Exception e) { return null; }
    }  
    
    public static String getMacAddr(Activity act) {
    	if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
    		try {
                List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface nif : all) {
                    if (!nif.getName().equalsIgnoreCase("wlan0")) continue;

                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(Integer.toHexString(b & 0xFF) + ":");
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            } catch (Exception ex) {
                //handle exception
            }
        } else {
        	WifiManager manager = (WifiManager) act.getSystemService(Context.WIFI_SERVICE);
        	WifiInfo info = manager.getConnectionInfo();
        	String address = info.getMacAddress();
        	return address.toString();
        }    	
        
        return "";
    }
    
    public static void MessageBox(String title, String message, Activity activity){
    	AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message)
                .setTitle(title);

        builder.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            		                
            }
        });       

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if ( Environment.MEDIA_MOUNTED.equals( state ) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals( state ) ) {
            return true;
        }
        return false;
    }
}
