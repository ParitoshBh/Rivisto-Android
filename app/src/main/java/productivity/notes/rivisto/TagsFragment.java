package productivity.notes.rivisto;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TagsFragment extends Fragment {
    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Tag, TagHolder> adapter;
    private DatabaseReference firebaseRef;

    public TagsFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tags, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        FirebaseApp firebaseApp = FirebaseApp.getInstance("Firebase");
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance(firebaseApp);

        firebaseRef =  firebaseDatabase.getReference("/tags");

        new getTags().execute();

        return view;
    }

    private class getTags extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            recyclerView.setHasFixedSize(true);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            adapter = new FirebaseRecyclerAdapter<Tag, TagHolder>(Tag.class, R.layout.tag, TagHolder.class, firebaseRef) {
                @Override
                public void populateViewHolder(TagHolder tagHolder, Tag tag, final int position) {
                    tagHolder.setTagName(adapter.getRef(position).getKey());
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
