package org.xululabs.datasources;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

public class Twitter4jApi {
	/**
	 * use to get twitter instance
	 * 
	 * @return
	 */
	Query query;
	QueryResult result;

	public Twitter getTwitterInstance(String consumerKey, String consumerSecret, String accessToken,
			String accessTokenSecret) {
		Twitter twitter = null;

		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true).setOAuthConsumerKey(consumerKey).setOAuthConsumerSecret(consumerSecret)
					.setOAuthAccessToken(accessToken).setOAuthAccessTokenSecret(accessTokenSecret);
			TwitterFactory tf = new TwitterFactory(cb.build());
			twitter = tf.getInstance();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return twitter;
	}

	/**
	 * use to retweet
	 * 
	 * @param twitter
	 * @param tweetIds
	 * @throws TwitterException
	 */
	public ArrayList<Long> retweet(Twitter twitter, ArrayList<Long> tweetIds) throws TwitterException {
		ArrayList<Long> remainingTweetIds = new ArrayList<Long>();
		int index = 0;
		while (twitter.getRetweetsOfMe().getRateLimitStatus().getLimit() > 0 && index < tweetIds.size()) {
			try {
				twitter.retweetStatus(tweetIds.get(index));
			} catch (Exception ex) {
              
			}
			index++;
		}
		if (index != tweetIds.size() - 1)
			remainingTweetIds = new ArrayList<Long>(tweetIds.subList(index, tweetIds.size()));
		twitter = null;
		return remainingTweetIds;

	}

}
