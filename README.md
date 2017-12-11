<a href="http://data.cervantesvirtual.com/"><img src=http://data.cervantesvirtual.com/blog/wp-content/uploads/2017/05/fbvmc.png></a> 

# rda2geonames
Disambiguation tool for geographic locations using external repositories such as Wikidata and Geonames

## Configuration
Update [GeonamesService.java](src/main/java/com/cervantesvirtual/rdf/rda2geonames/GeonameService.java) with your Geonames web services user:
```
private static String username = "yourUser";
```

Create the folder
```
/opt/tools/geonames
```

And add 
```
[ner-model.ser.gz](src/main/resources/ner-model.ser.gz)
```

Execute 
```
mvn clean package
```

Execute 
```
java -jar target/rda2geonames-0.0.1-jar-with-dependencies.jar http://data.cervantesvirtual.com/manifestation/283249
```

## License
MIT License

You are free to use this data without restrictions. We're thankful if you give attribution to:

*Biblioteca Virtual Miguel de Cervantes NER corpora*   
*https://github.com/fbvmc/ner-corpora/.*  
*http://data.cervantesvirtual.com/.*

[Semantic Enrichment on Cultural Heritage collections: A case study using geographic information. Gustavo Candela, Pilar Escobar, Manuel Marco-Such](http://dl.acm.org/citation.cfm?doid=3078081.3078090)


