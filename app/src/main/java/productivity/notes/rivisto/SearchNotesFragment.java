package productivity.notes.rivisto;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import productivity.notes.rivisto.utils.Helpers;

public class SearchNotesFragment extends Fragment implements TextView.OnEditorActionListener, View.OnClickListener,
        RecyclerViewAdapter.ViewHolder.ClickListener {
    private EditText noteSearchQuery;
    private TextView moreTags;
    private RelativeLayout relativeLayoutSearchResults;
    private String userKey;
    private RecyclerView recyclerView, recyclerViewSearch;
    private FirebaseRecyclerAdapter<Tag, TagHolder> adapter;
    private DatabaseReference firebaseRef, firebaseNotesRef;
    private ArrayList<Note> notes;
    private static final String LOG_SEARCH = "SearchEvent";
    private static final String LOG_TAG_CLICK = "TagClick";
    private static final String LOG_NOTE_CLICK = "NoteClick";

    public SearchNotesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_notes, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        recyclerViewSearch = (RecyclerView) view.findViewById(R.id.recycler_view_search_results);
        noteSearchQuery = (EditText) getActivity().findViewById(R.id.noteSearchQuery);
        moreTags = (TextView) view.findViewById(R.id.moreTags);
        relativeLayoutSearchResults = (RelativeLayout) view.findViewById(R.id.relativeLayoutSearchResults);

        SharedPreferences sharedPref = getActivity().getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
        userKey = sharedPref.getString(getString(R.string.userKey), null);

        if (userKey == null) {
            FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
            firebaseRef = firebaseDatabase.getReference("/tags");
            firebaseNotesRef = firebaseDatabase.getReference("/notes");
        } else {
            FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
            firebaseRef = firebaseDatabase.getReference(userKey + "/tags");
            firebaseNotesRef = firebaseDatabase.getReference(userKey + "/notes");
        }

        firebaseRef.keepSynced(true);

        new getTags().execute();

        if (noteSearchQuery.getVisibility() == View.GONE) {
            noteSearchQuery.setVisibility(View.VISIBLE);
        }

        noteSearchQuery.setOnEditorActionListener(this);

        moreTags.setOnClickListener(this);

        recyclerViewSearch.setItemAnimator(new DefaultItemAnimator());
        recyclerViewSearch.setHasFixedSize(true);
        recyclerViewSearch.setLayoutManager(new LinearLayoutManager(getActivity()));

        return view;
    }

    private class getTags extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 4));
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            adapter = new FirebaseRecyclerAdapter<Tag, TagHolder>(Tag.class, R.layout.tag, TagHolder.class,
                    firebaseRef.orderByChild("noteCount").limitToLast(8)) {
                @Override
                public void populateViewHolder(TagHolder tagHolder, Tag tag, final int position) {
                    tagHolder.setTagName('#' + adapter.getRef(position).getKey());

                    tagHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //Log.i(LOG_TAG_CLICK, adapter.getRef(position).getKey());
                            openTagNotesFragment(userKey, adapter.getRef(position).getKey());
                        }
                    });
                }
            };

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            recyclerView.setAdapter(adapter);
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

        if (i == EditorInfo.IME_ACTION_SEARCH) {
            performSearch();
        }

        return false;
    }

    private void performSearch() {
        Helpers.hideKeyboard(getActivity(), getView());
        String searchQuery = noteSearchQuery.getText().toString().trim().toLowerCase();
        //Log.i(LOG_SEARCH, searchQuery);
        new searchNotes().execute(searchQuery);
    }

    private class searchNotes extends AsyncTask<String, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            notes = new ArrayList<>();
            if (relativeLayoutSearchResults.getVisibility() == View.INVISIBLE) {
                relativeLayoutSearchResults.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected Boolean doInBackground(String... query) {

            final String searchQuery = query[0];

            firebaseNotesRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Note note = dataSnapshot.getValue(Note.class);
                    if (note.getTitle().toLowerCase().contains(searchQuery) || note.getContent().toLowerCase().contains(searchQuery)) {
                        note.setNoteKey(dataSnapshot.getKey());
                        notes.add(note);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            recyclerViewSearch.setAdapter(new RecyclerViewAdapter(notes, SearchNotesFragment.this));
        }
    }

    @Override
    public void onItemClicked(String noteKey) {
        //Log.i(LOG_NOTE_CLICK, "Clicked " + noteKey);
        openNoteActivity(noteKey, "notes", userKey);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.moreTags) {
            noteSearchQuery.setVisibility(View.GONE);
            Helpers.hideKeyboard(getActivity(), getView());
            openTagsFragment(userKey);
        }
    }

    private void openTagsFragment(String userKey) {
        TagsFragment tagsFragment = new TagsFragment();

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.userKey), userKey);

        tagsFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, tagsFragment)
                .addToBackStack("SearchNotesFragment")
                .commit();
    }

    private void openTagNotesFragment(String userKey, String tagName) {
        noteSearchQuery.setVisibility(View.GONE);
        Helpers.hideKeyboard(getActivity(), getView());

        TagNotesFragment newFragment = new TagNotesFragment();
        Bundle args = new Bundle();
        args.putString(getString(R.string.userKey), userKey);
        args.putString("tagName", tagName);
        newFragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack("TagNotesFragment")
                .commit();
    }

    private void openNoteActivity(String key, String lookupType, String userKey){
        Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
        openNoteIntent.putExtra("key", key);
        openNoteIntent.putExtra("lookup", lookupType);
        openNoteIntent.putExtra(getString(R.string.userKey), userKey);
        startActivity(openNoteIntent);
    }
}
