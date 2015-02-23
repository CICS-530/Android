package ca.ubc.icics.mss.cisc530;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
    private String LOG_TAG = "MainActivityLogTag";

    private Button Btn_MapView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Btn_MapView = (Button) findViewById(R.id.Btn_MapView);
        Btn_MapView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(LOG_TAG, "ELI:Button->Map View");

                Toast.makeText(getApplicationContext(), R.string.loading_maps, Toast.LENGTH_LONG).show();

                Intent intent = new Intent();
                intent.setClass(MainActivity.this, MapsActivity.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Log.d(LOG_TAG, "ELI:Menu->Settings");
            Toast.makeText(getApplicationContext(), R.string.action_settings, Toast.LENGTH_LONG).show();
            return true;
        } else if (id == R.id.action_quit) {
            Log.d(LOG_TAG, "ELI:Menu->Quit");
            Toast.makeText(getApplicationContext(), R.string.action_quit, Toast.LENGTH_LONG).show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
