package io.github.ololx.moonshine.config.maven.plugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PseudoTable {

    private final String topRowBorder;

    private final String middleRowBorder;

    private final String bottomRowBorder;

    private List<String> header;

    private Map<Integer, List<Object>> body;

    private int bodyRowsCount;

    private List<ColumnFormat> columnFormats = new ArrayList<>();

    public PseudoTable(List<ColumnFormat> columnFormats) {
        this.header = new ArrayList<>();
        this.body = new HashMap<>();
        this.columnFormats = columnFormats;

        topRowBorder =  columnFormats.stream().map(columnFormat -> {
            final StringBuilder line = new StringBuilder();
            line.append(IntStream.range(0, columnFormat.getWidth() + 2).mapToObj(formatPointer -> String.valueOf('\u2500')).collect(Collectors.joining()));

            if (columnFormat.getIndex() == 0) {
                line.insert(0, '\u250C');
            }

            if (columnFormat.getIndex() == columnFormats.size() - 1) {
                line.append('\u2510');
            } else {
                line.append('\u252C');
            }

            return line.toString();
        }).collect(Collectors.joining());

        middleRowBorder =  columnFormats.stream().map(columnFormat -> {
            final StringBuilder line = new StringBuilder();
            line.append(IntStream.range(0, columnFormat.getWidth() + 2).mapToObj(formatPointer -> String.valueOf('\u2500')).collect(Collectors.joining()));

            if (columnFormat.getIndex() == 0) {
                line.insert(0, '\u251C');
            }

            if (columnFormat.getIndex() == columnFormats.size() - 1) {
                line.append('\u2524');
            } else {
                line.append('\u253C');
            }

            return line.toString();
        }).collect(Collectors.joining());

        bottomRowBorder =  columnFormats.stream().map(columnFormat -> {
            final StringBuilder line = new StringBuilder();
            line.append(IntStream.range(0, columnFormat.getWidth() + 2).mapToObj(formatPointer -> String.valueOf('\u2500')).collect(Collectors.joining()));

            if (columnFormat.getIndex() == 0) {
                line.insert(0, '\u2514');
            }

            if (columnFormat.getIndex() == columnFormats.size() - 1) {
                line.append('\u2518');
            } else {
                line.append('\u2534');
            }

            return line.toString();
        }).collect(Collectors.joining());
    }

    public void setHeader(List<String> header) {
        if (header == null) {
            throw new IllegalArgumentException("The header must be not null");
        } else if (header.isEmpty()) {
            throw new IllegalArgumentException("The header must contains at least one column name");
        }

        this.header = header;
    }

    public void setBody(Map<Integer, List<Object>> body) {
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

        out.accept(topRowBorder);

        final String headerString = '\u2502' + columnFormats.stream()
                .map(format -> String.format(format.format(), header.get(format.index)) + '\u2502')
                .collect(Collectors.joining());
        out.accept(headerString);

        for (Map.Entry<Integer, List<Object>> rowEntry : this.body.entrySet()) {
            final List<Object> row = rowEntry.getValue();
            if (row.isEmpty()) {
                continue;
            }

            out.accept(middleRowBorder);

            final Map<Integer, List<String>> rowCells = reformatRowForColumnWidth(row);
            rowCells.entrySet().forEach(rowCellEntry -> {
                StringBuilder printedRow = new StringBuilder("\u2502");
                List<String> cells = rowCellEntry.getValue();

                columnFormats.forEach(format -> {
                    printedRow.append(String.format(format.format(), cells.get(format.getIndex())));
                    printedRow.append('\u2502');
                });

                out.accept(printedRow.toString());
            });
        }

        out.accept(bottomRowBorder);
    }

    private Map<Integer, List<String>> reformatRowForColumnWidth(List<Object> row) {
        final Map<Integer, List<String>> rowCells = new HashMap<>();
        columnFormats.forEach(format -> {
            final List<String> cellRows = getStringOrSplitInArray(String.valueOf(row.get(format.getIndex())), format.getWidth());
            IntStream.range(0, cellRows.size()).forEach(cellRowIterator -> {
                final List<String> cells = rowCells.getOrDefault(cellRowIterator, new ArrayList<>(columnFormats.size()));

                while (cells.size() < format.getIndex()) {
                    cells.add("");
                }

                cells.add(cellRows.get(cellRowIterator));
                rowCells.put(cellRowIterator, cells);
            });
        });

        return rowCells;
    }

    private List<String> getStringOrSplitInArray(String str, int size) {
        return Arrays.stream(str.split("(?<=\\G.{"+size+"})")).collect(Collectors.toList());
    }

    public static class ColumnFormat {

        private final String format;

        private final int index;

        private final int width;

        public ColumnFormat(int index, int width) {
            this.index = index;
            this.width = width;

            this.format = " %-" + width + "s ";
        }

        public int getIndex() {
            return index;
        }

        public int getWidth() {
            return this.width;
        }

        public boolean isBordered() {
            return true;
        }

        public String format() {
            return this.format;
        }
    }
}
