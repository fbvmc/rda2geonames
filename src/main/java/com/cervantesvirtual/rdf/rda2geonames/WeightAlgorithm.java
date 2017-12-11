package com.cervantesvirtual.rdf.rda2geonames;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.geonames.Toponym;
import org.geonames.ToponymSearchResult;

import com.cervantesvirtual.rdf.rda2geonames.model.GeonameItem;
import com.cervantesvirtual.rdf.rda2geonames.model.NERItem;
import com.cervantesvirtual.rdf.rda2geonames.model.RDAItem;

public class WeightAlgorithm {

	private ArrayList<GeonameItem> result;
	private String country;
	private boolean forceStatistics;
	
	Logger logger = LogManager.getLogger(WeightAlgorithm.class);
	
	public WeightAlgorithm(){
		result = new ArrayList<GeonameItem>();
		setForceStatistics(false);
	}
	
	public WeightAlgorithm(boolean forceStatistics){
		this.result = new ArrayList<GeonameItem>();
		this.setForceStatistics(forceStatistics);
	}
	
	public void process(RDAItem item){
		logger.trace("start process weight size place tokens:" + item.getPlaceTokens().size());
		if(item.getPlaceTokens().size() > 0){
			for (Object e : item.getPlaceTokens()) {
				String queryPlace = ((NERItem)e).getValue();
				queryPlace = queryPlace.replaceAll("de el", "del");
				queryPlace = queryPlace.replaceAll("\\?", "");
				queryPlace = queryPlace.replaceAll("Cordoua", "Córdoba");
				queryPlace = queryPlace.replaceAll("Cordubae", "Córdoba");
				logger.trace("queryPlace:" + queryPlace);
				GeonameService geonames = new GeonameService();
				
				ToponymSearchResult searchResult = geonames.search(queryPlace);
				
				if(!searchResult.getToponyms().isEmpty())
					allAreSame(searchResult.getToponyms(), searchResult.getToponyms().get(0).getCountryName());
				
				for(Toponym t: searchResult.getToponyms()){
					GeonameItem geonameItem = new GeonameItem();
					geonameItem.setUri(item.getUri());
					geonameItem.setCountry(t.getCountryName());
					geonameItem.setPlace(t.getName());
					geonameItem.setIdGeonames(t.getGeoNameId()+"");
					
					logger.trace("PLACE matching IdGeonames: " + t.getGeoNameId() + " " + t.getName() + " country: " + t.getCountryName());
					String countryPlace = t.getCountryName();
					
					for (Object s : item.getEditionStatementTokens()) {
						String queryEdition = ((NERItem)s).getValue();
						
						if(!queryEdition.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultEdition = geonames.search(queryEdition);
							for(Toponym ts: searchResultEdition.getToponyms()){
								String countryEdition = ts.getCountryName();
								if(countryPlace.equals(countryEdition)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 0.1);
									break;
								}
							}
						}
					}
					
					
					for (Object s : item.getSubjectTokens()) {
						String querySubject = ((NERItem)s).getValue();
						
						if(!querySubject.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultSubject = geonames.search(querySubject);
							for(Toponym ts: searchResultSubject.getToponyms()){
								String countrySubject = ts.getCountryName();
								if(countryPlace.equals(countrySubject)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 1);
									break;
								}
							}
						}
					}
					
					for (Object s : item.getTitleTokens()) {
						String queryTitle = ((NERItem)s).getValue();
						
						// remove query place in title Sevilla Cordoba 563300
						queryTitle = queryTitle.replaceAll(queryPlace, "").trim();
						//logger.trace("titletokens weight algo:" + queryTitle);
						
						if(!queryTitle.isEmpty() && !queryTitle.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultSubject = geonames.search(queryTitle);
							for(Toponym ts: searchResultSubject.getToponyms()){
								String countryTitle = ts.getCountryName();
								if(countryPlace.equals(countryTitle)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 1);
									break;
								}
							}
						}
					}
					
					for (Object s : item.getAuthorTokens()) {
						String queryAuthor = ((NERItem)s).getValue();
						
						if(!queryAuthor.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultAuthor = geonames.search(queryAuthor);
							for(Toponym ts: searchResultAuthor.getToponyms()){
								String countryAuthor = ts.getCountryName();
								
								if(countryPlace.equals(countryAuthor)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 1);
									break;
								}
							}
						}
					}
					
					for (Object s : item.getPrinterTokens()) {
						String queryPrinter = ((NERItem)s).getValue();
						
						if(!queryPrinter.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultPrinter = geonames.search(queryPrinter);
							for(Toponym ts: searchResultPrinter.getToponyms()){
								String countryPrinter = ts.getCountryName();
								
								if(countryPlace.equals(countryPrinter)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 1);
									break;
								}
							}
						}
					}
					
					for (Object s : item.getPartsTokens()) {
						String queryPart = ((NERItem)s).getValue();
						
						if(!queryPart.equalsIgnoreCase(queryPlace)){
							ToponymSearchResult searchResultPart = geonames.search(queryPart);
							for(Toponym ts: searchResultPart.getToponyms()){
								String countryPart = ts.getCountryName();
								
								if(countryPlace.equals(countryPart)){
									geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() + 1);
									break;
								}
							}
						}
					}
					
					// comparo NER lugar con el de geonames para que no sean diferentes
					int levDistance = Levenshtein.distance(queryPlace, geonameItem.getPlace());
					geonameItem.setPonderatedValue(geonameItem.getPonderatedValue() - levDistance*0.01);
					result.add(geonameItem);
				}
			}
			setWeightSameCountry();
			
			GeonameItem maxItem = getMaxValue();
			CompareItemsService.compare(maxItem.getUri(), maxItem.getIdGeonames());
			
			logger.trace("#### Weight algorithm result:" + maxItem);
		}
	}
	
	public GeonameItem getMaxValue(){
		GeonameItem resultItem = new GeonameItem(); 
		if(result.size() > 0)
			resultItem = result.get(0);
		
		for(GeonameItem ge: result){
			logger.trace("compare:" + ge);
			if(resultItem.getPonderatedValue() < ge.getPonderatedValue())
				resultItem = ge;
			else if(forceStatistics && resultItem.getPonderatedValue() == ge.getPonderatedValue()){
				// getCountry spain statistics
				if(ge.getCountry().equals("España"))
					resultItem = ge;
			}
		}
		
		return resultItem;
	}
	
	public void setWeightSameCountry(){
		for(GeonameItem ge: result){
			if(ge.getCountry().equals(country))
				ge.setPonderatedValue(ge.getPonderatedValue() + 1);
		}
	}

	public void setCountry(String country) {
		this.country = country;
	}
	
	private boolean allAreSame(List<Toponym> toponyms, String country){
	    for(Toponym t:toponyms){
	        if(!t.getCountryName().equals(country))
	             return false;
	    }
	    logger.trace("Same true");
	    this.country = country;
	    return true;
	    
	}

	public boolean isForceStatistics() {
		return forceStatistics;
	}

	public void setForceStatistics(boolean forceStatistics) {
		this.forceStatistics = forceStatistics;
	}
}
