package br.ufrgs.inf01059.wikipapers;

import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import br.ufrgs.inf01059.wikipapers.model.Note;
import br.ufrgs.inf01059.wikipapers.model.NotesDAO;
import br.ufrgs.inf01059.wikinotes.R;

/**
 * A fragment representing a single Note detail screen. This fragment is either
 * contained in a {@link PaperListActivity} in two-pane mode (on tablets) or a
 * {@link PaperDetailActivity} on handsets.
 */
public class PaperDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The dummy content this fragment is presenting.
	 */
	private Note mNote;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public PaperDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getArguments().containsKey(ARG_ITEM_ID)) {
			// Load the dummy content specified by the fragment
			// arguments. In a real-world scenario, use a Loader
			// to load content from a content provider.
			mNote = NotesDAO.getNote(getActivity(),
					Integer.parseInt(getArguments().getString(ARG_ITEM_ID)));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_note_detail,
				container, false);

		// Show the dummy content as text in a TextView.
		if (mNote != null) {
			List<Note> notes = NotesDAO.getNotes(getActivity());

			StringBuilder stb = new StringBuilder();
			String[] split = mNote.content.split(" ");
			for (int i = 0; i < split.length; i++) {
				Note note = null;
				for (int j = 0; j < notes.size(); j++) {
					if (split[i].equalsIgnoreCase(notes.get(j).title)) {
						note = notes.get(j);
						break;
					}
				}

				if (note == null) {
					stb.append(split[i]);
				} else {
					// linkify
					stb.append(
							"<a href=\"wikinote-scheme://br.ufrgs.inf01059.wikinotes/")
							.append(note.id).append("\">");
					stb.append(split[i]);
					stb.append("</a>");
				}
				stb.append(" ");
			}

			TextView textView = ((TextView) rootView
					.findViewById(R.id.note_detail));
			textView.setText(Html.fromHtml(stb.toString()));
			textView.setMovementMethod(LinkMovementMethod.getInstance());
		}

		return rootView;
	}
}
