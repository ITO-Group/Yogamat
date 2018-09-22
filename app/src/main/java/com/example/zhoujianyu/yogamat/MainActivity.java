package com.example.zhoujianyu.yogamat;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.emitter.Emitter;


import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    public static final int ROW_NUM = 28;
    public static final int COL_NUM = 16;
    public static final int TOUCH_THR=110;
    public int touchPoints[] = {1,2,3,4,5,6,7,8,9,10,11,12};
    short lastData[] = new short[ROW_NUM];
    public int minutes = 5;
    public int seconds = 1;
    int times = 1;
    public String server_ip = "10.19.20.229";
    public String socket_port = "5000";
    public String classify_port = "5000";

    public RequestQueue mQueue;
    public TextView action_text;
    public TextView time_view;
    public ImageView yoga_view;

    private com.github.nkzawa.socketio.client.Socket mSocket;
    private void initSocket(String server_ip,String socket_port){
        try {
            mSocket = IO.socket("http://"+server_ip+":"+socket_port);
        } catch (URISyntaxException e) {}
        finally{
            mSocket.on("change",onNewMessageListener);
            mSocket.connect();
        }
    }
    private Emitter.Listener onNewMessageListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            final String action = (String)args[0];
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(action.equals("0")){
                        action_text.setText("No Action");
                        yoga_view.setImageResource(R.drawable.action0_s);
                        yoga_view.setRotation(-90);
                        yoga_view.getLayoutParams().height=800;
                        minutes = 5;seconds=1;
                    }
                    else if(action.equals("1")){
                        action_text.setText("Lord of the Dance Pose");
                        yoga_view.setImageResource(R.drawable.action1_s);
                        yoga_view.setRotation(0);
                        yoga_view.getLayoutParams().height=550;
                        minutes = 5;seconds=1;
                    }
                    else if(action.equals("2")){
                        action_text.setText("Plank");
                        yoga_view.setImageResource(R.drawable.action2_s);
                        yoga_view.setRotation(0);
                        yoga_view.getLayoutParams().height=700;
                        minutes = 5;seconds=1;
                    }
                    else if(action.equals("3")){
                        action_text.setText("High Lunge Variation");
                        yoga_view.setImageResource(R.drawable.action3_s);
                        yoga_view.setRotation(0);
                        yoga_view.getLayoutParams().height=700;
                        minutes = 5;seconds=1;
                    }
                    else if(action.equals("4")){
                        action_text.setText("Camel Pose");
                        yoga_view.setImageResource(R.drawable.action4_s);
                        yoga_view.setRotation(0);
                        yoga_view.getLayoutParams().height=700;
                        minutes = 5;seconds=1;
                    }
                    else{
                        action_text.setText("5");
                        action_text.setText("Lotus Pose");
                        yoga_view.setImageResource(R.drawable.action5_s);
                        yoga_view.setRotation(0);
                        yoga_view.getLayoutParams().height=700;
                        minutes = 5;seconds=1;
                    }
                }
            });
        }
    };

    public Timer timer = new Timer();
    TimerTask task = new TimerTask() {
        @Override
        public void run() {
            if(minutes!=0){
                if(seconds==0) {seconds=59;minutes--;}
                else seconds--;
            }
            else{
                if(seconds>0) seconds--;
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    time_view.setText("0"+minutes+":"+(seconds<10?("0"+seconds):seconds));
                }
            });
        }
    };


    public ArrayList<String> getRawCapacityData()throws Exception{
        String line = "";
        ArrayList<String> rawData = new ArrayList<>();
        String command[] = {"aptouch_daemon_debug", "diffdata"};
        Process process = new ProcessBuilder(new String[] {"aptouch_daemon_debug", "diffdata"}).start();
        InputStream procInputStream = process.getInputStream();
        InputStreamReader reader = new InputStreamReader(procInputStream);
        BufferedReader bufferedreader = new BufferedReader(reader);
        while ((line = bufferedreader.readLine()) != null) {
            rawData.add(line);
        }
        return rawData;
    }

    public void refresh()throws Exception{
        ArrayList<String> rawData = getRawCapacityData();
        short data[] = new short[28*16];
        short tmp[][] = new short[16][28];
        for(int i = 0;i<rawData.size();i++){
            StringTokenizer t = new StringTokenizer(rawData.get(i));
            int j = 0;
            while(t.hasMoreTokens()){
                tmp[i][j++] = Short.parseShort(t.nextToken());
            }
        }
        int k = 0;
        for(int j = 0;j<28;j++){
            for(int i = 0;i<16;i++){
                data[k++] = tmp[i][j];
            }
        }
        String capaData = "";
        String gesture = "5";  //0:无动作，1：平板支撑，2：金鸡独立，3：静坐
        String type="store";
        boolean touched = false;
        boolean same = true;
        for(int i = 0;i<ROW_NUM;i++){
            int diff = data[i*COL_NUM]-lastData[i];
            if(diff!=0)same = false;
            capaData+=(","+data[i*COL_NUM]);
            if(diff>TOUCH_THR) {
                touched=true;
            }
            lastData[i] = data[i*COL_NUM];
        }

//        if(touched){
//            Log.e("bug",""+times++);
//            capaData = (gesture + capaData);
//            sendCloud(capaData+"\n",server_ip,classify_port,type);
//        }
//        if(!same){
//            capaData = (gesture + capaData);
//            sendCloud(capaData+"\n","10.19.195.93","5000",type);
//        }
    }

    public void sendCloud(final String data_str,String server_ip,String port,String type){
        /**
         * send the str to a remote server
         */
        String url = "http://"+server_ip+":"+port+"/";
        StringRequest req = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                //nothing here
//                Log.e("bug",response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
//                Log.e("bug",error.toString());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap data = new HashMap<String,String>();
                data.put("data",data_str);
                return data;
            }
        };
        mQueue.add(req);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mQueue = Volley.newRequestQueue(this);
        setContentView(R.layout.activity_main);
        action_text = findViewById(R.id.sample_text);
        time_view = findViewById(R.id.editText);
        yoga_view = findViewById(R.id.imageView3);
        timer.schedule(task,0,1000);
        initSocket(server_ip,socket_port);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true){
                    try {
                        refresh();
                    }catch (Exception e){

                    }
                }
            }
        }).start();
    }
}
