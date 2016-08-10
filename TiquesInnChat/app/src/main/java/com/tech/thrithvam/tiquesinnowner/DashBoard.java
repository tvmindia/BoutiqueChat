package com.tech.thrithvam.tiquesinnowner;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

public class DashBoard extends AppCompatActivity {
DatabaseHandler db=new DatabaseHandler(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        getSupportActionBar().setElevation(0);
    }
    public void chat(View view){
        Intent chatIntent=new Intent(DashBoard.this,ChatList.class);
        startActivity(chatIntent);
    }
    public void charts(View view){
        Intent chartIntent=new Intent(DashBoard.this,Charts.class);
        chartIntent.putExtra("chart","trending");
        startActivity(chartIntent);
    }
    public void purchases(View view){
        Intent chartIntent=new Intent(DashBoard.this,Charts.class);
        chartIntent.putExtra("chart","purchases");
        startActivity(chartIntent);
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
                                Intent goHome = new Intent(DashBoard.this, Login.class);
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
    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.slide_exit1,R.anim.slide_exit2);
    }
}
