package org.earthsystemmodeling.rulestest;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
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
			OWLOntologyManager manager_for_output = OWLManager
					.createOWLOntologyManager();
			File file = new File("owl/ESFDL_Ontology.owl");
			OWLOntology owlOntology = manager
					.loadOntologyFromOntologyDocument(file);
			IRI ontologyIRI = owlOntology.getOntologyID().getOntologyIRI();
			OWLOntology owlOntology1 = manager_for_output
					.createOntology(ontologyIRI);
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
				if (owlOntology.containsDataPropertyInSignature(IRI
						.create(ontologyIRI + "#has" + prop))) {
					OWLDataProperty dataPropObj = factory
							.getOWLDataProperty(IRI.create(ontologyIRI + "#has"
									+ prop));
					for (String val : valuesList) {
						boolean value = false;
						System.out.println(val);
						if (val.equals("yes")) {
							value = true;
						}
						OWLDataPropertyAssertionAxiom dataPropAsserAxiom = factory
								.getOWLDataPropertyAssertionAxiom(dataPropObj,
										modelInfraIndividal, value);
						AddAxiom addax = new AddAxiom(owlOntology1,
								dataPropAsserAxiom);
						manager_for_output.applyChange(addax);
						System.out.println(dataPropAsserAxiom);
					}
				} else {
					OWLObjectProperty propObj = factory
							.getOWLObjectProperty(IRI.create(ontologyIRI
									+ "#has" + prop));
					for (String val : valuesList) {
						val = val.replace(" ", "_");
						OWLIndividual valIndividual = factory
								.getOWLNamedIndividual(IRI.create(ontologyIRI
										+ "#" + val));
						OWLObjectPropertyAssertionAxiom objPropAsserAxiom = factory
								.getOWLObjectPropertyAssertionAxiom(propObj,
										modelInfraIndividal, valIndividual);
						AddAxiom addax = new AddAxiom(owlOntology1,
								objPropAsserAxiom);
						manager_for_output.applyChange(addax);
						System.out.println(objPropAsserAxiom);
					}
				}
			}
			manager_for_output.saveOntology(owlOntology1, new FileOutputStream(
					"EFFDL_From_Questionnaire.owl"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}