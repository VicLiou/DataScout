package da.api.view.element;

import javax.swing.JButton;

import da.api.model.JButtonModel;

public class Button {

    public JButton buttonSearch() {
        JButton searchButton = new JButton("查詢");
        searchButton.addActionListener(e -> new JButtonModel().filterSearch());

        return searchButton;
    }

}
