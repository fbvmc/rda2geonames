package com.cervantesvirtual.rdf.rda2geonames;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

public class JenaTest {

	public static String DOWNLOAD_FOLDER = System.getProperty("user.home") + "/geonames";
	
	//@Test
	public void jenaTest() throws MalformedURLException, IOException{
		String workFileName = "work2-584856.rdf";
    	File workFile = new File( DOWNLOAD_FOLDER, workFileName);
    	
    	FileUtils.copyURLToFile(new URL("http://data.cervantesvirtual.com/work/584856"), workFile);
    	
    	System.out.println("workFile.getPath():" + workFile.getPath());
		Model workModel = FileManager.get().loadModel(workFile.getPath(), null, "RDF/XML");
	}
	
	@Test
	public void jenaReadFileTest() throws MalformedURLException, IOException{
	
		Model model = FileManager.get().loadModel("src/test/resources/267866.rdf", null, "RDF/XML");
		ResIterator iter = model.listSubjectsWithProperty(RDF.type, model.createResource("http://rdaregistry.info/Elements/c/Manifestation"));
		
		while(iter.hasNext()) {
			Resource manifestationModel = iter.next();
			System.out.println("Entra:" + manifestationModel.getURI());
			
			Property titleProperty = model.createProperty("http://rdaregistry.info/Elements/m/title");
			
			Literal title = manifestationModel.getProperty(titleProperty).getObject().asLiteral();
			System.out.println(title.getString());
			
			Property noteEditionProperty = model.createProperty("http://rdaregistry.info/Elements/m/noteOnEditionStatement");
			if(manifestationModel.getProperty(noteEditionProperty) != null){
				Literal noteEdition = manifestationModel.getProperty(noteEditionProperty).getObject().asLiteral();
				System.out.println(noteEdition.getString());
			}
		}
		
		ResIterator iterPerson = model.listSubjectsWithProperty(RDF.type, model.createResource("http://rdaregistry.info/Elements/c/Person"));
		while(iterPerson.hasNext()) {
			Resource personModel = iterPerson.next();
			System.out.println("Entra:" + personModel.getURI());
			
			
			for (StmtIterator i = personModel.listProperties(); i.hasNext(); ) {
			    Statement s = i.next();
			    if(s.getPredicate().asResource().equals(OWL.sameAs) && s.getObject().asResource().getURI().startsWith("http://www.wikidata.org/entity/")){
			    	System.out.println( "person has property " + s.getPredicate() + 
			                        " with value " + s.getObject() );
			        
			    	String wikidataId = s.getObject().asResource().getURI().replaceAll("http://www.wikidata.org/entity/", "");
			    	WikidataService wikidataService = new WikidataService();
		        	try {
		        		System.out.println("Wikidata getAuthorData:" + wikidataService.getAuthorData(wikidataId));
					} catch (Exception e) {
						System.out.println("RDAItem setAuthors Error: " + e.getMessage());
					}
			    }
			}
		}
		
		model.close();
	}
}
