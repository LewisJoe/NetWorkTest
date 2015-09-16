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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * 解析JSON文件
 */
public class HttpClientParseJSON extends ActionBarActivity implements View.OnClickListener{

    private final static String TAG = "HttpClientParseJSON";

    private Button parseJSON;
    private EditText textAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_client_parse_json);
        parseJSON = (Button) findViewById(R.id.parse_json);
        textAddress = (EditText) findViewById(R.id.json_address);
        parseJSON.setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_http_client_parse_json, menu);
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
            case R.id.parse_json:
                sendRequestWithHttpClient();
                break;
            default:
                break;
        }
    }

    /**
     * 通过HttpClient访问网络
     */
    private void sendRequestWithHttpClient(){
        final String address = textAddress.getText().toString();
        if (TextUtils.isEmpty(address)){
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                HttpClient httpClient = new DefaultHttpClient();
                //指定访问的服务器地址是电脑本机
                HttpGet httpGet = new HttpGet(address);
                try {
                    HttpResponse httpResponse = httpClient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200){
                        //请求和响应都成功了
                        HttpEntity entity = httpResponse.getEntity();//获取实体
                        String response = EntityUtils.toString(entity,"utf-8");
                        parseJSONWithJSONObject(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 解析JSON文件通过JSONObject
     * @param jsonData
     */
    private void parseJSONWithJSONObject(String jsonData){
        try {
            JSONArray jsonArray = new JSONArray(jsonData);
            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String id = jsonObject.getString("id");
                String name = jsonObject.getString("name");
                String version = jsonObject.getString("version");
                Log.d(TAG,"id is "+id);
                Log.d(TAG,"name is "+name);
                Log.d(TAG,"version is "+version);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
