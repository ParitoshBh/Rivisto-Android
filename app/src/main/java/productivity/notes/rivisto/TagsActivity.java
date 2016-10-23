package productivity.notes.rivisto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class TagsActivity extends AppCompatActivity implements TagsFragment.OnTagSelectedListener {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, new TagsFragment())
                .commit();
    }

    @Override
    public void onTagSelected(String name) {
        TagNotesFragment newFragment = new TagNotesFragment();
        Bundle args = new Bundle();
        args.putString("tagName", name);
        newFragment.setArguments(args);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, newFragment)
                .addToBackStack("TagNoteFragment")
                .commit();
    }
}
