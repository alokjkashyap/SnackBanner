package dev.alox.snackbanner;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SnackBanner.make(findViewById(R.id.mainView),"success",SnackBanner.LENGTH_SHORT).setBackgroundColor(R.color.successGreen).show();
            }
        });
    }
}
