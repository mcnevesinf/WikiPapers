package br.ufrgs.inf01059.wikipapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import br.ufrgs.inf01059.wikinotes.R;

/**
 * An activity representing a single Note detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PaperListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PaperDetailFragment}.
 */
public class PaperDetailActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_detail);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		// savedInstanceState is non-null when there is fragment state
		// saved from previous configurations of this activity
		// (e.g. when rotating the screen from portrait to landscape).
		// In this case, the fragment will automatically be re-added
		// to its container so we don't need to manually add it.
		// For more information, see the Fragments API guide at:
		//
		// http://developer.android.com/guide/components/fragments.html
		//
		if (savedInstanceState == null) {
			// Create the detail fragment and add it to the activity
			// using a fragment transaction.

			Bundle arguments = new Bundle();
			arguments.putString(PaperDetailFragment.ARG_ITEM_ID, getIntent()
					.getStringExtra(PaperDetailFragment.ARG_ITEM_ID));
			PaperDetailFragment fragment = new PaperDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.add(R.id.note_detail_container, fragment).commit();
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   
		// Inflate the options menu from XML
		getMenuInflater().inflate(R.menu.note_detail, menu);
	   	    
	    return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpTo(this, new Intent(this,
					PaperListActivity.class));
			
			return true;
		case R.id.edit_note:
			
			return true;
		case R.id.save_note:
			
			return true;
			
        case R.id.return_button:
        	finish();
			return true;	
		case R.id.delete_note:
						
			Intent deleteIntent = new Intent(Intent.ACTION_DELETE);
			deleteIntent.putExtra("itemId", getIntent()
					.getStringExtra(PaperDetailFragment.ARG_ITEM_ID));
	        startActivity(deleteIntent);
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
}
