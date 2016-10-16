package productivity.notes.rivisto;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import jp.wasabeef.richeditor.RichEditor;

public class OpenNoteActivity extends AppCompatActivity {
    private DatabaseReference firebaseRef;
    private FirebaseDatabase firebaseDatabase;
    private RichEditor noteContent;
    private EditText noteTitle;
    private String noteKey;
    private boolean isNewNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_note);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        noteTitle = (EditText) findViewById(R.id.openNoteTitle);
        noteContent = (RichEditor) findViewById(R.id.openNoteContent);
        noteContent.setEditorFontSize(14);

        noteKey = getIntent().getStringExtra("key");

        FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
        firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

        if (noteKey.equalsIgnoreCase("null")) {
            isNewNote = true;

            firebaseRef = firebaseDatabase.getReference("/notes/");
        } else {
            isNewNote = false;

            firebaseRef = firebaseDatabase.getReference("/notes/" + noteKey);

            ValueEventListener newNoteListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    // Get Post object and use the values to update the UI
                    Note note = dataSnapshot.getValue(Note.class);
                    noteTitle.setText(note.getTitle());
                    noteContent.setHtml(note.getContent());
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // Getting Post failed, log a message
                    //Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                }
            };
            firebaseRef.addListenerForSingleValueEvent(newNoteListener);
        }

        setupRichTextEditorOptions();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_open_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save_note) {
            if (isNewNote) {
                createNewNote();
                Toast.makeText(this, "Note created", Toast.LENGTH_SHORT).show();
            } else {
                updateNote();
                Toast.makeText(this, "Note updated", Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (id == R.id.action_delete_note) {
            if (isNewNote) {
                Toast.makeText(this, "Note discarded", Toast.LENGTH_SHORT).show();
            } else {
                moveNoteToTrash();
                Toast.makeText(this, "Note moved to trash", Toast.LENGTH_SHORT).show();
            }
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNewNote() {
        String newNoteKey = firebaseRef.push().getKey();
        firebaseRef.child(newNoteKey)
                .setValue(new Note(
                        noteTitle.getText().toString(),
                        noteContent.getHtml(),
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
        firebaseRef.setValue(new Note(noteTitle.getText().toString(), noteContent.getHtml(), "Android", 1476044575974L));
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

    private void setupRichTextEditorOptions() {
        findViewById(R.id.action_bold).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setBold();
            }
        });

        findViewById(R.id.action_italic).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setItalic();
            }
        });

        findViewById(R.id.action_strikethrough).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setStrikeThrough();
            }
        });

        findViewById(R.id.action_underline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setUnderline();
            }
        });

        findViewById(R.id.action_align_left).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setAlignLeft();
            }
        });

        findViewById(R.id.action_align_center).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setAlignCenter();
            }
        });

        findViewById(R.id.action_align_right).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setAlignRight();
            }
        });

        findViewById(R.id.action_insert_bullets).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setBullets();
            }
        });

        findViewById(R.id.action_insert_numbers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noteContent.setNumbers();
            }
        });
    }
}
