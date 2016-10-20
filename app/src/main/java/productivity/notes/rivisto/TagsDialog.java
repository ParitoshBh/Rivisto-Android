package productivity.notes.rivisto;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

public class TagsDialog extends DialogFragment {

    public interface TagClickListener {
        public void onTagClick(DialogFragment dialogFragment, String tagName);
    }

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Tag, TagHolder> adapter;
    private DatabaseReference firebaseRef;
    private TagClickListener tagClickListener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Set view association
        View view = inflater.inflate(R.layout.dialog_tags, null);

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(view);

        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);

        firebaseRef = ((MainActivity)getActivity()).getFirebaseDatabase().getReference("/tags");

        new getTags().execute();

        return builder.create();
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

                    tagHolder.view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            tagClickListener.onTagClick(TagsDialog.this, adapter.getRef(position).getKey());
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            tagClickListener = (TagClickListener) getActivity();
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement TagClickListener");
        }
    }
}
