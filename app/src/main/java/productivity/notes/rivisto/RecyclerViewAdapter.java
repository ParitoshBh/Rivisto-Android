package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private static List<Note> noteList;
    private ViewHolder.ClickListener clickListener;
    private static final String LOG_ITEM_CLICK = "ItemClick";

    public RecyclerViewAdapter(ArrayList notes, ViewHolder.ClickListener clickListener) {
        noteList = notes;
        this.clickListener = clickListener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView noteTitle;
        TextView noteContent;
        private ClickListener listener;

        public ViewHolder(View itemView, ClickListener listener) {
            super(itemView);

            noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
            noteContent = (TextView) itemView.findViewById(R.id.noteContent);

            this.listener = listener;

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //Log.i(LOG_ITEM_CLICK, "Clicked " + noteList.get(getAdapterPosition()).getNoteKey());
            if (listener != null) {
                listener.onItemClicked(noteList.get(getAdapterPosition()).getNoteKey());
            }
        }

        public interface ClickListener {
            public void onItemClicked(String noteKey);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final Note note = noteList.get(position);

        holder.noteTitle.setText(note.getTitle());
        holder.noteContent.setText(note.getContent());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }
}
