package br.ufrgs.inf01059.wikipapers;

import br.ufrgs.inf01059.wikipapers.model.NotesDAO;
import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import br.ufrgs.inf01059.wikinotes.R;
import br.ufrgs.inf01059.wikipapers.SnmpAgent.AgentService;


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

	/** Messenger for communicating with service. */
	Messenger mService = null;
	/** Flag indicating whether we have called bind on the service. */
	boolean mIsBound;
	private String noteId = null;
	private final int ADD_NOTE = 1;
	private final int EDIT_NOTE = 2;
	private final int DETAIL_NOTE = 3;
	
	private boolean mReturningWithResult = false;
	
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

		Intent intent = new Intent(this, AgentService.class);
		startService(intent);
		doBindAgentService();

		// Verifying if we are filtering a intent, which means someone
		// clicked in a note's link and is arriving here. See manifest
		// and the intent filters defined for this activity
		handleIntent(getIntent());
	}

	/**
	 * Handler of incoming messages from service.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
				/*case AgentService.MSG_SET_VALUE:
					break;*/

				//case AgentService.MSG_SNMP_REQUEST_RECEIVED:
					/*
					aux.setText(AgentService.lastRequestReceived);
					messagesReceivedScrollView.addView(aux);*/

				//	break;

				//case AgentService.MSG_MANAGER_MESSAGE_RECEIVED:
					/*MIBtree miBtree = MIBtree.getInstance();
					String message = miBtree.getNext(MIBtree.MNG_MANAGER_MESSAGE_OID).getVariable().toString();
					messagesReceivedAdapter.add(message);
					messagesReceivedAdapter.notifyDataSetChanged();*/
				//	break;

				default:
					super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className,
									   IBinder service) {

			mService = new Messenger(service);

			// We want to monitor the service for as long as we are
			// connected to it.
			/*try {
				Message msg = Message.obtain(null,
						AgentService.MSG_REGISTER_CLIENT);
				msg.replyTo = mMessenger;
				mService.send(msg);

				// Give it some value as an example.
				msg = Message.obtain(null,
						AgentService.MSG_SET_VALUE, this.hashCode(), 0);
				mService.send(msg);
			} catch (RemoteException e) {

			}*/

		}

		public void onServiceDisconnected(ComponentName className) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
		}
	};

	void doBindAgentService() {
		// Establish a connection with the service.  We use an explicit
		// class name because there is no reason to be able to let other
		// applications replace our component.
		bindService(new Intent(this, AgentService.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindAgentService() {
		if (mIsBound) {
			// If we have received the service, and hence registered with
			// it, then now is the time to unregister.
			if (mService != null) {
				/*(try {
					Message msg = Message.obtain(null,
							AgentService.MSG_UNREGISTER_CLIENT);
					msg.replyTo = mMessenger;
					mService.send(msg);
				} catch (RemoteException e) {

				}*/
			}

			// Detach our existing connection.
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected void onDestroy() {
		doUnbindAgentService();
		super.onDestroy();
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

			Intent createNoteIntent = new Intent(this, CreateNoteActivity.class);
			startActivityForResult(createNoteIntent, ADD_NOTE);

			return true;
			
        case R.id.all_notes:
			
        	fillDataTransaction("", "date DESC", "");

        	noteId = null;
        	return true;
			
        case R.id.delete_note:
        	if(noteId != null){
        	     	NotesDAO.deleteNote(getApplicationContext(), Integer.parseInt(noteId));	
        	     	Toast.makeText(getApplicationContext(), "Note Deleted!", Toast.LENGTH_SHORT).show();
			    	fillDataTransaction("", "date DESC", "");
			    	onItemSelected(null);
			    	
        	}
        	else
        		Toast.makeText(getApplicationContext(), "Select Note First!", Toast.LENGTH_SHORT).show();
        	return true;
        	
        case R.id.edit_note:
        	Intent editNoteIntent = new Intent(this, CreateNoteActivity.class);
			editNoteIntent.putExtra(CreateNoteActivity.ARG_ITEM_ID, noteId);
			startActivityForResult(editNoteIntent, EDIT_NOTE);
			
        	return true;	
        case R.id.return_button:
        	
        	if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
	            getSupportFragmentManager().popBackStack();
	            
	        }
	        else{
	            finish();
	        }
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
		
		noteId = id;
		if (mTwoPane) {
			
			// In two-pane mode, show the detail view in this activity by
			// adding or replacing the detail fragment using a
			// fragment transaction.
			Bundle arguments = new Bundle();
			arguments.putString(PaperDetailFragment.ARG_ITEM_ID, id);
			PaperDetailFragment fragment = new PaperDetailFragment();
			fragment.setArguments(arguments);
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.replace(R.id.note_detail_container, fragment);
			
			if(getIntent().getAction().equals(Intent.ACTION_VIEW))
			     ft.addToBackStack(null);
			
			ft.commit();
			
		} else {
			// In single-pane mode, simply start the detail activity
			// for the selected item ID.
			
			Intent detailIntent = new Intent(this, PaperDetailActivity.class);
			detailIntent.putExtra(PaperDetailFragment.ARG_ITEM_ID, id);
			startActivityForResult(detailIntent, DETAIL_NOTE);
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
    	    	
    	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.note_list_container, fragment);
		
		ft.commit();
		
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((resultCode == RESULT_OK) 
        		&& (requestCode == EDIT_NOTE) || (requestCode == DETAIL_NOTE))
            mReturningWithResult = true;
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mReturningWithResult) {
        	fillDataTransaction("", "date DESC", "");
        }
        if(mTwoPane)
        	onItemSelected(noteId);
      
        mReturningWithResult = false;
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
