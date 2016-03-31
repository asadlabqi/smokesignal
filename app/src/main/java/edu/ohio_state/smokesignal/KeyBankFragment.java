package edu.ohio_state.smokesignal;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link KeyBankFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link KeyBankFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class KeyBankFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    List<String> fileList = new ArrayList<>();
    ArrayAdapter<String> arrayAdapter;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private TextView mTextView;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment KeyExchangeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static KeyBankFragment newInstance() {
        KeyBankFragment fragment = new KeyBankFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public KeyBankFragment() {
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
        return inflater.inflate(R.layout.fragment_key_bank, container, false);
    }

    @Override
    public void onViewCreated(View v, Bundle savedInstanceState) {
        ListView keyBank = (ListView) v.findViewById(R.id.key_bank_list);

        String path = getActivity().getFilesDir().getPath();
        File f = new File(path);
        File file[] = f.listFiles();
        for (int i=0; i < file.length; i++)
        {
            fileList.add(i, file[i].getName());
        }

        // Create the ArrayAdapter to work with the ListView.
        arrayAdapter = new ArrayAdapter<String>(
                getActivity().getApplicationContext(), R.layout.blacktestlist, fileList);
        keyBank.setAdapter(arrayAdapter);
        registerForContextMenu(keyBank);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.key_bank_list) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            menu.setHeaderTitle(fileList.get(info.position));
            String[] menuItems = getResources().getStringArray(R.array.menu);
            for (int i = 0; i<menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
        int selected = item.getItemId();
        String[] menuList = getResources().getStringArray(R.array.menu);
        String listItemName = fileList.get(info.position);

        if (selected == 0) {
            // The user selected Rename.
            String newName = "Test";
            rename(listItemName, newName);
        } else if (selected == 1) {
            // The user selected Send.

        } else {
            // The user selected Delete.
            delete(listItemName);
        }

        arrayAdapter.notifyDataSetChanged();

        return true;
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

    private boolean delete(String itemName) {
        File dir = getActivity().getFilesDir();
        File file = new File(dir, itemName);
        boolean deleted = file.delete();

        return deleted;
    }

    private void rename(String itemName, String newName) {
        File dir = getActivity().getFilesDir();
        File file = new File(dir, itemName);
        File newfile = new File(dir, newName);
        file.renameTo(newfile);
    }

}
