package com.example.zyy.wps;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.icu.text.SimpleDateFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private  TextView androidID;
    private  TextView oSV;
    private  TextView iP;
    private  TextView packageName;
    private  TextView userName;
    private  TextView time;
    private  Handler handler;
    private String android_id;
     String ip;
    private String osv;
    private String packa;
    private String user_name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        getWindow().setFeatureInt(android.view.Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        inti();
        new Thread(runnable).start();
        new Thread(postData).start();
    }
    //获取当前时间
    private String getTime() {
        Date day = new Date();
        SimpleDateFormat  fmt = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            fmt = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        }
        String sayDatetime = fmt.format(day.getTime());
        return sayDatetime;
    }

    //初始化
    @SuppressLint("HandlerLeak")
    public  void inti() {
        androidID = findViewById(R.id.tx_andoidIdText);
        oSV = findViewById(R.id.tx_oSVText);
        iP = findViewById(R.id.tx_ipText);
        packageName = findViewById(R.id.tx_packageNameText);
        userName = findViewById(R.id.tx_userNameText);
        time = findViewById(R.id.tx_timeText);


        handler = new Handler() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void handleMessage(Message msg) {
              super.handleMessage(msg);
              Map<String, String> map = (HashMap<String,String>) msg.obj;
                androidID.setText(map.get("android_id"));
                iP.setText(map.get("ip"));
                oSV.setText(map.get("osv"));
                packageName.setText(map.get("packa"));
                userName.setText(map.get("user_name"));
                time.setText(getTime());

            }
        };
    }


   private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                    ip = getIpAddress(MainActivity.this);
                    osv = Build.VERSION.RELEASE;
                    packa = getPackageName();
                    Intent intent = getIntent();
                    user_name = intent.getStringExtra("username");
                    Log.e("zyy", "ip地址：" + ip);


                    //将数据存在HashMap里面
                    HashMap<String, String> map = new HashMap<>();
                    map.put("android_id", android_id);
                    map.put("ip", ip);
                    map.put("osv", osv);
                    map.put("packa", packa);
                    map.put("user_name", user_name);

                    Message message = handler.obtainMessage();
                    message.obj = map;
                    handler.sendMessage(message);

                    Thread.sleep(300*1000);
                    new Thread(postData).start();

//                    new Handler(Looper.getMainLooper()).postDelayed(runnable, TimeUnit.MINUTES .toSeconds(5));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };



    private Runnable postData = new Runnable() {
        @Override
        public void run() {
            Log.e("zyy","进入线程");
            HttpURLConnection conn = null;

            try{
                String url = "http://www.mockhttp.cn/mock/wpstest";
                URL mURL = new URL(url);
                conn = (HttpURLConnection) mURL.openConnection();
                conn.setDoOutput(true);
                conn.setDoInput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded");
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(8000);
                String body = "AndroidID="+android_id+"ip="+ip+"&OSV="+osv+"&PackageName="+packa+"&UserName="+user_name+"&Time="+getTime();
                Log.e("zyy",body);
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream(), "UTF-8"));
                writer.write(body);
                writer.close();
                Log.e("zyy","ok11");
                int responseCode = conn.getResponseCode();
                Log.e("zyy","ok1");
                if (responseCode == 200) {
                    InputStream is = conn.getInputStream();
                    String reponse = getStringFromInputStream(is);
                    Log.e("zyy",reponse);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("zyy","catch");
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    };
    private String getStringFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream os =new ByteArrayOutputStream();
        byte[] buffer =new byte[1024];
        int len =-1;
        while((len=is.read(buffer))!=-1){
            os.write(buffer,0,len);
        }
        is.close();
        String state = os.toString();
        os.close();
        return  state;
    }

    //获取本地IP地址
    private String getIpAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                try {
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());
                return ipAddress;
            } else if (info.getType() == ConnectivityManager.TYPE_ETHERNET) {
                return getLocalIp();
            }
        }
        return null;
    }

    private String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    private String getLocalIp() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return "0.0.0.0";
    }
}
