package com.cervantesvirtual.rdf.rda2geonames;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLPRunnerTest {

      @Test
      public void basic() {
        System.out.println("Starting Stanford NLP");

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
          
        // Tokenize using Spanish settings
        props.setProperty("tokenize.language", "es");
        
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
        //props.setProperty("ner.model", "edu/stanford/nlp/models/ner/spanish.ancora.distsim.s512.crf.ser.gz");
        props.setProperty("ner.model", "src/main/resources/ner-model.ser.gz");
        //props.setProperty("regexner.mapping", "edu/stanford/nlp/models/kbp/kbp_regexner_mapping_sp.tag");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // // We're interested in NER for these things (jt->loc->sal)
        String[] tests =
            {
                "Guadalajara : Imprenta Daniel Ramírez, [1900?]",
                "Guadalajara : Imp. de Ramírez, 1909",
                "Guadalajara",
                "[Guadalaxara? : s.n., 1669?]",
                "Guadalajara : Impr. Provincial, 1884",
                "Guadalajara, Tipografía de Luis Pérez Verdía, 1883",
                "Guadalajara : Imprenta y Encuadernacion Provincial, 1884",
                "Impresso en Guadalajara : por Pedro de Robles y Francisco de Cormellas, 1564",
                "Guadalajara [Jalisco] : en la Oficina de Don José Fruto Romero, Año de 1811",
                "Guadalajara, Imprenta de J. Cabrera, 1895",
                "Guadalajara : Imprenta del Gobierno, a cargo de J. Santos Orozco, 1848"
                    };
        
        for (String s : tests) {

            s = s.replaceAll("\\[", " ");
            s = s.replaceAll("\\]", " ");
            System.out.println("s:" + s);
            //s = s.replaceAll("]", " ");
          // run all Annotators on the passed-in text
          Annotation document = new Annotation(s);
          pipeline.annotate(document);

          // these are all the sentences in this document
          // a CoreMap is essentially a Map that uses class objects as keys and has values with
          // custom types
          List<CoreMap> sentences = document.get(SentencesAnnotation.class);
          StringBuilder sb = new StringBuilder();
         
          List tokens = new ArrayList<>();
          //I don't know why I can't get this code out of the box from StanfordNLP, multi-token entities
          //are far more interesting and useful..
          //TODO make this code simpler..
          for (CoreMap sentence : sentences) {
            String prevNeToken = "O";
            String currNeToken = "O";
            boolean newToken = true;
            for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
              currNeToken = token.get(NamedEntityTagAnnotation.class);
              String word = token.get(TextAnnotation.class);

              if (currNeToken.equals("O")) {
                if (!prevNeToken.equals("O") && (sb.length() > 0)) {
                  handleEntity(prevNeToken, sb, tokens);
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
                //System.out.println("sb.append " + sb.toString());
              } else {
                // We're done with the current entity - print it out and reset
                // TODO save this token into an appropriate ADT to return for useful processing..
                handleEntity(prevNeToken, sb, tokens);
                newToken = true;
              }
              prevNeToken = currNeToken;
            }
            
            //last occurrence
            if(!sb.toString().isEmpty()){
                  handleEntity(prevNeToken, sb, tokens);
              }
          }
          //TODO - do some cool stuff with these tokens!
          //System.out.println("We extracted {} tokens of interest from the input text:"+ tokens.size());
          for(Object  e: tokens){
              //System.out.println("total:" + ((EmbeddedToken)e).getValue());
          }
        }
      }
      private void handleEntity(String inKey, StringBuilder inSb, List inTokens) {
        System.out.println("handleEntity " +  inSb + " " + inKey);
        //inTokens.add(new EmbeddedToken(inKey, inSb.toString()));
        inSb.setLength(0);
      }


    }
    