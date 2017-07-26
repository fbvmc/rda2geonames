package com.cervantesvirtual.rdf.rda2geonames;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;

public class CompareItemsService {

	//describe <http://data.cervantesvirtual.com/publicationStatement/673012>
	public static void compare(String uriManifestation, String idGeonamesCalculated){
		String queryTxt ="PREFIX rdam: <http://rdaregistry.info/Elements/m/#> " +
						"SELECT ?geonames WHERE{ " +
						"<" + uriManifestation + "> rdam:P30111 ?ps . " +
						"?ps rdam:P30088 ?geonames. " +
						"}"; 
		
		Query query = QueryFactory.create(queryTxt); 
        QueryExecution qExe = QueryExecutionFactory.sparqlService( "http://data.cervantesvirtual.com/bvmc-lod/repositories/ps-bvmc", query );
        ResultSet results = qExe.execSelect();
        //ResultSetFormatter.out(System.out, results, query) ;

        while(results.hasNext()){
            QuerySolution soln=results.nextSolution();
            if (soln.get("?geonames") != null){
            	String idGeonames = soln.get("?geonames").asResource().toString().replace("http://sws.geonames.org/", "");
            	idGeonames = idGeonames.replaceFirst("/", "");
            	
            	String comparisonText = "";
            	if(idGeonamesCalculated.equals(idGeonames))
            		comparisonText = "Matched: " + uriManifestation + "\n";
            	else
            		comparisonText = "Not matched:" + uriManifestation + " calculated:" + idGeonamesCalculated + " original:" + idGeonames + "\n";
            	
            	try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream("src/main/resources/comparison-results.txt", true), "utf-8"))) {
                    writer.write(comparisonText);
            	} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
        }
	}
}
