package com.tech.thrithvam.tiquesinnchat;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ChatList extends AppCompatActivity {
DatabaseHandler db=new DatabaseHandler(ChatList.this);
    Long lastMsgTime=0L;
    ListView chatHeadsList;
    TextView no_items;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);
        no_items=(TextView)findViewById(R.id.loadingText);
        chatHeadsList = (ListView) findViewById(R.id.chatHeadListView);
        if(db.GetUserDetail("UserID")==null){
            Toast.makeText(ChatList.this,R.string.please_login,Toast.LENGTH_LONG).show();
            Intent intentUser = new Intent(ChatList.this, Login.class);
            startActivity(intentUser);
            finish();
            overridePendingTransition(R.anim.slide_entry1,R.anim.slide_entry2);
        }
        loadChatHeads();
    }
    public void loadChatHeads()
    {
        ArrayList<String[]> chatHeadData=db.GetChatHeads();
        if(chatHeadData.size()==0){
            chatHeadsList.setVisibility(View.GONE);
            no_items.setVisibility(View.VISIBLE);
        }
        else {
            chatHeadsList.setVisibility(View.VISIBLE);
            no_items.setVisibility(View.GONE);
            if(Long.parseLong(chatHeadData.get(0)[2])>lastMsgTime) {
                lastMsgTime=Long.parseLong(chatHeadData.get(0)[2]);
                CustomAdapter adapter = new CustomAdapter(ChatList.this, chatHeadData, "chatHeads");
                chatHeadsList.setAdapter(adapter);
            }
        }
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                loadChatHeads();
            }
        },1000);
    }
    //---------------Menu creation---------------------------------------------
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.logout:
                new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)//.setTitle(R.string.exit)
                        .setMessage(R.string.logout_q)
                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                db.UserLogout();
                                Intent goHome = new Intent(ChatList.this, Login.class);
                                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                startActivity(goHome);
                                finish();
                                overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
                            }
                        }).setNegativeButton(R.string.no, null).show();
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
