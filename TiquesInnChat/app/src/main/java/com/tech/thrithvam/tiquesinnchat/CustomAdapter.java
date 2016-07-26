package com.tech.thrithvam.tiquesinnchat;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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
        //Chat Headers----------------------------
        TextView name, date,lastMessage;
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
                    convertView.setTag(holder);
                } else {
                    holder = (Holder) convertView.getTag();
                }
                //Label loading--------------------
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
            default:
                break;
        }

        return convertView;
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) adapterContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}