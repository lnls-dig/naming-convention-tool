/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui;

import java.util.List;
import org.openepics.names.ui.names.NamePartView;

/**
 *
 * @author Miha Vitorovic <miha.vitorovic@cosylab.com>
 */
public class NamePartSelectionView {
    private List<NamePartView> options;
    private Integer selectedId;

    public NamePartSelectionView(List<NamePartView> options) {
        this.options = options;
        selectedId = null;
    }

    public List<NamePartView> getOptions() {
        return options;
    }

    public void setOptions(List<NamePartView> options) {
        this.options = options;
    }

    // TODO remove after fixing JSF
    public void setSelectedId(Integer selectedId) {
        this.selectedId = selectedId;
    }

    // TODO remove after fixing JSF
    public Integer getSelectedId() {
        return selectedId;
    }

    public NamePartView getSelected() {
        throw new IllegalStateException(); // TODO
    }

    public void clear() {
        options.clear();
        selectedId = null;
    }

    public boolean isSelected() {
        return selectedId != null;
    }

}
