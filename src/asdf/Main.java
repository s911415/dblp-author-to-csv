package asdf;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;

public class Main {
    private static final String NEW_LINE = System.lineSeparator();

    public static void main(String[] args) throws Exception {
        final var xmlFile = new File(args[0]);
        final var outCsvFile = new File(args[1]);
        final var fr = new InputStreamReader(new FileInputStream(xmlFile), StandardCharsets.ISO_8859_1);
        final var fstream = new OutputStreamWriter(new FileOutputStream(outCsvFile), StandardCharsets.UTF_16);
        var factory = SAXParserFactory.newInstance();
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, false);
        var saxParser = factory.newSAXParser();
        var handler = new SaxHandler(fstream);
        var source = new InputSource(fr);
        saxParser.parse(source, handler);
        fr.close();
    }

    private static class SaxHandler extends DefaultHandler {
        private final static HashSet<String> WrapTag = new HashSet<>(Arrays.asList("article|inproceedings|proceedings|book|incollection|phdthesis|mastersthesis|www|person|data".split("\\|")));
        private final Writer writer;
        private final StringBuilder authorsList = new StringBuilder();
        private boolean isTagOpened = false;
        private boolean isAuthorOpened = false;


        SaxHandler(Writer writer) {
            this.writer = writer;
        }

        public void startElement(String uri, String localName, String qName,
                                 Attributes attributes) throws SAXException {

            final var TAG = qName.toLowerCase();
            if (WrapTag.contains(TAG)) {
                authorsList.setLength(0);
                isTagOpened = true;
            } else if (TAG.equals("author")) {
                isAuthorOpened = true;
            }
        }

        public void endElement(String uri, String localName,
                               String qName) throws SAXException {
            final var TAG = qName.toLowerCase();
            if (WrapTag.contains(TAG)) {
                try {
                    final var len = authorsList.length();
                    if (len > 0) {
                        authorsList.setLength(len - 1);
                        writer.write(authorsList.toString());
                        writer.write(NEW_LINE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                authorsList.setLength(0);
                isTagOpened = false;
            } else if (TAG.equals("author")) {
                isAuthorOpened = false;
            }
        }

        public void characters(char ch[], int start, int length) throws SAXException {
            if (isAuthorOpened) {
                authorsList.append('"');
                authorsList.append(ch, start, length);
                authorsList.append('"');
                authorsList.append(',');
            }
        }
    }
}
