package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

class NoteHolder extends RecyclerView.ViewHolder {
    View view;
    private TextView noteTitle;
    private TextView noteContent;

    public NoteHolder(View itemView) {
        super(itemView);
        view = itemView;
        noteTitle = (TextView) itemView.findViewById(R.id.noteTitle);
        noteContent =(TextView) itemView.findViewById(R.id.noteContent);
    }

    public void setNoteTitle(String title) {
        noteTitle.setText(title);
    }

    public void setNoteContent(String content){
        noteContent.setText(content);
    }

    public View getView() {
        return view;
    }

    public void bindClickToNote(View.OnClickListener clickListener){
        view.setOnClickListener(clickListener);
    }
}