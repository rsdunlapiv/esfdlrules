package org.earthsystemmodeling.rulestest;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

public class QuestionnaireResponseParser {

	File xmlFile;
	Document document;
	HashMap<String, List<String>> propertiesAndValues;

	public QuestionnaireResponseParser(String filename) {
		// TODO Auto-generated constructor stub
		SAXBuilder builder = new SAXBuilder();
		String path = "xml/" + filename;
		xmlFile = new File(path);
		propertiesAndValues = new HashMap<String, List<String>>();
		try {
			document = (Document) builder.build(xmlFile);
		} catch (JDOMException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Element getRootNode() {
		Element rootNode = document.getRootElement();
		return rootNode;
	}

	public List<Element> getChildNodes(Element parent) {
		List<Element> childNodes = parent.getChildren();
		return childNodes;
	}

	public Element getChildNode(Element parent, String childName) {
		Element childNode = parent.getChild(childName);
		return childNode;
	}

	public List<String> getValues(Element property) {
		List<String> valuesList = new ArrayList<String>();
		Element valuesElement = getChildNode(property, "values");
		List<Element> values = getChildNodes(valuesElement);
		for (Element e : values) {
			valuesList.add(e.getText());
		}
		return valuesList;
	}

	public void parsePropertiesAndValues() {
		Element rootNode = getRootNode();
		Element properties = getChildNode(rootNode, "properties");
		List<Element> propertyTags = getChildNodes(properties);
		for (Element e : propertyTags) {
			Element Values;
			String shortName = getChildNode(e, "shortName").getText();
			List<Element> valueTags;
			List<String> valuesList = new ArrayList<String>();
			if ((Values = getChildNode(e, "values")) != null) {
				valueTags = getChildNodes(Values);
				for (Element v : valueTags) {
					valuesList.add(v.getText());
				}
				propertiesAndValues.put(shortName, valuesList);
			} else {
				Element subPropertiesTag = getChildNode(e, "subProperties");
				List<Element> subProperties = getChildNodes(subPropertiesTag);
				for (Element v : subProperties) {
					shortName = getChildNode(v, "shortName").getText();
					valuesList = new ArrayList<String>();
					Element valuesTag = getChildNode(v, "values");
					valueTags = getChildNodes(valuesTag);
					for (Element m : valueTags) {
						valuesList.add(m.getText());
					}
					propertiesAndValues.put(shortName, valuesList);
				}
			}
		}
	}

	public void printPropertiesAndValues() {
		Iterator<Entry<String, List<String>>> it = propertiesAndValues
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, List<String>> pairs = (Map.Entry<String, List<String>>) it
					.next();
			System.out.println("Property: " + pairs.getKey() + " \nvalues: "
					+ pairs.getValue());
		}
	}

	public HashMap<String, List<String>> getPropertiesAndValues() {
		return propertiesAndValues;
	}

	public static void main(String args[]) {
		QuestionnaireResponseParser QRP = new QuestionnaireResponseParser(
				"questionnaire_output.xml");
		QRP.parsePropertiesAndValues();
		QRP.printPropertiesAndValues();
	}
}