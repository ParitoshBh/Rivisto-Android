package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {
    private List<Note> noteList;

    public RecyclerViewAdapter(ArrayList notes) {
        noteList = notes;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView noteTitle;
        TextView noteContent;

        public ViewHolder(View itemView) {
            super(itemView);

            noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
            noteContent = (TextView) itemView.findViewById(R.id.noteContent);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.note, parent, false);
        return new ViewHolder(v);
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
