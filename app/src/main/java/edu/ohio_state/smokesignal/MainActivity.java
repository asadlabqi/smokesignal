package edu.ohio_state.smokesignal;

import android.content.Intent;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks,
                    KeyBankFragment.OnKeySharedListener
 {

     private String TAG = "Main Activity";
     private NavigationDrawerFragment mNavigationDrawerFragment;
     private CharSequence mTitle;

    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        /*
        // Check for available NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (mNfcAdapter == null) {
            Log.d("Main Activity","NFC NOT AVAILABLE");
            return;
        }
        // Register callback
        mNfcAdapter.setNdefPushMessageCallback(this, this);

        Log.d("MainActivity", "nfc adapter success!!");
        */

    }

     //NAVIGATION DRAWER
     @Override
     public void onNavigationDrawerItemSelected(int position) {
         // update the main content by replacing fragments
         FragmentManager fragmentManager = getSupportFragmentManager();

         Log.d(TAG, "Position: " + position);

         Fragment fragment;
         if (position == 0) {
             fragment = EncryptionFragment.newInstance();
         }else if(position == 1){
             fragment = KeyExchangeFragment.newInstance(null);
         }else if(position == 2){
             fragment = MessagesFragment.newInstance();
         }else if(position == 3){
             fragment = KeyBankFragment.newInstance(null);
         } else {
             fragment = SettingsFragment.newInstance();
         }

         fragmentManager.beginTransaction()
                 .replace(R.id.container, fragment).commit();

     }

     public void onSectionAttached(int number) {
         switch (number) {
             case 1:
                 mTitle = getString(R.string.encryption);
                 break;
             case 2:
                 mTitle = getString(R.string.share_key);
                 break;
             case 3:
                 mTitle = getString(R.string.messages);
                 break;
             case 4:
                 mTitle = getString(R.string.key_bank);
                 break;
             case 5:
                 mTitle = getString(R.string.settings);
                 break;
         }
     }

     public void restoreActionBar() {
         ActionBar actionBar = getSupportActionBar();
         actionBar.setDisplayShowTitleEnabled(true);
         actionBar.setTitle(mTitle);
     }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
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

     @Override
     public void onNewIntent(Intent intent) {
         // onResume gets called after this to handle the intent
         setIntent(intent);
     }

     /**
      * Parses the NDEF Message from the intent and shows it on a Toast. The Activity then reloads
      * the KeyBank so that the user can see their new key.
      */
     void processIntent(Intent intent) {
         Log.d(TAG, "NDEF Intent is being processed.");
         mTextView = (TextView) findViewById(R.id.key_exchange_text);
         Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                 NfcAdapter.EXTRA_NDEF_MESSAGES);
         // only one message sent during the beam
         NdefMessage msg = (NdefMessage) rawMsgs[0];
         // record 0 contains the MIME type, record 1 is the AAR, if present
         byte[] messageText = msg.getRecords()[0].getPayload();
         Toast.makeText(MainActivity.this, Arrays.toString(messageText), Toast.LENGTH_SHORT).show();

         FragmentManager fragmentManager = getSupportFragmentManager();
         Fragment fragment = KeyBankFragment.newInstance(messageText);
         fragmentManager.beginTransaction()
                 .replace(R.id.container, fragment).commit();
     }

     @Override
     public void onResume() {
         super.onResume();
         // Check to see that the Activity started due to an Android Beam
         if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
             processIntent(getIntent());
         }
     }

     // Called when the user selects a key to be shared in the Key Bank Fragment.
     @Override
     public void OnKeyShared(Uri uri) {
         Log.d(TAG, "Key received from Key Bank. URI = " + uri.toString());
         FragmentManager fragmentManager = getSupportFragmentManager();
         Fragment fragment = KeyExchangeFragment.newInstance(uri);
         fragmentManager.beginTransaction()
                 .replace(R.id.container, fragment).commit();
     }
}

