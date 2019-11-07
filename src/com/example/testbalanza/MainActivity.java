package com.example.testbalanza;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import android.support.v7.app.ActionBarActivity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;


enum PRINTER_PROTOCOL {
	CH34, PROLIFIC
}

public class MainActivity extends ActionBarActivity {

	private static String ACTION_USB_PERMISSION = "com.example.testbalanza.USB_PERMISSION";
	private static UsbManager managerScale = null;
	private static UsbManager managerPrinter  = null;
	private static UsbDevice deviceFoundScale = null;	
	private static UsbDevice deviceFoundPrinter= null;	
	private static UsbDeviceConnection writeConnectionScale = null;
	private static UsbDeviceConnection writeConnectionPrinter = null;
	private static PendingIntent mPermissionIntent = null;
	private static UsbInterface writeIntfScale = null;
	private static UsbInterface writeIntfPrinter = null;
	private static UsbInterface usbInterfaceFound = null;
	private static UsbEndpoint writeEpScale = null;
	private static UsbEndpoint writeIpScale = null;
	private static UsbEndpoint writeEpPrinter = null;
	private static UsbEndpoint writeIpPrinter = null;
	private static IntentFilter filterScale = null;
	private static IntentFilter filterPrinter = null;
	private static char _secuencia = 0;
		
	private static int targetVendorIDScale = 1659;
	private static int targetProductIDScale = 8963;
	private static int targetVendorIDPrinter = 2655;
	private static int targetProductIDPrinter = 10;
	
	private static PRINTER_PROTOCOL printerProtocol = PRINTER_PROTOCOL.PROLIFIC;
	
	TextView hola;
	Button buttonHola;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if ( tools.isExternalStorageWritable() ) {

            File appDirectory = new File( Environment.getExternalStorageDirectory() + "/TEStt" );
            File logDirectory = new File( appDirectory + "/log" );
            File logFile = new File( logDirectory, "logcat" + ".txt" );

            // create app folder
            if ( !appDirectory.exists() ) {
                appDirectory.mkdir();
            }

            // create log folder
            if ( !logDirectory.exists() ) {
                logDirectory.mkdir();
            }

            // clear the previous logcat and then write the new one to the file
            try {
                Process process = Runtime.getRuntime().exec("logcat -c");
                process = Runtime.getRuntime().exec("logcat -f " + logFile + "*:S MainActivity MainActivity:D");
            } catch ( IOException e ) {
                e.printStackTrace();
            }
        } 
        
        setContentView(R.layout.activity_main);        
        hola = (TextView) findViewById(R.id.hola);
        buttonHola = (Button) findViewById(R.id.buttonHola);
                
