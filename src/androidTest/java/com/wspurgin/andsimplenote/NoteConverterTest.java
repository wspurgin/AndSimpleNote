package com.wspurgin.andsimplenote;

import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.Assert;

/**
 * Created by Will on 12/11/14.
 * Test of NoteConverter
 */
public class NoteConverterTest implements Test {
    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public int countTestCases() {
        return 0;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public void run(TestResult testResult) {
        Assert.assertEquals("Proper Tag remove",
                String.format("Hello,%nWorld%n"),
                NoteConverter.unXHTMLify("<div>Hello,</div><div>World</div>"));
        Assert.assertEquals("Proper Tag insertion",
                "<div>Hello,</div><div>World</div>",
                NoteConverter.XHTMLify(String.format("Hello,%nWorld%n")));
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
