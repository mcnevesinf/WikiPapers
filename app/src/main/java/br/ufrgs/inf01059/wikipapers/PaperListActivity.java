package br.ufrgs.inf01059.wikipapers;

import br.ufrgs.inf01059.wikipapers.model.NotesDAO;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import br.ufrgs.inf01059.wikinotes.R;

/**
 * An activity representing a list of Notes. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link PaperDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link PaperListFragment} and the item details (if present) is a
 * {@link PaperDetailFragment}.
 * <p>
 * This activity also implements the required
 * {@link PaperListFragment.Callbacks} interface to listen for item selections.
 */
public class PaperListActivity extends ActionBarActivity implements
		PaperListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_note_list);
		
		if (findViewById(R.id.note_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

		}	   
		// TODO: If exposing deep links into your app, handle intents here.

		// Verifying if we are filtering a intent, which means someone
		// clicked in a note's link and is arriving here. See manifest
		// and the intent filters defined for this activity
		handleIntent(getIntent());	
		
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	   
		// Inflate the options menu from XML
		getMenuInflater().inflate(R.menu.note_list, menu);
	   	    
	    // Get the SearchView and set the searchable configuration
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.search_notes).getActionView();
	    
	    // Current activity is the searchable activity
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
	    searchView.setSubmitButtonEnabled(true);
	    	    
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add_note:
			
			return true;
			
        case R.id.all_notes:
			
        	fillDataTransaction("", "date DESC", "");
        	return true;
			
		}
		return super.onOptionsItemSelected(item);
	}
	

	/**
	 * Callback method from {@link PaperListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String id) {
		
		if (mTwoPane) {
			
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(PaperDetailFragment.ARG_ITEM_ID, id);
			PaperDetailFragment fragment = new PaperDetailFragment();
			fragment.setArguments(arguments);
			getSupportFragmentManager().beginTransaction()
					.replace(R.id.note_detail_container, fragment).commit();

		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			
			Intent detailIntent = new Intent(this, PaperDetailActivity.class);
			detailIntent.putExtra(PaperDetailFragment.ARG_ITEM_ID, id);
			startActivity(detailIntent);
		}
	}
	
	private void handleIntent(Intent intent) {
		   
		if(Intent.ACTION_MAIN.equals(intent.getAction())){
			fillDataTransaction("", "date DESC", "5");
		}
		else if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String searchQuery = intent.getStringExtra(SearchManager.QUERY);
			fillDataTransaction(searchQuery, "date DESC", "");
		}
		else  if(Intent.ACTION_VIEW.equals(intent.getAction())){
			String id = checkIntentForID();
			onItemSelected(id);
		}		
		else if(Intent.ACTION_DELETE.equals(intent.getAction())){
			NotesDAO.deleteNote(getApplicationContext(), Integer.parseInt(intent.getStringExtra("itemId")));
		    fillDataTransaction("", "date DESC", "");
		    Toast.makeText(getApplicationContext(), "Note Deleted!", Toast.LENGTH_SHORT).show();
		}
	}
	
	/**
	 * Update the note list with a new listFragment transaction
	 * 
	 * @param titleQuery
	 * @param orderByQuery
	 * @param limitQuery
	 */
    public void fillDataTransaction(String titleQuery, String orderByQuery, String limitQuery){
		
		Bundle arguments = new Bundle();
		PaperListFragment fragment = new PaperListFragment();
		arguments.putString(PaperListFragment.TITLE, titleQuery);
    	arguments.putString(PaperListFragment.ORDERBY, orderByQuery);
    	arguments.putString(PaperListFragment.LIMIT, limitQuery);
    	arguments.putBoolean(PaperListFragment.TWO_PANE, mTwoPane);
    	fragment.setArguments(arguments); 
    	getSupportFragmentManager().beginTransaction()
    	.replace(R.id.note_list_container, fragment).commit();
		
	}
	

	/**
	 * Verify the intent that was passed to this activity for any note's ID. If
	 * there are any ID, then it is returned as a String.
	 * 
	 * Otherwise NULL is returned
	 * 
	 * @return The received ID, or NULL
	 */
	private String checkIntentForID() {
		String id = null;
		Uri data = getIntent().getData();
		String ssp = data.getSchemeSpecificPart();
		String[] ssp_split = ssp.split("/");
		if (ssp_split.length > 0) {
			id = ssp_split[ssp_split.length - 1];
		}
		return id;
	}
	
	
}
