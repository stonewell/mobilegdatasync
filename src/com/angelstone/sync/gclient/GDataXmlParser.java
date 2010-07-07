package com.angelstone.sync.gclient;

import java.io.IOException;
import java.io.Reader;
import java.util.Hashtable;

import org.kxml.Xml;
import org.kxml.parser.ParseEvent;
import org.kxml.parser.XmlParser;

public class GDataXmlParser extends XmlParser {
    
    public GDataXmlParser(Reader reader, int bufSize) throws IOException {
        super(reader, bufSize);
    }
    
    public String resolveCharacterEntity(String name) throws IOException {
        Object charCode = LATIN1_ENTITIES.get(name);
        if (charCode != null) {
            return String.valueOf((char) Integer.parseInt((String) charCode));
        } else {
            return '&' + name + ';';
        }
    }
    
    public static boolean isEnd(ParseEvent event) {
		return event.getType() == Xml.END_DOCUMENT;
	}

    public static boolean isEnd(ParseEvent event, String tagName) {
		return isEnd(event)
				|| ((event.getType() == Xml.END_TAG) && (event.getName()
						.equals(tagName)));
	}

    public static boolean isCancelled(String eventStatus) {
        return (eventStatus != null) && eventStatus.endsWith("canceled");
    }

    public static String parseTextAttribute(XmlParser xmlParser, String nodeName,
			String attributeName) throws IOException {
		ParseEvent nextEvent = xmlParser.peek();
	
		while (!isEnd(nextEvent)) {
			String nextName = nextEvent.getName();
			int type = nextEvent.getType();
	
			if ((type == Xml.END_TAG) && nodeName.equals(nextName)) {
				// end tag found
				return null;
			}
	
			xmlParser.read();
			if (type == Xml.START_TAG && nodeName.equals(nextName)) {
				String text = nextEvent.getAttribute(attributeName).getValue();
				return text;
			}
			nextEvent = xmlParser.peek();
		}
		// no more events found
		return null;
	}

    public static String parseTextNode(XmlParser xmlParser, String name)
			throws IOException {
		ParseEvent nextEvent = xmlParser.peek();
		while (!isEnd(nextEvent)) {
			String nextName = nextEvent.getName();
			int type = nextEvent.getType();
			if (type == Xml.END_TAG && name.equals(nextName)) {
				// end tag found
				return null;
			}
	
			xmlParser.read();
			if (type == Xml.TEXT) {
				return nextEvent.getText();
			}
			nextEvent = xmlParser.peek();
		}
		// no more events found
		return null;
	}

	private static Hashtable LATIN1_ENTITIES = new Hashtable();
    
