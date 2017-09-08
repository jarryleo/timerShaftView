package cn.leo.testtimershaftview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements TimerShaftView.OnTimeChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        TimerShaftView timerShaftView = (TimerShaftView) findViewById(R.id.timer_shaft);
        timerShaftView.setOnTimeChangeListener(this);
    }

    @Override
    public void onTimeChange(String time) {
        //Toast.makeText(this, time, Toast.LENGTH_SHORT).show();
    }
}
