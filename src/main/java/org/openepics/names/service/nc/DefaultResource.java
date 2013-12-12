package org.openepics.names.service.nc;

import org.openepics.names.NamesEJB;
import org.openepics.names.NamesEJBLocal;
import org.openepics.names.model.NCName;
import org.openepics.names.model.NameEvent;
import org.openepics.names.nc.NamingConventionEJB;
import org.openepics.names.nc.NamingConventionEJBLocal;
import org.openepics.names.nc.NamingConventionEJBLocal.ESSNameConstructionMethod;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.Response.Status;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
@Path("/")
public class DefaultResource {

    @PersistenceContext EntityManager em;
    @Inject NamesEJBLocal namesEJB;
    @Inject NamingConventionEJBLocal namingConventionEJB;

    @POST
    @Path("{convention}")
    public Response create(@PathParam("convention") String convention, @QueryParam("section_id") Integer sectionId, @QueryParam("discipline_id") Integer disciplineId, @QueryParam("signal_id") Integer signalId) {
        final ESSNameConstructionMethod constructionMethod = constructionMethod(convention);
        if (constructionMethod == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            final NameEvent section = namesEJB.findEventById(sectionId);
            final NameEvent discipline = namesEJB.findEventById(disciplineId);
            final NameEvent signal = namesEJB.findEventById(signalId);
            namingConventionEJB.createNCNameDevice(section, discipline, constructionMethod);
            return Response.ok().build();
        }
    }

    @DELETE
    @Path("{convention}/{id}")
    public Response remove(@PathParam("convention") String convention, @PathParam("id") Integer id) {
        final ESSNameConstructionMethod constructionMethod = constructionMethod(convention);
        if (constructionMethod == null) {
            return Response.status(Status.NOT_FOUND).build();
        } else {
            try {
                final NCName ncName = namingConventionEJB.findNCNameById(id);
                em.remove(ncName);
                return Response.ok().build();
            } catch (NoResultException e) {
                return Response.status(Status.NOT_FOUND).build();
            }
        }
    }

    private ESSNameConstructionMethod constructionMethod(String conventionId) {
        if (conventionId.equals("acc")) return ESSNameConstructionMethod.ACCELERATOR;
        else if (conventionId.equals("target")) return ESSNameConstructionMethod.TARGET;
        else return null;
    }
}
