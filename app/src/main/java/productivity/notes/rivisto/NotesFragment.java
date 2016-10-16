package productivity.notes.rivisto;

import android.app.Fragment;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class NotesFragment extends Fragment {
    private DatabaseReference firebaseRef;
    private FirebaseRecyclerAdapter<Note, NoteHolder> adapter;
    private FloatingActionButton floatingActionButton;
    private RecyclerView recyclerView;

    public NotesFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        setHasOptionsMenu(true);

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        firebaseRef = ((MainActivity)getActivity()).getFirebaseDatabase().getReference("/notes");

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
                openNoteIntent.putExtra("key", "null");
                startActivity(openNoteIntent);
            }
        });

        new getNotes().execute();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_notes);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        ComponentName componentName = new ComponentName(getActivity(), SearchActivity.class);

        searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent openSettingsIntent = new Intent(getActivity(), SettingsActivity.class);
                startActivity(openSettingsIntent);
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
                    noteHolder.setTitle(note.getTitle());
                    noteHolder.setLabel(note.getLabel());

                    noteHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
                            openNoteIntent.putExtra("key", adapter.getRef(position).getKey());
                            startActivity(openNoteIntent);
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
}