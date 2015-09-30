package br.ufrgs.inf01059.wikipapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import br.ufrgs.inf01059.wikinotes.R;

/**
 
 */
public class CreateNoteActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_note_layout);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);
	
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   
		// Inflate the options menu from XML
		getMenuInflater().inflate(R.menu.create_note_detail, menu);
	   	    
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
