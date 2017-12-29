package com.cervantesvirtual.rdf.rda2geonames.model;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.DC;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cervantesvirtual.rdf.rda2geonames.WikidataService;

public class RDAItem {

	public static String DOWNLOAD_FOLDER = "/tmp/geonames";
	public static String DOWNLOAD_URL = "http://data.cervantesvirtual.com/getRDF?uri=";
	public static String DOMAIN = "http://data.cervantesvirtual.com/";
	
	Logger logger = LogManager.getLogger(RDAItem.class);
	
	private String uri;
	private String idManifestation;
	private String title;
	private Model manifestationModel;
	private Model workModel;
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
	
    public RDAItem(String uri){
    	
    	this.uri = uri;
		this.setIdManifestation(uri.replace(DOMAIN + "manifestation/", ""));
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
	
	public RDAItem(String placePublication, String idManifestation){
		this.uri = DOMAIN + "manifestation/" + idManifestation;
		this.setPlacePublication(placePublication);
		this.setIdManifestation(idManifestation);
		this.setSubjects(new ArrayList<String>());
		this.setAuthors(new ArrayList<String>());
		this.setPrinters(new ArrayList<String>());
		this.setPartsTokens(partsTokens);
		
		placeTokens = new ArrayList<NERItem>();
		titleTokens = new ArrayList<NERItem>();
		subjectTokens = new ArrayList<NERItem>();
		setEditionStatementTokens(new ArrayList<NERItem>());
		authorTokens = new ArrayList<NERItem>();
		setPrinterTokens(new ArrayList<NERItem>());
		partsTokens = new ArrayList<NERItem>();
		
		this.setFRBR();
	}

	public String getIdManifestation() {
		return idManifestation;
	}

	public void setIdManifestation(String idManifestation) {
		this.idManifestation = idManifestation;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Model getManifestationModel() {
		return manifestationModel;
	}

	public void setManifestationModel(Model manifestationModel) {
		this.manifestationModel = manifestationModel;
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

	@Override
	public String toString() {
		return "RDAItem [idManifestation=" + idManifestation + ", title="
				+ title + ", manifestationModel=" + manifestationModel
				+ ", placePublication=" + placePublication + ", placeTokens="
				+ placeTokens + ", titleTokens=" + titleTokens + "]";
	}
	
	public void showNER() {
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
	}

	public List<NERItem> getSubjectTokens() {
		return subjectTokens;
	}

	public void setSubjectTokens(List<NERItem> subjectTokens) {
		this.subjectTokens = subjectTokens;
	}

	public void setFRBR(){
		File rdfFile = new File( DOWNLOAD_FOLDER, "manifestation-" + idManifestation + ".rdf");
		try {
			FileManager.get().addLocatorClassLoader(RDAItem.class.getClassLoader());
			FileUtils.copyURLToFile(new URL(DOWNLOAD_URL + uri), rdfFile);
			logger.trace("rdfFile.getPath:" + rdfFile.getPath());
			manifestationModel = FileManager.get().loadModel(rdfFile.getPath(), null, "RDF/XML");
			
			Property titleProperty = manifestationModel.createProperty("http://rdaregistry.info/Elements/m/title");
			Literal title = manifestationModel.getProperty(manifestationModel.getResource(uri), titleProperty).getObject().asLiteral();
			this.setTitle(title.getString());
			
			Property noteEditionProperty = manifestationModel.createProperty("http://rdaregistry.info/Elements/m/noteOnEditionStatement");
			if(manifestationModel.getProperty(manifestationModel.getResource(uri), noteEditionProperty) != null){
				Literal noteEdition = manifestationModel.getProperty(manifestationModel.getResource(uri), noteEditionProperty).getObject().asLiteral();
				this.setNoteOnEditionStatement(noteEdition.getString());
			}
			
			if(placePublication == null || placePublication.isEmpty()){
				Property placeOfProductionProperty = manifestationModel.createProperty("http://rdaregistry.info/Elements/m/placeOfProduction");
				if(manifestationModel.getProperty(manifestationModel.getResource(uri), placeOfProductionProperty) != null){
					Literal placeOfProduction = manifestationModel.getProperty(manifestationModel.getResource(uri), placeOfProductionProperty).getObject().asLiteral();
					this.setPlacePublication(placeOfProduction.getString());
				}
			}
						 
			Property partProperty = manifestationModel.createProperty("http://rdaregistry.info/Elements/m/wholePartManifestationRelationship");
			NodeIterator parts = manifestationModel.listObjectsOfProperty(partProperty);
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
			}
			
			Property workManifestedProperty = manifestationModel.createProperty("http://rdaregistry.info/Elements/m/workManifested");
			Resource workResource = manifestationModel.getProperty(manifestationModel.getResource(uri), workManifestedProperty).getObject().asResource();
			String workIdentifier = workResource.toString().replace("http://", "").replaceAll("/", "-");
	    	String workFileName = "work-" + workIdentifier + ".rdf";
	    	File workFile = new File( DOWNLOAD_FOLDER, workFileName);
	    	
	    	FileUtils.copyURLToFile(new URL(DOWNLOAD_URL + workResource.toString()), workFile);
			workModel = FileManager.get().loadModel(workFile.getPath(), null, "RDF/XML");
	    	
			NodeIterator subjects = workModel.listObjectsOfProperty(DC.subject);
			if(subjects != null){
			    while ( subjects.hasNext() ) {
			    	RDFNode dataset = subjects.next();
			        
			        if(dataset.isLiteral()){
			        	this.subjects.add(dataset.asLiteral().getString());
			        }
			    }
			}
		    
		    Property authorProperty = workModel.createProperty("http://rdaregistry.info/Elements/w/author");
		    NodeIterator authors = workModel.listObjectsOfProperty(authorProperty);
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
		    }
		    
		    Property printerProperty = workModel.createProperty("http://rdaregistry.info/Elements/m/printer");
		    NodeIterator printers = workModel.listObjectsOfProperty(printerProperty);
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
		    }
		} catch (MalformedURLException e) {
			logger.trace("RDAItem MalformedURLException: " + e.getMessage());
		} catch (IOException e) {
			logger.trace("RDAItem IOException: " + e.getMessage());
		}
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

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Model getWorkModel() {
		return workModel;
	}

	public void setWorkModel(Model workModel) {
		this.workModel = workModel;
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
