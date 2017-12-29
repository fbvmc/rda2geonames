package com.cervantesvirtual.rdf.rda2geonames.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
import org.apache.jena.vocabulary.DC;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cervantesvirtual.rdf.rda2geonames.WikidataService;

public class RDFItemFile {

	Logger logger = LogManager.getLogger(RDFItemFile.class);
	public static String WIKIDATA_PREFIX = "http://www.wikidata.org/entity/";
	
	private String rdfPath;
	private String idEntity;
	private String title;
	private String placePublication;
	private String noteOnEditionStatement;
	private ArrayList<String> subjects;
	private ArrayList<String> authors;
	private ArrayList<String> printers;
	private ArrayList<String> parts;
	
	private List<NERItem> placeTokens;
	private List<NERItem> titleTokens;
	private List<NERItem> subjectTokens;
	private List<NERItem> editionStatementTokens;
	private List<NERItem> authorTokens;
	private List<NERItem> printerTokens;
	private List<NERItem> partsTokens;
	
    public RDFItemFile(String rdfPath){
    	
    	this.rdfPath = rdfPath;
		this.setSubjects(new ArrayList<String>()); 
		this.setAuthors(new ArrayList<String>());
		this.setPrinters(new ArrayList<String>());
		this.setParts(new ArrayList<String>());
		
		placeTokens = new ArrayList<NERItem>();
		titleTokens = new ArrayList<NERItem>();
		subjectTokens = new ArrayList<NERItem>();
		setEditionStatementTokens(new ArrayList<NERItem>());
		authorTokens = new ArrayList<NERItem>();
		setPrinterTokens(new ArrayList<NERItem>());
		partsTokens = new ArrayList<NERItem>();
		
		this.setFRBR();
	}
	
	public String getIdEntity() {
		return idEntity;
	}

