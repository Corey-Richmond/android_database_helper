package com.vokal.codegen.tools;

import java.util.Collections;

public class FileFormatter extends Formatter {

    @Override
    Formatter addLine(String... aStrings) {
        Collections.addAll(mLines, aStrings);
        return this;
    }

    @Override
    String format() {
        StringBuilder builder = new StringBuilder();
        for (String line : mLines) {
            append(builder, line);
        }
        return builder.toString();
    }
}
