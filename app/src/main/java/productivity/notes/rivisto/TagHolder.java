package productivity.notes.rivisto;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class TagHolder extends RecyclerView.ViewHolder {
    View view;
    private TextView tagName;

    public TagHolder(View itemView) {
        super(itemView);
        view = itemView;
        tagName = (TextView) itemView.findViewById(R.id.tagName);
    }

    public void setTagName(String title) {
        tagName.setText(title);
    }

    public View getView() {
        return view;
    }
}
