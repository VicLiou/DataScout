package da.api.view.element;

import javax.swing.JScrollPane;

import da.api.model.JTableModel;

public class Table {

        public JScrollPane tableSearchResult() {
                return new JScrollPane(new JTableModel().returnExcelData());
        }
}
