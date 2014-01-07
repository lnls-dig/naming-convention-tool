/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import org.openepics.names.services.NamesEJB;
import org.openepics.names.model.NamePartRevision;
/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
@Path("name")
public class NameElementResource {
    @EJB
    private NamesEJB namesEJB;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public List<NameElement> getNameElements(@DefaultValue("%") @QueryParam("category") String categoty,
        @DefaultValue("false") @QueryParam("deleted") Boolean deleted) {
        List<NamePartRevision> nameEvents;
        List<NameElement> nameElements = new ArrayList<NameElement>();
        
        nameEvents = namesEJB.getStandardNames(categoty, deleted);
        
        for (NamePartRevision ne: nameEvents) {
            nameElements.add(new NameElement(ne.getId(), ne.getName(), ne.getNameCategory().getId(), ne.getFullName()));
        }
        return nameElements;
    }
    
}
