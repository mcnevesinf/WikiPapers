package br.ufrgs.inf01059.wikipapers;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import br.ufrgs.inf01059.wikinotes.R;
import br.ufrgs.inf01059.wikipapers.model.Note;
import br.ufrgs.inf01059.wikipapers.model.NotesDAO;

/**
 
 */
public class CreateNoteActivity extends ActionBarActivity {

	private EditText mTitleText;
	private EditText mBodyText;
	private Note mNote;
	private boolean mEdit = false;

	public static final String ARG_ITEM_ID = "item_id";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_note_layout);
		setTitle("Create Note");

		mTitleText = (EditText) findViewById(R.id.edit_title);
		mBodyText = (EditText) findViewById(R.id.edit_body);

		// Show the Up button in the action bar.
		getActionBar().setDisplayHomeAsUpEnabled(true);

		Intent intent = getIntent();
		if (intent.hasExtra(ARG_ITEM_ID)) {

			mEdit = true;
			setTitle("Edit Note");
			int noteId = Integer.parseInt(intent.getStringExtra(ARG_ITEM_ID));
			mNote = NotesDAO.getNote(getApplicationContext(), noteId);
			mTitleText.setText(mNote.title);
			mBodyText.setText(mNote.content);
		}


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
			case R.id.save_note:

				if (mEdit) {
					NotesDAO.editNote(getApplicationContext(), Integer.parseInt(mNote.id),
							mTitleText.getText().toString(), mBodyText.getText().toString());
					Toast.makeText(getApplicationContext(), "Note Edited!", Toast.LENGTH_SHORT).show();
				} else {
					NotesDAO.createNote(getApplicationContext(),
							mTitleText.getText().toString(), mBodyText.getText().toString());
					Toast.makeText(getApplicationContext(), "Created New Note!", Toast.LENGTH_SHORT).show();
				}

				Intent saveNoteIntent = new Intent();
				setResult(RESULT_OK, saveNoteIntent);
				finish();

				return true;
			case R.id.cancel:
				Intent cancelNoteIntent = new Intent();
				setResult(RESULT_CANCELED, cancelNoteIntent);
				finish();
				return true;

		}
		return super.onOptionsItemSelected(item);
	}

}

