package org.earthsystemmodeling.rulestest;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

public class BuildOntologyUsingOWLAPI {

	public static void main(String[] args) {
		QuestionnaireResponseParser qrp = new QuestionnaireResponseParser(
				"questionnaire_output.xml");
		qrp.parsePropertiesAndValues();
		HashMap<String, List<String>> propertiesAndValues = qrp
				.getPropertiesAndValues();
		try {
			OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
			OWLDataFactory factory = manager.getOWLDataFactory();
			IRI ontologyIRI = IRI.create("http://www.wwe.com");
			OWLOntology ont = manager.createOntology(ontologyIRI);
			OWLClass modellinginfrastructure = factory.getOWLClass(IRI
					.create(ontologyIRI + "#modellinginfrastructure"));
			Iterator<Entry<String, List<String>>> it = propertiesAndValues
					.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String, List<String>> pairs = (Map.Entry<String, List<String>>) it
						.next();
				OWLClass propname = factory.getOWLClass(IRI.create(ontologyIRI
						+ "#" + pairs.getKey()));
				OWLObjectProperty objprop = factory.getOWLObjectProperty(IRI
						.create(ontologyIRI + "#has" + pairs.getKey()));
				OWLObjectPropertyDomainAxiom axiom_domain = factory
						.getOWLObjectPropertyDomainAxiom(objprop,
								modellinginfrastructure);
				AddAxiom adaax = new AddAxiom(ont, axiom_domain);
				manager.applyChange(adaax);
				OWLObjectPropertyRangeAxiom axiom_range = factory
						.getOWLObjectPropertyRangeAxiom(objprop, propname);
				AddAxiom adaax1 = new AddAxiom(ont, axiom_range);
				manager.applyChange(adaax1);
			}
			manager.saveOntology(ont, new FileOutputStream("temp.xml"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}