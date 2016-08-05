package com.tech.thrithvam.tiquesinnchat;

import android.animation.PropertyValuesHolder;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.db.chart.Tools;
import com.db.chart.listener.OnEntryClickListener;
import com.db.chart.model.Bar;
import com.db.chart.model.BarSet;
import com.db.chart.view.BarChartView;
import com.db.chart.view.Tooltip;
import com.db.chart.view.animation.Animation;
import com.db.chart.view.animation.easing.CircEase;

public class Charts extends AppCompatActivity {
    BarChartView chart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charts);
        chart=(BarChartView)findViewById(R.id.barchart);

        BarSet dataSet=new BarSet();
        dataSet.addBar(new Bar("first", 5));
        dataSet.addBar(new Bar("second",7));
        dataSet.addBar(new Bar("third", 4));
        dataSet.addBar(new Bar("forth", 3));
        dataSet.addBar(new Bar("fifth", 5));
        dataSet.addBar(new Bar("sixth",9));
        dataSet.addBar(new Bar("seventh", 2));
        dataSet.addBar(new Bar("eighth", 6));
        dataSet.addBar(new Bar("ninth", 4));
        dataSet.addBar(new Bar("tenth",6));
        dataSet.setColor(getResources().getColor(R.color.primary));
        chart.addData(dataSet);
        chart.setRoundCorners(3);

        Animation anim = new Animation(3000);
        anim.setEasing(new CircEase());
        chart.show(anim);

        chart.setOnEntryClickListener(new OnEntryClickListener() {
            @Override
            public void onClick(int setIndex, int entryIndex, Rect rect) {
                chart.dismissAllTooltips();
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
                tip.setDimensions((int) Tools.fromDpToPx(100), (int) Tools.fromDpToPx(100));
                tip.setMargins(0, 0, 0, (int) Tools.fromDpToPx(10));
                tip.prepare(chart.getEntriesArea(0).get(entryIndex), 0);
                chart.showTooltip(tip,true);
            }
        });
    }
}
