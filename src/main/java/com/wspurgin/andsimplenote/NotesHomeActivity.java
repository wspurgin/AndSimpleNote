package com.wspurgin.andsimplenote;

import android.app.Activity;

import android.app.ActionBar;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.ClientFactory;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.limits.Constants;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.thrift.transport.TTransportException;

import java.util.ArrayList;
import java.util.List;


public class NotesHomeActivity extends Activity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;

    private ArrayList<SimpleNote> mNotes;

    private EvernoteSession mEvernoteSession;

    AsyncNoteStoreClient mNoteStoreClient;

    private static final String LOGTAG = "ASN-NotesHome";

    private static final String APP_PREF = "AndSimpleNote-data";

    private static final String PREF_GUID = "noteBookGUID";

    private static final String NOTE_BOOK_NAME = "AndSimpleNoteBook";

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNotes = new ArrayList<SimpleNote>();
        final SharedPreferences preferences = getSharedPreferences(APP_PREF, Activity.MODE_PRIVATE);
        mEvernoteSession = EvernoteSession.getInstance(this, EvernoteConsts.getConsumerKey(),
                EvernoteConsts.getConsumerSecret(), EvernoteConsts.getEvernoteService());
        if (!mEvernoteSession.isLoggedIn()) {
            mEvernoteSession.authenticate(this);
        }
        // After data has been retrieved, set up activity
        setContentView(R.layout.activity_notes_home);
        Log.i(LOGTAG, "Content view has been set");
        ClientFactory clientFactory = mEvernoteSession.getClientFactory();
        try {
            mNoteStoreClient = clientFactory.createNoteStoreClient();
            final Notebook notebook = new Notebook();
            notebook.setName(NOTE_BOOK_NAME);
            if (!preferences.contains(PREF_GUID)) {
                Log.i(LOGTAG, "Notebook GUID not in preferences, attempting to create");
                mNoteStoreClient.createNotebook(notebook, new OnClientCallback<Notebook>() {
                    @Override
                    public void onSuccess(Notebook data) {
                        preferences.edit().putString(PREF_GUID, data.getGuid()).apply();
                        notebook.setGuid(data.getGuid());
                        Log.i(LOGTAG, notebook.toString());
                        setUpNavigation(notebook.getGuid());
                    }

                    @Override
                    public void onException(Exception exception) {
                        Log.i(LOGTAG, exception.getMessage(), exception);
                    }
                });
            } else {
                Log.i(LOGTAG, "Notebook GUID detected in preferences");
                setUpNavigation(preferences.getString(PREF_GUID, ""));
            }

        } catch (TTransportException e) {
            Log.i(LOGTAG, e.getMessage(), e);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getFragmentManager();
        Log.i(LOGTAG, String.valueOf(position));
        if (mNavigationDrawerFragment == null)
            Log.w(LOGTAG, "navigation fragment is null");
        SimpleNote note = mNavigationDrawerFragment.getNote(position);
        mTitle = note.getTitle();
        Log.i(LOGTAG, note.toString());
        fragmentManager.beginTransaction()
                .replace(R.id.container, SimpleNoteFragment.newInstance(note))
                .commit();
    }

    public void setUpNavigation(final String notebookGUID) {
        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);
        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                mNoteStoreClient, notebookGUID);
    }

    public void onSectionAttached(SimpleNote note) {
        mTitle = note.getTitle();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.notes_home, menu);
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            // Update UI when oauth activity returns result
            case EvernoteSession.REQUEST_CODE_OAUTH:
                if (resultCode == Activity.RESULT_OK) {
                    // Authentication was successful, do what you need to do in your app
                    Log.i(LOGTAG, "Authenticated with Evernote");
                }
                break;
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class SimpleNoteFragment extends Fragment {

        private SimpleNote mNote;

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static SimpleNoteFragment newInstance(SimpleNote note) {
            SimpleNoteFragment fragment = new SimpleNoteFragment();
            fragment.mNote = note;
            Bundle args = new Bundle();
            args.putSerializable("note", fragment.mNote);
            fragment.setArguments(args);
            return fragment;
        }

        public SimpleNoteFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_notes_home, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((NotesHomeActivity) activity).onSectionAttached( (SimpleNote)
                    getArguments().getSerializable("note"));
        }
    }

}
