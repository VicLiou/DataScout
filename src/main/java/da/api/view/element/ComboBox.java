package da.api.view.element;

import javax.swing.JComboBox;

public class ComboBox {

    public JComboBox<String> comboBoxCloudType() {
        String[] options = { "我要全部!!", "中台雲", "資訊雲", "數位雲", "APIM" };
        JComboBox<String> cloudeTypeComboBox = new JComboBox<>(options);

        cloudeTypeComboBox.addActionListener(e -> {
            String selected = (String) cloudeTypeComboBox.getSelectedItem();
            System.out.println("選擇：" + selected);
        });

        return cloudeTypeComboBox;
    }

    public JComboBox<String> comboBoxApidtype() {
        String[] options = { "我要全部!!", "MID-LX-MDP-01", "MID-LX-MIP-01", "CRD-NT-CAP-12", "BRN-LX-DBS-01",
                "BRN-LX-DBT-01" };
        JComboBox<String> apidTypeComboBox = new JComboBox<>(options);

        apidTypeComboBox.addActionListener(e -> {
            String selected = (String) apidTypeComboBox.getSelectedItem();
            System.out.println("選擇：" + selected);
        });

        return apidTypeComboBox;
    }

    public JComboBox<String> comboBoxEnvtype() {
        String[] options = { "我要全部!!", "UT", "UAT", "PROD" };
        JComboBox<String> envTypeComboBox = new JComboBox<>(options);

        envTypeComboBox.addActionListener(e -> {
            String selected = (String) envTypeComboBox.getSelectedItem();
            System.out.println("選擇：" + selected);
        });

        return envTypeComboBox;
    }
}