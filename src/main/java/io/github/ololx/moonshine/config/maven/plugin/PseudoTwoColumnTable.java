package io.github.ololx.moonshine.config.maven.plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PseudoTwoColumnTable {

    private static final String ROW_FORMAT = '\u2502' + " %-40s " + '\u2502' + " %-60s " + '\u2502';

    private static final String TOP_ROW_BORDER = '\u250C'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u252C"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2510';

    private static final String MIDDLE_ROW_BORDER = '\u251C'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u253C"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2524';

    private static final String BOTTOM_ROW_BORDER = '\u2514'
            + Stream.iterate(0, i -> i++).limit(42).map(i -> "\u2500").reduce(String::concat).get()
            + "\u2534"
            + Stream.iterate(0, i -> i++).limit(62).map(i -> "\u2500").reduce(String::concat).get()
            + '\u2518';

    private List<String> header;

    private Map<Long, List<Object>> body;

    private long bodyRowsCount;

    public PseudoTwoColumnTable() {
        this.header = new ArrayList<>();
        this.body = new HashMap<>();
    }

    public void setHeader(List<String> header) {
        if (header == null) {
            throw new IllegalArgumentException("The header must be not null");
        } else if (header.isEmpty()) {
            throw new IllegalArgumentException("The header must contains at least one column name");
        }

        this.header = header;
    }

    public void setBody(Map<Long, List<Object>> body) {
        if (body == null) {
            throw new IllegalArgumentException("The body must be not null");
        } else if (body.isEmpty()) {
            throw new IllegalArgumentException("The body must contains at least one row");
        }

        this.body = body;
        this.bodyRowsCount = body.size();
    }

    public void addBodyRow(List<Object> row) {
        if (row == null) {
            throw new IllegalArgumentException("The body row must be not null");
        } else if (row.isEmpty()) {
            throw new IllegalArgumentException("The body row must contains at least one row");
        }

        this.body.put(++bodyRowsCount, row);
    }

    public void print(Consumer<String> out) {
        if (this.header.size() != 2) {
            throw new RuntimeException("The header must contains of two cells");
        }

        out.accept(TOP_ROW_BORDER);

        final String headerString = String.format(ROW_FORMAT, header.get(0), header.get(1));
        out.accept(headerString);

        for (Map.Entry<Long, List<Object>> rowEntry : this.body.entrySet()) {
            final List<Object> row = rowEntry.getValue();
            if (row.size() < 2) {
                continue;
            }

            final String[] rowValues = getStringOrSplitInArray(String.valueOf(row.get(1)), 60);

            out.accept(MIDDLE_ROW_BORDER);

            for (int valuePointer = 0; valuePointer < rowValues.length; valuePointer++) {
                if (valuePointer == (rowValues.length - 1) / 2) {
                    out.accept(String.format(ROW_FORMAT, row.get(0), rowValues[valuePointer]));
                } else {
                    out.accept(String.format(ROW_FORMAT, "", rowValues[valuePointer]));
                }
            }
        }

        out.accept(BOTTOM_ROW_BORDER);
    }

    private String[] getStringOrSplitInArray(String str, int size) {
        return str == null ? new String[0] : str.split("(?<=\\G.{"+size+"})");
    }
}
