package productivity.notes.rivisto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class OpenNoteActivity extends AppCompatActivity {
    private DatabaseReference firebaseRef;
    private FirebaseDatabase firebaseDatabase;
    private EditText noteTitle, noteContent;
    private String noteKey, noteLookup;
    private boolean isNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteTitle = (EditText) findViewById(R.id.openNoteTitle);
        noteContent = (EditText) findViewById(R.id.openNoteContent);

        noteKey = getIntent().getStringExtra("key");
        noteLookup = getIntent().getStringExtra("lookup");

        FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

        if (noteKey.equalsIgnoreCase("null")) {
            isNewNote = true;

            firebaseRef = firebaseDatabase.getReference("/notes/");
        } else {
            isNewNote = false;

            if (noteLookup.equalsIgnoreCase("trash")){
                noteTitle.setEnabled(false);
                noteContent.setEnabled(false);

                firebaseRef = firebaseDatabase.getReference("/trash/" + noteKey);
            } else if (noteLookup.equalsIgnoreCase("notes")){
                firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);
            }

            ValueEventListener newNoteListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    Note note = dataSnapshot.getValue(Note.class);
                    noteTitle.setText(note.getTitle());
                    noteContent.setText(note.getContent());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            firebaseRef.addListenerForSingleValueEvent(newNoteListener);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (getIntent().getStringExtra("lookup").equalsIgnoreCase("trash")){
            getMenuInflater().inflate(R.menu.menu_open_trash_note, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_open_note, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.action_save_note:
                if (isNewNote) {
                    createNewNote();
                    Toast.makeText(this, "Note created", Toast.LENGTH_SHORT).show();
                } else {
                    updateNote();
                    Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.action_delete_note:
                if (isNewNote) {
                    Toast.makeText(this, "Note discarded", Toast.LENGTH_SHORT).show();
                } else {
                    moveNoteToTrash();
                    Toast.makeText(this, "Note moved to trash", Toast.LENGTH_SHORT).show();
                }
                finish();
                break;
            case R.id.action_restore_note:
                restoreNote();
                Toast.makeText(this, "Note restored", Toast.LENGTH_SHORT).show();
                finish();
                break;
            case R.id.action_permanent_delete_note:
                permanentlyDeleteNote();
                Toast.makeText(this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewNote() {
        String newNoteKey = firebaseRef.push().getKey();
        firebaseRef.child(newNoteKey)
                .setValue(new Note(
                        noteTitle.getText().toString(),
                        noteContent.getText().toString(),
                        "Android",
                        1476044575974L
                ));

        // Update note key to newly created note key
        noteKey = newNoteKey;

        // Update isNewNote boolean. Note isn't a new one anymore.
        isNewNote = false;

        // Update firebase reference
        firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);
    }

    private void updateNote() {
        firebaseRef.setValue(new Note(noteTitle.getText().toString(), noteContent.getText().toString(), "Android", 1476044575974L));
    }

    private void moveNoteToTrash() {
        // Get details of note
        ValueEventListener newNoteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Note note = dataSnapshot.getValue(Note.class);
                String newTrashKey = firebaseDatabase.getReference("/trash/").push().getKey();
                firebaseDatabase.getReference("/trash/" + newTrashKey)
                        .setValue(new Note(
                                note.getTitle(),
                                note.getContent(),
                                note.getLabel(),
                                1476044575974L
                        ));
                firebaseRef.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        firebaseRef.addListenerForSingleValueEvent(newNoteListener);
    }

    private void restoreNote(){
        // Get details of note
        ValueEventListener newNoteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Note note = dataSnapshot.getValue(Note.class);
                String newRestoredKey = firebaseDatabase.getReference("/notes/").push().getKey();
                firebaseDatabase.getReference("/notes/" + newRestoredKey)
                        .setValue(new Note(
                                note.getTitle(),
                                note.getContent(),
                                note.getLabel(),
                                1476044575974L
                        ));
                firebaseRef.removeValue();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        firebaseRef.addListenerForSingleValueEvent(newNoteListener);
    }

    private void permanentlyDeleteNote(){
        firebaseRef.removeValue();
    }

}
