package productivity.notes.rivisto;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

public class TagsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tags_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String userKey = this.getIntent().getStringExtra(getString(R.string.userKey));

        openTagsFragment(userKey);
    }

    private void openTagsFragment(String userKey){
        TagsFragment tagsFragment = new TagsFragment();

        Bundle bundle = new Bundle();
        bundle.putString(getString(R.string.userKey), userKey);

        tagsFragment.setArguments(bundle);

        getFragmentManager()
                .beginTransaction()
                .add(R.id.fragment_container, tagsFragment)
                .commit();
    }
}
