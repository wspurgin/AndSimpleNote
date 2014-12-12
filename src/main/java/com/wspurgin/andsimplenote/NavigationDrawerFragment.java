package com.wspurgin.andsimplenote;


import android.app.Activity;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.evernote.client.android.AsyncNoteStoreClient;
import com.evernote.client.android.EvernoteSession;
import com.evernote.client.android.OnClientCallback;
import com.evernote.edam.limits.Constants;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteMetadata;
import com.evernote.edam.notestore.NotesMetadataList;
import com.evernote.edam.notestore.NotesMetadataResultSpec;
import com.evernote.edam.type.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    private static final String DEFAULT_NOTE_NAME = "Notes";

    private static final String DEFAULT_NOTE_CONTENT = "My first SimpleNote!";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

    private static final String LOGTAG = "ASN-NavigationFrag";

    private AsyncNoteStoreClient mNoteStoreClient;
    private ArrayAdapter<SimpleNote> mAdapter;
    private String mNotebookGUID;

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private int mCurrentSelectedPosition = 0;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private EvernoteSession mEvernoteSession;

    public NavigationDrawerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        mAdapter = new ArrayAdapter<SimpleNote>(
                getActionBar().getThemedContext(),
                android.R.layout.simple_list_item_activated_1,
                android.R.id.text1,
                new ArrayList<SimpleNote>());
        mAdapter.setNotifyOnChange(true);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        // Select either the default item (0) or the last selected item.
//        selectItem(mCurrentSelectedPosition);
        mCurrentSelectedPosition = -1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mDrawerListView = (ListView) inflater.inflate(
                R.layout.fragment_navigation_drawer, container, false);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position);
            }
        });

        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        return mDrawerListView;
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param fragmentId   The android:id of this fragment in its activity's layout.
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(int fragmentId, DrawerLayout drawerLayout,
                      AsyncNoteStoreClient noteStoreClient, String notebookGUID) {
        Log.i(LOGTAG, "Setting up navigation fragment");
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;
        mNoteStoreClient = noteStoreClient;
        mEvernoteSession = EvernoteSession.getInstance(
                this.getActivity(),
                EvernoteConsts.getConsumerKey(),
                EvernoteConsts.getConsumerSecret(),
                EvernoteConsts.getEvernoteService()
                );

        mNotebookGUID = notebookGUID;

        // set list view
        mDrawerListView.setAdapter(mAdapter);

        this.retrieveData();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    // The user manually opened the drawer; store this flag to prevent auto-showing
                    // the navigation drawer automatically in the future.
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // If the user hasn't 'learned' about the drawer, open it to introduce them to the drawer,
        // per the navigation drawer design guidelines.
        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void selectItem(int position) {
        mCurrentSelectedPosition = position;
        if (mDrawerListView != null) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(position);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // If the drawer is open, show the global app actions in the action bar. See also
        // showGlobalContextActionBar, which controls the top-left area of the action bar.
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_add_note) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this.getActivity());

            alert.setTitle("Add Note");
            alert.setMessage("Enter the new note name");

            // Set an EditText view to get user input
            final EditText input = new EditText(this.getActivity());
            alert.setView(input);

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = input.getText().toString();
                    SimpleNote note = new SimpleNote(value, "SimpleNote: " + value);
                    mAdapter.add(note);
                    persistNoteWithEvernote(note);
                    selectItem(mAdapter.getCount() - 1); // select newly created item
                }
            });

            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                    dialog.cancel();
                }
            });

            alert.show();
            // see http://www.androidsnippets.com/prompt-user-input-with-an-alertdialog
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(R.string.app_name);
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(int position);
    }

    public void retrieveData() {
        if (mEvernoteSession.isLoggedIn()) {
            Log.i(LOGTAG, "Attempting to retrieve data");
            NoteFilter filter = new NoteFilter();
            filter.setNotebookGuid(mNotebookGUID);
            NotesMetadataResultSpec spec = new NotesMetadataResultSpec();
            spec.unsetIncludeContentLength();
            spec.unsetIncludeAttributes();
            spec.unsetIncludeLargestResourceMime();
            spec.unsetIncludeLargestResourceSize();
            spec.unsetIncludeNotebookGuid();
            spec.unsetIncludeTagGuids();
            spec.unsetIncludeUpdated();
            spec.unsetIncludeCreated();
            spec.unsetIncludeUpdateSequenceNum();
            mNoteStoreClient.findNotesMetadata(filter,
                    0,
                    Constants.EDAM_USER_NOTES_MAX,
                    spec,
                    new OnClientCallback<NotesMetadataList>() {
                        @Override
                        public void onSuccess(NotesMetadataList data) {
                            Log.i(LOGTAG, "Notes retrieved from Evernote");
                            if (data.getNotesSize() > 0) {
                                Log.i(LOGTAG, String.format("Retrieved %d notes", data.getNotesSize()));
                                List<NoteMetadata> notes = data.getNotes();
                                for (NoteMetadata note : notes) {
                                    Log.i(LOGTAG, note.toString());
                                    // make call for content
                                    mNoteStoreClient.getNote(note.getGuid(), true, false, false, false, new OnClientCallback<Note>() {
                                        @Override
                                        public void onSuccess(Note data) {
                                            mAdapter.add(NoteConverter.toSimpleNote(data));
                                            if (mCurrentSelectedPosition == -1) {
                                                // select the first item (by default)
                                                selectItem(0);
                                            }
                                        }

                                        @Override
                                        public void onException(Exception exception) {
                                            Log.i(LOGTAG, exception.getMessage(), exception);
                                        }
                                    });
                                }
                            } else {
                                Log.i(LOGTAG, "Adding default note");
                                // Add the first note and have it created in the background.
                                SimpleNote note = new SimpleNote(DEFAULT_NOTE_NAME, DEFAULT_NOTE_CONTENT);
                                mAdapter.add(note);
                                persistNoteWithEvernote(note);

                                // select the first item (by default)
                                selectItem(0);
                            }
                        }

                        @Override
                        public void onException(Exception exception) {
                            Log.i(LOGTAG, exception.getMessage(), exception);
                        }
                    });
        } else {
            Log.w(LOGTAG, "User is note authenticated");
        }
    }

    public SimpleNote getNote(int position) {
        Log.i(LOGTAG, String.valueOf(position));
        return mAdapter.getItem(position);
    }

    private void persistNoteWithEvernote(SimpleNote note) {
        // send the note to Evernote
        Note everNote = NoteConverter.toEverNote(note);
        everNote.setNotebookGuid(mNotebookGUID);
        mNoteStoreClient.createNote(everNote, new OnClientCallback<Note>() {
            @Override
            public void onSuccess(Note data) {
                Log.i(LOGTAG, "created first note");
            }

            @Override
            public void onException(Exception exception) {
                Log.i(LOGTAG, exception.getMessage(), exception);
            }
        });
    }
}
