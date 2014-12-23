package org.earthsystemmodeling.rulestest;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class LoadOntologyWithResultsFromParser {

	public static void main(String[] args) {
		QuestionnaireResponseParser qrp = new QuestionnaireResponseParser(
				"questionnaire_output.xml");
		qrp.parsePropertiesAndValues();
		HashMap<String, List<String>> propertiesAndValues = qrp
				.getPropertiesAndValues();
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			File file = new File("owl/esfdl.owl");
			OWLOntology owlOntology = manager
					.loadOntologyFromOntologyDocument(file);
			IRI ontologyIRI = owlOntology.getOntologyID().getOntologyIRI();
			String modellingInfrastructureName = qrp
					.getModelingInfrastructureName();
			OWLDataFactory factory = manager.getOWLDataFactory();
			OWLIndividual modelInfraIndividal = factory
					.getOWLNamedIndividual(IRI.create(ontologyIRI + "#"
							+ modellingInfrastructureName));
			Iterator<Entry<String, List<String>>> it = propertiesAndValues
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, List<String>> pairs = (Map.Entry<String, List<String>>) it
						.next();
				String prop = pairs.getKey();
				List<String> valuesList = pairs.getValue();
				if (prop.equalsIgnoreCase("DeveloperDocumentation")
						|| prop.equalsIgnoreCase("OpenSource")
						|| prop.equalsIgnoreCase("ModelComponentAbstraction")
						|| prop.equalsIgnoreCase("SparseMatrixMultiplication")) {
					OWLDataProperty dataPropObj = factory
							.getOWLDataProperty(IRI.create(ontologyIRI + "#has"
									+ prop));
					for (String val : valuesList) {
						OWLDataPropertyAssertionAxiom dataPropAsserAxiom = factory
								.getOWLDataPropertyAssertionAxiom(dataPropObj,
										modelInfraIndividal, val);
						System.out.println(dataPropAsserAxiom);
					}
				} else {
					OWLObjectProperty propObj = factory
							.getOWLObjectProperty(IRI.create(ontologyIRI
									+ "#has" + prop));
					for (String val : valuesList) {
						OWLIndividual valIndividual = factory
								.getOWLNamedIndividual(IRI.create(ontologyIRI
										+ "#" + val));
						OWLObjectPropertyAssertionAxiom objPropAsserAxiom = factory
								.getOWLObjectPropertyAssertionAxiom(propObj,
										modelInfraIndividal, valIndividual);
						System.out.println(objPropAsserAxiom);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}