	public void setIdEntity(String idEntity) {
		this.idEntity = idEntity;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPlacePublication() {
		return placePublication;
	}

	public void setPlacePublication(String placePublication) {
		this.placePublication = placePublication;
	}

	public List<NERItem> getPlaceTokens() {
		return placeTokens;
	}

	public void setPlaceTokens(List<NERItem> placeTokens) {
		this.placeTokens = placeTokens;
	}

	public List<NERItem> getTitleTokens() {
		return titleTokens;
	}

	public void setTitleTokens(List<NERItem> titleTokens) {
		this.titleTokens = titleTokens;
	}

	public void showNER() {
		logger.trace("### show NER");
		for (NERItem s: this.getPlaceTokens()){
			logger.trace("token place:" + s.getValue());
		}
		for (NERItem s: this.getTitleTokens()){
			logger.trace("token title:" + s.getValue());
		}
		for (NERItem s: this.getSubjectTokens()){
			logger.trace("token subject:" + s.getValue());
		}
		for (NERItem s: this.getEditionStatementTokens()){
			logger.trace("token edition:" + s.getValue());
		}
		for (NERItem s: this.getAuthorTokens()){
			logger.trace("token author:" + s.getValue());
		}
		for (NERItem s: this.getPrinterTokens()){
			logger.trace("token printer:" + s.getValue());
		}
		for (NERItem s: this.getPartsTokens()){
			logger.trace("token parts:" + s.getValue());
		}
		logger.trace("### show NER");
	}

	public List<NERItem> getSubjectTokens() {
		return subjectTokens;
	}

	public void setSubjectTokens(List<NERItem> subjectTokens) {
		this.subjectTokens = subjectTokens;
	}

	public void setFRBR(){
		File rdfFile = new File(rdfPath);
		
		logger.trace("rdfFile.getPath:" + rdfFile.getPath());
		Model model = FileManager.get().loadModel(rdfFile.getPath(), null, "RDF/XML");
		
		ResIterator iter = model.listSubjectsWithProperty(RDF.type, model.createResource("http://rdaregistry.info/Elements/c/Manifestation"));
		
		while(iter.hasNext()) {
			Resource manifestationModel = iter.next();
			logger.trace("RDFItemFile manifestation uri:" + manifestationModel.getURI());
			
			Property titleProperty = model.createProperty("http://rdaregistry.info/Elements/m/title");
			
			Literal title = manifestationModel.getProperty(titleProperty).getObject().asLiteral();
			this.setTitle(title.getString());
			
			Property noteEditionProperty = model.createProperty("http://rdaregistry.info/Elements/m/noteOnEditionStatement");
			if(manifestationModel.getProperty(noteEditionProperty) != null){
				Literal noteEdition = manifestationModel.getProperty(noteEditionProperty).getObject().asLiteral();
				this.setNoteOnEditionStatement(noteEdition.getString());
			}
			
			if(placePublication == null || placePublication.isEmpty()){
				Property placeOfProductionProperty = model.createProperty("http://rdaregistry.info/Elements/m/placeOfProduction");
				if(manifestationModel.getProperty(placeOfProductionProperty) != null){
					Literal placeOfProduction = manifestationModel.getProperty(placeOfProductionProperty).getObject().asLiteral();
					this.setPlacePublication(placeOfProduction.getString());
				}
			}
			
			/*Property partProperty = model.createProperty("http://rdaregistry.info/Elements/m/wholePartManifestationRelationship");
			NodeIterator parts = model.listObjectsOfProperty(partProperty);
			if(parts != null){
			    while ( parts.hasNext() ) {
			    	RDFNode dataset = parts.next();
			        
			    	Resource partResource = dataset.asResource();
					String partIdentifier = partResource.toString().replace("http://", "").replaceAll("/", "-");
			    	String partFileName = "manifestation-part" + partIdentifier + ".rdf";
			    	File partFile = new File( DOWNLOAD_FOLDER, partFileName);
			    	
			    	FileUtils.copyURLToFile(new URL(DOWNLOAD_URL + partResource.toString()), partFile);
					Model partModel = FileManager.get().loadModel(partFile.getPath(), null, "RDF/XML");
					
					Property partTitleProperty = partModel.createProperty("http://rdaregistry.info/Elements/m/title");
					Literal partTitle = partModel.getProperty(partResource, partTitleProperty).getObject().asLiteral();
			    	this.parts.add(partTitle.getString());
			    }
			}*/
			
			//Property workManifestedProperty = model.createProperty("http://rdaregistry.info/Elements/m/workManifested");
			//Resource workResource = manifestationModel.getProperty(workManifestedProperty).getObject().asResource();
			
	    	NodeIterator subjects = model.listObjectsOfProperty(DC.subject);
			if(subjects != null){
			    while ( subjects.hasNext() ) {
			    	RDFNode dataset = subjects.next();
			        
			        if(dataset.isLiteral()){
			        	this.subjects.add(dataset.asLiteral().getString());
			        }
			    }
			}
			
			/*Property authorProperty = model.createProperty("http://rdaregistry.info/Elements/w/author");
		    NodeIterator authors = model.listObjectsOfProperty(authorProperty);
		    if(authors != null){
			    while ( authors.hasNext() ) {
			    	RDFNode dataset = authors.next();
			        
			        if(dataset.isResource()){
			        	String idAuthor = dataset.asResource().toString().replace("http://data.cervantesvirtual.com/person/", "");
			        	WikidataService wikidataService = new WikidataService();
			        	try {
							this.authors = wikidataService.getAuthorData("", idAuthor);
						} catch (Exception e) {
							logger.trace("RDAItem setAuthors Error: " + e.getMessage());
						}
			        }
			    }
		    }*/
		    
		    /*Property printerProperty = model.createProperty("http://rdaregistry.info/Elements/m/printer");
		    NodeIterator printers = model.listObjectsOfProperty(printerProperty);
		    if(printers != null){
			    while ( printers.hasNext() ) {
			    	RDFNode dataset = printers.next();
			        
			        if(dataset.isResource()){
			        	String idAuthor = dataset.asResource().toString().replace("http://data.cervantesvirtual.com/person/", "");
			        	WikidataService wikidataService = new WikidataService();
			        	try {
							this.setPrinters(wikidataService.getAuthorData("", idAuthor));
						} catch (Exception e) {
							logger.trace("RDAItem setPrinters Error: " + e.getMessage());
						}
			        }
			    }
		    }*/
		}
		
		ResIterator iterPerson = model.listSubjectsWithProperty(RDF.type, model.createResource("http://rdaregistry.info/Elements/c/Person"));
		while(iterPerson.hasNext()) {
			Resource personModel = iterPerson.next();
			logger.trace("person uri:" + personModel.getURI());
			
			for (StmtIterator i = personModel.listProperties(); i.hasNext(); ) {
			    Statement s = i.next();
			    if(s.getPredicate().asResource().equals(OWL.sameAs) && s.getObject().asResource().getURI().startsWith(WIKIDATA_PREFIX)){
			    	logger.trace( "person has property " + s.getPredicate() + 
			                        " with value " + s.getObject() );
			        
			    	String wikidataId = s.getObject().asResource().getURI().replaceAll(WIKIDATA_PREFIX, "");
			    	WikidataService wikidataService = new WikidataService();
		        	try {
		        		this.authors = wikidataService.getAuthorData(wikidataId);
					} catch (Exception e) {
						logger.error("RDAItem setAuthors Error: " + e.getMessage());
					}
			    }
			}
		}
		
		model.close();
	}

	public ArrayList<String> getSubjects() {
		return subjects;
	}

	public void setSubjects(ArrayList<String> subjects) {
		this.subjects = subjects;
	}

	public String getNoteOnEditionStatement() {
		return noteOnEditionStatement;
	}

	public void setNoteOnEditionStatement(String noteOnEditionStatement) {
		this.noteOnEditionStatement = noteOnEditionStatement;
	}

	public List<NERItem> getEditionStatementTokens() {
		return editionStatementTokens;
	}

	public void setEditionStatementTokens(List<NERItem> editionStatementTokens) {
		this.editionStatementTokens = editionStatementTokens;
	}

	public ArrayList<String> getAuthors() {
		return authors;
	}

	public void setAuthors(ArrayList<String> authors) {
		this.authors = authors;
	}

	public List<NERItem> getAuthorTokens() {
		return authorTokens;
	}

	public void setAuthorTokens(List<NERItem> authorTokens) {
		this.authorTokens = authorTokens;
	}

	public List<NERItem> getPrinterTokens() {
		return printerTokens;
	}

	public void setPrinterTokens(List<NERItem> printerTokens) {
		this.printerTokens = printerTokens;
	}

	public ArrayList<String> getPrinters() {
		return printers;
	}

	public void setPrinters(ArrayList<String> printers) {
		this.printers = printers;
	}

	public String getRdfPath() {
		return rdfPath;
	}

	public void setRdfPath(String rdfPath) {
		this.rdfPath = rdfPath;
	}

	public List<NERItem> getPartsTokens() {
		return partsTokens;
	}

	public void setPartsTokens(List<NERItem> partsTokens) {
		this.partsTokens = partsTokens;
	}

	public ArrayList<String> getParts() {
		return parts;
	}

	public void setParts(ArrayList<String> parts) {
		this.parts = parts;
	}
}
