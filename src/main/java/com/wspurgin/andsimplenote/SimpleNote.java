package com.wspurgin.andsimplenote;

import com.evernote.edam.type.Note;

import java.io.Serializable;

/**
 * Created by Will on 12/11/14.
 * A data model representation of a note created in AndSimpleNote
 */
public class SimpleNote implements Serializable {
    private String mTitle;
    private String mBody;

    public SimpleNote() {}

    public SimpleNote(SimpleNote other) {
        mTitle = other.getTitle();
        mBody = other.getBody();
    }

    public SimpleNote(Note note) {
        this(NoteConverter.toSimpleNote(note));
    }

    public SimpleNote(String title, String body) {
        mTitle = title;
        mBody = body;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        this.mBody = body;
    }

    public String toString() {
        return this.mTitle;
    }
}
