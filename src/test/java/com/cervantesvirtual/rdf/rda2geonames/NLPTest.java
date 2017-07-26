package com.cervantesvirtual.rdf.rda2geonames;

import java.util.List;
import java.util.Properties;

import org.junit.Test;

import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class NLPTest {

      @Test
      public void basic() {
        System.out.println("Starting Stanford NLP");

        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
          
        props.setProperty("tokenize.language", "es");
        props.setProperty("pos.model", "edu/stanford/nlp/models/pos-tagger/spanish/spanish-distsim.tagger");
        props.setProperty("ner.model", "src/main/resources/ner-model.ser.gz");
        props.setProperty("ner.applyNumericClassifiers", "false");
        props.setProperty("ner.useSUTime", "false");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

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

            Annotation document = new Annotation(s);
            pipeline.annotate(document);

            List<CoreMap> sentences = document.get(SentencesAnnotation.class);
          
            for(CoreMap sentence: sentences) {
                for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                    String word = token.get(TextAnnotation.class);
                    String pos = token.get(PartOfSpeechAnnotation.class);
                    String ne = token.get(NamedEntityTagAnnotation.class);
                
                    if(ne.equals("LOC"))
                        System.out.println("Lugar: " + word + " pos: " + pos + " ne:" + ne);
                }
            }
        }
    }
}
