package com.example.iot_client;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    MyService myService;
    boolean isService = false;
    //이미지 변환시킬 토글버튼
    ToggleButton doorbutton;
    ToggleButton lightbutton;
    ToggleButton frontbutton;
    ToggleButton rearbutton;
    ToggleButton heaterbutton;
    ToggleButton airbutton;
    ToggleButton trunkbutton;
    String humidity = "0";
    String temperture = "0";
    TextView humi,temp;
    String recvData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        humi = (TextView) findViewById(R.id.humi); // -> String : humidity
        temp = (TextView) findViewById(R.id.temp); // -> String : temperture
        //이미지 바뀔 토글 버튼 init
        doorbutton = (ToggleButton) findViewById(R.id.doorButton);
        lightbutton = (ToggleButton) findViewById(R.id.lightButton);
        frontbutton = (ToggleButton) findViewById(R.id.frontButton);
        rearbutton = (ToggleButton) findViewById(R.id.rearButton);
        heaterbutton = (ToggleButton) findViewById(R.id.heaterButton);
        airbutton = (ToggleButton) findViewById(R.id.airButton);
        trunkbutton = (ToggleButton) findViewById(R.id.trunkButton);
        //서버로 값 전달할 GPIO Button init
        doorbutton.setOnClickListener(this); // 누르면 이미지 close로 바꿔 - 토글버튼
        lightbutton.setOnClickListener(this);
        frontbutton.setOnClickListener(this);
        rearbutton.setOnClickListener(this);
        heaterbutton.setOnClickListener(this);
        airbutton.setOnClickListener(this);
        trunkbutton.setOnClickListener(this);
        findViewById(R.id.trunkButton).setOnClickListener(this);
        findViewById(R.id.gpsButton).setOnClickListener(this);
        findViewById(R.id.drivemode).setOnClickListener(this);
        findViewById(R.id.keyButton).setOnClickListener(this);
        findViewById(R.id.managementButton).setOnClickListener(this);

        serviceBind();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (!Thread.interrupted())
                    try {
                        Thread.sleep(1000);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recvData = myService.receiveMessage();
                                if (recvData.length() != 0) {
                                    String [] splited = recvData.split(",");
                                    humidity = splited[0];
                                    temperture = splited[1];
                                    humi.setText(humidity);
                                    temp.setText(temperture);

                                } else {
                                    Toast.makeText(getApplicationContext(), "no Data!", Toast.LENGTH_LONG).show();
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
    public void onClick(View view) {
        int id = view.getId();
        String value = "";
        switch (id) {
            case R.id.doorButton:
                if (doorbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(doorbutton.isChecked()));
                    value = "o";
                    doorbutton.setBackground(getResources().getDrawable(R.drawable.close));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(doorbutton.isChecked()));
                    value = "c";
                    doorbutton.setBackground(getResources().getDrawable(R.drawable.open));
                    myService.sendMessage(value);
                }
                break;
            case R.id.airButton:
                if (airbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(airbutton.isChecked()));
                    value = "a";
                    airbutton.setBackground(getResources().getDrawable(R.drawable.airoff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(airbutton.isChecked()));
                    value = "A";
                    airbutton.setBackground(getResources().getDrawable(R.drawable.air));
                    myService.sendMessage(value);
                }
                break;
            case R.id.heaterButton:
                if (heaterbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(heaterbutton.isChecked()));
                    value = "h";
                    heaterbutton.setBackground(getResources().getDrawable(R.drawable.heateroff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(heaterbutton.isChecked()));
                    value = "H";
                    heaterbutton.setBackground(getResources().getDrawable(R.drawable.heater));
                    myService.sendMessage(value);
                }
                break;
            case R.id.lightButton:

                if (lightbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(lightbutton.isChecked()));
                    value = "l";
                    lightbutton.setBackground(getResources().getDrawable(R.drawable.warningoff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(lightbutton.isChecked()));
                    value = "L";
                    lightbutton.setBackground(getResources().getDrawable(R.drawable.warning));
                    myService.sendMessage(value);
                }
                break;
            case R.id.trunkButton:
                if (trunkbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(trunkbutton.isChecked()));
                    value = "t";
                    trunkbutton.setBackground(getResources().getDrawable(R.drawable.trunkoff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(trunkbutton.isChecked()));
                    value = "T";
                    trunkbutton.setBackground(getResources().getDrawable(R.drawable.trunk));
                    myService.sendMessage(value);
                }
                break;
            case R.id.gpsButton:
                value = "g";
                myService.sendMessage(value);
                break;
            case R.id.keyButton:
                value = "k";
                myService.sendMessage(value);
                break;
            case R.id.frontButton:

                if (frontbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(frontbutton.isChecked()));
                    value = "f";
                    frontbutton.setBackground(getResources().getDrawable(R.drawable.frontoff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(frontbutton.isChecked()));
                    value = "F";
                    frontbutton.setBackground(getResources().getDrawable(R.drawable.front));
                    myService.sendMessage(value);
                }
                break;
            case R.id.rearButton:
                if (rearbutton.isChecked()) {
                    Log.e("checked?", String.valueOf(rearbutton.isChecked()));
                    value = "r";
                    rearbutton.setBackground(getResources().getDrawable(R.drawable.rearoff));
                    myService.sendMessage(value);
                } else {
                    Log.e("checked?", String.valueOf(rearbutton.isChecked()));
                    value = "R";
                    rearbutton.setBackground(getResources().getDrawable(R.drawable.rear));
                    myService.sendMessage(value);
                }
                break;
            case R.id.managementButton:
                startActivity(new Intent(MainActivity.this, Management.class));
                break;
            case R.id.drivemode:
                startActivity(new Intent(MainActivity.this,DriveActivity.class));
                break;
        }
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
                MainActivity.this, // 현재 화면
                MyService.class); // 다음넘어갈 컴퍼넌트

        bindService(intent, // intent 객체
                conn, // 서비스와 연결에 대한 정의
                Context.BIND_AUTO_CREATE);
        //처음 서비스를 시작하는 액티비티에서는 Context.BIND_AUTO_CREATE
        //다른 액티비티에서는 Context.BIND_NOT_FOREGROUND를 주어야합니다.
    }
}
