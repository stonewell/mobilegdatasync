package com.angelstone.sync.contact.gcontact;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.angelstone.sync.gclient.GDataXmlUtils;

public class GDataFeedsParser {
	private int version_ = 3;

	public GDataFeedsParser(int version) {
		setVersion(version);
	}

	public GDataFeeds parseFeeds(byte[] feedsBytes) throws GDataFeedsException {
		// extract feed URLs into vector
		GDataFeeds contacts = new GDataFeeds();

		if (feedsBytes != null && feedsBytes.length > 0) {
			try {
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				factory.setNamespaceAware(true);

				InputStreamReader reader = null;
				try {
					reader = new InputStreamReader(new ByteArrayInputStream(
							feedsBytes), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					reader = new InputStreamReader(new ByteArrayInputStream(
							feedsBytes));
				}

				XmlPullParser xmlParser = factory.newPullParser();
				GDataXmlUtils.defineCharacterEntity(xmlParser);

				xmlParser.setInput(reader);
				int eventType = xmlParser.next();
				while (!GDataXmlUtils.isEnd(eventType)) {
					if (eventType == XmlPullParser.START_TAG) {
						String name = xmlParser.getName();
						if ("entry".equals(name)) {
							GDataEntry feed = parseDataEntry(xmlParser);
							if (feed != null) {
								contacts.getEntrys().add(feed);
							}
						} else if ("id".equals(name)) {
							contacts.setId(GDataXmlUtils.parseTextNode(
									xmlParser, "id"));
						} else if ("updated".equals(name)) {
							contacts.setUpdated(GDataXmlUtils.parseTextNode(
									xmlParser, "updated"));
						} else if ("title".equals(name)) {
							contacts.setTitle(GDataXmlUtils.parseTextNode(
									xmlParser, "title"));
						} else if ("totalResults".equals(name)) {
							contacts.setTotal(Integer.parseInt(GDataXmlUtils
									.parseTextNode(xmlParser, "totalResults")));
						}
					}
					eventType = xmlParser.next();
				}
			} catch (IOException e) {
				throw new GDataFeedsException(this.getClass(), "parseFeeds", e);
			} catch (XmlPullParserException e) {
				throw new GDataFeedsException(this.getClass(), "parseFeeds", e);
			}
		}

		return contacts;
	}

	private GDataEntry parseDataEntry(XmlPullParser xmlParser)
			throws IOException, XmlPullParserException {

		GDataEntry dataEntry = new GDataEntry();

		xmlParser.next();
		while (!GDataXmlUtils.isEnd(xmlParser, "entry")) {
			String name = xmlParser.getName();
			int type = xmlParser.getEventType();

			if ((type == XmlPullParser.START_TAG)) {
				if ("id".equals(name)) {
					dataEntry.setId(GDataXmlUtils
							.parseTextNode(xmlParser, "id"));
				} else if ("title".equals(name)) {
					dataEntry.setTitle(GDataXmlUtils.parseTextNode(xmlParser,
							"title"));
				} else if ("updated".equals(name)) {
					dataEntry.setUpdated(GDataXmlUtils.parseTextNode(xmlParser,
							"updated"));
				} else if ("link".equals(name)) {
					String rel = xmlParser.getAttributeValue(null, "rel");
					if (rel != null) {
						String href = xmlParser.getAttributeValue(null, "href");
						if (href != null) {
							dataEntry.getLinks().put(rel, href);
						}
					}
				} else if ("email".equals(name)) {
					String rel = xmlParser.getAttributeValue(null, "rel");
					String address = xmlParser.getAttributeValue(null,
							"address");
					String primary = xmlParser.getAttributeValue(null,
							"primary");
					if (rel != null && address != null) {
						dataEntry.getEmails().addElement(address);

						if (primary != null && "true".equals(primary)) {
							dataEntry.setPrimaryEmail(address);
						}
					}
				} else if ("phoneNumber".equals(name)) {
					String rel = xmlParser.getAttributeValue(null, "rel");
					String value = GDataXmlUtils.parseTextNode(xmlParser,
							"phoneNumber");

					if (rel != null && value != null && value.length() > 0) {
						dataEntry.getPhoneNumbers().addElement(
								new GDataPhoneNumber(rel, value));
					}
				} else if ("category".equals(name)) {
					String term = xmlParser.getAttributeValue(null, "term");
					if (term != null) {
						dataEntry.setCategory(term);
					}
				} else if ("content".equals(name)) {
					dataEntry.setContent(GDataXmlUtils.parseTextNode(xmlParser,
							"content"));
				} else if ("systemGroup".equals(name)) {
					String id = xmlParser.getAttributeValue(null, "id");
					if (id != null) {
						dataEntry.setSystemGroup(id);
					}
				} else if ("groupMembershipInfo".equals(name)) {
					String href = xmlParser.getAttributeValue(null, "href");
					if (href != null) {
						dataEntry.getGroups().addElement(href);
					}
				} else if ("name".equals(name)) {
					parseVersion3Name(dataEntry, xmlParser);
				} else if ("birthday".equals(name)) {
					String when = xmlParser.getAttributeValue(null, "when");
					if (when != null) {
						dataEntry.setBirthday(when);
					}
				} else if ("event".equals(name)) {
					GDataEvent dEvent = parseDataEvent(xmlParser);

					if (dEvent != null) {
						dataEntry.getEvents().addElement(dEvent);
					}
				} else if ("structuredPostalAddress".equals(name)) {
					String rel = xmlParser.getAttributeValue(null, "rel");

					if (rel != null) {
						String relValue = rel;
						GDataStructuredPostalAddress dAddress = parseStructuredPostalAddress(xmlParser);

						if (dAddress != null) {
							dAddress.setRel(relValue);
							dataEntry.getStructuredPostalAddresses()
									.addElement(dAddress);
						}
					}
				} else if ("organization".equals(name)) {
					String rel = xmlParser.getAttributeValue(null, "rel");

					if (rel != null) {
						String relValue = rel;
						GDataOrganization d = parseOrganization(xmlParser);

						if (d != null) {
							d.setRel(relValue);
							dataEntry.getOrganizations().addElement(d);
						}
					}
				}
			}// start tag

			xmlParser.next();
		}

		return dataEntry;
	}

	private void parseVersion3Name(GDataEntry dataEntry, XmlPullParser xmlParser)
			throws IOException, XmlPullParserException {

		xmlParser.next();
		while (!GDataXmlUtils.isEnd(xmlParser, "name")) {
			String name = xmlParser.getName();
			int type = xmlParser.getEventType();

			if ((type == XmlPullParser.START_TAG)) {
				if ("fullName".equals(name)) {
					dataEntry.setFullName(GDataXmlUtils.parseTextNode(
							xmlParser, "fullName"));
				} else if ("givenName".equals(name)) {
					dataEntry.setGivenName(GDataXmlUtils.parseTextNode(
							xmlParser, "givenName"));
				} else if ("familyName".equals(name)) {
					dataEntry.setFamilyName(GDataXmlUtils.parseTextNode(
							xmlParser, "familyName"));
				}
			}// start tag

			xmlParser.next();
		}
	}

	private GDataEvent parseDataEvent(XmlPullParser xmlParser)
			throws IOException, XmlPullParserException {

		GDataEvent dEvent = new GDataEvent();

		xmlParser.next();
		while (!GDataXmlUtils.isEnd(xmlParser, "event")) {
			String name = xmlParser.getName();
			int type = xmlParser.getEventType();

			if ((type == XmlPullParser.START_TAG)) {
				if ("when".equals(name)) {
					String startTime = xmlParser
							.getAttributeValue(null, "when");

					if (startTime != null) {
						GDataWhen when = new GDataWhen();
						when.setStartTime(startTime);
						dEvent.getWhens().addElement(when);
					}
				}
			}// start tag

			xmlParser.next();
		}

		return dEvent;
	}

	private GDataStructuredPostalAddress parseStructuredPostalAddress(
			XmlPullParser xmlParser) throws IOException, XmlPullParserException {

		GDataStructuredPostalAddress dAddress = new GDataStructuredPostalAddress();

		xmlParser.next();
		while (!GDataXmlUtils.isEnd(xmlParser, "name")) {
			String name = xmlParser.getName();
			int type = xmlParser.getEventType();

			if ((type == XmlPullParser.START_TAG)) {
				if ("formattedAddress".equals(name)) {
					dAddress.setFormattedAddress(GDataXmlUtils.parseTextNode(
							xmlParser, "formattedAddress"));
				} else if ("street".equals(name)) {
					dAddress.setStreet(GDataXmlUtils.parseTextNode(xmlParser,
							"street"));
				} else if ("postcode".equals(name)) {
					dAddress.setPostcode(GDataXmlUtils.parseTextNode(xmlParser,
							"postcode"));
				}
			}// start tag

			xmlParser.next();
		}

		return dAddress;
	}

	private GDataOrganization parseOrganization(XmlPullParser xmlParser)
			throws IOException, XmlPullParserException {

		GDataOrganization d = new GDataOrganization();

		xmlParser.next();
		while (!GDataXmlUtils.isEnd(xmlParser, "name")) {
			String name = xmlParser.getName();
			int type = xmlParser.getEventType();

			if ((type == XmlPullParser.START_TAG)) {
				if ("orgName".equals(name)) {
					d.setOrgName(GDataXmlUtils.parseTextNode(xmlParser,
							"orgName"));
				}
			}// start tag

			xmlParser.next();
		}

		return d;
	}

	public void setVersion(int version) {
		version_ = version;
	}

	public int getVersion() {
		return version_;
	}
}
