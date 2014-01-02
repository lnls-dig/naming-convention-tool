/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui;

import java.util.List;
import org.openepics.names.model.NameEvent;

/**
 *
 * @author Miha Vitorovic <miha.vitorovic@cosylab.com>
 */
public class MnemonicNameView {
    private List<NameEvent> options;
    private Integer selectedId;

    public MnemonicNameView(List<NameEvent> options) {
        this.options = options;
        selectedId = null;
    }

    public List<NameEvent> getOptions() {
        return options;
    }

    public void setOptions(List<NameEvent> options) {
        this.options = options;
    }

    public void setSelectedId(Integer selectedId) {
        this.selectedId = selectedId;
    }

    public Integer getSelectedId() {
        return selectedId;
    }

    public void clear() {
        options.clear();
        selectedId = null;
    }

    public boolean isSelected() {
        return selectedId != null;
    }

}
