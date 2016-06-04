package com.realtek.uartthrough;


import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.text.format.Formatter;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceListener;
import javax.jmdns.ServiceInfo;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by WUJINZHOU on 5/2/15.
 */
public class NsdHelper extends AsyncTask<Void, Void, Void> {
	
	private final Lock _mutex = new ReentrantLock(true);
   
	
    //private final String mServiceType = "_uart_data._tcp.local.";//"_uart_control._tcp.local.";
    private final String tag = "@NsdHelper ";
    private JmDNS jmdns = null;
    private ServiceListener listener = null;
    
    ServiceInfo mService;
    Globals_ctrl mG_ctrl;
    Globals_d mG_d;
    
    private  String mServiceType; 
    String mServiceType_c ;	
   	String mServiceType_d ;	
   	String mIP;
    //ServiceEvent mServiceEvent;


  
    
    public NsdHelper(String serviceType, Globals_ctrl g_ctrl, String ip){
    	mG_ctrl = g_ctrl;
    	mServiceType_c = mG_ctrl.getServiceType();
    	mServiceType = serviceType;
    	mIP = ip;
    }
    
    public NsdHelper(String serviceType, Globals_d g_d, String ip){
    	mG_d = g_d;
    	mServiceType_d = mG_d.getServiceType();
    	mServiceType = serviceType;
    	mIP = ip;
    }

    @Override
    protected Void doInBackground(Void... params) {
        startNSD();
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        stopNSD();
    }

    private void startNSD() {
        Log.v(tag,"startNSD() " + mServiceType);
        try {        	
        	
            jmdns = JmDNS.create(InetAddress.getByName(mIP));//JmDNS.create();
            jmdns.addServiceListener(mServiceType, listener = new ServiceListener() {

                @Override
                public void serviceAdded(ServiceEvent event) {
                    jmdns.requestServiceInfo(event.getType(), event.getName(), true); //resolve service
                    Log.v(tag, "serviceAdded() "+event.getName());
                }

                @Override
                public void serviceRemoved(ServiceEvent event) {
                    Log.v(tag,"serviceRemoved() " + event.getName());
                }

                @Override
                public void serviceResolved(ServiceEvent event) {
                	 boolean serviceDuplicate = false;                	 
                	 mService = event.getInfo();

                    //do someting with this service event
                	 
                	 if( !(mServiceType_c == null) ){
                		 _mutex.lock();
	                	 if ( mService.getType().contains(mServiceType_c)) {	                         
	                         for(int i=0;i< mG_ctrl.getDeviceList().size();i++){
	                        	 if( mG_ctrl.getDeviceList().elementAt(i).getName().equalsIgnoreCase(mService.getName())){
	                        		 serviceDuplicate = true;	
	                        		 break;
	                        	 }
	                         }
	                         if (serviceDuplicate == false) 
	                         {
		                		 mG_ctrl.opStates().setOpState(mG_ctrl.opStates().stateResoledService());
		                		 mG_ctrl.addInfo(tag + "Resolve Succeeded.\n " + mService);
		                		 mG_ctrl.setSuccessRec(tag + "Resolve Succeeded. " + mService);
		                		 mG_ctrl.getDeviceList().add(mService); //TODO check ip before add to list
		                		 mG_ctrl.setResolveFinish();
		                		 //Log.i(tag,"nsdhelper: isResolveFinish " + mG_ctrl.isResolveFinish());
		                		 serviceDuplicate = false;	
	                         }
	                     }	                	 
	                	 _mutex.unlock();
                	 }
                	 
                	 if( !(mServiceType_d == null) ){
	                	 if (!mServiceType_d.equals(null) && mService.getType().equals(mServiceType_d)) {
	                         Log.e(tag, "G_D onServiceResolved");
	                    	 mG_d.opStates().setOpState(mG_d.opStates().stateResoledService());	                         
	                    	 mG_d.setSuccessRec(tag + "Resolve Succeeded. " + mService);
	                    	 mG_d.getDeviceList().add(mService);//TODO check ip before add to list
	                    	 mG_d.setResolveFinish();
	                    	
	                     }
                	 }
                     Log.w(tag, "onServiceResolved Succeeded. " + mService);
                    
                }

            });

        } catch (IOException e) {
            Log.e(tag,"startNSD() IOException e:"+e);
            e.printStackTrace();
        }
    }

    private void stopNSD() {
        
        if (jmdns != null) {
            if (listener != null) {
                jmdns.removeServiceListener(mServiceType, listener);
                listener = null;
            }
            jmdns.unregisterAllServices();
            try {
                jmdns.close();
                Log.v(tag,"stopNSD() ");
            } catch (IOException e) {
                Log.e(tag,"stopNSD() IOException e:"+e);
                e.printStackTrace();
            }
            jmdns = null;
        }
    }
}

