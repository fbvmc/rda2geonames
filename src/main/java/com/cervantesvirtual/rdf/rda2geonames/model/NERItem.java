package com.cervantesvirtual.rdf.rda2geonames.model;

public class NERItem {

      private String name;
      private String value;

      public String getName() {
        return name;
      }

      public String getValue() {
        return value;
      }

      public NERItem(String name, String value) {
        super();
        this.name = name;
        this.value = value;
      }
}
