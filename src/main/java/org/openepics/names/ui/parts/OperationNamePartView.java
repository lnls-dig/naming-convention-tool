/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui.parts;

/**
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
public class OperationNamePartView {
    final private NamePartView namePartView;
    final private boolean isAffected;

    public OperationNamePartView(NamePartView namePartView, boolean isAffected) {
        this.namePartView = namePartView;
        this.isAffected = isAffected;
    }

    public NamePartView getNamePartView() { return namePartView; }

    public boolean isAffected() { return isAffected; }
}
