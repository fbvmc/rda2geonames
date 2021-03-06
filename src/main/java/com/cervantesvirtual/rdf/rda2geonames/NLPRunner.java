package com.cervantesvirtual.rdf.rda2geonames;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.cervantesvirtual.rdf.rda2geonames.model.NERItem;
import com.cervantesvirtual.rdf.rda2geonames.model.RDFItemFile;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLPRunner {
	
	private List<RDFItemFile> RDFItems;
	private StanfordCoreNLP pipeline;

	public NLPRunner() {
		RDFItems = new ArrayList<RDFItemFile>();
	}

	public void start(String nerModelPath) throws MalformedURLException, IOException {
		// creates a StanfordCoreNLP object, with POS tagging, lemmatization,
		// NER, parsing, and
		Properties props = new Properties();
		props.put("annotators", "tokenize, ssplit, pos, lemma, ner");

		// Tokenize using Spanish settings
		props.setProperty("tokenize.language", "es");

		props.setProperty("pos.model",
				"edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
		// props.setProperty("ner.model",
		// "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
		//props.setProperty("ner.model", "src/main/resources/ner-model.ser.gz");
		//props.setProperty("ner.model", "/opt/tools/geonames/ner-model.ser.gz"); // configuration for jar
		props.setProperty("ner.model", nerModelPath);
		// props.setProperty("regexner.mapping",
		// "edu/stanford/nlp/models/kbp/kbp_regexner_mapping_sp.tag");
		props.setProperty("ner.applyNumericClassifiers", "false");
		props.setProperty("ner.useSUTime", "false");

		pipeline = new StanfordCoreNLP(props);

		for (RDFItemFile ri : RDFItems) {

			process(ri.getPlacePublication(), ri.getPlaceTokens());
            process(ri.getTitle(), ri.getTitleTokens());
            
            if(ri.getNoteOnEditionStatement() != null)
                process(ri.getNoteOnEditionStatement(), ri.getEditionStatementTokens());
			
            if(ri.getSubjects() != null)
				for(String s: ri.getSubjects()){
					process(s, ri.getSubjectTokens());
				}
			
			if(ri.getAuthors() != null)
				for(String s: ri.getAuthors()){
					process(s, ri.getAuthorTokens());
				}
			
			if(ri.getPrinters() != null)
				for(String s: ri.getPrinters()){
					process(s, ri.getPrinterTokens());
				}
			
			if(ri.getParts() != null)
				for(String s: ri.getParts()){
					process(s, ri.getPartsTokens());
				}
			ri.showNER();
		}
	}

	private void handleEntity(String inKey, StringBuilder inSb, List<NERItem> tokens) {
		if(inKey.equals("LOC"))
		    tokens.add(new NERItem(inKey, inSb.toString()));
		inSb.setLength(0);
	}
	
	public void process(String s, List<NERItem> items){
		
		s = s.replaceAll("\\[", " ");
		s = s.replaceAll("\\]", " ");
		s = s.replaceAll("\"", " ");
		s = s.replaceAll("'", " ");

		// run all Annotators on the passed-in text
		Annotation document = new Annotation(s);
		pipeline.annotate(document);

		// read sentences in document
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);
		StringBuilder sb = new StringBuilder();

		for (CoreMap sentence : sentences) {
			String prevNeToken = "O";
			String currNeToken = "O";
			boolean newToken = true;
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				currNeToken = token.get(NamedEntityTagAnnotation.class);
				String word = token.get(TextAnnotation.class);
				if (currNeToken.equals("O")) {
					if (prevNeToken.equals("LOC") && (sb.length() > 0)) {
						handleEntity(prevNeToken, sb, items);
						newToken = true;
					}
					continue;
				}

				if (newToken) {
					prevNeToken = currNeToken;
					newToken = false;
					sb.append(word);
					continue;
				}

				if (currNeToken.equals(prevNeToken)) {
					sb.append(" " + word);
				} else {
					// handle current entity and reset
					handleEntity(prevNeToken, sb, items);
					newToken = true;
				}
				
				prevNeToken = currNeToken;
			}

			// last occurrence
			if (!newToken && sb.length() > 0 && prevNeToken.equals("LOC")) {
				handleEntity(prevNeToken, sb, items);
			}
		}

	}

	public static void main(String[] args){
		
		Logger logger = LogManager.getLogger(NLPRunner.class);
		
		NLPRunner nlp = new NLPRunner();
		
		String rdfPath = "";
		String nerModelPath = "";
		if(args.length == 0){
	        logger.trace("Proper usage is: java -jar rda2geonames-0.0.2-jar-with-dependencies.jar nerModelPath rdfpath ");
	        //exit 0; //TODO force out of the program
	    }else{
	    	nerModelPath = args[0];
	    	rdfPath = args[1];
	    }
		
		try{
		    nlp.RDFItems.add(new RDFItemFile(rdfPath));
			boolean forceStatistics = false;
			logger.trace("start ner process model");
		    nlp.start(nerModelPath);
		    logger.trace("finish ner process model");
		    for(RDFItemFile ri: nlp.RDFItems){
		    	ri.showNER();
		    	WeightAlgorithm weightAlgorithm = new WeightAlgorithm(forceStatistics);
		    	logger.trace("### Init weightAlgorithm process for place of publication: " + ri.getPlacePublication());
		    	weightAlgorithm.process(ri);
		    	logger.trace("### finish weightAlgorithm process for place of publication: " + ri.getPlacePublication());
		    }
		}catch(Exception e){
			logger.trace("Error:" + e.getMessage());
		}
	}
}
