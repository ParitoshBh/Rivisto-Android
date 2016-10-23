package productivity.notes.rivisto;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TagNotesFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Note, NoteHolder> adapter;
    private DatabaseReference firebaseRef;
    private String tagName;

    public TagNotesFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_notes, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        Bundle args = this.getArguments();
        tagName = args.getString("tagName", "Nothing");

        ((TagsActivity) getActivity()).getSupportActionBar().setTitle("Filed in " + tagName);

        FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

        firebaseRef = firebaseDatabase.getReference("/notes");

        new getTagNotes().execute();

        return view;
    }

    private class getTagNotes extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            adapter = new FirebaseRecyclerAdapter<Note, NoteHolder>(Note.class, R.layout.note, NoteHolder.class,
                    firebaseRef.orderByChild("label").startAt(tagName).endAt(tagName)) {
                @Override
                public void populateViewHolder(NoteHolder noteHolder, Note note, final int position) {
                    noteHolder.setNoteTitle(note.getTitle());
                    noteHolder.setNoteLabel(note.getLabel());
                    noteHolder.setNoteContent(note.getContent());

                    noteHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent openNoteIntent = new Intent(getActivity(), OpenNoteActivity.class);
                            openNoteIntent.putExtra("key", adapter.getRef(position).getKey());
                            openNoteIntent.putExtra("lookup", "notes");
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
