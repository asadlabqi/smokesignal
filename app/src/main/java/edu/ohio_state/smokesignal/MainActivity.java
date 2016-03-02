package edu.ohio_state.smokesignal;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends AppCompatActivity {


    private byte[] encrypt(byte[] keyStream, byte[] plaintext) {
        int i;
        int mLength = plaintext.length;
        byte[] ciphertext = new byte[mLength];

        for (i = 0; i < mLength ; i++) {
            ciphertext[i] = (byte) (plaintext[i]^keyStream[i]);
        }

        return ciphertext;
    }

    private byte[] decrypt(byte[] keyStream, byte[] ciphertext) {
        int i;
        int cLength = ciphertext.length;
        byte[] plaintext = new byte[cLength];

        for (i = 0; i < cLength ; i++) {
            plaintext[i] = (byte) (ciphertext[i]^keyStream[i]);
        }

        return plaintext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
