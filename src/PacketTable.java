import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.HashSet;

interface PacketTableConstants {
    String TIMESTAMP_COL_NAME = "Timestamp";
    String SRC_COL_NAME = "Source IP";
    String SRC_PORT_COL_NAME = "Source Port";
    String DEST_COL_NAME = "Destination IP";
    String DEST_PORT_COL_NAME = "Destination Port";
    String SIZE_COL_NAME = "IP Packet Size";
    String[] COL_NAMES = {TIMESTAMP_COL_NAME, SRC_COL_NAME, SRC_PORT_COL_NAME, DEST_COL_NAME, DEST_PORT_COL_NAME, SIZE_COL_NAME};

    int TIMESTAMP_COL = 0;
    int SRC_COL = 1;
    int SRC_PORT_COL = 2;
    int DEST_COL = 3;
    int DEST_PORT_COL = 4;
    int SIZE_COL = 5;
}

public class PacketTable extends JTable implements PropertyChangeListener, PacketTableConstants {

    protected static final Color EVEN_ROW_COLOR = new Color(245, 245, 245);
    protected static final Color BOTTOM_ROW_COLOR = new Color(225, 225, 225);

    // For hiding/showing columns
    private static HashSet<String> visibleCols = new HashSet<>(Arrays.asList(COL_NAMES));
    private TableColumn timestampCol, srcIPCol, srcPortCol, destIPCol, destPortCol, sizeCol;

    /**
     * Creates a new PacketTable
     */
    public PacketTable() {
        super();
        setCellSelectionEnabled(true);
        addPropertyChangeListener("model", this);
        setModel(new PacketTableModel(new Packet[0], true));
    }

    /**
     * Sets the given column name to be shown/hidden.
     *
     * @param columnName the name of the column to show/hide
     * @param setVisible true to show the column, false to hide it
     * @see PacketTableConstants
     */
    public static void setColumnVisibility(String columnName, boolean setVisible) {
        if (setVisible) {
            visibleCols.add(columnName);
        } else {
            visibleCols.remove(columnName);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            timestampCol = getColumn(TIMESTAMP_COL_NAME);
            srcIPCol = getColumn(SRC_COL_NAME);
            srcPortCol = getColumn(SRC_PORT_COL_NAME);
            destIPCol = getColumn(DEST_COL_NAME);
            destPortCol = getColumn(DEST_PORT_COL_NAME);
            sizeCol = getColumn(SIZE_COL_NAME);
        } catch (IllegalArgumentException e) {
            System.out.println(e);
        }

        updateColumnVisibility();
    }

    /**
     * Refreshes the visible columns on this table, showing/hiding columns as needed.
     */
    public void updateColumnVisibility() {
        TableColumn tableColumn;
        int lastCol;
        for (String columnName : COL_NAMES) {
            tableColumn = getTableColumnByName(columnName);
            if (tableColumn == null) {
                continue;
            }
            if (visibleCols.contains(columnName)) {
                try {
                    // If no error is thrown this column is already being displayed on this table
                    getColumn(columnName);
                } catch (IllegalArgumentException e) {
                    addColumn(tableColumn);
                    lastCol = getColumnCount() - 1;
                    moveColumn(lastCol, Math.min(lastCol, tableColumn.getModelIndex()));
                }
            } else {
                removeColumn(tableColumn);
            }
        }
    }

    private TableColumn getTableColumnByName(String columnName) {
        TableColumn tableColumn;
        switch (columnName) {
            case TIMESTAMP_COL_NAME:
                tableColumn = timestampCol;
                break;
            case SRC_COL_NAME:
                tableColumn = srcIPCol;
                break;
            case SRC_PORT_COL_NAME:
                tableColumn = srcPortCol;
                break;
            case DEST_COL_NAME:
                tableColumn = destIPCol;
                break;
            case DEST_PORT_COL_NAME:
                tableColumn = destPortCol;
                break;
            case SIZE_COL_NAME:
                tableColumn = sizeCol;
                break;
            default:
                throw new IllegalArgumentException("No column with name \"" + columnName + "\"");
        }
        return tableColumn;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {

        Component component = super.prepareRenderer(renderer, row, column);

        Color bgColor;

        if (isCellSelected(row, column)) {
            bgColor = getSelectionBackground();
        } else if (row >= getRowCount() - 2) {
            bgColor = BOTTOM_ROW_COLOR;
        } else if (row % 2 == 0) {
            bgColor = EVEN_ROW_COLOR;
        } else {
            bgColor = Color.white;
        }

        component.setBackground(bgColor);

        return component;
    }

}
