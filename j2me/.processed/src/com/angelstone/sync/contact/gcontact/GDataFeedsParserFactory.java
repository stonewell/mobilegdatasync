package com.angelstone.sync.contact.gcontact;

public class GDataFeedsParserFactory {

	public static GDataFeedsParser createParser(int version) {
		return new GDataFeedsParser(version);
	}

}
