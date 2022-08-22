package io.github.ololx.moonshine.config.maven.plugin;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PseudoTable {

    private final BorderFormat topRowBorderFormat;

    private final BorderFormat middleRowBorder;

    private final BorderFormat bottomRowBorder;

    private List<String> header;

    private Map<Integer, List<Object>> body;

    private int bodyRowsCount;

    private List<ColumnFormat> columnFormats = new ArrayList<>();

    public PseudoTable(List<ColumnFormat> columnFormats) {
        this.header = new ArrayList<>();
        this.body = new HashMap<>();
        this.columnFormats = columnFormats;
        this.topRowBorderFormat = new BorderFormat(
                BorderStyle.builder()
                        .base(BorderStyle.BASE)
                        .left(BorderStyle.TOP_LEFT)
                        .right(BorderStyle.TOP_RIGHT)
                        .middle(BorderStyle.TOP_MIDDLE)
                        .build(),
                columnFormats
        );
        this.middleRowBorder = new BorderFormat(
                BorderStyle.builder()
                        .base(BorderStyle.BASE)
                        .left(BorderStyle.MIDDLE_LEFT)
                        .right(BorderStyle.MIDDLE_RIGHT)
                        .middle(BorderStyle.MIDDLE_MIDDLE)
                        .build(),
                columnFormats
        );
        this.bottomRowBorder = new BorderFormat(
                BorderStyle.builder()
                        .base(BorderStyle.BASE)
                        .left(BorderStyle.BOTTOM_LEFT)
                        .right(BorderStyle.BOTTOM_RIGHT)
                        .middle(BorderStyle.BOTTOM_MIDDLE)
                        .build(),
                columnFormats
        );
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

        out.accept(topRowBorderFormat.format);

        final String headerString = '\u2502' + columnFormats.stream()
                .map(format -> String.format(format.format(), header.get(format.index)) + '\u2502')
                .collect(Collectors.joining());
        out.accept(headerString);

        for (Map.Entry<Integer, List<Object>> rowEntry : this.body.entrySet()) {
            final List<Object> row = rowEntry.getValue();
            if (row.isEmpty()) {
                continue;
            }

            out.accept(middleRowBorder.format);

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

        out.accept(bottomRowBorder.format);
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

    public static class BorderStyle {

        public static final char TOP_LEFT = '\u250C';

        public static final char TOP_RIGHT = '\u2510';

        public static final char TOP_MIDDLE = '\u252C';

        public static final char MIDDLE_LEFT = '\u251C';

        public static final char MIDDLE_RIGHT = '\u2524';

        public static final char MIDDLE_MIDDLE = '\u253C';

        public static final char BOTTOM_LEFT = '\u2514';

        public static final char BOTTOM_RIGHT = '\u2518';

        public static final char BOTTOM_MIDDLE = '\u2534';

        public static final char BASE = '\u2500';

        private final char left;

        private final char middle;

        private final char right;

        private final char base;

        public BorderStyle(char left, char middle, char right, char base) {
            this.left = left;
            this.middle = middle;
            this.right = right;
            this.base = base;
        }

        public char getBase() {
            return base;
        }

        public char getLeft() {
            return left;
        }

        public char getMiddle() {
            return middle;
        }

        public char getRight() {
            return right;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {

            private static final char DEFAULT_STYLE = '*';

            private char left = DEFAULT_STYLE;

            private char middle = DEFAULT_STYLE;

            private char right = DEFAULT_STYLE;

            private char base = DEFAULT_STYLE;

            private Builder() {}

            public Builder base(char base) {
                this.base = base;

                return this;
            }

            public Builder left(char left) {
                this.left = left;

                return this;
            }

            public Builder middle(char middle) {
                this.middle = middle;

                return this;
            }

            public Builder right(char right) {
                this.right = right;

                return this;
            }

            public BorderStyle build() {
                return new BorderStyle(
                        this.left,
                        this.middle,
                        this.right,
                        this.base
                );
            }
        }
    }

    public static class BorderFormat {

        private final String format;

        public BorderFormat(BorderStyle style, Collection<ColumnFormat> columnFormats) {
            this.format = columnFormats.stream().map(columnFormat -> {
                final StringBuilder line = new StringBuilder();
                line.append(
                        IntStream.range(0, columnFormat.getWidth() + 2)
                                .mapToObj(formatPointer -> {
                                    return String.valueOf(style.base);
                                }).collect(Collectors.joining())
                );

                if (columnFormat.getIndex() == 0) {
                    line.insert(0, style.left);
                }

                if (columnFormat.getIndex() == columnFormats.size() - 1) {
                    line.append(style.right);
                } else {
                    line.append(style.middle);
                }

                return line.toString();
            }).collect(Collectors.joining());
        }

        public String format() {
            return this.format;
        }
    }
}
