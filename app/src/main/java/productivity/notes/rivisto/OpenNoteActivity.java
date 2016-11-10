package productivity.notes.rivisto;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
    private String noteKey, noteLookup, selectedNoteLabel, userKey, selectedNoteTitle, selectedNoteContent;
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

        // Initialize selected note value with empty string
        selectedNoteTitle = selectedNoteContent = selectedNoteLabel = "";

        //Log.i("USER KEY", userKey);

        if (userKey == null) {
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
                    updateSelectedNoteValues(note.getTitle(), note.getContent(), note.getLabel());
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
                } else {
                    updateNote();
                }
                return super.onOptionsItemSelected(item);
            case R.id.action_delete_note:
                if (isNewNote) {
                    Toast.makeText(this, "Note discarded", Toast.LENGTH_SHORT).show();
                } else {
                    moveNoteToTrash();
                    Toast.makeText(this, "Note trashed", Toast.LENGTH_SHORT).show();
                }
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.action_restore_note:
                restoreNote();
                Toast.makeText(this, "Note restored", Toast.LENGTH_SHORT).show();
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.action_permanent_delete_note:
                permanentlyDeleteNote();
                Toast.makeText(this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.action_share_note:
                shareNote();
                return super.onOptionsItemSelected(item);
            case android.R.id.home:
                //Log.i("OpenNoteActivity", "Home button pressed");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void updateSelectedNoteValues(String title, String content, String label) {
        selectedNoteTitle = title;
        selectedNoteContent = content;
        selectedNoteLabel = label;
    }

    private void createNewNote() {
        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        // Note content can be left empty
        // Note title cannot be empty
        // If content is there and no title is entered, title is generated from content

        // Check if title is left empty
        if (title.isEmpty()) {
            // Check if content of note is empty
            if (content.isEmpty()) {
                // Show error message and do not save the note
                Snackbar.make(coordinatorLayout, "Empty note cannot be saved", Snackbar.LENGTH_SHORT).show();
            } else {
                // Use first 5 words of content as title
                title = generateTitleFromContent(content);
                // Show title in note
                noteTitle.setText(title);
                saveNote(title, content);
            }
        } else {
            saveNote(title, content);
        }
    }

    private void saveNote(String title, String content) {
        String newNoteKey = firebaseRef.push().getKey();
        String label = extractTag(content);

        firebaseRef.child(newNoteKey)
                .setValue(new Note(
                        title,
                        content,
                        label,
                        getCurrentTime()
                ));

        // Show confirmation message to user
        showConfirmationMessage(NOTE_CREATED);

        // Update note key to newly created note key
        noteKey = newNoteKey;

        updateSelectedNoteValues(title, content, label);

        // Update isNewNote boolean. Note isn't a new one anymore.
        isNewNote = false;

        // Update firebase reference
        if (userKey == null) {
            firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);
        } else {
            firebaseRef = firebaseDatabase.getReference(userKey + "/notes/" + noteKey);
        }

        // Add tag to database is it is not empty i.e. there is a tag present
        if (!label.equals("")) {
            incrementTagNoteCount(label);
        }
    }

    private String generateTitleFromContent(String content) {
        // Use first 5 words of content as note title

        StringBuilder stringBuilder = new StringBuilder();
        for (String word : content.split(" ", 5)) {
            stringBuilder.append(word).append(" ");
        }

        return stringBuilder.toString();
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

                    firebaseDatabase.getReference(userKey + "/tags/" + tag).setValue(new Tag(noteCount));

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

                    if (tagObj != null) {
                        Long noteCount = tagObj.getNoteCount();

                        noteCount -= 1L;

                        if (noteCount < 1) {
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

                    if (tagObj != null) {
                        Long noteCount = tagObj.getNoteCount();

                        noteCount -= 1L;

                        if (noteCount < 1) {
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

    private void adjustTagNoteCount(String oldLabel, String newLabel) {
        if (!oldLabel.equals("")) {
            decrementTagNoteCount(oldLabel);
        }

        if (!newLabel.equals("")) {
            incrementTagNoteCount(newLabel);
        }
    }

    private void updateNote() {
        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        // Note content can be left empty
        // Note title cannot be empty
        // If content is there and no title is entered, title is generated from content

        // Check if anything has changed at all, proceed if required
        if (selectedNoteTitle.equals(title) && selectedNoteContent.equals(content)) {
            // No need to update anything, show a message
            Snackbar.make(coordinatorLayout, "Note is up to date", Snackbar.LENGTH_SHORT).show();
        } else {
            // Check if title is left empty
            if (title.isEmpty()) {
                // Check if content of note is empty
                if (content.isEmpty()) {
                    // Show error message and do not save the note
                    Snackbar.make(coordinatorLayout, "Empty note cannot be saved", Snackbar.LENGTH_SHORT).show();
                } else {
                    // Use first 5 words of content as title
                    title = generateTitleFromContent(content);
                    // Show title in note
                    noteTitle.setText(title);
                    saveUpdatedNote(title, content);
                }
            } else {
                saveUpdatedNote(title, content);
            }
        }

    }

    private void saveUpdatedNote(String title, String content) {
        String updatedNoteLabel = extractTag(content);

        firebaseRef.setValue(
                new Note(
                        title,
                        content,
                        updatedNoteLabel,
                        getCurrentTime()
                ));

        showConfirmationMessage(NOTE_UPDATED);

        if (!updatedNoteLabel.equals(selectedNoteLabel)) {
            adjustTagNoteCount(selectedNoteLabel, updatedNoteLabel);
        }

        updateSelectedNoteValues(title, content, updatedNoteLabel);
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
        String label = "";

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

    private void showConfirmationMessage(String type) {
        String confirmationMessage = "";

        switch (type) {
            case NOTE_CREATED:
                confirmationMessage = "Note created";
                break;
            case NOTE_UPDATED:
                confirmationMessage = "Note updated";
                break;
        }

        if (!Helpers.isConnectedToInternet(this)) {
            confirmationMessage = confirmationMessage.concat(", It'll be sync'd once you're online.");
        }

        Snackbar.make(coordinatorLayout, confirmationMessage, Snackbar.LENGTH_SHORT).show();
    }

    private void shareNote() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        String shareBodyText = noteContent.getText().toString().trim();

        if (isNoteContentAvailable(shareBodyText)) {
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBodyText);

            startActivity(Intent.createChooser(sharingIntent, "Shearing Option"));
        } else {
            Snackbar.make(coordinatorLayout, "Nothing to share!", Snackbar.LENGTH_SHORT).show();
        }
    }

    private boolean isNoteContentAvailable(String content) {
        if (content.isEmpty() || content.equalsIgnoreCase("")) {
            return false;
        } else {
            return true;
        }
    }

    private boolean isNoteTitleAvailable(String title) {
        if (title.isEmpty() || title.equalsIgnoreCase("")) {
            return false;
        } else {
            return true;
        }
    }

    private void showConfirmationDialog(final String title, final String content) {
        // 1. Instantiate an AlertDialog.Builder with its constructor
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // 2. Chain together various setter methods to set the dialog characteristics
        builder.setMessage("Note hasn't been saved yet!").setTitle("Save note?");

        // 3. Add the buttons
        builder.setPositiveButton(R.string.alert_dialog_save, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked OK button
                if (noteKey.equalsIgnoreCase("null")){
                    saveNote(title, content);
                } else {
                    updateNote();
                }
            }
        });
        builder.setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
                dialog.dismiss();
                OpenNoteActivity.super.onBackPressed();
            }
        });

        // 4. Get the AlertDialog from create()
        AlertDialog dialog = builder.create();

        // 5. show the AlertDialog
        dialog.show();
    }

    @Override
    public void onBackPressed() {
        String title = noteTitle.getText().toString().trim();
        String content = noteContent.getText().toString().trim();

        if (noteKey.equalsIgnoreCase("null")) {
            // A new note was attempted to be created
            //Log.i("OpenNoteActivity", "New note was being created");
            // Check if there's something to save in note i.e. a title or content
            if (isNoteTitleAvailable(title) || isNoteContentAvailable(content)) {
                // If either of them is filled out, then show a notification asking to save the note
                showConfirmationDialog(title, content);
            } else {
                // If nothing is in there, simply follow the back button click
                super.onBackPressed();
            }
        } else {
            // An existing note was being viewed
            //Log.i("OpenNoteActivity", "Existing note was being viewed");
            // Check if the note has been updated i.e. if either title or content has changed
            if (selectedNoteTitle.equals(title) && selectedNoteContent.equals(content)) {
                // No need to show confirmation message, simply follow back button click
                super.onBackPressed();
            } else {
                // Show confirmation alert dialog
                showConfirmationDialog(title, content);
            }
        }

    }

}
