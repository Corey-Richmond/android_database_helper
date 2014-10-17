package com.vokal.codegen.tools;

import java.util.ArrayList;

public abstract class Formatter {

    protected static final String NEWLINE = "\n";

    protected int    mOpenBracketCount   = 0;
    protected int    mClosedBracketCount = 0;
    protected String mIndents            = "";

    protected ArrayList<String> mLines = new ArrayList<>();


    abstract Formatter addLine(String... aStrings);

    abstract String format();

    protected void append(StringBuilder builder, String line) {
        countClosedBrackets(line);

        getIndents();
        line = mIndents + line + NEWLINE;
        countOpenBrackets(line);
        builder.append(line);
    }

//    protected void parseData(String string) {
//        countBrackets(string);
//        getIndents();
//    }

    private void countClosedBrackets(String aString) {
        mClosedBracketCount += aString.length() - aString.replace("}", "").length();
    }

    private void countOpenBrackets(String aString) {
        mOpenBracketCount   += aString.length() - aString.replace("{", "").length();
    }

    private void countBrackets(String aString) {
        mOpenBracketCount   += aString.length() - aString.replace("{", "").length();
        mClosedBracketCount += aString.length() - aString.replace("}", "").length();
    }

    private String getIndents() {
        mIndents = "";
        for (int i = 0; i < (mOpenBracketCount - mClosedBracketCount); i++) {
            mIndents += "\t";
        }
        return mIndents;
    }
}
