import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

public class PacketTable extends JTable {

    protected static final Color EVEN_ROW_COLOR = new Color(250, 250, 250);
    protected static final Color BOTTOM_ROW_COLOR = new Color(225, 225, 225);

    /**
     * Creates a new PacketTable
     */
    PacketTable() {
        setCellSelectionEnabled(true);
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
