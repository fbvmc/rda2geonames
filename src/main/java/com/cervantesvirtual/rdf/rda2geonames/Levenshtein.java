package com.cervantesvirtual.rdf.rda2geonames;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Levenshtein {
	 
    public static int distance(String a, String b) {
        a = a.toLowerCase();
        b = b.toLowerCase();
        // i == 0
        int [] costs = new int [b.length() + 1];
        for (int j = 0; j < costs.length; j++)
            costs[j] = j;
        for (int i = 1; i <= a.length(); i++) {
            // j == 0; nw = lev(i - 1, j)
            costs[0] = i;
            int nw = i - 1;
            for (int j = 1; j <= b.length(); j++) {
                int cj = Math.min(1 + Math.min(costs[j], costs[j - 1]), a.charAt(i - 1) == b.charAt(j - 1) ? nw : nw + 1);
                nw = costs[j];
                costs[j] = cj;
            }
        }
        return costs[b.length()];
    }
 
    public static void main(String [] args) {
    	Logger logger = LogManager.getLogger(Levenshtein.class);
		
        String [] data = { "Cuenca", "Cuenca", " Cuenca / Mariscal Lamar ", "Cantón Cuenca" };
        for (int i = 0; i < data.length-1; i++)
        	logger.trace("distance(" + data[i] + ", " + data[i+1] + ") = " + distance(data[i], data[i+1]));
    }
}
