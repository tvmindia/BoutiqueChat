package com.tech.thrithvam.tiquesinnchat;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class Login extends AppCompatActivity {
    DatabaseHandler db=new DatabaseHandler(Login.this);
    EditText userName;
    EditText password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(db.GetUserDetail("UserID")!=null)
        {
            Intent goHome = new Intent(Login.this, DashBoard.class);
            goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(goHome);
            finish();
        }
        setContentView(R.layout.activity_login);

        userName=(EditText)findViewById(R.id.userName);
        password=(EditText)findViewById(R.id.password);
    }
    public void loginButton(View view) {
        if(isOnline()) {
            userName.setText(userName.getText().toString().trim());
            password.setText(password.getText().toString().trim());
            if (userName.getText().toString().equals("")) {
                userName.setError(getResources().getString(R.string.username_error_msg));
                userName.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    public void afterTextChanged(Editable edt) {
                        userName.setError(null);
                    }
                });
            } else if (password.getText().toString().equals("")) {
                password.setError(getResources().getString(R.string.password_error_msg));
            } else {
                new UserLogin().execute();
            }
        }
        else {
            Toast.makeText(Login.this,R.string.network_off_alert, Toast.LENGTH_LONG).show();
        }
    }
    public class UserLogin extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData, passwordString,usernameString,UserID,BoutiqueID,BoutiqueName;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        ProgressDialog pDialog=new ProgressDialog(Login.this);
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            usernameString=userName.getText().toString();
            passwordString=password.getText().toString();
            pDialog.setMessage(getResources().getString(R.string.wait));
            pDialog.setCancelable(false);
            pDialog.show();
            //----------encrypting ---------------------------
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ChatAppUserLogin";
            HttpURLConnection c = null;
            try {
                postData = "{\"username\":\"" +usernameString + "\",\"password\":\"" + passwordString + "\"}";
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
                        strJson="{\"JSON\":" + strJson.replace("\\\"","\"").replace("\\\\","\\") + "}";
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            } finally {
                if (c != null) {
                    try {
                        c.disconnect();
                    } catch (Exception ex) {
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
                    pass=jsonObject.optBoolean("Flag");
                    UserID=jsonObject.optString("UserID");
                    BoutiqueID=jsonObject.optString("BoutiqueID");
                    BoutiqueName=jsonObject.optString("BoutiqueName");
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
                new AlertDialog.Builder(Login.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(msg)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                password.setText("");
                            }
                        }).setCancelable(false).show();
            }
            else {
                db.UserLogout();
                db.UserLogin(userName.getText().toString(), UserID,BoutiqueID,BoutiqueName);
                Intent loginIntent = new Intent(Login.this, DashBoard.class);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                loginIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                Toast.makeText(Login.this,msg,Toast.LENGTH_LONG).show();
                startActivity(loginIntent);
                overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                finish();
            }
        }
    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    //-------------fn to be deleted------------
    public void backdoor(View view) {
//        userName.setText("Neima");
//        password.setText("1234");
        userName.setText("sree");
        password.setText("sree");
        new UserLogin().execute();
    }
}

