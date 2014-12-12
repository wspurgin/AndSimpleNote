package com.wspurgin.andsimplenote;

import android.util.Log;

import com.evernote.client.android.EvernoteUtil;
import com.evernote.edam.type.Note;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Will on 12/11/14.
 * A class for converting between the data models used in AndSimpleNote and Evernote's data types.
 */
public class NoteConverter {

    private static final String LOGTAG = "ASN-NoteConverter";

    private static final String noteOpenTag = "<en-note>";
    private static final String noteCloseTag = "</en-note>";

    public static Note toEverNote(SimpleNote simpleNote) {
        String content = EvernoteUtil.NOTE_PREFIX + XHTMLify(simpleNote.getBody()) + EvernoteUtil.NOTE_SUFFIX;
        Note note = new Note();
        note.setTitle(simpleNote.getTitle());
        note.setContent(content);
        note.setGuid(simpleNote.getGuid());
        return note;
    }

    public static String XHTMLify(String text) {
        String[] pieces = text.split("\\n");
        String glued = "";
        if (pieces.length > 1) {
            for (String piece : pieces) {
                glued += "<div>" + piece + "</div>";
            }
        } else {
            // text was only one line long which is fine
            glued = text;
        }
        return glued;
    }

    public static String unXHTMLify(String xhtml) {
        String body = "";
        String stripEverNoteTags = "(.+)" + noteOpenTag + "(.+)"+ noteCloseTag + "$";
        Log.i(LOGTAG, stripEverNoteTags);
        if (xhtml.matches(stripEverNoteTags))
            xhtml = xhtml.replaceAll(stripEverNoteTags, "$2"); // strip off xml and en-note tags
        else {
            Log.i(LOGTAG, "No Evernote xml tags detected for xhtml");
        }

        if (xhtml.matches("(?i)(<div.*?>)(.+?)(</div>)")) {
            body = xhtml.replaceAll("(?i)(<div.*?>)(.+?)(</div>)", "$2"+String.format("%n"));
        } else {
            body = xhtml;
        }
        return body;
    }

    public static SimpleNote toSimpleNote(Note everNote) {
        SimpleNote note = new SimpleNote();
        note.setTitle(everNote.getTitle()); // the easy parts
        note.setGuid(everNote.getGuid());

        // Parse the contents to un-XHTMLify it.
        String body = unXHTMLify(everNote.getContent());
        note.setBody(body);
        return note;
    }
}
