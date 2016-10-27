package productivity.notes.rivisto;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

import java.util.Date;

import productivity.notes.rivisto.utils.Helpers;

public class OpenNoteActivity extends AppCompatActivity {
    private DatabaseReference firebaseRef;
    private FirebaseDatabase firebaseDatabase;
    private EditText noteTitle, noteContent;
    private CoordinatorLayout coordinatorLayout;
    private String noteKey, noteLookup, selectedNoteLabel, userKey;
    private static final String NOTE_CREATED = "Created", NOTE_UPDATED = "Updated";
    private boolean isNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayoutActivityOpenNote);
        noteTitle = (EditText) findViewById(R.id.openNoteTitle);
        noteContent = (EditText) findViewById(R.id.openNoteContent);

        noteKey = getIntent().getStringExtra("key");
        noteLookup = getIntent().getStringExtra("lookup");
        userKey = getIntent().getStringExtra(getString(R.string.userKey));

        //Log.i("USER KEY", userKey);

        if (userKey == null){
            FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
            firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);
        } else {
            firebaseDatabase = FirebaseDatabase.getInstance();
        }

        if (noteKey.equalsIgnoreCase("null")) {
            isNewNote = true;

            if (userKey == null) {
                firebaseRef = firebaseDatabase.getReference("/notes/");
            } else {
                firebaseRef = firebaseDatabase.getReference(userKey + "/notes/");
            }
        } else {
            isNewNote = false;

            if (noteLookup.equalsIgnoreCase("trash")) {
                noteTitle.setEnabled(false);
                noteContent.setEnabled(false);

                if (userKey == null) {
                    firebaseRef = firebaseDatabase.getReference("/trash/" + noteKey);
                } else {
                    firebaseRef = firebaseDatabase.getReference(userKey + "/trash/" + noteKey);
                }
            } else if (noteLookup.equalsIgnoreCase("notes")) {
                if (userKey == null) {
                    firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);
                } else {
                    firebaseRef = firebaseDatabase.getReference(userKey + "/notes/" + noteKey);
                }
            }

            ValueEventListener newNoteListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    Note note = dataSnapshot.getValue(Note.class);
                    noteTitle.setText(note.getTitle());
                    noteContent.setText(note.getContent());
                    selectedNoteLabel = note.getLabel();
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
        if (getIntent().getStringExtra("lookup").equalsIgnoreCase("trash")) {
            getMenuInflater().inflate(R.menu.menu_open_trash_note, menu);
        } else {
            getMenuInflater().inflate(R.menu.menu_open_note, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_save_note:
                if (isNewNote) {
                    createNewNote();
                    showConfirmationMessage(NOTE_CREATED);
                } else {
                    updateNote();
                    showConfirmationMessage(NOTE_UPDATED);
                }
                break;
            case R.id.action_delete_note:
                if (isNewNote) {
                    Toast.makeText(this, "Note discarded", Toast.LENGTH_SHORT).show();
                } else {
                    moveNoteToTrash();
                    Toast.makeText(this, "Note trashed", Toast.LENGTH_SHORT).show();
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
        String noteLabel = extractTag(noteContent.getText().toString());
        firebaseRef.child(newNoteKey)
                .setValue(new Note(
                        noteTitle.getText().toString(),
                        noteContent.getText().toString(),
                        noteLabel,
                        getCurrentTime()
                ));

        // Update note key to newly created note key
        noteKey = newNoteKey;

        selectedNoteLabel = noteLabel;

        // Update isNewNote boolean. Note isn't a new one anymore.
        isNewNote = false;

        // Update firebase reference
        if (userKey == null) {
            firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);
        } else {
            firebaseRef = firebaseDatabase.getReference(userKey + "/notes/" + noteKey);
        }

        // Add tag to database is it is not null i.e. there is a tag present
        if (noteLabel != null){
            incrementTagNoteCount(noteLabel);
        }
    }

    private void incrementTagNoteCount(final String tag) {
        if (userKey == null) {
            firebaseDatabase.getReference("/tags/" + tag).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long noteCount = 1L;

                    if (dataSnapshot.hasChildren()) {
                        // Increment the note count
                        Tag tagObj = dataSnapshot.getValue(Tag.class);
                        noteCount += tagObj.getNoteCount();
                    }

                    firebaseDatabase.getReference("/tags/" + tag).setValue(
                            new Tag(
                                    noteCount
                            )
                    );

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            firebaseDatabase.getReference(userKey + "/tags/" + tag).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Long noteCount = 1L;

                    if (dataSnapshot.hasChildren()) {
                        // Increment the note count
                        Tag tagObj = dataSnapshot.getValue(Tag.class);
                        noteCount += tagObj.getNoteCount();
                    }

                    firebaseDatabase.getReference(userKey + "/tags/" + tag).setValue(
                            new Tag(
                                    noteCount
                            )
                    );

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void decrementTagNoteCount(final String tag) {
        if (userKey == null) {
            firebaseDatabase.getReference("/tags/" + tag).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Increment the note count
                    Tag tagObj = dataSnapshot.getValue(Tag.class);

                    if (tagObj != null){
                        Long noteCount = tagObj.getNoteCount();

                        noteCount -= 1L;

                        if (noteCount < 1){
                            firebaseDatabase.getReference("/tags/" + tag).removeValue();
                        } else {
                            firebaseDatabase.getReference("/tags/" + tag).setValue(new Tag(noteCount));
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        } else {
            firebaseDatabase.getReference(userKey + "/tags/" + tag).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    // Increment the note count
                    Tag tagObj = dataSnapshot.getValue(Tag.class);

                    if (tagObj != null){
                        Long noteCount = tagObj.getNoteCount();

                        noteCount -= 1L;

                        if (noteCount < 1){
                            firebaseDatabase.getReference(userKey + "/tags/" + tag).removeValue();
                        } else {
                            firebaseDatabase.getReference(userKey + "/tags/" + tag).setValue(new Tag(noteCount));
                        }
                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    private void adjustTagNoteCount(String oldLabel, String newLabel){
        if (oldLabel != null){
            decrementTagNoteCount(oldLabel);
        }

        if (newLabel != null){
            incrementTagNoteCount(newLabel);
        }
    }

    private void updateNote() {
        String updatedNoteLabel = extractTag(noteContent.getText().toString());

        firebaseRef.setValue(
                new Note(
                        noteTitle.getText().toString(),
                        noteContent.getText().toString(),
                        updatedNoteLabel,
                        getCurrentTime()
                ));

        if (updatedNoteLabel != selectedNoteLabel){
            adjustTagNoteCount(selectedNoteLabel, updatedNoteLabel);
        }

        selectedNoteLabel = updatedNoteLabel;
    }

    private void moveNoteToTrash() {
        // Get details of note
        ValueEventListener newNoteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Note note = dataSnapshot.getValue(Note.class);
                if (userKey == null) {
                    String newTrashKey = firebaseDatabase.getReference("/trash/").push().getKey();
                    firebaseDatabase.getReference("/trash/" + newTrashKey)
                            .setValue(new Note(
                                    note.getTitle(),
                                    note.getContent(),
                                    note.getLabel(),
                                    note.getTime()
                            ));
                } else {
                    String newTrashKey = firebaseDatabase.getReference(userKey + "/trash/").push().getKey();
                    firebaseDatabase.getReference(userKey + "/trash/" + newTrashKey)
                            .setValue(new Note(
                                    note.getTitle(),
                                    note.getContent(),
                                    note.getLabel(),
                                    note.getTime()
                            ));
                }

                firebaseRef.removeValue();
                decrementTagNoteCount(note.getLabel());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        firebaseRef.addListenerForSingleValueEvent(newNoteListener);
    }

    private void restoreNote() {
        // Get details of note
        ValueEventListener newNoteListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get Post object and use the values to update the UI
                Note note = dataSnapshot.getValue(Note.class);

                if (userKey == null) {
                    String newRestoredKey = firebaseDatabase.getReference("/notes/").push().getKey();
                    firebaseDatabase.getReference("/notes/" + newRestoredKey)
                            .setValue(new Note(
                                    note.getTitle(),
                                    note.getContent(),
                                    note.getLabel(),
                                    note.getTime()
                            ));
                } else {
                    String newRestoredKey = firebaseDatabase.getReference(userKey + "/notes/").push().getKey();
                    firebaseDatabase.getReference(userKey + "/notes/" + newRestoredKey)
                            .setValue(new Note(
                                    note.getTitle(),
                                    note.getContent(),
                                    note.getLabel(),
                                    note.getTime()
                            ));
                }

                firebaseRef.removeValue();
                incrementTagNoteCount(note.getLabel());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        };
        firebaseRef.addListenerForSingleValueEvent(newNoteListener);
    }

    private void permanentlyDeleteNote() {
        firebaseRef.removeValue();
    }

    private String extractTag(String noteContent) {
        String contentWords[] = noteContent.split(" ");
        String label = null;

        for (String word : contentWords) {
            if (word.startsWith("#") && (word.length() > 1)) {
                label = word.substring(1);
                break;
            }
        }

        return label;
    }

    private Long getCurrentTime() {
        Date date = new Date();
        return date.getTime();
    }

    private void showConfirmationMessage(String type){
        String confirmationMessage = "";

        switch (type){
            case NOTE_CREATED:
                confirmationMessage = "Note created";
                break;
            case NOTE_UPDATED:
                confirmationMessage = "Note updated";
                break;
        }

        if (!Helpers.isConnectedToInternet(this)){
            confirmationMessage = confirmationMessage.concat(", It'll be sync'd once you're online.");
        }

        Snackbar.make(coordinatorLayout, confirmationMessage, Snackbar.LENGTH_SHORT).show();
    }
}
