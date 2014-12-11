package com.wspurgin.andsimplenote;

import com.evernote.edam.type.Note;

/**
 * Created by Will on 12/11/14.
 * A class for converting between the data models used in AndSimpleNote and Evernote's data types.
 */
public class NoteConverter {
    private static final String openTag = "<en-note>";
    private static final String closeTag = "</en-note>";
    private static final String xmlDeclaration = "<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>";
    private static final String docType = "<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">";

    public static Note toEverNote(SimpleNote simpleNote) {
        String content = xmlDeclaration + docType + openTag + XHTMLify(simpleNote.getBody()) + closeTag;
        Note note = new Note();
        note.setTitle(simpleNote.getTitle());
        note.setContent(content);
        return note;
    }

    public static String XHTMLify(String text) {
        String[] pieces = text.split("\\n");
        String glued = "";
        for (String piece : pieces) {
            glued += "<div>" + piece + "</div>";
        }
        return glued;
    }
}
