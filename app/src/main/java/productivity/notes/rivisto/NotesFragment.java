package productivity.notes.rivisto;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import productivity.notes.rivisto.utils.Helpers;

public class NotesFragment extends Fragment {
    private DatabaseReference firebaseRef;
    private FirebaseRecyclerAdapter<Note, NoteHolder> adapter;
    private RecyclerView recyclerView;
    private String userKey;
    private ViewGroup viewGroup;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);
        viewGroup = container;

        ((MainActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.all_notes));

        setHasOptionsMenu(true);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        final Bundle bundle = this.getArguments();
        userKey = bundle.getString(getString(R.string.userKey));

        if (bundle.getBoolean(getString(R.string.isAccountHolder), false)){
            firebaseRef = ((MainActivity)getActivity()).getFirebaseDatabase()
                    .getReference(userKey + "/notes/");
        } else {
            firebaseRef = ((MainActivity)getActivity()).getFirebaseDatabase().getReference("/notes");
        }
        firebaseRef.keepSynced(true);

        new getNotes().execute();

        checkInternetConnectivity();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent openSettingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(openSettingsIntent);
                break;
            case R.id.action_view_trash:
                Intent openTrashIntent = new Intent(getActivity(), TrashActivity.class);
                openTrashIntent.putExtra(getString(R.string.userKey), userKey);
                startActivity(openTrashIntent);
                break;
            case R.id.action_search:
                Intent searchNotesIntent = new Intent(getActivity(), SearchNotesActivity.class);
                startActivity(searchNotesIntent);
                break;
            case R.id.action_create_note:
                openNoteActivity("null", "notes", userKey);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class getNotes extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            adapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(Note.class, R.layout.note, NoteHolder.class, firebaseRef) {
                @Override
                public void populateViewHolder(NoteHolder noteHolder, Note note, final int position) {
                    noteHolder.setNoteTitle(note.getTitle());
                    noteHolder.setNoteContent(note.getContent());

                    final String noteKey = getRef(position).getKey();

                    noteHolder.bindClickToNote(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openNoteActivity(noteKey, "notes", userKey);
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

    private void openNoteActivity(String key, String lookupType, String userKey){
        Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
        openNoteIntent.putExtra("key", key);
        openNoteIntent.putExtra("lookup", lookupType);
        openNoteIntent.putExtra(getString(R.string.userKey), userKey);
        startActivity(openNoteIntent);
    }

    private void checkInternetConnectivity(){
        if (!Helpers.isConnectedToInternet(getActivity())){
            Snackbar.make(viewGroup, "You're Offline. Notes aren't sync'd.", Snackbar.LENGTH_LONG).show();
        }
    }
}
