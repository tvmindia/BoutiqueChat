package com.tech.thrithvam.tiquesinnchat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat extends AppCompatActivity {
    DatabaseHandler db=new DatabaseHandler(this);
    EditText inputMessage;
    ImageView send;
    String lastProductIdSeen="";
    Bundle extras;
    ListView msgList;
    Handler handler = new Handler();
    int loadedMsgCount=0;
    String UserID;
    LinearLayout productDetail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        productDetail=(LinearLayout)findViewById(R.id.productDetail);
        extras=getIntent().getExtras();
        getSupportActionBar().setElevation(0);
        android.support.v7.app.ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setTitle(extras.getString("UserName"));
        }
        msgList= (ListView) findViewById(R.id.messagesListView);
        if(db.GetUserDetail("UserID")==null){
            Toast.makeText(Chat.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(Chat.this, Login.class);
            startActivity(intentUser);
            finish();
            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
        }



        UserID=extras.getString("UserID");

        if (isOnline()) {

        } else {
            Toast.makeText(Chat.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
            finish();
        }
        inputMessage=(EditText)findViewById(R.id.msgInput);
        send=(ImageView) findViewById(R.id.submitMsg);
        inputMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                inputMessage.setLines(3);
            }
        });

        //Messages Loading------------------

        msgList.setAdapter(null);
        loadMessages();
    //    loadingTxt=(TextView)findViewById(R.id.loadingText);

        //Product Details----------------------

        new ProductDetailsForChat().execute();
        //productDetail.setVisibility(View.GONE);
    }
    //----------Loading messages-----------------
    public void loadMessages()
    {
        final ArrayList<String[]> msgData=db.GetMsgs(UserID);
        lastProductIdSeen=msgData.get(msgData.size()-1)[3];                     //Default product is last one
        CustomAdapter adapter=new CustomAdapter(Chat.this, msgData,"chat");
        //New messages are arrived. List is refreshed
        if(adapter.getCount()>loadedMsgCount)
        {
            msgList.setAdapter(adapter);
            msgList.setOnItemClickListener(null);
            msgList.setSelection(msgList.getCount() - 1);
            loadedMsgCount=msgList.getCount();
           /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                msgList.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                    @Override
                    public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                        if(msgList.getLastVisiblePosition()==msgData.size()-1){                     //Chat reached at end
                            lastProductIdSeen=msgData.get(msgData.size()-1)[3];
                            new ProductDetailsForChat().execute();
                        }
                        else if(!msgData.get(msgList.getLastVisiblePosition())[3].equals(lastProductIdSeen)
                                &&
                                !msgData.get(msgList.getLastVisiblePosition())[3].equals("null")){
                            lastProductIdSeen=msgData.get(msgList.getLastVisiblePosition())[3];
                            new ProductDetailsForChat().execute();
                        }
                    }
                });
            }
            else {*/
                msgList.setOnScrollListener(new AbsListView.OnScrollListener() {
                    @Override
                    public void onScrollStateChanged(AbsListView view, int scrollState) {

                        if(msgList.getLastVisiblePosition()==msgData.size()-1){
                            lastProductIdSeen=msgData.get(msgData.size()-1)[3];
                            new ProductDetailsForChat().execute();
                        }
                        else if(!msgData.get(msgList.getLastVisiblePosition())[3].equals(lastProductIdSeen)
                                &&
                                !msgData.get(msgList.getLastVisiblePosition())[3].equals("null")){
                            lastProductIdSeen=msgData.get(msgList.getLastVisiblePosition())[3];
                            new ProductDetailsForChat().execute();
                        }
                    }

                    @Override
                    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                    }
                });
         //   }
        }
        if(loadedMsgCount==0){
            msgList.setVisibility(View.GONE);
            productDetail.setVisibility(View.GONE);
            //loadingTxt.setVisibility(View.VISIBLE);
        }
        else {
            msgList.setVisibility(View.VISIBLE);
          //  loadingTxt.setVisibility(View.GONE);
        }
        handler.postDelayed(new Runnable() {
            public void run() {
                loadMessages();
            }
        },1000);

    }

    @Override
    public void onBackPressed() {
        Intent back=new Intent(Chat.this,ChatList.class);
        startActivity(back);
        finish();
        handler.removeCallbacksAndMessages(null);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    public void sendMsg(View view){
        if(db.GetUserDetail("UserID")==null){
            Toast.makeText(Chat.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(Chat.this, Login.class);
            startActivity(intentUser);
            finish();
            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
        }
        if(!inputMessage.getText().toString().trim().equals(""))
        {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            send.setEnabled(false);
            new SendMessage().execute();
        }
    }
    //-----------------------Async tasks----------------------------
    public class SendMessage extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        String sendMsg;
        ProgressDialog pDialog=new ProgressDialog(Chat.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            sendMsg=inputMessage.getText().toString().trim();
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/InsertChat";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + "" + "\",\"replyPersonID\":\"" + db.GetUserDetail("UserID") + "\",\"boutiqueID\":\"" + db.GetUserDetail("BoutiqueID") + "\",\"userID\":\"" + UserID + "\",\"direction\":\"" + "in"  + "\",\"message\":\"" + sendMsg + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            if(!pass) {
                new AlertDialog.Builder(Chat.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setCancelable(false).show();
            }
            else {
                inputMessage.setText("");
            }
            send.setEnabled(true);
        }
    }
    public class ProductDetailsForChat extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        String productName,priceString,productImage;
        Integer productNoInt;
        AVLoadingIndicatorView avLoadingIndicatorView;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)findViewById(R.id.prodDetLoading);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/GetProductDetailsOnChat";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + lastProductIdSeen + "\",\"boutiqueID\":\"" + db.GetUserDetail("BoutiqueID") + "\"}";
                URL u = new URL(url);
                c = (HttpURLConnection) u.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json; charset=utf-16");
                c.setRequestProperty("Content-length", Integer.toString(postData.length()));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.setUseCaches(false);
                c.setConnectTimeout(10000);
                c.setReadTimeout(10000);
                DataOutputStream wr = new DataOutputStream(c.getOutputStream());
                wr.writeBytes(postData);
                wr.flush();
                wr.close();
                status = c.getResponseCode();
                switch (status) {
                    case 200:
                    case 201: BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                        sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line).append("\n");
                        }
                        br.close();
                        int a=sb.indexOf("[");
                        int b=sb.lastIndexOf("]");
                        strJson=sb.substring(a, b + 1);
                        //   strJson=cryptography.Decrypt(strJson);
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
                        Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                        msg=ex.getMessage();
                    }
                }
            }
            if(strJson!=null)
            {try {
                JSONObject jsonRootObject = new JSONObject(strJson);
                jsonArray = jsonRootObject.optJSONArray("JSON");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    msg=jsonObject.optString("Message");
                    pass=jsonObject.optBoolean("Flag",true);
                    productName=jsonObject.optString("Name").replace("\\u0026", "&");
                    priceString=String.format(Locale.US,"%.2f", jsonObject.optDouble("Price"));
                    productNoInt=jsonObject.optInt("ProductNo");
                    productImage=getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(!pass) {
                /*new AlertDialog.Builder(Chat.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();*/
              //  productDetail.setVisibility(View.GONE);
            }
            else {
                TextView pName=(TextView)findViewById(R.id.productName);
                TextView pNo=(TextView)findViewById(R.id.productNo);
                TextView pPrice=(TextView)findViewById(R.id.productPrice);
                ImageView pImage=(ImageView)findViewById(R.id.productImg);

                pName.setText(productName);
                pNo.setText(getResources().getString(R.string.product_no, productNoInt));
                pPrice.setText(getResources().getString(R.string.rs, priceString));
                Picasso.with(Chat.this).load(productImage).into(pImage);
                productDetail.setVisibility(View.VISIBLE);
            }
            avLoadingIndicatorView.setVisibility(View.GONE);
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
}