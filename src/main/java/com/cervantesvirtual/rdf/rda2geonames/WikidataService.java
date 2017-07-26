package com.cervantesvirtual.rdf.rda2geonames;

import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class WikidataService {
	
	public static String wikidataServer = "https://query.wikidata.org/sparql";
	
	public WikidataService(){}
	
	public ArrayList<String> getAuthorData(String viaf, String bvmcIdentifier) throws Exception{
		
		ArrayList<String> wikidataLocations = new ArrayList<String>();
		
		String sparql = "PREFIX wdt: <http://www.wikidata.org/prop/direct/> " + 
		        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
		        "SELECT ?birthplacename ?deathplacename ?nationalityname " +
                "WHERE " +
                "{ " +
                  "{?agent wdt:P214 \""+ viaf +"\"} " +
                  "UNION { ?agent wdt:P2799 \""+ bvmcIdentifier + "\" } . " +
                  "OPTIONAL {?agent wdt:P20 ?deathplace . ?deathplace wdt:P1448 ?deathplacename} . " +
                  "OPTIONAL {?agent wdt:P19 ?birthplace . ?birthplace wdt:P1448 ?birthplacename} . " +
                  "OPTIONAL {?agent wdt:P27 ?nationality . ?nationality rdfs:label ?nationalityname . FILTER(LANG(?nationalityname) = \"es\")} . " +
                "}" +
                "LIMIT 100";
		//System.out.println("Sparql query wikidata:" + sparql);
		
		Query query = QueryFactory.create(sparql);
		QueryExecution qExe = QueryExecutionFactory.sparqlService( wikidataServer, query );
		
		ResultSet results = qExe.execSelect();
		//ResultSetFormatter.out(System.out, results, query);
		
		while ( results.hasNext() ) {
            final QuerySolution qs = results.next();
            
            if(qs.get( "deathplacename" ) != null )
            	wikidataLocations.add(qs.get( "deathplacename" ).asLiteral().getString());
            if(qs.get( "birthplacename" ) != null )
            	wikidataLocations.add(qs.get( "birthplacename" ).asLiteral().getString());
            if(qs.get( "nationalityname" ) != null )
            	wikidataLocations.add(qs.get( "nationalityname" ).asLiteral().getString());
        }
		
		return wikidataLocations;
	}
}
