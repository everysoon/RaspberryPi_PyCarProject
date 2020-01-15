package com.example.iot_client;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MyService extends Service {
    private String serverIp = "192.168.1.228";
    private int serverPort = 7000;

    //소켓 객체와 Input스트림, Output스트림
    Socket serverSocket;
    InputStream reader;
    OutputStream writer;
    //액티비티에서 onBind로 연결해주기 위해선 바인더 객체를 반환해야 하므로
    //바인더 객체를 생성하여 서비스 내의 메소드를 액티비티에서 활용할수 있도록 하여준다.
    IBinder mIBinder = new MyBinder();
    //임시로 사용하게 될 문자열
    String response = "";

    //IBinder 객체를 생성할때 new 연산자로 활용할 Binder 객체이다.
    class MyBinder extends Binder {
        MyService getService(){
            //자기 자신 서비스를 반환해야 한다.
            return MyService.this;
        }
    }
    //생성자이다.
    public MyService() {

    }
    //서비스를 생성하였을때 실행되는 메소드이다.
    @Override
    public void onCreate() {
        super.onCreate();
        //서버에 연결함과 동시에 비동기로 메시지를 수신하는 역할을 하는 스레드이다.
        Thread ServerConnect = new Thread(new Runnable() {
            //수신한 바이트 정보를 담을 바이트 배열이다.
            byte[] buffer = new byte[1024];
            //sendBroadcast로 송신할 Intent 객체이다.
            Intent messageIntent = new Intent();
            @Override
            public void run() {
                try{
                    //Server Ip와 Server Port를 이용하여 소켓을 연결해주고
                    //해당 소켓에 해당하는 InputStream과 OutputStream을 받아줍니다.

                    serverSocket = new Socket(serverIp, serverPort);
                    writer = serverSocket.getOutputStream();
                    reader = serverSocket.getInputStream();
                    Log.e("디버깅", "접속 성공");
                }
                catch (Exception ex){
                    ex.printStackTrace();
                    Log.e("디버깅", "접속 실패했다");

                }
                response = "";
                while(true){
                    try {
                        //Log.e("디버깅", "서버 상태 : 메시지수신 시작");
                        //수신한 메세지를 바이트 배열로 ㅈ
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(1024);

                        int bytesRead;

                        while ((bytesRead = reader.read(buffer)) != -1){
                            response = "";
                            //바이트 정보 받아서 문자열로 변환
                            byteArrayOutputStream.write(buffer, 0, bytesRead);
                            response = byteArrayOutputStream.toString("UTF-8");

                            Log.e("디버깅 ", "수신한 메세지"+response);
                      /*      Intent sendMessage = new Intent(MyService.this,MainActivity.class);
                            sendMessage.putExtra("response",response);*/
                            //JSONObject jsonObject = new JSONObject(response);

                            //byteArrayOutputStream을 reset으로 초기화 하여 비워줍니다.
                            byteArrayOutputStream.reset();

                            messageIntent.setAction("RECEIVE_MESSAGE");
                            messageIntent.putExtra("message", response);

                            sendBroadcast(messageIntent);
                        }
                    }
                    catch (Exception ex){
                        //Log.d("서버 이상", "해 ");
                    }
                }
            }
        });
        ServerConnect.start();
    }

    //서비스가 종료될때 불려지는 메소드이다.
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            Log.e("디스트로이","되었나요?");
            //Input스트림과 Output스트림을 닫아주지 않으면 서버의 버퍼에 쓰래기 값이 계속 전송된다.
            writer.close();
            reader.close();
        }
        catch (Exception e){
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY; //서비스가 멈췄으면 다시 시작합니다.
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        Log.e("LOG", "onBind()");
        return mIBinder;
    }
    public String receiveMessage(){
        return response;
    }

    //서버에 메세지를 보내는 메소드입니다.
    public void sendMessage(final String message){
        //이는 스레드에서 동작해야 합니다.
        Thread sendThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.e("센드 매새지1:",message);
                    writer.write(message.getBytes());
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    Log.d("디버깅", "서버 상태 접속 실패");
                }
            }
        });
        sendThread.start();
    }
}
