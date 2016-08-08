package com.tech.thrithvam.tiquesinnowner;

import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.model.ChartEntry;
import com.db.chart.view.BarChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CircEase;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Charts extends AppCompatActivity {
    DatabaseHandler db=new DatabaseHandler(this);
    BarChartView chart;
    AsyncTask getTrends;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);

        if (isOnline()) {
                getTrends=new GetProductsByCategory().execute();
        } else {
            Toast.makeText(Charts.this, R.string.network_off_alert, Toast.LENGTH_LONG).show();
            finish();
        }


        chart=(BarChartView)findViewById(R.id.barchart);



      /*  dataSet.addBar(new Bar("second",7));
        dataSet.addBar(new Bar("third", 4));
        dataSet.addBar(new Bar("forth", 3));
        dataSet.addBar(new Bar("fifth", 5));
        dataSet.addBar(new Bar("sixth",9));
        dataSet.addBar(new Bar("seventh", 2));
        dataSet.addBar(new Bar("eighth", 6));
        dataSet.addBar(new Bar("ninth", 4));
        dataSet.addBar(new Bar("tenth",6));*/

    }
    public boolean isOnline() {
        ConnectivityManager cm =(ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }
    @Override
    public void onBackPressed() {
        finish();
        if(getTrends!=null) getTrends.cancel(true);
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
    public class GetProductsByCategory extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        AVLoadingIndicatorView pDialog=(AVLoadingIndicatorView)findViewById(R.id.itemsLoading);
        ArrayList<String[]> productItems=new ArrayList<>();

        BarSet dataSet=new BarSet();
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog.setVisibility(View.VISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            String url =getResources().getString(R.string.url) + "WebServices/WebService.asmx/ProductsByCategory";
            HttpURLConnection c = null;
            try {
                postData =  "{\"CategoryCode\":\"" + "trends" + "\",\"boutiqueID\":\"" +  db.GetUserDetail("BoutiqueID") + "\",\"userID\":\"" + (db.GetUserDetail("UserID")==null?"":db.GetUserDetail("UserID"))+ "\",\"limit\":\"" + "10" + "\"}";
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
                    String[] data=new String[5];
                    data[0]=jsonObject.optString("ProductID");
                    data[1]=jsonObject.optString("Name");
                    data[2]=getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
                    data[3]=jsonObject.optString("ProductNo");
                    data[4]=jsonObject.optString("ProductCounts","null");
                    productItems.add(data);
                    dataSet.addBar(new Bar(data[3], Float.parseFloat(data[4])));
                }
            } catch (Exception ex) {
                msg=ex.getMessage();
            }}
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            pDialog.setVisibility(View.GONE);
            if(!pass) {
                new AlertDialog.Builder(Charts.this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle("")
                        .setMessage(R.string.no_items)
                        .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setCancelable(false).show();
            }
            else {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    dataSet.setColor(getColor(R.color.primary));
                }
                else {
                    dataSet.setColor(getResources().getColor(R.color.primary));
                }
                chart.addData(dataSet);
                chart.setRoundCorners(3);


                //--------Creating Y axis labels-------------------
                int max=Integer.parseInt(productItems.get(0)[4]);

                int next;
                int step;
                int stepMultiplierMultiplier=1;
                int[] stepMultiplier={2,5,10};
                do{
                    step=stepMultiplier[0]*stepMultiplierMultiplier;
                    if(max/step<10)
                            break;
                    step=stepMultiplier[1]*stepMultiplierMultiplier;
                    if(max/step<10)
                            break;
                    step=stepMultiplier[2]*stepMultiplierMultiplier;
                    if(max/step<10)
                            break;

                    stepMultiplierMultiplier *= 10;
                }while (true);

                next=max+(step-max%step);
                chart.setAxisBorderValues(0,next,step);
                //----------------------------------------------------

                Animation anim = new Animation(3000);
                anim.setEasing(new CircEase());
                chart.show(anim);

                //-----------Tooltip on entry click---------------------
                chart.setOnEntryClickListener(new OnEntryClickListener() {
                    @Override
                    public void onClick(int setIndex, int entryIndex, Rect rect) {
                        chart.dismissAllTooltips();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dataSet.setColor(getColor(R.color.primary));
                        }
                        else {
                            dataSet.setColor(getResources().getColor(R.color.primary));
                        }
                        ChartEntry bar=dataSet.getEntry(entryIndex);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            bar.setColor(getColor(R.color.primary_light));
                        }
                        else {
                            bar.setColor(getResources().getColor(R.color.primary_light));
                        }

                        Tooltip tip=new Tooltip(Charts.this,R.layout.graph_tips);
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {

                            tip.setEnterAnimation(PropertyValuesHolder.ofFloat(View.ALPHA, 1),
                                    PropertyValuesHolder.ofFloat(View.SCALE_X, 1f),
                                    PropertyValuesHolder.ofFloat(View.SCALE_Y, 1f)).setDuration(400);

                            tip.setExitAnimation(PropertyValuesHolder.ofFloat(View.ALPHA,0),
                                    PropertyValuesHolder.ofFloat(View.SCALE_X,0f),
                                    PropertyValuesHolder.ofFloat(View.SCALE_Y,0f)).setDuration(400);
                        }
                        tip.setVerticalAlignment(Tooltip.Alignment.BOTTOM_TOP);
                        tip.setDimensions((int) Tools.fromDpToPx(200), (int) Tools.fromDpToPx(120));
                       // tip.setMargins(0, 0, 0, (int) Tools.fromDpToPx(10));
                        tip.prepare(chart.getEntriesArea(0).get(entryIndex), 0);

                        //------Product Details---------
                        TextView pName=(TextView)tip.findViewById(R.id.productName);
                        pName.setText(productItems.get(entryIndex)[1]);
                        TextView pNo=(TextView)tip.findViewById(R.id.productNo);
                        pNo.setText(getResources().getString(R.string.p_no,productItems.get(entryIndex)[3]));
                        TextView pCount=(TextView)tip.findViewById(R.id.productCount);
                        pCount.setText(getResources().getString(R.string.count,productItems.get(entryIndex)[4]));
                        ImageView pImg=(ImageView)tip.findViewById(R.id.productImg);
                        Picasso.with(Charts.this).load(productItems.get(entryIndex)[2]).into(pImg);
                        //--------------------------------
                        chart.showTooltip(tip,true);
                    }
                });
                //-----------Cancel tooltip-------------------------------
                chart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        chart.dismissAllTooltips();
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            dataSet.setColor(getColor(R.color.primary));
                        }
                        else {
                            dataSet.setColor(getResources().getColor(R.color.primary));
                        }
                    }
                });
                //--------------------------------------------------------------------------
            }
        }
    }
}
