package org.xenei.galway2020.source.twitter.writer;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFList;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.xenei.galway2020.vocab.Wgs84_pos;

import twitter4j.GeoLocation;

public class GeoLocationToRDF {

	private final Model model;

	public GeoLocationToRDF(Model model) {
		this.model = model;
	}

	public Resource write(GeoLocation geoLoc) {
		Resource point = model.createResource();
		point.addProperty(RDF.type, Wgs84_pos.Point);

		point.addLiteral(Wgs84_pos.lat, geoLoc.getLatitude());
		point.addLiteral(Wgs84_pos.long_, geoLoc.getLongitude());
		return point;
	}

	public Resource writeCoordinates(GeoLocation[][] geoLocMatrix) {
		
		List<RDFList> lstlst = new ArrayList<RDFList>();	
		for (GeoLocation[] geoLst : geoLocMatrix) {
			List<Resource> geoLocs = new ArrayList<Resource>();
			for (GeoLocation geoLoc : geoLst) {
				geoLocs.add(write(geoLoc));
			}
			lstlst.add( model.createList( geoLocs.iterator() ));			
		}
		if (lstlst.isEmpty())
		{
			return model.createList();
		}
		
		return model.createList( lstlst.iterator() );
	}
}
