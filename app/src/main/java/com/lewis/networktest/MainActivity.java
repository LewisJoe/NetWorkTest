package com.lewis.networktest;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;


public class MainActivity extends ActionBarActivity implements View.OnClickListener{

    private final static int SHOW_RESPONSE = 0;
    private Button sendRequest;
    private TextView responseText;
    private Button gotoHttpClient;
    private Button gotoParseXML;
    private Button gotoParseJSON;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SHOW_RESPONSE:
                    String response = (String) msg.obj;
                    //在这里进行UI操作，将结果显示到界面上
                    responseText.setText(response);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sendRequest = (Button) findViewById(R.id.send_request);
        responseText = (TextView) findViewById(R.id.response);
        sendRequest.setOnClickListener(this);
        gotoHttpClient = (Button) findViewById(R.id.goto_next);
        gotoHttpClient.setOnClickListener(this);
        gotoParseXML = (Button) findViewById(R.id.goto_pull_parse_xml);
        gotoParseXML.setOnClickListener(this);
        gotoParseJSON = (Button) findViewById(R.id.goto_parse_json);
        gotoParseJSON.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.send_request:
                sendRequestWithHttpURLConnection();
                break;
            case R.id.goto_next:
                Intent intent = new Intent(this,HttpClientActivity.class);
                startActivity(intent);
                break;
            case R.id.goto_pull_parse_xml:
                Intent i = new Intent(this,HttpClientParseXML.class);
                startActivity(i);
                break;
            case R.id.goto_parse_json:
                Intent intent_to = new Intent(this,HttpClientParseJSON.class);
                startActivity(intent_to);
                break;
            default:
                break;
        }
    }

    private void sendRequestWithHttpURLConnection(){
        //开启线程来发起网络请求
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL("http://www.baidu.com");
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");//GET请求
                    connection.setConnectTimeout(8000);//连接超时
                    connection.setReadTimeout(8000);//读取超时
                    InputStream in = connection.getInputStream();
                    //下面对获取到的输入流进行读取
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    Message message = new Message();
                    message.what = SHOW_RESPONSE;
                    //将服务器返回的结果存放到Message中
                    message.obj = response.toString();
                    handler.sendMessage(message);
                } catch (Exception e) {
                    e.printStackTrace();
                }finally {
                    if (connection != null){
                        connection.disconnect();
                    }
                }
            }
        }).start();
    }
}
