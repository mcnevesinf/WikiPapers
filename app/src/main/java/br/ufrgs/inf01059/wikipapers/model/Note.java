package br.ufrgs.inf01059.wikipapers.model;

import java.util.Date;

public class Note {
	public String id;
	public String title;
	public String content;
    public Date creationDate;
    
	public Note(String id, String title, String content, Date creationDate) {
		this.id = id;
		this.title = title;
		this.content = content;
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		return title;
	}
}
