package org.earthsystemmodeling.rulestest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Set;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxClassExpressionParser;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.HermiT.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.OWLExpressionParser;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.reasoner.ConsoleProgressMonitor;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLAPIRule;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.core.SWRLRuleEngineFactory;
import org.swrlapi.drools.core.DroolsSWRLRuleEngineCreator;
import org.swrlapi.parser.SWRLParseException;

public class RulesTest {

	public static void main(String[] args) throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		
		queryExample();
		
	}
	
	public static void queryExample() throws OWLOntologyCreationException, OWLOntologyStorageException, FileNotFoundException {
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		File file = new File("owl/ESFDL_Ontology.owl");
		OWLOntology owlOntology = manager.loadOntologyFromOntologyDocument(file);
	
		IRI ontologyIRI = owlOntology.getOntologyID().getOntologyIRI();
		System.out.println("ontologyIRI = " + ontologyIRI);
		PrefixManager pm = new DefaultPrefixManager(ontologyIRI.toString());
		
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		//get handle on ModelingInfrastructure class
		OWLClass classMI = factory.getOWLClass(pm.getIRI("#ModelingInfrastructure"));
		
		/*
		 * Create a class that represents the query - in other words, the
		 * instances of this class are the results of the query.
		 * 
		 * The query below finds all instances of ModelingInfrastructure
		 * that have AngleConversion as a BasicCapability.
		 * 
		 * Class definition in Manchester OWL syntax:
		 * 
		 * Class: esfdl:ModelingInfrastructureQuery
		 *
    	 *	EquivalentTo: 
         *		esfdl:ModelingInfrastructure
         *		and (esfdl:hasBasicCapabilities some ({esfdl:AngleConversion}))
		 *
		 */
		OWLClass classQuery = factory.getOWLClass(pm.getIRI("#ModelingInfrastructureQuery"));
				
		OWLObjectProperty objPropBasicCap = factory.getOWLObjectProperty(pm.getIRI("#hasBasicCapabilities"));
		OWLNamedIndividual indivAngleConv = factory.getOWLNamedIndividual("#AngleConversion", pm);		
		OWLObjectSomeValuesFrom basicCaps = factory.getOWLObjectSomeValuesFrom(objPropBasicCap, factory.getOWLObjectOneOf(indivAngleConv));
		OWLObjectIntersectionOf intersect = factory.getOWLObjectIntersectionOf(classMI, basicCaps);
		
		OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(classQuery, intersect);
		manager.addAxiom(owlOntology, axiom);
		
		//saving the ontology, as in the line below, is not necessary for the reasoner, but can be helpful
		//to debug the created class definition in Protege
		//manager.saveOntology(owlOntology, new FileOutputStream("owl/ESFDL_Ontology_out.owl"));
		
		// above we created the class definition programmatically, here we create
		// it from a string using an OWL expression parser
		//String query = "ModelingInfrastructure and (hasBasicCapabilities some ({AngleConversion}))";
		//OWLClassExpression expr = parseManchester(manager, owlOntology, query);
		//System.out.println("Parsed expression: " + expr);
		//OWLAxiom axiom = factory.getOWLEquivalentClassesAxiom(classQuery, expr);
		//manager.addAxiom(owlOntology, axiom);

		
		//run the HermiT reasoner, which will determine which individuals are 
		//instances of the created class
		OWLReasoner reasoner = new Reasoner.ReasonerFactory().createReasoner(owlOntology);
		reasoner.precomputeInferences();
        //System.out.println("Consistent: " + reasoner.isConsistent());
            
        //NodeSet<OWLNamedIndividual> nodeSet = reasoner.getInstances(classMI, false);
		//for (OWLIndividual indiv : nodeSet.getFlattened()) {
		//	System.out.println("Modeling Infrastructure: " + indiv);
		//}
        
		//display list of individuals that satisfy the query
		NodeSet<OWLNamedIndividual> nodeSet = reasoner.getInstances(classQuery, true);
		for (OWLIndividual indiv : nodeSet.getFlattened()) {
			System.out.println("Query result: " + indiv);
		}
		
		
		
		
	}
	
	
	private static OWLClassExpression parseManchester(OWLOntologyManager manager, OWLOntology ont, String manchesterQuery){
		
		ManchesterOWLSyntaxEditorParser parser = new
				ManchesterOWLSyntaxEditorParser(manager.getOWLDataFactory(), manchesterQuery);
		parser.setDefaultOntology(ont);
		Set<OWLOntology> importsClosure = ont.getImportsClosure();
		BidirectionalShortFormProvider bidiShortFormProvider = 
				new BidirectionalShortFormProviderAdapter(manager, importsClosure, new SimpleShortFormProvider());
		OWLEntityChecker entityChecker = 
				new ShortFormEntityChecker(bidiShortFormProvider);
		parser.setOWLEntityChecker(entityChecker);

		OWLClassExpression classExp = null;
		try {
			classExp = parser.parseClassExpression();
		} catch (ParserException e) {
			e.printStackTrace();
		}

		return classExp;
	}
	
	
	public static void rulesExample() throws OWLOntologyCreationException, SWRLParseException {
		
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
		String ruleText = "esfdl:supportsPlatform(?mi2, ?plat) ^ esfdl:supportsPlatform(?mi1, ?plat) -> esfdl:compatible(?mi1, ?mi2)";
		swrlOntology.createSWRLRule("RuleName", ruleText, "Comment", true);
		
		IRI modelingInfrastructure = IRI.create("http://www.earthsystemcog.org/projects/es-fdl/esfdl.owl#ModelingInfrastructure");
		OWLClass classMI = manager.getOWLDataFactory().getOWLClass(modelingInfrastructure);
		
		IRI compatIRI = IRI.create("http://www.earthsystemcog.org/projects/es-fdl/esfdl.owl#compatible");
		OWLObjectProperty compatProp = manager.getOWLDataFactory().getOWLObjectProperty(compatIRI);
		
		
		
		
	}

}
