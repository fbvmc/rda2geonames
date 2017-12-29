package com.cervantesvirtual.rdf.rda2geonames;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geonames.FeatureClass;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

public class GeonameService 
{
	private static String username = "gcandela";
	private static int maxResults = 5;
	
    public ToponymSearchResult search(String query){
        WebService.setUserName(username);
        ToponymSearchResult searchResult = null;
        
        Logger logger = LogManager.getLogger(GeonameService.class);
        
        if(!query.isEmpty()){
    		
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		    searchCriteria.setQ(query);
		    searchCriteria.setLanguage("es");
		    searchCriteria.setMaxRows(maxResults);
		    searchCriteria.setFeatureClass(FeatureClass.P);
		    
			try {
				searchResult = WebService.search(searchCriteria);
				
			} catch (Exception e) {
				logger.trace("Error Geonames webservice." + e.getMessage());
			}
		}
        return searchResult;
    }
}
