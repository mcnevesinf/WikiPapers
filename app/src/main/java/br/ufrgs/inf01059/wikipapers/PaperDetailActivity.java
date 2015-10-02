package br.ufrgs.inf01059.wikipapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import br.ufrgs.inf01059.wikinotes.R;
import br.ufrgs.inf01059.wikipapers.model.NotesDAO;

/**
 * An activity representing a single Note detail screen. This activity is only
 * used on handset devices. On tablet-size devices, item details are presented
 * side-by-side with a list of items in a {@link PaperListActivity}.
 * <p>
 * This activity is mostly just a 'shell' activity containing nothing more than
 * a {@link PaperDetailFragment}.
 */

public class PaperDetailActivity extends ActionBarActivity {
	
	private final int EDIT_NOTE = 1;

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
			
			setTitle(NotesDAO.getNote(getApplicationContext(),Integer.parseInt(getIntent()
					.getStringExtra(PaperDetailFragment.ARG_ITEM_ID))).title);
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

			Intent editNoteIntent = new Intent(this, CreateNoteActivity.class);
			editNoteIntent.putExtra(CreateNoteActivity.ARG_ITEM_ID, getIntent());
			
			return true;
		case R.id.save_note:
			
			return true;

        case R.id.return_button:
        	Intent returnNoteIntent = new Intent();
		    setResult(RESULT_CANCELED, returnNoteIntent);  
			finish();
			return true;	
			
		case R.id.delete_note:
			NotesDAO.deleteNote(getApplicationContext(), Integer.parseInt(getIntent()
					.getStringExtra(PaperDetailFragment.ARG_ITEM_ID)));
			Toast.makeText(getApplicationContext(), "Note Deleted!", Toast.LENGTH_SHORT).show();
			Intent deleteNoteIntent = new Intent();
		    setResult(RESULT_OK, deleteNoteIntent);  
			finish();
			
			
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == EDIT_NOTE && resultCode == RESULT_OK){
	    	Intent editNoteIntent = new Intent();
	        setResult(RESULT_OK, editNoteIntent);  
		    finish();
	    } 	
	    
	}
	
}
