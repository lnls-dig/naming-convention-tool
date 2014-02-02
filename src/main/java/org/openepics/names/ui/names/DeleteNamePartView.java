/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openepics.names.ui.names;

/**
 *
 * @author Miha Vitorovič <miha.vitorovic@cosylab.com>
 */
public class DeleteNamePartView {
    final private String fullName;
    final private String name;
    final boolean delete;

    public DeleteNamePartView(String name, String fullName, boolean delete) {
        this.name = name;
        this.fullName = fullName;
        this.delete = delete;
    }

    public String getFullName() {
        return fullName;
    }

    public String getName() {
        return name;
    }

    public boolean isDelete() {
        return delete;
    }
}
