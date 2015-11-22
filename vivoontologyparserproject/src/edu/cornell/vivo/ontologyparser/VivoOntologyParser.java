package edu.cornell.vivo.ontologyparser;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is VIVO Ontology parser.
 * The VIVO Ontology extends a number of higher level ontologies such as BIBO, OBO, FOAF, VCARD, EVENT etc.
 * The purpose of this project is to parse the vivo ontology file into its separate ontology modules.
 * 
 * 
 * @author Muhammad Javed
 * Cornell University
 * Nov 20, 2015
 *
 */
public class VivoOntologyParser {

	private final static Logger logger = LoggerFactory.getLogger(VivoOntologyParser.class);

	private static String UNMAPPED_FILEPATH = "resources/unmapped/unmapped.nt";
	private static String FORMAT = "TURTLE";
	private static final String ANNOTATION_PROP = OWL.AnnotationProperty.toString();
	private static final String DATATYPE_PROP = OWL.DatatypeProperty.toString();
	private static final String OBJECT_PROP = OWL.ObjectProperty.toString();
	private Map<Resource, String> typeMap = new HashMap<Resource, String>();
	private Set<String> namespaces = new HashSet<String>();
	private Set<Statement> unmapped_stmts = new HashSet<Statement>();
	private Map<String, Model> ont_model_map = new HashMap<String, Model>();
	private Map<String, String> bnode_ont_map = new TreeMap<String, String>();
	private Map<String, String> namespace_ont_map = new HashMap<String, String>();
	private Model main_model;

	private static final String OUTPUT_FILE_EXTENSION = ".rdf";  //.nt , .rdf   (file extension)
	private static final String OUTPUT_FILE_FORMAT = "RDF/XML-ABBREV";  //RDF/XML-ABBREV , RDF/XML, N-TRIPLE
	private static final String OUTPUT_FILE_FOLDER = "rdf";  // rdf , nt or test
	private static File VIVO_FILE = 
			new File("resources/input/vivo-isf-public-1.6.nt");
			//new File("resources/input/test.nt");
	
	public static void main(String args[]){
		VivoOntologyParser parser = new VivoOntologyParser();
		parser.main_model = parser.loadOntology(VIVO_FILE, FORMAT);
		parser.typeMap = new ResourceTypeReader().getTypeMap(parser.main_model);
		parser.createURIOntologyMap();
		parser.buildEmptyRDFModelMaps();
		parser.readAndParseModel();
		parser.saveData();
		logger.info("Process completed.");
	}

	private void saveData(){
		saveModels();
		saveUnMappedStatments();
	}

