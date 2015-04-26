package org.openepics.names.services;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
//import java.util.logging.Level;
//import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;

@SessionScoped
public class SessionViewService implements Serializable{
	
    private static final long serialVersionUID = 827187290632697101L;
    /**
	 * Bean to preserve status of tree node (expanded or collapsed) during a session. 
	 */
//	private static final Logger LOGGER = Logger.getLogger(SessionViewService.class.getName());
	private Map<Object,Boolean> nodeMap = new HashMap<Object,Boolean>(); 

	@Inject
	public SessionViewService(){
		init();
	}
	
	public void init(){
		if(nodeMap==null){
		nodeMap = new HashMap<Object,Boolean>();
//		LOGGER.log(Level.INFO, "Initialilsed");
		}
	}	
		
	public boolean isExpanded(Object object){
		try {
			return object!=null ? nodeMap.get(object): true;			
		} catch (Exception e) {
			collapse(object);
			return object!=null ? nodeMap.get(object): true;			
		}
	}

	public void expand(Object object) {
			nodeMap.put(object, true);		
	}

	public void collapse(Object object) {
			nodeMap.put(object, false);
	}
		
	@PreDestroy
	public void cleanup(){
		nodeMap.clear();
		nodeMap=null;
//		LOGGER.log(Level.INFO, "Cleaned");
	}
}
