package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class NoteHolder extends RecyclerView.ViewHolder {
    View view;
    TextView noteTitle;
    TextView noteLabel;

    public NoteHolder(View itemView) {
        super(itemView);
        view = itemView;
        noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
        noteLabel = (TextView) itemView.findViewById(R.id.noteLabel);
    }

    public void setTitle(String title) {
        noteTitle.setText(title);
    }

    public void setLabel(String label) {
        noteLabel.setText(label);
    }

    public View getView() {
        return view;
    }
}