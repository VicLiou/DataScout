package da.api.model;

import javax.swing.JComboBox;

import da.api.view.element.ComboBox;

public class JButtonModel {

    String cloudeTypeValue, apidTypeValue, envTypeValue;

    public void filterSearch() {

        JComboBox<String> cloudeTypeComboBox = new ComboBox().comboBoxCloudType();
        cloudeTypeComboBox.addActionListener(e -> {
            cloudeTypeValue = cloudeTypeComboBox.getSelectedItem().toString();
        });

        // new ComboBox().comboBoxApidtype().getSelectedItem().toString();
        // new ComboBox().comboBoxEnvtype().getSelectedItem().toString();

        System.out.println(cloudeTypeValue);
        System.out.println(apidTypeValue);
        System.out.println(envTypeValue);

    }

}
