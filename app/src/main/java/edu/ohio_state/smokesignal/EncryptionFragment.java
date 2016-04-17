package edu.ohio_state.smokesignal;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EncryptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EncryptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EncryptionFragment extends Fragment {
    private static String LOGTAG = "EncryptionFragment";

    String keyFile;
    byte[] keyStream;

    private List<String> fileList = new ArrayList<>();

    //Array for converting byte array to printable format
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EncryptionFragment.
     */
    public static EncryptionFragment newInstance() {
        EncryptionFragment fragment = new EncryptionFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public EncryptionFragment() {
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
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        final TextView inputView = (TextView) v.findViewById(R.id.inputView);
        inputView.setHint("Text Message");
        final TextView decryptView = (TextView) v.findViewById(R.id.decryptView);
        decryptView.setHint("Encrypted Text");
        final TextView phoneView = (TextView) v.findViewById(R.id.phoneWindow);
        phoneView.setHint("Phone Number");

        // Get all the keys from the Key Bank.
        String path = getActivity().getFilesDir().getPath();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++)
        {
            fileList.add(i, file[i].getName());
        }

        // Logic handling for the key selection dropdown.
        Spinner dropdown = (Spinner) v.findViewById(R.id.key_bank_dropdown);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, fileList);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                keyFile = fileList.get(position);
                String line = "";

                try {
                    FileInputStream fis = getContext().openFileInput(keyFile);
                    InputStreamReader isr = new InputStreamReader(fis);
                    BufferedReader bufferedReader = new BufferedReader(isr);
                    StringBuilder sb = new StringBuilder();

                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                    }
                    line = sb.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                keyStream = line.getBytes();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing!

            }
        });

        // Encrypt the text in the EditText window.
        final Button sendButton = (Button) v.findViewById(R.id.sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String plaintext = inputView.getText().toString();
                Log.d(LOGTAG, "Message was: " + plaintext);
                String phoneNo = phoneView.getText().toString();
                Log.d(LOGTAG, "Phone number was: " + phoneNo);
                byte[] text = plaintext.getBytes();

                // This assumes that the key is destroyed as parts of it are used.
                if(text.length <= keyStream.length) {
                    byte[] cText = encrypt(keyStream, text);
                    Log.d(LOGTAG, "Encrypted Message was: " + bytesToHex(cText));

                    SmsManager smsManager = SmsManager.getDefault();
                    smsManager.sendTextMessage(phoneNo, null, bytesToHex(cText), null, null);

                    Log.d(LOGTAG, "Message of length " + text.length + " bytes sent.");

                    // If we've gotten this far without error, than a message has been sent. Destroy
                    // the part of the key we used so that it will never be used again.
                    shrinkKeyStream(text);

                } else {
                    Toast.makeText(getActivity(), "Out of key. Message must be " + keyStream.length + " characters long.", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Decrypt the text in the EditText window.
        final Button decryptButton = (Button) v.findViewById(R.id.decryptButton);
        decryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] decrypted;
                String hexMsg = decryptView.getText().toString();
                byte[] cText = hexStringToByteArray(hexMsg);

                if(keyStream.length>=cText.length) {
                    Log.d(LOGTAG, "Encrypted Message Received: " + hexMsg);
                    decrypted = decrypt(keyStream, cText);
                    Log.d(LOGTAG, "Decrypted Hex was: " + bytesToHex(decrypted));
                    String output = hexToASCII(bytesToHex(decrypted));
                    Log.d(LOGTAG, "Message decrypted to: " + output);
                    decryptView.setText(output);

                    // Destroy the part of the key we used so that it will never be used again.
                    shrinkKeyStream(decrypted);
                }else{
                    Toast.makeText(getActivity(), "Out of key. Must exchange new key", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;
        ((MainActivity) activity).onSectionAttached(1);
        Log.d(LOGTAG, "IN ON ATTACH");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

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

    //Converts byte array to a string of hex digits while keeping leading zeroes
    //Source: http://stackoverflow.com/questions/9655181/how-to-convert-a-byte-array-to-a-hex-string-in-java
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    // Converts a Hex string into a byte array.
    // Source: http://stackoverflow.com/questions/140131/convert-a-string-representation-of-a-hex-dump-to-a-byte-array-using-java
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    // Converts a string of hex digits into printable ASCII.
    public static String hexToASCII(String hexString) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hexString.length(); i+=2) {
            String str = hexString.substring(i, i+2);
            sb.append((char)Integer.parseInt(str, 16));
        }
        return sb.toString();
    }

    private void shrinkKeyStream(byte[] message) {
        FileOutputStream outputStream;
        Context cxt = getContext();
        //File dir = getActivity().getFilesDir();
        //File file = new File(dir, keyFile);

        //boolean deleted = file.delete();
        //Log.d(LOGTAG, "Result of deletion of " + keyFile + " was: " + deleted);

        byte[] newKeyStream = new byte[keyStream.length - message.length];
        for(int i = 0 ; i < newKeyStream.length ; i++) {
            newKeyStream[i] = keyStream[i + message.length];
        }
        Log.d(LOGTAG,"Keystream Length " + keyStream.length);

        Log.d(LOGTAG,"OLD KEYSTREAM" + keyStream);
        Log.d(LOGTAG,"New KEYSTREAM" + newKeyStream);

        //keyStream = Arrays.copyOf(newKeyStream,newKeyStream.length);
        keyStream = newKeyStream;
        Log.d(LOGTAG, "Keystream Length " + keyStream.length);
        //File newFile = new File(keyFile);
        try {
            outputStream = cxt.openFileOutput(keyFile, 0);
            outputStream.write(newKeyStream);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.d(LOGTAG, "Key Stream reduced by " + (message.length) + " characters.");
    }

}
