package com.tech.thrithvam.tiquesinnowner;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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

public class CustomAdapter extends BaseAdapter {
    Context adapterContext;
    private static LayoutInflater inflater = null;
    private ArrayList<String[]> objects;
    private String calledFrom;

    DatabaseHandler db;
    public CustomAdapter(Context context, ArrayList<String[]> objects, String calledFrom) {
        // super(context, textViewResourceId, objects);
        adapterContext = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.objects = objects;
        this.calledFrom = calledFrom;
        db=new DatabaseHandler(context);
    }

    public class Holder {
        //Chat------------------------------------
        TextView message, time;
        RelativeLayout msgBox;
        CardView productDetail;
        //Chat Headers----------------------------
        TextView name, date,lastMessage;
        //Products--------------------------------
        TextView pName, pNo, pPrice;
        ImageView pImage;
        AVLoadingIndicatorView loading;
    }

    @Override
    public int getCount() {
        return objects.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Holder holder;
        SimpleDateFormat formatted = new SimpleDateFormat("dd-MMM-yyyy", Locale.US);
        Calendar cal = Calendar.getInstance();
        switch (calledFrom) {
            //-------------------------------Chat Items----------------------------------------
            case "chat":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.message_item, null);
                    holder.message = (TextView) convertView.findViewById(R.id.msgItem);
                    holder.time = (TextView) convertView.findViewById(R.id.msgDate);
                    holder.msgBox = (RelativeLayout) convertView.findViewById(R.id.msgBox);
                    holder.productDetail=(CardView)convertView.findViewById(R.id.productDetail);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                if(objects.get(position)[0].equals("$$NewProduct$$")){
                    holder.productDetail.setVisibility(View.VISIBLE);
                    holder.msgBox.setVisibility(View.GONE);
                    new ProductDetailsForChat(objects.get(position)[3],convertView).execute();
                    break;
                }
                else {
                    holder.productDetail.setVisibility(View.GONE);
                    holder.msgBox.setVisibility(View.VISIBLE);
                    holder.message.setText(objects.get(position)[0]);
                if (!objects.get(position)[1].equals("null")) {
                    SimpleDateFormat formattedWithTime = new SimpleDateFormat("hh:mm a dd-MMM-yyyy", Locale.US);
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[1]));
                    holder.time.setText(formattedWithTime.format(cal.getTime()));
                }
                //Left Right positioning of message boxes-------
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                if (objects.get(position)[2].equals("in")) {
                    params.setMargins(100, 7, 7, 7);//(left, top, right, bottom)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.msgBox.setBackground(adapterContext.getDrawable(R.drawable.out_chat));
                    }
                    else {
                        holder.msgBox.setBackgroundDrawable(adapterContext.getResources().getDrawable(R.drawable.out_chat));
                    }
                } else if (objects.get(position)[2].equals("out")) {
                    params.setMargins(7, 7, 100, 7);//(left, top, right, bottom)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        holder.msgBox.setBackground(adapterContext.getDrawable(R.drawable.in_chat));
                    }
                    else {
                        holder.msgBox.setBackgroundDrawable(adapterContext.getResources().getDrawable(R.drawable.in_chat));
                    }
                }
                holder.msgBox.setLayoutParams(params);
                }
                break;
            //-------------------------------Chat Head Items----------------------------------------
            case "chatHeads":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.chat_header_item, null);
                    holder.name = (TextView) convertView.findViewById(R.id.name);
                    holder.date = (TextView) convertView.findViewById(R.id.date);
                    holder.lastMessage = (TextView) convertView.findViewById(R.id.lastMessage);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
                holder.name.setText(objects.get(position)[3]);
                if (!objects.get(position)[2].equals("null")) {
                    Calendar now=Calendar.getInstance();
                    cal.setTimeInMillis(Long.parseLong(objects.get(position)[2]));
                    SimpleDateFormat formattedWithTime;
                    if(now.get(Calendar.DATE)==cal.get(Calendar.DATE)
                            &&now.get(Calendar.MONTH)==cal.get(Calendar.MONTH)
                                && now.get(Calendar.YEAR)==cal.get(Calendar.YEAR)){
                        formattedWithTime=new SimpleDateFormat("hh:mm a", Locale.US);
                    }
                    else {
                        formattedWithTime = new SimpleDateFormat("dd-MMM", Locale.US);
                    }
                    holder.date.setText(formattedWithTime.format(cal.getTime()));
                }
                holder.lastMessage.setText(objects.get(position)[1]);
                final int FinalPos=position;
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent chatIntent=new Intent(adapterContext,Chat.class);
                        chatIntent.putExtra("UserID",objects.get(FinalPos)[0]);
                        chatIntent.putExtra("UserName",objects.get(FinalPos)[3]);
                        chatIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        adapterContext.startActivity(chatIntent);
                        ((Activity)adapterContext).finish();
                        ((Activity)adapterContext).overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
                    }
                });
                convertView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        new AlertDialog.Builder(adapterContext).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                                .setMessage(R.string.delete_chathead_q)
                                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        db.DeleteChatHeads(objects.get(FinalPos)[0]);
                                        Intent intent=new Intent(adapterContext,ChatList.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        adapterContext.startActivity(intent);
                                        ((Activity)adapterContext).finish();
                                    }
                                }).setNegativeButton(R.string.no, null).show();
                        return false;
                    }
                });

                if(objects.get(position)[4].equals("false")){
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        convertView.setBackgroundColor(adapterContext.getColor(R.color.primary_light));
                    }
                    else {
                        convertView.setBackgroundColor(adapterContext.getResources().getColor(R.color.primary_light));
                    }
                }
                else {
                        convertView.setBackgroundColor(0x00000000);
                }
                break;
            //-------------------------------Product Items----------------------------------------
            case "products":
                if (convertView == null) {
                    holder = new Holder();
                    convertView = inflater.inflate(R.layout.message_item, null);
                    holder.pName=(TextView)convertView.findViewById(R.id.productName);
                    holder.pNo=(TextView)convertView.findViewById(R.id.productNo);
                    holder.pPrice=(TextView)convertView.findViewById(R.id.productPrice);
                    holder.pImage=(ImageView)convertView.findViewById(R.id.productImg);
                    holder.msgBox = (RelativeLayout) convertView.findViewById(R.id.msgBox);
                    holder.loading=(AVLoadingIndicatorView)convertView.findViewById(R.id.prodDetLoading);
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                holder.msgBox.setVisibility(View.GONE);
                holder.loading.setVisibility(View.GONE);
                //Label loading--------------------
                holder.pName.setText(objects.get(position)[1]);
                holder.pNo.setText(objects.get(position)[3]);
                holder.pPrice.setText(objects.get(position)[4]);
                Picasso.with(adapterContext).load(objects.get(position)[2]).into(holder.pImage);

                break;
            default:
                break;
        }

        return convertView;
    }

    public class ProductDetailsForChat extends AsyncTask<Void , Void, Void> {
        int status;StringBuilder sb;
        String strJson, postData;
        JSONArray jsonArray;
        String msg;
        boolean pass=false;
        String productName,priceString,productImage;
        Integer productNoInt;
        String productID;
        View prodDetView;
        public ProductDetailsForChat(String productID,View convertView){
            this.productID=productID;
            this.prodDetView=convertView;
        }
        TextView pName;
        TextView pNo;
        TextView pPrice;
        ImageView pImage;
        AVLoadingIndicatorView avLoadingIndicatorView;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            avLoadingIndicatorView=(AVLoadingIndicatorView)prodDetView.findViewById(R.id.prodDetLoading);
            avLoadingIndicatorView.setVisibility(View.VISIBLE);
            pName=(TextView)prodDetView.findViewById(R.id.productName);
            pNo=(TextView)prodDetView.findViewById(R.id.productNo);
            pPrice=(TextView)prodDetView.findViewById(R.id.productPrice);
            pImage=(ImageView)prodDetView.findViewById(R.id.productImg);
            pName.setVisibility(View.INVISIBLE);
            pNo.setVisibility(View.INVISIBLE);
            pPrice.setVisibility(View.INVISIBLE);
            pImage.setVisibility(View.INVISIBLE);
            //----------encrypting ---------------------------
            // usernameString=cryptography.Encrypt(usernameString);
        }
        @Override
        protected Void doInBackground(Void... arg0) {
            String url =adapterContext.getResources().getString(R.string.url) + "WebServices/WebService.asmx/GetProductDetailsOnChat";
            HttpURLConnection c = null;
            try {
                postData = "{\"productID\":\"" + productID + "\",\"boutiqueID\":\"" + db.GetUserDetail("BoutiqueID") + "\"}";
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
                    productName=jsonObject.optString("Name");
                    priceString=String.format(Locale.US,"%.2f", jsonObject.optDouble("Price"));
                    productNoInt=jsonObject.optInt("ProductNo");
                    productImage=adapterContext.getResources().getString(R.string.url) + jsonObject.optString("Image").substring((jsonObject.optString("Image")).indexOf("Media"));
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
            }
            else {
                // viewProd.setVisibility(View.VISIBLE);

                pName.setVisibility(View.VISIBLE);
                pNo.setVisibility(View.VISIBLE);
                pPrice.setVisibility(View.VISIBLE);
                pImage.setVisibility(View.VISIBLE);


                pName.setText(productName);
                pNo.setText(adapterContext.getResources().getString(R.string.product_no, productNoInt));
                pPrice.setText(adapterContext.getResources().getString(R.string.rs, priceString));
                Picasso.with(adapterContext).load(productImage).into(pImage);
                avLoadingIndicatorView.setVisibility(View.GONE);
            }
        }
    }
}