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

	public String getModelingInfrastructureName() {
		Element rootNode = document.getRootElement();
		Element valuesElement = rootNode.getChild("shortName");
		return valuesElement.getText();
	}

	public Element getRootNode() {
		Element rootNode = document.getRootElement();
		return rootNode;
	}

	public void parsePropertiesAndValues() {
		Element rootNode = getRootNode();
		Element properties = rootNode.getChild("properties");
		List<Element> propertyTags = properties.getChildren();
		for (Element e : propertyTags) {
			Element Values;
			String shortName = e.getChild("shortName").getText();
			List<Element> valueTags;
			List<String> valuesList = new ArrayList<String>();
			if ((Values = e.getChild("values")) != null) {
				valueTags = Values.getChildren();
				for (Element v : valueTags) {
					valuesList.add(v.getText());
				}
				propertiesAndValues.put(shortName, valuesList);
			} else {
				Element subPropertiesTag = e.getChild("subProperties");
				List<Element> subProperties = subPropertiesTag.getChildren();
				for (Element v : subProperties) {
					shortName = v.getChild("shortName").getText();
					valuesList = new ArrayList<String>();
					Element valuesTag = v.getChild("values");
					valueTags = valuesTag.getChildren();
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