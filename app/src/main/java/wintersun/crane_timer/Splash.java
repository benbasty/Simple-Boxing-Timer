package wintersun.crane_timer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class Splash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        Thread th = new Thread(){
            @Override
            public void run(){
                try {
                    sleep(1500);
                }	catch (Exception ex){
                    ex.printStackTrace();
                } finally {
                    Splash.this.startActivity(new Intent(Splash.this, MainActivity.class));
                    finish();
                }
            }
        };
        th.start();
    }
}

