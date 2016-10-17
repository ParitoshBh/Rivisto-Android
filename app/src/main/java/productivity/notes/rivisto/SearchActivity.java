package productivity.notes.rivisto;

import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseRecyclerAdapter<Note, NoteHolder> adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new searchNotes().execute(query.trim().toLowerCase());
        }

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
    }

    private class searchNotes extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... query) {

            final String searchQuery = query[0];

            adapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(Note.class, R.layout.note, NoteHolder.class,
                    firebaseDatabase.getReference("/notes/")) {

                @Override
                public void populateViewHolder(NoteHolder noteHolder, Note note, final int position) {
                    if (note.getTitle().toLowerCase().contains(searchQuery) || note.getContent().toLowerCase().contains(searchQuery)) {
                        noteHolder.setNoteTitle(note.getTitle());
                        noteHolder.setNoteLabel(note.getLabel());
                    }

//                    noteHolder.view.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
////                            Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
////                            openNoteIntent.putExtra("key", adapter.getRef(position).getKey());
////                            startActivity(openNoteIntent);
//                        }
//                    });
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
