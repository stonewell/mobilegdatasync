package com.angelstone.sync.contact.gcontact;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.kxml.Attribute;
import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;

import com.angelstone.sync.gclient.GDataXmlParser;


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
				InputStreamReader reader = null;
				try {
					reader = new InputStreamReader(new ByteArrayInputStream(
							feedsBytes), "UTF-8");
				} catch (UnsupportedEncodingException e) {
					reader = new InputStreamReader(new ByteArrayInputStream(
							feedsBytes));
				}
				XmlParser xmlParser = new GDataXmlParser(reader, 300);
				ParseEvent event = xmlParser.read();
				while (!GDataXmlParser.isEnd(event)) {
					if (event.getType() == Xml.START_TAG) {
						String name = event.getName();
						if ("entry".equals(name)) {
							GDataEntry feed = parseDataEntry(xmlParser);
							if (feed != null) {
								contacts.getEntrys().addElement(feed);
							}
						} else if ("id".equals(name)) {
							contacts.setId(GDataXmlParser.parseTextNode(
									xmlParser, "id"));
						} else if ("updated".equals(name)) {
							contacts.setUpdated(GDataXmlParser.parseTextNode(
									xmlParser, "updated"));
						} else if ("title".equals(name)) {
							contacts.setTitle(GDataXmlParser.parseTextNode(
									xmlParser, "title"));
						} else if ("totalResults".equals(name)) {
							contacts.setTotal(Integer.parseInt(GDataXmlParser
									.parseTextNode(xmlParser, "totalResults")));
						}
					}
					event = xmlParser.read();
				}
			} catch (IOException e) {
				throw new GDataFeedsException(this.getClass(), "parseFeeds", e);
			}
		}

		return contacts;
	}

	private GDataEntry parseDataEntry(XmlParser xmlParser) throws IOException {

		GDataEntry dataEntry = new GDataEntry();

		ParseEvent nextEvent = xmlParser.peek();
		while (!GDataXmlParser.isEnd(nextEvent, "entry")) {
			String name = nextEvent.getName();
			int type = nextEvent.getType();
			xmlParser.read();

			if ((type == Xml.START_TAG)) {
				if ("id".equals(name)) {
					dataEntry.setId(GDataXmlParser.parseTextNode(xmlParser,
							"id"));
				} else if ("title".equals(name)) {
					dataEntry.setTitle(GDataXmlParser.parseTextNode(xmlParser,
							"title"));
				} else if ("updated".equals(name)) {
					dataEntry.setUpdated(GDataXmlParser.parseTextNode(
							xmlParser, "updated"));
				} else if ("link".equals(name)) {
					Attribute rel = nextEvent.getAttribute("rel");
					if (rel != null) {
						Attribute href = nextEvent.getAttribute("href");
						if (href != null) {
							dataEntry.getLinks().put(rel.getValue(),
									href.getValue());
						}
					}
				} else if ("email".equals(name)) {
					Attribute rel = nextEvent.getAttribute("rel");
					Attribute address = nextEvent.getAttribute("address");
					Attribute primary = nextEvent.getAttribute("primary");
					if (rel != null && address != null) {
						dataEntry.getEmails().addElement(address.getValue());

						if (primary != null
								&& "true".equals(primary.getValue())) {
							dataEntry.setPrimaryEmail(address.getValue());
						}
					}
				} else if ("phoneNumber".equals(name)) {
					Attribute rel = nextEvent.getAttribute("rel");
					String value = GDataXmlParser.parseTextNode(xmlParser,
							"phoneNumber");

					if (rel != null && value != null && value.length() > 0) {
						dataEntry.getPhoneNumbers().addElement(
								new GDataPhoneNumber(rel.getValue(), value));
					}
				} else if ("category".equals(name)) {
					Attribute term = nextEvent.getAttribute("term");
					if (term != null) {
						dataEntry.setCategory(term.getValue());
					}
				} else if ("content".equals(name)) {
					dataEntry.setContent(GDataXmlParser.parseTextNode(
							xmlParser, "content"));
				} else if ("systemGroup".equals(name)) {
					Attribute id = nextEvent.getAttribute("id");
					if (id != null) {
						dataEntry.setSystemGroup(id.getValue());
					}
				} else if ("groupMembershipInfo".equals(name)) {
					Attribute href = nextEvent.getAttribute("href");
					if (href != null) {
						dataEntry.getGroups().addElement(href.getValue());
					}
				} else if ("name".equals(name)) {
					parseVersion3Name(dataEntry, xmlParser);
				} else if ("birthday".equals(name)) {
					Attribute when = nextEvent.getAttribute("when");
					if (when != null) {
						dataEntry.setBirthday(when.getValue());
					}
				} else if ("event".equals(name)) {
					GDataEvent dEvent = parseDataEvent(xmlParser);

					if (dEvent != null) {
						dataEntry.getEvents().addElement(dEvent);
					}
				} else if ("structuredPostalAddress".equals(name)) {
					Attribute rel = nextEvent.getAttribute("rel");

					if (rel != null) {
						String relValue = rel.getValue();
						GDataStructuredPostalAddress dAddress = parseStructuredPostalAddress(xmlParser);

						if (dAddress != null) {
							dAddress.setRel(relValue);
							dataEntry.getStructuredPostalAddresses()
									.addElement(dAddress);
						}
					}
				} else if ("organization".equals(name)) {
					Attribute rel = nextEvent.getAttribute("rel");

					if (rel != null) {
						String relValue = rel.getValue();
						GDataOrganization d = parseOrganization(xmlParser);

						if (d != null) {
							d.setRel(relValue);
							dataEntry.getOrganizations().addElement(d);
						}
					}
				}
			}// start tag

			nextEvent = xmlParser.peek();
		}

		return dataEntry;
	}

	private void parseVersion3Name(GDataEntry dataEntry, XmlParser xmlParser)
			throws IOException {

		ParseEvent nextEvent = xmlParser.peek();
		while (!GDataXmlParser.isEnd(nextEvent, "name")) {
			String name = nextEvent.getName();
			int type = nextEvent.getType();
			xmlParser.read();

			if ((type == Xml.START_TAG)) {
				if ("fullName".equals(name)) {
					dataEntry.setFullName(GDataXmlParser.parseTextNode(
							xmlParser, "fullName"));
				} else if ("givenName".equals(name)) {
					dataEntry.setGivenName(GDataXmlParser.parseTextNode(
							xmlParser, "givenName"));
				} else if ("familyName".equals(name)) {
					dataEntry.setFamilyName(GDataXmlParser.parseTextNode(
							xmlParser, "familyName"));
				}
			}// start tag

			nextEvent = xmlParser.peek();
		}
	}

	private GDataEvent parseDataEvent(XmlParser xmlParser) throws IOException {

		GDataEvent dEvent = new GDataEvent();

		ParseEvent nextEvent = xmlParser.peek();
		while (!GDataXmlParser.isEnd(nextEvent, "event")) {
			String name = nextEvent.getName();
			int type = nextEvent.getType();
			xmlParser.read();

			if ((type == Xml.START_TAG)) {
				if ("when".equals(name)) {
					Attribute startTime = nextEvent.getAttribute("when");

					if (startTime != null) {
						GDataWhen when = new GDataWhen();
						when.setStartTime(startTime.getValue());
						dEvent.getWhens().addElement(when);
					}
				}
			}// start tag

			nextEvent = xmlParser.peek();
		}

		return dEvent;
	}

	private GDataStructuredPostalAddress parseStructuredPostalAddress(
			XmlParser xmlParser) throws IOException {

		GDataStructuredPostalAddress dAddress = new GDataStructuredPostalAddress();

		ParseEvent nextEvent = xmlParser.peek();
		while (!GDataXmlParser.isEnd(nextEvent, "name")) {
			String name = nextEvent.getName();
			int type = nextEvent.getType();
			xmlParser.read();

			if ((type == Xml.START_TAG)) {
				if ("formattedAddress".equals(name)) {
					dAddress.setFormattedAddress(GDataXmlParser.parseTextNode(
							xmlParser, "formattedAddress"));
				} else if ("street".equals(name)) {
					dAddress.setStreet(GDataXmlParser.parseTextNode(xmlParser,
							"street"));
				} else if ("postcode".equals(name)) {
					dAddress.setPostcode(GDataXmlParser.parseTextNode(
							xmlParser, "postcode"));
				}
			}// start tag

			nextEvent = xmlParser.peek();
		}

		return dAddress;
	}

	private GDataOrganization parseOrganization(XmlParser xmlParser)
			throws IOException {

		GDataOrganization d = new GDataOrganization();

		ParseEvent nextEvent = xmlParser.peek();
		while (!GDataXmlParser.isEnd(nextEvent, "name")) {
			String name = nextEvent.getName();
			int type = nextEvent.getType();
			xmlParser.read();

			if ((type == Xml.START_TAG)) {
				if ("orgName".equals(name)) {
					d.setOrgName(GDataXmlParser.parseTextNode(xmlParser,
							"orgName"));
				}
			}// start tag

			nextEvent = xmlParser.peek();
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
