package org.xenei.galway2020.source.twitter.writer;

import org.apache.commons.lang3.StringUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.apache.jena.vocabulary.DC_11;
import org.apache.jena.vocabulary.RDF;
import org.xenei.galway2020.utils.DateToRDF;
import org.xenei.galway2020.vocab.Galway2020;

import twitter4j.HashtagEntity;
import twitter4j.MediaEntity;
import twitter4j.Status;
import twitter4j.SymbolEntity;
import twitter4j.URLEntity;
import twitter4j.UserMentionEntity;

public class StatusToRDF {

	private final Model model;
	private final MediaEntityToRDF mediaEntityWriter;
	private final GeoLocationToRDF geoLocationWriter;
	private final HashtagToRDF hashtagWriter;
	private final UserToRDF userWriter;
	private final PlaceToRDF placeWriter;
	private final SymbolToRDF symbolWriter;
	private final UrlEntityToRDF urlWriter;

	public Resource getId(long id) {
		String url = String.format(
				"http://galway2020.xenei.net/twitter/status#%s", id);
		Resource retval = model.createResource(url, Galway2020.Tweet);
		retval.addProperty( RDF.type, FOAF.Document );
		return retval;
	}

	public StatusToRDF(Model model) {
		this.model = model;
		this.mediaEntityWriter = new MediaEntityToRDF(model);
		this.geoLocationWriter = new GeoLocationToRDF(model);
		this.hashtagWriter = new HashtagToRDF(model);
		this.userWriter = new UserToRDF(model, this);
		this.placeWriter = new PlaceToRDF(model);
		this.symbolWriter = new SymbolToRDF(model);
		this.urlWriter = new UrlEntityToRDF(model);
	}

	public Resource write(Status status) {

		Resource main = getId(status.getId());
		main.addLiteral(Galway2020.accessLevel, status.getAccessLevel());
		if (status.getContributors() != null) {
			for (long l : status.getContributors()) {
				main.addLiteral(Galway2020.contributorId, getId(l));
			}
		}
		main.addLiteral(DC_11.date,  DateToRDF.asDateTime(status.getCreatedAt()));
		// Not in the graph
		// main.addLiteral( RDFWriter.retweetId,
		// status.getCurrentUserRetweetId());

		for (MediaEntity media : status.getExtendedMediaEntities()) {
			main.addProperty(Galway2020.media, mediaEntityWriter.write(media));
		}

		main.addLiteral(Galway2020.favoriteCount, status.getFavoriteCount());
		if (status.getGeoLocation() != null) {
			geoLocationWriter.write(status.getGeoLocation());
		}

		for (HashtagEntity hashtag : status.getHashtagEntities()) {
			main.addProperty(DC_11.subject, hashtagWriter.write(hashtag));
		}

		if (status.getInReplyToStatusId() >= 0) {
			main.addProperty(Galway2020.replyTo,
					getId(status.getInReplyToStatusId()));
		}

		userWriter.setUserScreenName(status.getInReplyToUserId(),
				status.getInReplyToScreenName());

		if (StringUtils.isNotBlank(status.getLang()))
		{
			main.addProperty(DC_11.language, status.getLang());
		}

		for (MediaEntity media : status.getMediaEntities()) {
			main.addProperty(Galway2020.media, mediaEntityWriter.write(media));
		}

		if (status.getPlace() != null) {
			main.addProperty(Galway2020.place,
					placeWriter.write(status.getPlace()));
		}

		if (status.getRetweetCount()>-1)
		{
			main.addLiteral(Galway2020.retweetCount, status.getRetweetCount());
		}
		
		if (status.getRetweetedStatus() != null) {
			main.addProperty(Galway2020.retweet,
					write(status.getRetweetedStatus()));
		}

		if (status.getScopes() != null) {
			for (String placeId : status.getScopes().getPlaceIds()) {
				main.addProperty(Galway2020.scope, placeWriter.getId(placeId));
			}
		}

		main.addLiteral(Galway2020.source, status.getSource());

		for (SymbolEntity symbol : status.getSymbolEntities()) {
			symbolWriter.write(symbol);
		}

		main.addLiteral(Galway2020.text, status.getText());

		for (URLEntity url : status.getURLEntities()) {
			if (StringUtils.isNotBlank(url.getURL())) {
				main.addProperty(FOAF.topic, urlWriter.write(url));
			}
		}

		if (status.getUser() != null) {
			userWriter.write(status.getUser()).addProperty(Galway2020.tweet,
					main);
		}

		for (UserMentionEntity userMention : status.getUserMentionEntities()) {
			main.addProperty(Galway2020.mentions,
					userWriter.write(status.getUser(), userMention));
		}
		return main;
	}
}
