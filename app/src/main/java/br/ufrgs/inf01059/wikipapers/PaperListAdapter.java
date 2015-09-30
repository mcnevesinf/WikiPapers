package br.ufrgs.inf01059.wikipapers;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import br.ufrgs.inf01059.wikipapers.model.Note;
import br.ufrgs.inf01059.wikinotes.R;

public class PaperListAdapter extends BaseAdapter{
	
	private Context context;
    private List<Note> noteList;
    GregorianCalendar calendar;
    SimpleDateFormat sdf;
    
    
    public PaperListAdapter(Context context, List<Note> noteList){
        this.context = context;
        this.noteList = noteList;
        calendar = new GregorianCalendar();
        sdf = new SimpleDateFormat("dd/MM/yyyy HH'h'mm");
    }
    
    @Override
    public int getCount() {
        return noteList.size();
    }
 
    @Override
    public Object getItem(int position) {
        return noteList.get(position);
    }
 
    @Override
    public long getItemId(int position) {
    	return Long.parseLong(noteList.get(position).id);
    	
    }
 
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the note of the current position
        Note note = noteList.get(position);
        
        ViewHolder holder;
        
        // Instance of the XML layout for each note  
        if (convertView == null) {
            
        LayoutInflater inflater = (LayoutInflater)
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(R.layout.list_view_notes, null);
        
        holder = new ViewHolder();
        holder.title = (TextView) convertView.findViewById(R.id.textTitle);
        holder.date = (TextView) convertView.findViewById(R.id.textDate);
        
        convertView.setTag(holder);
        
        } else {
           
            holder = (ViewHolder) convertView.getTag();
        }
 
        
        // Note Title
        holder.title.setText(note.title);
        
        // Note Date
        calendar.setTime(note.creationDate);
        holder.date.setText("Modified Date: " + sdf.format(calendar.getTime()));
 
        return convertView;
        
    }
    
    static class ViewHolder {
    	
        public TextView title;
        public TextView date;
    }

}
