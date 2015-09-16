package com.lewis.networktest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

/**
 * parse xml with pull and sax
 */
public class HttpClientParseXML extends ActionBarActivity implements View.OnClickListener{

    private static final String TAG = "HttpClientParseXML";
    private EditText editText;
    private Button pullButton;
    private Button saxButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_client_parse_xml);
        editText = (EditText) findViewById(R.id.local_address);
        pullButton = (Button) findViewById(R.id.parse_with_pull);
        saxButton = (Button) findViewById(R.id.parse_with_sax);
        pullButton.setOnClickListener(this);
        saxButton.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_http_client_parse_xml, menu);
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
            case R.id.parse_with_pull:
                sendRequestWithHttpClient(1);
                break;
            case R.id.parse_with_sax:
                sendRequestWithHttpClient(2);
                break;
            default:
                break;
        }
    }

    private void sendRequestWithHttpClient(final int flag){
        final String address = editText.getText().toString();
        if (TextUtils.isEmpty(address)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                //指定访问的服务器地址是电脑地址
                HttpGet httpGet = new HttpGet(address);
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        //响应和请求都成功了
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity,"utf-8");
                        if (flag == 1){
                            parseXMLWithPull(response);
                        }else {
                            parseXMLWithSAX(response);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

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
                        try {
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
                    case XmlPullParser.END_TAG:
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

    private void parseXMLWithSAX(String xmlData){
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            XMLReader xmlReader = factory.newSAXParser().getXMLReader();
            MyHandler handler = new MyHandler();
            //将MyHandler的实例设置到XMLReader中
            xmlReader.setContentHandler(handler);
            //开始执行解析
            try {
                xmlReader.parse(new InputSource(new StringReader(xmlData)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }
}
