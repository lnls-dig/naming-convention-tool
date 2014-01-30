/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openepics.names.webservice;

import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.openepics.names.model.NamePart;
import org.openepics.names.model.NamePartRevision;
import org.openepics.names.services.NamePartService;
/**
 *
 * @author Vasu V <vuppala@frib.msu.org>
 */
@Stateless
@Path("name")
public class NameElementResource {
    @Inject private NamePartService namePartService;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.TEXT_XML})
    public List<NameElement> getNameElements(@DefaultValue("%") @QueryParam("category") String categoty, @DefaultValue("false") @QueryParam("deleted") Boolean deleted) {

        final List<NamePart> nameParts = namePartService.approvedNames(deleted);

        // TODO: Filter by category

        final List<NameElement> nameElements = new ArrayList<NameElement>();
        for (NamePart namePart : nameParts) {
            final NamePartRevision namePartRevision = namePartService.approvedRevision(namePart);
            nameElements.add(new NameElement(namePartRevision.getId(), namePartRevision.getName(), namePartRevision.getNameCategory().getId(), namePartRevision.getFullName()));
        }
        return nameElements;
    }

}
