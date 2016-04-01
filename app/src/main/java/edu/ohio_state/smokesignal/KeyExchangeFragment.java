package edu.ohio_state.smokesignal;

import android.content.Context;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcEvent;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.io.File;
import java.security.SecureRandom;

import static android.nfc.NdefRecord.createMime;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link KeyExchangeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyExchangeFragment extends Fragment implements NfcAdapter.CreateNdefMessageCallback {

    NfcAdapter mNfcAdapter;
    private Context mContext;
    private TextView mTextView;
    public boolean mSharing;

    private static String LOGTAG = "KeyExchangeFragment";

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KeyExchangeFragment.
     */
    public static KeyExchangeFragment newInstance(Uri keyUri) {
        KeyExchangeFragment fragment = new KeyExchangeFragment();
        Bundle args = new Bundle();
        if(keyUri != null) {
            args.putString("uri", keyUri.getPath());
            Log.d(LOGTAG, "A key was shared at:" + args.getString("uri"));
        }
        fragment.setArguments(args);
        return fragment;
    }

    public KeyExchangeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_key_exchange, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        mTextView = (TextView) v.findViewById(R.id.key_exchange_text);

        // Get the key from the file specified in args.
        String keyPath = savedInstanceState.getString("uri");
        if (keyPath != null) {
            // TODO: Get file from the URI.
            // TODO: Get the byte array (i.e. the key) from the file.
            // TODO: Share the key with {@link createNdefMessage}.
        }

        final Button shareKeyButton = (Button) v.findViewById(R.id.keyExchangeButton);
        shareKeyButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSharing = true;
                //mTextView.setText(getActivity().toString());

                mNfcAdapter = NfcAdapter.getDefaultAdapter(mContext);
                if (mNfcAdapter == null) {
                    Log.d("Main Activity","NFC NOT AVAILABLE");
                    return;
                }
                // Register callback
                mNfcAdapter.setNdefPushMessageCallback(KeyExchangeFragment.this, getActivity());
                mTextView.setText("Sharing key via NFC");
            }
        });

    }

    //NFC METHODS
    @Override
    public NdefMessage createNdefMessage(NfcEvent event) {
        // TODO: Change text to the file contents of the URI in args.
        String text = "This should be a key!";

        NdefMessage msg = new NdefMessage(
                new NdefRecord[] { createMime(
                        "application/edu.ohio_state.smokesignal", text.getBytes())
                        /**
                         * The Android Application Record (AAR) is commented out. When a device
                         * receives a push with an AAR in it, the application specified in the AAR
                         * is guaranteed to run. The AAR overrides the tag dispatch system.
                         * You can add it back in to guarantee that this
                         * activity starts when receiving a beamed message. For now, this code
                         * uses the tag dispatch system.
                         */
                        //,NdefRecord.createApplicationRecord("com.example.android.beam")
                });
        return msg;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        Log.d("KeyExchangeFragment", "IN ON ATTACH");
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

}
