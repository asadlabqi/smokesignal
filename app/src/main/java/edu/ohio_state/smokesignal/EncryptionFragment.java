package edu.ohio_state.smokesignal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.security.SecureRandom;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link EncryptionFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link EncryptionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EncryptionFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    byte[] keyStream = new byte[16];
    byte[] text;
    byte[] cText;

    //Array for converting byte array to printable format
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    SecureRandom mSecureRandom;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView mTextView;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment EncryptionFragment.
     */
    // TODO: Rename and change types and number of parameters
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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_encryption, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        final TextView PRNGView = (TextView) v.findViewById(R.id.prngView);
        final TextView plaintextView = (TextView) v.findViewById(R.id.plaintextView);
        final TextView ciphertextView = (TextView) v.findViewById(R.id.ciphertextView);
        final TextView decryptView = (TextView) v.findViewById(R.id.decryptView);
        final TextView messageView = (TextView) v.findViewById((R.id.inputView));

        final Button PRNGButton = (Button) v.findViewById(R.id.prngButton);
        PRNGButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mSecureRandom = new SecureRandom();
                mSecureRandom.nextBytes(keyStream);

                String prng = bytesToHex(keyStream);
                PRNGView.setText(prng);

                String filename = "KEY-" + Calendar.getInstance().get(Calendar.SECOND);
                FileOutputStream outputStream;
                Context cxt = getContext();

                try {
                    outputStream = cxt.openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(keyStream);
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final Button encryptButton = (Button) v.findViewById(R.id.encryptButton);
        encryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String plaintext = messageView.getText().toString();
                text = plaintext.getBytes();
                plaintextView.setText(bytesToHex(text));
                cText = encrypt(keyStream, text);
                ciphertextView.setText(bytesToHex(cText));
            }
        });

        final Button decryptButton = (Button) v.findViewById(R.id.decryptButton);
        decryptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                byte[] decrypted;
                decrypted = decrypt(keyStream, cText);
                decryptView.setText(bytesToHex(decrypted));
            }
        });


    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
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
        // TODO: Update argument type and name
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

}
