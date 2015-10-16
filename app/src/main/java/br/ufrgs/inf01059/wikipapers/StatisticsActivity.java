package br.ufrgs.inf01059.wikipapers;

/**
 * Created by miguel on 10/15/15.
 */
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import br.ufrgs.inf01059.wikinotes.R;
import br.ufrgs.inf01059.wikipapers.model.Note;
import br.ufrgs.inf01059.wikipapers.model.NotesDAO;

public class StatisticsActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.stats_layout);
        setTitle("Statistics");

        // Show the Up button in the action bar.
        getActionBar().setDisplayHomeAsUpEnabled(true);

        SharedPreferences sharedPrefSettings = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences sharedPref = this.getSharedPreferences(getString(R.string.preference_file_id),
                                                                 Context.MODE_PRIVATE);

        StringBuilder screenText = new StringBuilder();

        //Username
        screenText.append("Username: " + sharedPrefSettings.getString("username", "DefaultUser") + "\n");

        //Total of notes
        Context context = getApplicationContext();
        List<Note> Notes = NotesDAO.getNotes(context);
        int nNotes = Notes.size();
        screenText.append("Number of notes: " + nNotes + "\n");

        //Number of synchronized notes
        int nSyncNotes = sharedPref.getInt("nSyncNotes", 0);
        screenText.append("Number of notes synced: " + nSyncNotes + "\n");

        //Last synchronization date
        Long longSyncDate = sharedPref.getLong("syncDate", 0);
        String formattedDate;
        if (longSyncDate == 0){
            formattedDate = "-";
        }
        else {
            Date unformattedDate = new Date(longSyncDate*1000);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            formattedDate = dateFormat.format(unformattedDate);
        }
        screenText.append("Synchronization date: " + formattedDate + "\n");

        TextView statsScreenTextView = (TextView) findViewById(R.id.usernameSettings);
        statsScreenTextView.setText(screenText.toString());
        //Intent intent = getIntent();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the options menu from XML
        getMenuInflater().inflate(R.menu.view_stats, menu);
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
            case R.id.cancel_stats:
                Intent cancelStatsIntent = new Intent();
                setResult(RESULT_CANCELED, cancelStatsIntent);
                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

}
