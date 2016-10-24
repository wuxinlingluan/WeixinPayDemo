package com.github.weixinpaydemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private IWXAPI api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        api = WXAPIFactory.createWXAPI(this, "wxb4ba3c02aa476ea1");
    }

    public void weixinPay(View v) {
        String url = "http://wxpay.weixin.qq.com/pub_v2/app/app_pay.php?plat=android";


        Response.Listener<String> listener=new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {


                    try {
                        wechatPay(response);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
            }
        };
        Response.ErrorListener errorListener=new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "请求失败");
            }
        };
        StringRequest request=new StringRequest(0,url, listener, errorListener);
        Volley.newRequestQueue(this).add(request);
    }

        /** 把支付信息提交给微信插件完成支付
         * @throws JSONException */
    protected void wechatPay(String content) throws JSONException {
        // 将该app注册到微信
        api.registerApp(Constants.APP_ID);

        boolean isPaySupported = api.getWXAppSupportAPI() >= Build.PAY_SUPPORTED_SDK_INT;
        if (!isPaySupported) {
            Toast.makeText(this, "您没有安装微信或者微信版本太低", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject json = new JSONObject(content);
        if(null != json && !json.has("retcode") ){
            PayReq req = new PayReq();
            //req.appId = "wxf8b4f85f3a794e77";  // 测试用appId
            req.appId			= json.getString("appid");
            req.partnerId		= json.getString("partnerid");
            req.prepayId		= json.getString("prepayid");
            req.nonceStr		= json.getString("noncestr");
            req.timeStamp		= json.getString("timestamp");
            req.packageValue	= json.getString("package");
            req.sign			= json.getString("sign");
            req.extData			= "app data"; // optional
            Toast.makeText(this, "正常调起支付", Toast.LENGTH_SHORT).show();
            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
            api.sendReq(req);
        }else{
            Log.d("PAY_GET", "返回错误"+json.getString("retmsg"));
            Toast.makeText(this, "返回错误"+json.getString("retmsg"), Toast.LENGTH_SHORT).show();
        }
    }
}
