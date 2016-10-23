package productivity.notes.rivisto;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private DatabaseReference firebaseRef;
    private ArrayList<Note> notes;
    private SharedPreferences sharedPref;
    private String userKey;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        sharedPref = this.getSharedPreferences("FirebaseCredentials", Context.MODE_PRIVATE);
        userKey = sharedPref.getString(getString(R.string.userKey), null);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (userKey == null) {
            FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
            firebaseRef = FirebaseDatabase.getInstance(firebaseApp).getReference("/notes/");
        } else {
            firebaseRef = FirebaseDatabase.getInstance().getReference(userKey + "/notes/");
        }

        notes = new ArrayList<>();

        Intent intent = getIntent();

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            new searchNotes().execute(query.trim().toLowerCase());
        }
    }

    private class searchNotes extends AsyncTask<String, Void, Boolean> {
        @Override
        protected Boolean doInBackground(String... query) {

            final String searchQuery = query[0];

            firebaseRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Note note = dataSnapshot.getValue(Note.class);
                    if (note.getTitle().toLowerCase().contains(searchQuery) || note.getContent().toLowerCase().contains(searchQuery)) {
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
            recyclerView.setAdapter(new RecyclerViewAdapter(notes));
        }
    }
}
