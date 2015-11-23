package edu.cornell.vivo.ontologyparser;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

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
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
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

	private static String UNMAPPED_FILEPATH = "resources/unmapped/unmapped.txt";
	private static String FORMAT = "TURTLE";
	private static final String ANNOTATION_PROP = OWL.AnnotationProperty.toString();
	private static final String DATATYPE_PROP = OWL.DatatypeProperty.toString();
	private static final String OBJECT_PROP = OWL.ObjectProperty.toString();
	private Map<Resource, String> typeMap = new HashMap<Resource, String>();
	private Set<String> namespaces = new TreeSet<String>();
	private Set<Statement> aux_stmts = new HashSet<Statement>();
	private Set<Statement> unmapped_stmts = new HashSet<Statement>();
	private Map<String, Model> ont_model_map = new HashMap<String, Model>();
	private Map<String, String> bnode_ont_map = new TreeMap<String, String>();
	private Map<String, String> namespace_ont_map = new HashMap<String, String>();
	private Model main_model;
	Map<Resource, Set<Statement>> rdfs = null;
	Map<Resource, Set<Statement>> rdf = null;
	Map<Resource, Set<Statement>> owl = null;
	Map<Resource, Set<Statement>> xsd = null;

	private static final String OUTPUT_FILE_EXTENSION = ".nt";  //.nt , .rdf   (file extension)
	private static final String OUTPUT_FILE_FORMAT = "N-TRIPLE";  //RDF/XML-ABBREV , RDF/XML, N-TRIPLE
	private static final String OUTPUT_FILE_FOLDER = "nt";  // rdf , nt or test
	private static File VIVO_FILE = new File("resources/input/vivo-isf-public-1.6.nt");

	public static void main(String args[]){

		VivoOntologyParser parser = new VivoOntologyParser();
		// load ontology
		parser.main_model = parser.loadOntology(VIVO_FILE, FORMAT);
		// list of namespaces.
		parser.browseNamespaces(parser.main_model);
		// load object/datatype/annotation properties map.
		parser.typeMap = new ResourceTypeReader().getTypeMap(parser.main_model);
		// load owl, rdf and rdfs maps
		parser.rdfs =  new RdfOwlRdfdTripleExtractor().listRDFSEntitiesData(parser.main_model);
		parser.rdf =  new RdfOwlRdfdTripleExtractor().listRDFEntitiesData(parser.main_model);
		parser.owl =  new RdfOwlRdfdTripleExtractor().listOWLEntitiesData(parser.main_model);
		parser.xsd =  new RdfOwlRdfdTripleExtractor().listXSDEntitiesData(parser.main_model);
		parser.createAuxStatementSet();
		// create namespace-ontologyname maps
		parser.createNamespaceOntologyMap();
		// build empty ontologyname-rdfmodels maps
		parser.buildEmptyRDFModelMaps();
		// parser and load distinct models
		parser.readAndParseModel();
		// save models and unmapped entries
		parser.saveData();
		logger.info("Process completed.");
	}

	/**
	 * saving the distinct models (in files /resources/nt  or /resources/rdf depending upon selection of the format)
	 * saving the unmapped statements (in files/resources/unmapped/)
	 */
	private void saveData(){
		saveModels();
		saveUnMappedStatments();
	}

	private void saveUnMappedStatments() {
		Set<Statement> unmapped = new HashSet<Statement>();
		for(Statement stmt: unmapped_stmts){
			if(!aux_stmts.contains(stmt)){
				unmapped.add(stmt);
			}
		}

		logger.info("Step: Saving unmapped triple (count:"+unmapped.size()+")");
		PrintWriter printWriter = null;
		try {
			printWriter = new PrintWriter (UNMAPPED_FILEPATH);
			for(Statement stmt: unmapped){
//				if(!stmt.getSubject().getNameSpace().equals(RDFS.getURI()) || 
//						!stmt.getSubject().getNameSpace().equals(OWL.getURI())){
//					System.out.println(stmt.getSubject().getNameSpace());
//				}
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

	/**
	 * creates namespace-ontologyname map
	 */
	private void createNamespaceOntologyMap() {
		logger.info("Step: Creating Namespace-OntologyName Map...");
		Namespace namespaces[] = Namespace.values();
		for(Namespace ns: namespaces){
			namespace_ont_map.put(ns.getNamespace(), ns.getOntologyName());
		}
		logger.info("Step: Creating Namespace-OntologyName Map... COMPLETED.");
	}

	/**
	 * builds empty OntologyName-(Empty)RDFModels map.
	 */
	private void buildEmptyRDFModelMaps() {
		logger.info("Step: Building empty RDF models...");
		Namespace namespaces[] = Namespace.values();
		for(Namespace ns: namespaces){
			ont_model_map.put(ns.getOntologyName(), ModelFactory.createDefaultModel());
		}
		logger.info("Step: Building empty RDF models... COMPLETED.");
	}

	/**
	 * Loads ontology from a file.
	 * @param file (ontology file)
	 * @param format (format of the ontology file)
	 * @return
	 */
	Model loadOntology(File file, String format) {
		logger.info("Step: Loading VIVO Ontology...");
		Model model = ModelFactory.createDefaultModel();
		model.read(file.getAbsolutePath(), format) ;
		logger.info("Step: Loading VIVO Ontology... COMPLETED.");
		return model;
	}

	/**
	 * The is the main method that reads the loaded ontology file 
	 * and split it in distinct ontology modules.
	 */
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
					Model model = ont_model_map.get(ontology);
					if(ontology != null){
						model.add(stmt);
						checkAuxiliaryStatements(model, stmt);
					}else{
						logger.debug("No ontology mapping found for:"+ stmt.toString());
						unmapped_stmts.add(stmt);
					}
				}else {	
					String ontology = bnode_ont_map.get(sub.toString());
					if(ontology != null){
						Model model = ont_model_map.get(ontology);
						model.add(stmt);
						checkAuxiliaryStatements(model, stmt);
					}else{
						logger.debug("No ontology mapping found for:"+ stmt.toString());
						unmapped_stmts.add(stmt);
					}
				}
			}catch(NullPointerException exp){
				logger.error(exp.toString());
			}
		}
	}

	private void checkAuxiliaryStatements(Model model, Statement stmt) {
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

		String ns = stmt.getPredicate().getNameSpace();
		checkForAdditionalTriplesObject(model, ns, stmt);
		RDFNode node = stmt.getObject();
		if(node.isURIResource()){
			checkForAdditionalTriplesObject(model, node.asResource().getNameSpace(), stmt);
		}

	}

	private void createAuxStatementSet() {
		Set<Resource> set = rdfs.keySet();
		for(Resource r: set){
			Set<Statement> rdfs_stmt = rdfs.get(r);
			for(Statement s: rdfs_stmt){
				aux_stmts.add(s);
			}
		}
		set = rdf.keySet();
		for(Resource r: set){
			Set<Statement> rdf_stmt = rdf.get(r);
			for(Statement s: rdf_stmt){
				aux_stmts.add(s);
			}
		}
		set = owl.keySet();
		for(Resource r: set){
			Set<Statement> owl_stmt = owl.get(r);
			for(Statement s: owl_stmt){
				aux_stmts.add(s);
			}
		}
		set = xsd.keySet();
		for(Resource r: set){
			Set<Statement> xsd_stmt = xsd.get(r);
			for(Statement s: xsd_stmt){
				aux_stmts.add(s);
			}
		}		
	}


	private void checkForAdditionalTriplesObject(Model model, String ns, Statement stmt){
		if(ns.equals(OWL.getURI())){
			Set<Statement> set = owl.get(stmt.getPredicate()); // get additional triples for owl property, if any
			if(set != null){
				logger.debug("addtional triples match found for: "+stmt);
				for(Statement s: set){
					model.add(s);
				}
			}
		}else if(ns.equals(RDF.getURI())){
			Set<Statement> set = rdf.get(stmt.getPredicate()); // get additional triples for rdf property, if any
			if(set != null){
				logger.debug("addtional triples match found for: "+stmt);
				for(Statement s: set){
					model.add(s);
				}
			}
		}else if(ns.equals(RDFS.getURI())){
			Set<Statement> set = rdfs.get(stmt.getPredicate()); // get additional triples for rdfs property, if any
			if(set != null){

				logger.debug("addtional triples match found for: "+stmt);
				for(Statement s: set){
					model.add(s);
				}
			}
		}else if(ns.equals(XSD.getURI())){
			Set<Statement> set = xsd.get(stmt.getObject()); // get additional triples for xsd object, if any
			if(set != null){
				logger.debug("addtional triples match found for: "+stmt);
				for(Statement s: set){
					model.add(s);
				}
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


	private void browseNamespaces(Model model) {
		StmtIterator stmtIterator = model.listStatements();
		for(;stmtIterator.hasNext();){
			Statement stmt = stmtIterator.next();
			if(stmt.getSubject().isURIResource() && !namespaces.contains(stmt.getSubject().getNameSpace())){
				namespaces.add(stmt.getSubject().getNameSpace());
			}
			if(stmt.getObject().isURIResource()){
				Resource obj = stmt.getObject().asResource();
				if(!namespaces.contains(obj.getNameSpace())){
					namespaces.add(obj.getNameSpace());
				}
			}
		}
		logger.info("List of Namespaces:");
		for(String ns: namespaces){
			logger.info("\t\t"+ns);
		}
	}



}
