package productivity.notes.rivisto;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import productivity.notes.rivisto.utils.Helpers;

public class TrashActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private FirebaseDatabase firebaseDatabase;
    private FirebaseRecyclerAdapter<Note, NoteHolder> adapter;
    private DatabaseReference firebaseRef;
    private String userKey;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trash);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutActivityTrash);
        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        userKey = this.getIntent().getStringExtra(getString(R.string.userKey));

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        if (userKey == null) {
            FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
            firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

            firebaseRef = firebaseDatabase.getReference("/trash");
        } else {
            firebaseRef = FirebaseDatabase.getInstance().getReference(userKey + "/trash");
        }

        firebaseRef.keepSynced(true);

        new trashedNotes().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_trash, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_empty_trash:
                firebaseRef.removeValue();
                showConfirmationMessage();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class trashedNotes extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... params) {

            adapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(Note.class, R.layout.note, NoteHolder.class,
                    firebaseRef) {

                @Override
                public void populateViewHolder(NoteHolder noteHolder, Note note, final int position) {
                    noteHolder.setNoteTitle(note.getTitle());
                    noteHolder.setNoteContent(note.getContent());

                    noteHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            openNoteActivity(adapter.getRef(position).getKey(), "trash", userKey);
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

    private void openNoteActivity(String key, String lookupType, String userKey) {
        Intent openNoteIntent = new Intent(this, OpenNoteActivity.class);
        openNoteIntent.putExtra("key", key);
        openNoteIntent.putExtra("lookup", lookupType);
        openNoteIntent.putExtra(getString(R.string.userKey), userKey);
        startActivity(openNoteIntent);
    }

    private void showConfirmationMessage() {
        int trashedNotesCount = adapter.getItemCount();
        String confirmationMessage = "";

        if (trashedNotesCount > 0) {
            confirmationMessage = "Emptied trash";
            if (!Helpers.isConnectedToInternet(this)) {
                confirmationMessage = confirmationMessage.concat(", It'll be sync'd once you're online.");
            }
        } else {
            confirmationMessage = "Trash is already empty!";
        }

        Snackbar.make(coordinatorLayout, confirmationMessage, Snackbar.LENGTH_SHORT).show();
    }
}
