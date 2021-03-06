package com.tech.thrithvam.tiquesinnowner;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Chat extends AppCompatActivity {
    DatabaseHandler db=new DatabaseHandler(this);
    EditText inputMessage;
    ImageView send;
    Bundle extras;
    ListView msgList;
    Handler handler = new Handler();
    int loadedMsgCount=0;
    String UserID;
    AsyncTask userDetails;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
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
           // finish();
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
    }
    //----------Loading messages-----------------
    public void loadMessages()
    {
        final ArrayList<String[]> msgData=db.GetMsgs(UserID);                  //Default product is last one
        CustomAdapter adapter=new CustomAdapter(Chat.this, msgData,"chat");
        //New messages are arrived. List is refreshed
        if(adapter.getCount()>loadedMsgCount)
        {
            msgList.setAdapter(adapter);
            msgList.setOnItemClickListener(null);
            msgList.setSelection(msgList.getCount() - 1);
            loadedMsgCount=msgList.getCount();

        }
        if(loadedMsgCount==0){
            msgList.setVisibility(View.GONE);
        }
        else {
            msgList.setVisibility(View.VISIBLE);
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
        if(userDetails!=null) userDetails.cancel(true);
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

    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    //---------------Menu creation---------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.chat_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.user_details:
                userDetails=new GetUserDetails().execute();
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    public class GetUserDetails extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView avLoadingIndicatorView;
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(Chat.this);
        LayoutInflater inflater = Chat.this.getLayoutInflater();
        View dialogView;
        String nameString, mobileString,emailString,DOBString,anniversaryString,loyaltyCardNoString,loyaltyPointsString;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialogView = inflater.inflate(R.layout.user_details, null);
            dialogBuilder.setView(dialogView)
                    .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                    .setIcon(R.drawable.user_who)
                    .show();
            avLoadingIndicatorView=(AVLoadingIndicatorView)dialogView.findViewById(R.id.details_loading);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/UserDetails";
            HttpURLConnection c = null;
            try {
                postData = "{\"userID\":\"" + extras.getString("UserID") + "\",\"boutiqueID\":\"" + db.GetUserDetail("BoutiqueID") + "\"}";
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
                    nameString =jsonObject.optString("Name");
                    mobileString =jsonObject.optString("Mobile");
                    emailString =jsonObject.optString("Email");
                    loyaltyCardNoString =jsonObject.optString("LoyaltyCardNo");
                    loyaltyPointsString=jsonObject.optString("LoyaltyPoints");
                    DOBString =jsonObject.optString("DOB","").replace("/Date(", "").replace(")/", "");
                    anniversaryString =jsonObject.optString("Anniversary","").replace("/Date(", "").replace(")/", "");
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
                new AlertDialog.Builder(Chat.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }).setCancelable(false).show();
            }
            else {
                TextView user_name=(TextView)dialogView.findViewById(R.id.userName);
                user_name.setText(nameString);
                TextView mobno=(TextView)dialogView.findViewById(R.id.mobile);
                mobno.setText(mobileString);
                mobno.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {                                   //Phone call function
                        Uri number = Uri.parse("tel:" + mobileString);
                        Intent callIntent = new Intent(Intent.ACTION_DIAL, number);
                        startActivity(callIntent);
                        overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                TextView email=(TextView)dialogView.findViewById(R.id.email);
                email.setText(emailString);
                email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{emailString});
//                        intent.setType("text/plain");
                        intent.setType("message/rfc822");
                        startActivity(Intent.createChooser(intent, "Send Email"));
                    }
                });
                TextView loyaltyCardNo=(TextView)dialogView.findViewById(R.id.loyaltyCardNo);
                loyaltyCardNo.setText(loyaltyCardNoString);
                TextView loyaltyPoints=(TextView)dialogView.findViewById(R.id.loyaltyPoints);
                if(loyaltyPointsString.equals("null")||loyaltyPointsString.equals("")) {
                    loyaltyPoints.setText("0");
                }
                else {
                    loyaltyPoints.setText(loyaltyPointsString);
                }
                TextView dob=(TextView)dialogView.findViewById(R.id.dob);
                TextView anniversary=(TextView)dialogView.findViewById(R.id.anniversory);
                TextView dobLabel=(TextView)dialogView.findViewById(R.id.textView6);
                TextView anniversaryLabel=(TextView)dialogView.findViewById(R.id.textView7);
                SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
                Calendar cal = Calendar.getInstance();
                if (!DOBString.equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(DOBString));
                    dob.setText(formatted.format(cal.getTime()));
                } else {
                    dob.setVisibility(View.GONE);
                    dobLabel.setVisibility(View.GONE);
                }
                if (!anniversaryString.equals("null")) {
                    cal.setTimeInMillis(Long.parseLong(anniversaryString));
                    anniversary.setText(formatted.format(cal.getTime()));
                } else {
                    anniversary.setVisibility(View.GONE);
                    anniversaryLabel.setVisibility(View.GONE);
                }
                RelativeLayout userDetails=(RelativeLayout)dialogView.findViewById(R.id.user_details);
                userDetails.setVisibility(View.VISIBLE);
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
        }
    }

}