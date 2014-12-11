package com.wspurgin.andsimplenote;

/**
 * Created by Will on 12/11/14.
 */
public class SimpleNote {
    private String mTitle;
    private String mBody;

    public SimpleNote() {}

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
}