        buttonHola.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View arg0) {
				hola.setText(enviarRecibir("W"));				
			}
		});
        
        selectorUSB();
        abrirUSB();
        
        
        Thread t1 = new Thread() {
			@Override
			public void run() {
				try {
					while (!isInterrupted()) {
						Thread.sleep(500);
						final String peso = enviarRecibir("W");
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								hola.setText(peso);
							}
						});
					}
				} catch (InterruptedException e) {
				}
			}
		};
		t1.start();
    }

	private void abrirUSB() {		
		if (writeConnectionScale == null) {
			if (printerProtocol == PRINTER_PROTOCOL.CH34){
				try {
					writeConnectionScale = managerScale.openDevice(deviceFoundScale);
					writeConnectionScale.claimInterface(writeIntfScale, true);

					final byte[] defaultSetLine = new byte[] { (byte) 0x80, // [0:3] Baud rate (reverse hex encoding
																			// 9600:00 00 25 80 -> 80 25 00 00)
							(byte) 0x25, (byte) 0x00, (byte) 0x00, (byte) 0x00, // [4] Stop Bits (0=1, 1=1.5, 2=2)
							(byte) 0x02, // [5] Parity (0=NONE 1=ODD 2=EVEN 3=MARK 4=SPACE)
							(byte) 0x08 // [6] Data Bits (5=5, 6=6, 7=7, 8=8)
					};

					final byte[] defaultSetLine2 = new byte[] { (byte) 0x80, // [0:3] Baud rate (reverse hex encoding
																				// 9600:00 00 25 80 -> 80 25 00 00)
							(byte) 0x25, (byte) 0x00, (byte) 0x00, (byte) 0x00, // [4] Stop Bits (0=1, 1=1.5, 2=2)
							(byte) 0x00, // [5] Parity (0=NONE 1=ODD 2=EVEN 3=MARK 4=SPACE)
							(byte) 0x08 // [6] Data Bits (5=5, 6=6, 7=7, 8=8)
					};

					final int PL2303_REQTYPE_HOST2DEVICE_VENDOR = 0x40;
					final int PL2303_REQTYPE_DEVICE2HOST_VENDOR = 0xC0;
					final int PL2303_REQTYPE_HOST2DEVICE = 0x21;

					final int PL2303_VENDOR_WRITE_REQUEST = 0x01;
					final int PL2303_SET_LINE_CODING = 0x20;
					final int PL2303_SET_CONTROL_REQUEST = 0x22;

					byte[] buf = new byte[1];
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0404, 0, null, 0, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0404, 1, null, 0, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf, 1, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0000, 1, null, 0, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0001, 0, null, 0, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0002, 0x0044, null, 0, 0);
					// End of specific vendor stuff
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_CONTROL_REQUEST,
							0x0003, 0, null, 0, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0,
							defaultSetLine2, 7, 0);
					writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
							PL2303_VENDOR_WRITE_REQUEST, 0x0505, 0x1311, null, 0, 0);
				} catch (Exception e) {
				}
			}
		}
		
		if (printerProtocol == PRINTER_PROTOCOL.PROLIFIC){
			try {
				writeConnectionScale = managerScale.openDevice(deviceFoundScale);
				writeConnectionScale.claimInterface(writeIntfScale, true);

				final byte[] defaultSetLine = new byte[] { (byte) 0x80, // [0:3] Baud rate (reverse hex encoding
																		// 9600:00 00 25 80 -> 80 25 00 00)
						(byte) 0x25, (byte) 0x00, (byte) 0x00, (byte) 0x00, // [4] Stop Bits (0=1, 1=1.5, 2=2)
						(byte) 0x02, // [5] Parity (0=NONE 1=ODD 2=EVEN 3=MARK 4=SPACE)
						(byte) 0x08 // [6] Data Bits (5=5, 6=6, 7=7, 8=8)
				};

				final byte[] defaultSetLine2 = new byte[] { (byte) 0x80, // [0:3] Baud rate (reverse hex encoding
																			// 9600:00 00 25 80 -> 80 25 00 00)
						(byte) 0x25, (byte) 0x00, (byte) 0x00, (byte) 0x00, // [4] Stop Bits (0=1, 1=1.5, 2=2)
						(byte) 0x00, // [5] Parity (0=NONE 1=ODD 2=EVEN 3=MARK 4=SPACE)
						(byte) 0x08 // [6] Data Bits (5=5, 6=6, 7=7, 8=8)
				};

				final int PL2303_REQTYPE_HOST2DEVICE_VENDOR = 0x40;
				final int PL2303_REQTYPE_DEVICE2HOST_VENDOR = 0xC0;
				final int PL2303_REQTYPE_HOST2DEVICE = 0x21;

				final int PL2303_VENDOR_WRITE_REQUEST = 0x01;
				final int PL2303_SET_LINE_CODING = 0x20;
				final int PL2303_SET_CONTROL_REQUEST = 0x22;

				byte[] buf = new byte[1];
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0404, 0, null, 0, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0404, 1, null, 0, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8484, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_DEVICE2HOST_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x8383, 0, buf, 1, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0000, 1, null, 0, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0001, 0, null, 0, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0002, 0x0044, null, 0, 0);
				// End of specific vendor stuff
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_CONTROL_REQUEST,
						0x0003, 0, null, 0, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE, PL2303_SET_LINE_CODING, 0x0000, 0,
						defaultSetLine2, 7, 0);
				writeConnectionScale.controlTransfer(PL2303_REQTYPE_HOST2DEVICE_VENDOR,
						PL2303_VENDOR_WRITE_REQUEST, 0x0505, 0x1311, null, 0, 0);
			} catch (Exception e) {
			}		
		}
	}

	private void selectorUSB() {
		// TODO Auto-generated method stub
		managerScale = (UsbManager) getSystemService(Context.USB_SERVICE);
		HashMap<String, UsbDevice> deviceList = managerScale.getDeviceList();
		Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
		
		while (deviceIterator.hasNext()) 
		{
			UsbDevice device = deviceIterator.next();
			
			if(device.getVendorId()==targetVendorIDScale)
			{
				if(device.getProductId()==targetProductIDScale)
				{
					deviceFoundScale = device;
					
					try
					{
						writeConnectionScale = managerScale.openDevice( deviceFoundScale );
					}
					catch(Exception e){}
					
					if (writeConnectionScale == null) 
					{
						managerScale.requestPermission(deviceFoundScale, mPermissionIntent);	
						
						writeIntfScale = deviceFoundScale.getInterface(0);
				        for (int i = 0; i < writeIntfScale.getEndpointCount(); i++) 
				        {
				            if (writeIntfScale.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) 
				            {
				                if (writeIntfScale.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
				                	writeIpScale = writeIntfScale.getEndpoint(i);
				                else
				                	writeEpScale = writeIntfScale.getEndpoint(i);
				            } 				            
				        }
					}					
					else
					{
						writeIntfScale = deviceFoundScale.getInterface(0);
				        for (int i = 0; i < writeIntfScale.getEndpointCount(); i++) 
				        {
				            if (writeIntfScale.getEndpoint(i).getType() == UsbConstants.USB_ENDPOINT_XFER_BULK) 
				            {
				                if (writeIntfScale.getEndpoint(i).getDirection() == UsbConstants.USB_DIR_IN)
				                	writeIpScale = writeIntfScale.getEndpoint(i);
				                else
				                	writeEpScale = writeIntfScale.getEndpoint(i);
				            } 
				            else 
				            {
				            }
				        }
					}
				}
			}
		}
		
	}

	private String enviarRecibir(String s){
		String sp = "";
		String datos = "";
		String buffer = "";
		int len = 0;
		boolean bucle = true;
		boolean exito = false;

		if (writeConnectionScale != null) {
			datos = s;
			try {
				byte[] bytes = datos.getBytes();
				len = writeConnectionScale.bulkTransfer(writeEpScale, bytes, bytes.length, 100);

				bucle = true;
				exito = false;
				int j = 0;

				while (bucle) {
					j++;
					byte[] buf = new byte[512];
					len = writeConnectionScale.bulkTransfer(writeIpScale, buf, buf.length, 200);

					for (int i = 0; i < len; i++)
						if (buf[i] == 13) {
							i = len;
							bucle = false;
							exito = true;
						} else
							sp = sp + (char) buf[i];

					if (j > 3)
						bucle = false;
				}
				String speso = "";
				if (sp.trim().length() >= 6)
					speso = sp.substring(1);
				else
					speso = "ESPERE";
				//hola.setText(speso);
				//hola.refreshDrawableState();
				return speso;
			} catch (Exception e) {
			}
		}
		return "NO CONEXION";
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    private static BroadcastReceiver usbReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						if (deviceFoundScale != null) {
						}
					} else {
					}
				}
			}
		}
	};	
}
