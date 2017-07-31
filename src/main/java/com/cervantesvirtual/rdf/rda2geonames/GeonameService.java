package com.cervantesvirtual.rdf.rda2geonames;

import org.geonames.FeatureClass;
import org.geonames.ToponymSearchCriteria;
import org.geonames.ToponymSearchResult;
import org.geonames.WebService;

public class GeonameService 
{
	private static String username = "yourUser";
	private static int maxResults = 5;
	
    public ToponymSearchResult search(String query){
        WebService.setUserName(username);
        ToponymSearchResult searchResult = null;
        
        if(!query.isEmpty()){
    		
			ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		    searchCriteria.setQ(query);
		    searchCriteria.setLanguage("es");
		    searchCriteria.setMaxRows(maxResults);
		    searchCriteria.setFeatureClass(FeatureClass.P);
		    
			try {
				searchResult = WebService.search(searchCriteria);
				
			} catch (Exception e) {
				System.out.println("Error Geonames webservice." + e.getMessage());
			}
		}
        return searchResult;

    }
}
