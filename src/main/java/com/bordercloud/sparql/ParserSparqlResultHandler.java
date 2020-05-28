package com.bordercloud.sparql;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

public class ParserSparqlResultHandler  extends DefaultHandler {
        /**
         * @var array
         */
        private HashMap<String, Object> _result;

        /**
         * @var
         */
        private int _rowCurrent;

        /**
         * @var
         */
        private String _cellCurrent;

        /**
         * @var
         */
        private StringBuffer _value;

        /**
         *  constructor.
         */
        public ParserSparqlResultHandler() {
            super();
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
            _result = new HashMap();
        }

    public HashMap<String, Object> getResult() {
        return _result;
    }

//        /**
//         * callback for the start of each element
//         *
//         * @param parserObject
//         * @param elementname
//         * @param attribute
//         */
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if (name.equalsIgnoreCase("sparql")) {
            // init a new response
            _result.remove("boolean");
            _result.put("result", new HashMap<String, ArrayList<String>>());
        } else if (name.equalsIgnoreCase("head")) {
            ((HashMap<String, ArrayList<String>>) _result.get("result")).put("variables", new ArrayList<String>());
        } else if (name.equalsIgnoreCase("variable")) {
            ((HashMap<String, ArrayList<String>>) _result.get("result")).get("variables").add(attributes.getValue("name"));
        } else if (name.equalsIgnoreCase("results")) {
            _rowCurrent = -1;
            ((HashMap<String, ArrayList<List>>) _result.get("result")).put("rows", new ArrayList<List>());
        } else if (name.equalsIgnoreCase("result")) {
            _rowCurrent++;
            ((ArrayList) ((HashMap<String, ArrayList<List>>) _result.get("result")).get("rows")).add(new HashMap<String, Object>());
        } else if (name.equalsIgnoreCase("binding")) {
            _value = new StringBuffer();
            _cellCurrent = attributes.getValue("name");
        } else if (_cellCurrent != null) {
            addValueCurrentRow(_cellCurrent + " type", name);
            if (attributes.getValue("xml:lang") != null) {
                addValueCurrentRow(_cellCurrent + " lang", attributes.getValue("xml:lang"));
            }
            if (attributes.getValue("datatype") != null) {
                addValueCurrentRow(_cellCurrent + " datatype", attributes.getValue("datatype"));
            }
        } else if (name.equalsIgnoreCase("boolean")) {
            _value = new StringBuffer();
            _cellCurrent = "boolean" ;
        }
    }
    
        @Override
        public void endElement(String uri, String localName, String name) throws SAXException {
            if (name.equalsIgnoreCase("binding")) {
                String value = "";
                if (_value == null) {
                    return;
                }                   
                value = _value.toString().trim();
                if (value.length() == 0) {
                    return;
                }

                if (!isKeyExistCurrentRow(_cellCurrent + " type")) {
                    addValueCurrentRow(_cellCurrent + " type", null);
                }

                if (getValueCurrentRow(_cellCurrent + " type") == "uri") {
                    addValueCurrentRow(_cellCurrent, value);
                } else if (getValueCurrentRow(_cellCurrent + " type")  == "bnode") {
                    addValueCurrentRow(_cellCurrent, value);
                } else if (getValueCurrentRow(_cellCurrent + " type")  == "literal") {
                    if (isKeyExistCurrentRow(_cellCurrent + " datatype")) {
                        //https://xmlbeans.apache.org/docs/3.0.0/guide/conXMLBeansSupportBuiltInSchemaTypes.html
                    //if (array_key_exists(this.cellCurrent . " datatype", this.result['result']['rows'][this.rowCurrent])) {
                        switch (Datatype.find((String) getValueCurrentRow(_cellCurrent + " datatype"))){
                            case xsdshort :
                            case xsdunsignedByte :
                                addValueCurrentRow(_cellCurrent, new Short(value));
                                break;
                            case xsdint :
                            case xsdunsignedShort :
                                addValueCurrentRow(_cellCurrent, new Integer(value));
                                break;
                            case xsdinteger :
                            case xsdnonPositiveInteger :
                            case xsdnegativeInteger :
                            case xsdnonNegativeInteger :
                            case xsdpositiveInteger :
                                addValueCurrentRow(_cellCurrent, new BigInteger(value));
                                break;
                            case xsdlong :
                            case xsdunsignedInt :
                            case xsdunsignedLong :
                                addValueCurrentRow(_cellCurrent, new Long(value));
                                break;
                            case xsddecimal :
                                addValueCurrentRow(_cellCurrent, new BigDecimal(value));
                                break;
                            case xsdfloat :
                                addValueCurrentRow(_cellCurrent, new Float(value));
                                break;
                            case xsddouble :
                                addValueCurrentRow(_cellCurrent, new Double(value));
                                break;
                            case xsdbyte :
                                addValueCurrentRow(_cellCurrent, new Byte(value));
                                break;
                            case xsddateTime :
                                TemporalAccessor ta = DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(value);
                                Instant i = Instant.from(ta);
                                Calendar datetime = GregorianCalendar.from(ZonedDateTime.from(i));
                                addValueCurrentRow(_cellCurrent, datetime);
                                break;
                            case xsdstring :
                            default:
                                addValueCurrentRow(_cellCurrent, value);
                        }
                    } else {
                        addValueCurrentRow(_cellCurrent, value);
                    }
                } else {
                    addValueCurrentRow(_cellCurrent, value);
                }
                _cellCurrent = null;
                _value = new StringBuffer();
            }else if (name.equalsIgnoreCase("boolean")) {
                boolean bool = false;
                if (_value != null) {
                    bool = _value.toString().trim().equalsIgnoreCase("true");
                }
                _result.put("boolean",bool);
                _result.remove("result");
                _cellCurrent = null;
                _value = new StringBuffer();
            }
        }

    private void addValueCurrentRow(String key, Object value) {
        ((HashMap<String, Object>) ((ArrayList) ((HashMap<String, ArrayList<List>>) _result.get("result")).get("rows")).get(_rowCurrent)).put(key, value);
    }

    private boolean isKeyExistCurrentRow(String key) {
        return null != ((HashMap<String, Object>) ((ArrayList) ((HashMap<String, ArrayList<List>>) _result.get("result")).get("rows")).get(_rowCurrent)).get(key);
    }

    private Object getValueCurrentRow(String key) {
        return ((HashMap<String, Object>) ((ArrayList) ((HashMap<String, ArrayList<List>>) _result.get("result")).get("rows")).get(_rowCurrent)).get(key);
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (_cellCurrent != null && _value != null) {
            _value.append(ch, start, length);
        }
    }
}
