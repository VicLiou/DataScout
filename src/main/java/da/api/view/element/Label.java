package da.api.view.element;

import javax.swing.JLabel;

public class Label {

    public JLabel labelSearchTitle() {
        return addLabel("資料查詢");
    }

    public JLabel labelCloudeType() {
        return addLabel("雲類型：");
    }

    public JLabel labelApidType() {
        return addLabel("APID：");
    }

    public JLabel labelEnvType() {
        return addLabel("環境：");
    }

    private JLabel addLabel(String name) {
        return new JLabel(name);
    }

}
