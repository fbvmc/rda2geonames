package com.cervantesvirtual.rdf.rda2geonames;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.util.FileManager;
import org.junit.Test;

public class JenaTest {

	public static String DOWNLOAD_FOLDER = System.getProperty("user.home") + "/geonames";
	
	@Test
	public void jenaTest() throws MalformedURLException, IOException{
		String workFileName = "work2-584856.rdf";
    	File workFile = new File( DOWNLOAD_FOLDER, workFileName);
    	
    	FileUtils.copyURLToFile(new URL("http://data.cervantesvirtual.com/work/584856"), workFile);
    	
    	System.out.println("workFile.getPath():" + workFile.getPath());
		Model workModel = FileManager.get().loadModel(workFile.getPath(), null, "RDF/XML");
	}
}
