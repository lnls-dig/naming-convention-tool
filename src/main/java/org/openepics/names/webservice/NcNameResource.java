package org.openepics.names.webservice;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.openepics.names.services.NamesEJB;
import org.openepics.names.model.NcName;
import org.openepics.names.model.NameEvent;
import org.openepics.names.services.NamingConventionEJB;
import org.openepics.names.services.EssNameConstructionMethod;

/**
 * @author Marko Kolar <marko.kolar@cosylab.com>
 */
@Stateless
@Path("nc")
public class NcNameResource {

	@PersistenceContext
	EntityManager em;
	@Inject
	NamesEJB namesEJB;
	@Inject
	NamingConventionEJB namingConventionEJB;

	@POST
	@Path("{convention}")
	public Response create(@PathParam("convention") String convention, @QueryParam("section_id") Integer sectionId,
			@QueryParam("discipline_id") Integer disciplineId, @QueryParam("signal_id") Integer signalId) {
		final EssNameConstructionMethod constructionMethod = constructionMethod(convention);
		if (constructionMethod == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			final NameEvent section = namesEJB.findEventById(sectionId);
			final NameEvent discipline = namesEJB.findEventById(disciplineId);
			final NameEvent signal = namesEJB.findEventById(signalId);
			if (signal == null)
				namingConventionEJB.createNcNameDevice(section, discipline, constructionMethod);
			else
				namingConventionEJB.createNcNameSignal(section, discipline, "", signal, constructionMethod);
			return Response.ok().build();
		}
	}

	@DELETE
	@Path("{convention}/{id}")
	public Response remove(@PathParam("convention") String convention, @PathParam("id") Integer id) {
		final EssNameConstructionMethod constructionMethod = constructionMethod(convention);
		if (constructionMethod == null) {
			return Response.status(Status.NOT_FOUND).build();
		} else {
			try {
				final NcName ncName = namingConventionEJB.findNcNameById(id);
				em.remove(ncName);
				return Response.ok().build();
			} catch (NoResultException e) {
				return Response.status(Status.NOT_FOUND).build();
			}
		}
	}

	private EssNameConstructionMethod constructionMethod(String conventionId) {
		if (conventionId.equals("acc"))
			return EssNameConstructionMethod.ACCELERATOR;
		else if (conventionId.equals("target"))
			return EssNameConstructionMethod.TARGET;
		else
			return null;
	}
}