    static {
        LATIN1_ENTITIES.put("nbsp", "160");
        LATIN1_ENTITIES.put("iexcl", "161");
        LATIN1_ENTITIES.put("cent", "162");
        LATIN1_ENTITIES.put("pound", "163");
        LATIN1_ENTITIES.put("curren", "164");
        LATIN1_ENTITIES.put("yen", "165");
        LATIN1_ENTITIES.put("brvbar", "166");
        LATIN1_ENTITIES.put("sect", "167");
        LATIN1_ENTITIES.put("uml", "168");
        LATIN1_ENTITIES.put("copy", "169");
        LATIN1_ENTITIES.put("ordf", "170");
        LATIN1_ENTITIES.put("laquo", "171");
        LATIN1_ENTITIES.put("not", "172");
        LATIN1_ENTITIES.put("shy", "173");
        LATIN1_ENTITIES.put("reg", "174");
        LATIN1_ENTITIES.put("macr", "175");
        LATIN1_ENTITIES.put("deg", "176");
        LATIN1_ENTITIES.put("plusmn", "177");
        LATIN1_ENTITIES.put("sup2", "178");
        LATIN1_ENTITIES.put("sup3", "179");
        LATIN1_ENTITIES.put("acute", "180");
        LATIN1_ENTITIES.put("micro", "181");
        LATIN1_ENTITIES.put("para", "182");
        LATIN1_ENTITIES.put("middot", "183");
        LATIN1_ENTITIES.put("cedil", "184");
        LATIN1_ENTITIES.put("sup1", "185");
        LATIN1_ENTITIES.put("ordm", "186");
        LATIN1_ENTITIES.put("raquo", "187");
        LATIN1_ENTITIES.put("frac14", "188");
        LATIN1_ENTITIES.put("frac12", "189");
        LATIN1_ENTITIES.put("frac34", "190");
        LATIN1_ENTITIES.put("iquest", "191");
        LATIN1_ENTITIES.put("Agrave", "192");
        LATIN1_ENTITIES.put("Aacute", "193");
        LATIN1_ENTITIES.put("Acirc", "194");
        LATIN1_ENTITIES.put("Atilde", "195");
        LATIN1_ENTITIES.put("Auml", "196");
        LATIN1_ENTITIES.put("Aring", "197");
        LATIN1_ENTITIES.put("AElig", "198");
        LATIN1_ENTITIES.put("Ccedil", "199");
        LATIN1_ENTITIES.put("Egrave", "200");
        LATIN1_ENTITIES.put("Eacute", "201");
        LATIN1_ENTITIES.put("Ecirc", "202");
        LATIN1_ENTITIES.put("Euml", "203");
        LATIN1_ENTITIES.put("Igrave", "204");
        LATIN1_ENTITIES.put("Iacute", "205");
        LATIN1_ENTITIES.put("Icirc", "206");
        LATIN1_ENTITIES.put("Iuml", "207");
        LATIN1_ENTITIES.put("ETH", "208");
        LATIN1_ENTITIES.put("Ntilde", "209");
        LATIN1_ENTITIES.put("Ograve", "210");
        LATIN1_ENTITIES.put("Oacute", "211");
        LATIN1_ENTITIES.put("Ocirc", "212");
        LATIN1_ENTITIES.put("Otilde", "213");
        LATIN1_ENTITIES.put("Ouml", "214");
        LATIN1_ENTITIES.put("times", "215");
        LATIN1_ENTITIES.put("Oslash", "216");
        LATIN1_ENTITIES.put("Ugrave", "217");
        LATIN1_ENTITIES.put("Uacute", "218");
        LATIN1_ENTITIES.put("Ucirc", "219");
        LATIN1_ENTITIES.put("Uuml", "220");
        LATIN1_ENTITIES.put("Yacute", "221");
        LATIN1_ENTITIES.put("THORN", "222");
        LATIN1_ENTITIES.put("szlig", "223");
        LATIN1_ENTITIES.put("agrave", "224");
        LATIN1_ENTITIES.put("aacute", "225");
        LATIN1_ENTITIES.put("acirc", "226");
        LATIN1_ENTITIES.put("atilde", "227");
        LATIN1_ENTITIES.put("auml", "228");
        LATIN1_ENTITIES.put("aring", "229");
        LATIN1_ENTITIES.put("aelig", "230");
        LATIN1_ENTITIES.put("ccedil", "231");
        LATIN1_ENTITIES.put("egrave", "232");
        LATIN1_ENTITIES.put("eacute", "233");
        LATIN1_ENTITIES.put("ecirc", "234");
        LATIN1_ENTITIES.put("euml", "235");
        LATIN1_ENTITIES.put("igrave", "236");
        LATIN1_ENTITIES.put("iacute", "237");
        LATIN1_ENTITIES.put("icirc", "238");
        LATIN1_ENTITIES.put("iuml", "239");
        LATIN1_ENTITIES.put("eth", "240");
        LATIN1_ENTITIES.put("ntilde", "241");
        LATIN1_ENTITIES.put("ograve", "242");
        LATIN1_ENTITIES.put("oacute", "243");
        LATIN1_ENTITIES.put("ocirc", "244");
        LATIN1_ENTITIES.put("otilde", "245");
        LATIN1_ENTITIES.put("ouml", "246");
        LATIN1_ENTITIES.put("divide", "247");
        LATIN1_ENTITIES.put("oslash", "248");
        LATIN1_ENTITIES.put("ugrave", "249");
        LATIN1_ENTITIES.put("uacute", "250");
        LATIN1_ENTITIES.put("ucirc", "251");
        LATIN1_ENTITIES.put("uuml", "252");
        LATIN1_ENTITIES.put("yacute", "253");
        LATIN1_ENTITIES.put("thorn", "254");
        LATIN1_ENTITIES.put("yuml", "255");
    }
}
