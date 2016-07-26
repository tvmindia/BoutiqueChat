package com.tech.thrithvam.tiquesinnchat;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class DashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
    }
    public void chat(View view){
        Intent chatIntent=new Intent(DashBoard.this,ChatList.class);
        startActivity(chatIntent);
    }
}
