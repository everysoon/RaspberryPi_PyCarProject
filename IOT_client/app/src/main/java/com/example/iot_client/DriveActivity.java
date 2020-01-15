package com.example.iot_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.daum.android.map.MapView;
import net.daum.mf.map.api.MapPoint;

public class DriveActivity extends AppCompatActivity {
    //서버에서 받은 문자열 이용해서 업데이트할 UI와 값 String들
    String kakaokey = "2f82d8f4eadfb1deb3a5e061fde2d7a5";
    TextView km, kmh,time;
    String distance = "0";
    String speed = "0";
    String hour = "0";
    String min = "0";
    String sec = "0";
    String recvData;
    String axelonoff="";
    String breakonoffis="";
    MyService myService;
    int i = 0;
    boolean isService = false;
    Button axel,start,breakonoff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        //서버로 전달받아 값 변할 TextView 정의
        //서버로 부터 distance,speed,humidity,temperture 순서로 구분자","를 사용해서 문자열 결합해서 한 문자열로 받음
        km = (TextView) findViewById(R.id.km); // -> String : distance
        kmh = (TextView) findViewById(R.id.kmh); // -> String : speed
        time = (TextView) findViewById(R.id.time);
        axel = (Button) findViewById(R.id.axelbutton);
        breakonoff=(Button)findViewById(R.id.breakbutton);
        start=(Button)findViewById(R.id.startbutton);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myService.sendMessage("drivemode");
            }
        });
        serviceBind();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(300);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recvData = myService.receiveMessage();
                                if (recvData.length() > 5 ) {
                                    String [] splited = recvData.split(",");

                                    hour = splited[4];
                                    min = splited[5];
                                    sec = splited[6];
                                    /*String [] ab = splited[7].split(",");*/
                                    axelonoff =splited[7];
                                    breakonoffis =splited[8];
                                    distance = splited[2];
                                    speed = splited[3];

                                    km.setText(distance);
                                    kmh.setText(speed);
                                    time.setText(hour+"시  "+min+"분  "+sec+"초");
                                } else {
                                    Toast.makeText(getApplicationContext(), "no Data!", Toast.LENGTH_LONG).show();
                                }
                                if(axelonoff.equals("axel")){
                                    Log.e("a","a");
                                    axel.setBackground(getResources().getDrawable(R.drawable.axeloff));
                                    breakonoff.setBackground(getResources().getDrawable(R.drawable.breakon));
                                }
                                if(breakonoffis.equals("break")){
                                    Log.e("b","b");
                                    axel.setBackground(getResources().getDrawable(R.drawable.axel));
                                    breakonoff.setBackground(getResources().getDrawable(R.drawable.breakoff));
                                }
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
            }
        }).start();// 스레드를 사용해서 서버에서 받은 정보로 TextView 업데이트

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isService) {
            unbindService(conn); // 서비스 종료
            isService = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    ServiceConnection conn = new ServiceConnection() {
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            // 서비스와 연결되었을 때 호출되는 메서드
            // 서비스 객체를 전역변수로 저장
            MyService.MyBinder mb = (MyService.MyBinder) service;
            myService = mb.getService(); // 서비스가 제공하는 메소드 호출하여
            // 서비스쪽 객체를 전달받을수 있슴
            isService = true;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결",
                    Toast.LENGTH_LONG).show();
        }

        public void onServiceDisconnected(ComponentName name) {
            // 서비스와 연결이 끊겼을 때 호출되는 메서드
            isService = false;
            Toast.makeText(getApplicationContext(),
                    "서비스 연결 해제",
                    Toast.LENGTH_LONG).show();
        }
    };

    public void serviceBind() {

        Intent intent = new Intent(
                DriveActivity.this, // 현재 화면
                MyService.class); // 다음넘어갈 컴퍼넌트

        bindService(intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_NOT_FOREGROUND);
        //처음 서비스를 시작하는 액티비티에서는 Context.BIND_AUTO_CREATE
        //다른 액티비티에서는 Context.BIND_NOT_FOREGROUND를 주어야합니다.
    }
}
