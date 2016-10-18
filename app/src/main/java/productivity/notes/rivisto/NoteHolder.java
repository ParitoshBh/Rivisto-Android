package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class NoteHolder extends RecyclerView.ViewHolder {
    View view;
    private TextView noteTitle;
    private TextView noteLabel;
    private TextView noteContent;

    public NoteHolder(View itemView) {
        super(itemView);
        view = itemView;
        noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
        noteLabel = (TextView) itemView.findViewById(R.id.noteLabel);
        noteContent =(TextView) itemView.findViewById(R.id.noteContent);
    }

    public void setNoteTitle(String title) {
        noteTitle.setText(title);
    }

    public void setNoteLabel(String label) {
        noteLabel.setText(label);
    }

    public void setNoteContent(String content){
        noteContent.setText(content);
    }

    public void hideView(){
        view.setVisibility(View.GONE);
    }

    public View getView() {
        return view;
    }
}