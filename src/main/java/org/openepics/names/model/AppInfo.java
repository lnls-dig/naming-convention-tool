package org.openepics.names.model;

import javax.persistence.Entity;

/**
 * A singleton entity representing the installed Naming Tool application. At the moment the presence of this entity is
 * only used to indicate that the database has been initialized. Later it could carry things like schema version and
 * application configuration.
 *
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Entity
public class AppInfo extends Persistable {
    
    private int schemaVersion;
    
    public AppInfo(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }
    
    protected AppInfo() {}
    
    public int getSchemaVersion() { return schemaVersion; }
    
    public void updateSchemaVersion() { schemaVersion++; }
}
