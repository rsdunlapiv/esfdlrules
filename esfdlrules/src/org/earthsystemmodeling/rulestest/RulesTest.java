package org.earthsystemmodeling.rulestest;

import java.io.File;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleEngineFactory;
import org.swrlapi.drools.core.DroolsSWRLRuleEngineCreator;
import org.swrlapi.parser.SWRLParseException;

public class RulesTest {

	public static void main(String[] args) throws OWLOntologyCreationException, SWRLParseException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("owl/esfdl.owl");
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(file);
		IRI documentIRI = manager.getOntologyDocumentIRI(owlOntology);
				
		System.out.println("documentIRI = " + documentIRI);
		
		SWRLAPIOWLOntology swrlOntology = SWRLAPIFactory.createOntology(owlOntology);
		SWRLRuleEngineFactory factory = SWRLAPIFactory.createSWRLRuleEngineFactory();
		factory.registerRuleEngine(new DroolsSWRLRuleEngineCreator());
		
		SWRLRuleEngine ruleEngine = factory.createSWRLRuleEngine(swrlOntology);			
		
		for (SWRLAPIRule rule : ruleEngine.getSWRLRules()) {
			System.out.println("rule: " + rule);
		}
		
		ruleEngine.reset();
		ruleEngine.importSWRLRulesAndOWLKnowledge();
		ruleEngine.run();
		ruleEngine.writeInferredKnowledge();
		
		//ruleEngine.infer();  //does the above 4
		
		//add a new rule
		String ruleText = "simple:supportsPlatform(?mi2, ?plat) ^ simple:supportsPlatform(?mi1, ?plat) -> simple:compatible(?mi1, ?mi2)";
		swrlOntology.createSWRLRule("RuleName", ruleText, "Comment", true);
		
		IRI modelingInfrastructure = IRI.create("http://www.earthsystemcog.org/projects/es-fdl/simple.owl#ModelingInfrastructure");
		OWLClass classMI = manager.getOWLDataFactory().getOWLClass(modelingInfrastructure);
		
		IRI compatIRI = IRI.create("http://www.earthsystemcog.org/projects/es-fdl/simple.owl#compatible");
		OWLObjectProperty compatProp = manager.getOWLDataFactory().getOWLObjectProperty(compatIRI);
		
		Set<OWLIndividual> instances = classMI.getIndividuals(owlOntology);
		for (OWLIndividual indiv : instances) {
			System.out.println("Modeling Infrastructure: " + indiv);
			Set<OWLIndividual> propVals = indiv.getObjectPropertyValues(compatProp, owlOntology);
			for (OWLIndividual val : propVals) {
				System.out.println("\t Compatible with --> " + val);
			}
		}
		
		
	}

}