	private void saveUnMappedStatments() {
		logger.info("Step: Saving unmapped triple (count:"+unmapped_stmts.size()+")");
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter (UNMAPPED_FILEPATH);
			for(Statement stmt: unmapped_stmts){
				printWriter.println (stmt.toString());
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally{
			printWriter.close();   
		}
	}

	private void saveModels() {
		PrintWriter printer = null;
		Set<String> ont = ont_model_map.keySet();
		for(String ontology: ont){
			Model model = ont_model_map.get(ontology);
			logger.info("Step: Saving "+ontology+" ontology: (triple count:"+model.size()+")");
			if(!model.isEmpty()){
				try {
					String newF = "resources/"+OUTPUT_FILE_FOLDER+"/"+ontology+OUTPUT_FILE_EXTENSION;
					printer = new PrintWriter(newF);
					model.write(printer, OUTPUT_FILE_FORMAT);   
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				}finally{
					printer.close();
				}
			}
		}
	}

	private void createURIOntologyMap() {
		logger.info("Step: Creating Namespace-OntologyName Map...");
		Namespace namespaces[] = Namespace.values();
		for(Namespace ns: namespaces){
			namespace_ont_map.put(ns.getNamespace(), ns.getOntologyName());
		}
		logger.info("Step: Creating Namespace-OntologyName Map... COMPLETED.");
	}

	private void buildEmptyRDFModelMaps() {
		logger.info("Step: Building empty RDF models...");
		Namespace namespaces[] = Namespace.values();
		for(Namespace ns: namespaces){
			ont_model_map.put(ns.getOntologyName(), ModelFactory.createDefaultModel());
		}
		logger.info("Step: Building empty RDF models... COMPLETED.");
	}

	Model loadOntology(File file, String format) {
		logger.info("Step: Loading VIVO Ontology...");
		Model model = ModelFactory.createDefaultModel();
		model.read(file.getAbsolutePath(), format) ;
		logger.info("Step: Loading VIVO Ontology... COMPLETED.");
		return model;
	}

	private void readAndParseModel() {
		logger.info("Step: Reading VIVO ontology statements");
		StmtIterator stmtIterator = main_model.listStatements();
		Set<Statement> stmtSet = stmtIterator.toSet();
		logger.info(": RDF triples count: "+stmtSet.size()+" triples.");
		//browseTriples(stmtIterator);
		createBNodeMap(stmtSet);
		for(Statement stmt: stmtSet){
			try{
				Resource sub = stmt.getSubject();
				if(sub.isURIResource()){
					String ontology = getOntology(sub);
					if(ontology != null){
						Model model = ont_model_map.get(ontology);
						model.add(stmt);
						checkPredicateType(model, stmt);
					}else{
						logger.warn("No mapping found for:"+ stmt.toString());
						unmapped_stmts.add(stmt);
					}
				}else {	
					String ontology = bnode_ont_map.get(sub.toString());
					if(ontology != null){
						Model model = ont_model_map.get(ontology);
						model.add(stmt);
						checkPredicateType(model, stmt);
					}else{
						logger.warn("No mapping found for:"+ stmt.toString());
						unmapped_stmts.add(stmt);
					}
				}
			}catch(NullPointerException exp){
				logger.error(exp.toString());
			}
		}
	}

	private void checkPredicateType(Model model, Statement stmt) {
		Property pred = stmt.getPredicate();
		String type = typeMap.get(pred);
		if(type != null){
			if(type.equals(ANNOTATION_PROP)){
				model.add(stmt.getPredicate(), RDF.type, OWL.AnnotationProperty);
			}else if(type.equals(DATATYPE_PROP)){
				model.add(stmt.getPredicate(), RDF.type, OWL.DatatypeProperty);
			}else if(type.equals(OBJECT_PROP)){
				model.add(stmt.getPredicate(), RDF.type, OWL.ObjectProperty);
			}
		}
	}

	private void createBNodeMap(Set<Statement> stmtSet) {
		logger.info("Step: Mapping blank nodes to ontologies.");
		for(Statement stmt: stmtSet){
			RDFNode obj = stmt.getObject();
			Resource sub = stmt.getSubject();
			if(sub.isURIResource() && obj.isAnon() && !bnode_ont_map.keySet().contains(obj.toString())){
				String ont = getOntology(sub);
				if(ont != null){
					bnode_ont_map.put(obj.toString(), ont);
					runRecurrsiveOp(obj, ont);
				}else{
					logger.warn("namespace-ontology map not found: "+stmt+" (continuing...)");
				}
			}
		}
		logger.info("Step: Mapping blank nodes to ontologies...COMPLETED");
	}

	private String getOntology(Resource sub) {
		String ontology = namespace_ont_map.get(sub.getNameSpace());
		return ontology;
	}

	private void runRecurrsiveOp(RDFNode node, String ont) {
		bnode_ont_map.put(node.toString(), ont);
		StmtIterator iter = main_model.listStatements(
				new SimpleSelector(node.asResource(), null, (RDFNode) null){
					public boolean selects(Statement s){
						return s.getObject().isAnon();
					}
				});
		for(;iter.hasNext();){
			Statement stmt = iter.next();
			RDFNode obj = stmt.getObject();
			if(obj.isAnon() && !bnode_ont_map.keySet().contains(obj.toString())){
				bnode_ont_map.put(obj.toString(), ont);
				runRecurrsiveOp(obj, ont);
			}else{
				bnode_ont_map.put(node.toString(), ont);
				return;
			}
		}
	}


	private void browseTriples(StmtIterator stmtIterator) {
		for(;stmtIterator.hasNext();){
			Statement stmt = stmtIterator.next();
			if(!namespaces.contains(stmt.getSubject().getNameSpace())){
				if(stmt.getSubject().getNameSpace()!= null && stmt.getSubject().getNameSpace().startsWith("http://aims.fao.org/aos/")){
					//System.out.println(stmt.getSubject());
				}
				namespaces.add(stmt.getSubject().getNameSpace());
			}
			if(stmt.getObject().isResource()){
				Resource obj = stmt.getObject().asResource();
				if(!namespaces.contains(obj.getNameSpace())){
					if(stmt.getSubject().getNameSpace()!= null && stmt.getSubject().getNameSpace().startsWith("http://aims.fao.org/aos/")){
						//System.out.println(obj);
					}
					namespaces.add(obj.getNameSpace());
				}
			}
		}
	}


	
}
