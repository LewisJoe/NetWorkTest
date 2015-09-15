package com.lewis.networktest;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;


public class HttpClientActivity extends ActionBarActivity implements View.OnClickListener{

    private Button send;
    private TextView showText;
    private EditText inputText;
    private final static int SEND_REQUEST = 0;
    private final static String TAG = "HttpClientActivity";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case SEND_REQUEST:
                    String response = (String) msg.obj;
                    showText.setText(response);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_client);
        send = (Button) findViewById(R.id.send_r);
        showText = (TextView) findViewById(R.id.http_response);
        send.setOnClickListener(this);
        inputText = (EditText) findViewById(R.id.input_address);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_http_client, menu);
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
            case R.id.send_r:
                sendRequestWithHttpClient();
                break;
            default:
                break;
        }
    }

    private void sendRequestWithHttpClient(){
        final String address = inputText.getText().toString();
        if (TextUtils.isEmpty(address)){
            Toast.makeText(this,"Please input the web address",Toast.LENGTH_SHORT).show();
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(address);
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        //请求和响应都成功了
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
                        Message message = new Message();
                        message.what = SEND_REQUEST;
                        //将服务器返回的结果存放到Message中
                        message.obj = response.toString();
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * PULL方法解析xml文件
     * @param xmlData
     */
    private void parseXMLWithPull(String xmlData){
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xmlPullParser = factory.newPullParser();
            xmlPullParser.setInput(new StringReader(xmlData));
            int eventType = xmlPullParser.getEventType();
            String id = "";
            String name = "";
            String version = "";
            while (eventType != XmlPullParser.END_DOCUMENT){
                String nodeName = xmlPullParser.getName();
                switch (eventType){
                    //开始解析某个节点
                    case XmlPullParser.START_TAG:
                        try{
                            if ("id".equals(nodeName)){
                                id = xmlPullParser.nextText();
                            }else if ("name".equals(nodeName)){
                                name = xmlPullParser.nextText();
                            }else if ("version".equals(nodeName)){
                                version = xmlPullParser.nextText();
                            }
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        break;
                    //完成解析某个节点
                    case XmlPullParser.END_DOCUMENT:
                        if ("app".equals(nodeName)){
                            Log.d(TAG,"id is "+id);
                            Log.d(TAG,"name is "+name);
                            Log.d(TAG,"version is "+version);
                        }
                        break;
                    default:
                        break;
                }
                try {
                    eventType = xmlPullParser.next();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
    }
}
