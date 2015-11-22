package edu.cornell.vivo.ontologyparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
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

public class ResourceTypeReader {

	private final static Logger logger = LoggerFactory.getLogger(ResourceTypeReader.class);

	public Map<Resource, String> getTypeMap(Model model){
		Map<Resource, String> typeMap = new HashMap<Resource, String>();
		
		Set<Resource> anot =  listAnnotationProperties(model);
		for(Resource r: anot){
			typeMap.put(r, OWL.AnnotationProperty.toString());
		}
		Set<Resource> data =  listDataProperties(model);
		for(Resource r: data){
			typeMap.put(r, OWL.DatatypeProperty.toString());
		}
		Set<Resource> obj =  listObjectProperties(model);
		for(Resource r: obj){
			typeMap.put(r, OWL.ObjectProperty.toString());
		}
		
		return typeMap;
	}
	
	
	
	
	private Set<Resource> listAnnotationProperties(Model model) {
		Set<Resource> annot_prop = new HashSet<Resource>();
		logger.info("Step: Listing annotation properties.");
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						Property p = st.getPredicate();
						RDFNode o = st.getObject();
						return p.equals(RDF.type) && o.equals(OWL.AnnotationProperty);
					}
				});
		
		for(;iter.hasNext();){
			Statement st = iter.next();
			annot_prop.add(st.getSubject());
		}
		logger.info("Annotation properties count: "+annot_prop.size());
		return annot_prop;
	}
	
	private Set<Resource> listDataProperties(Model model) {
		Set<Resource> data_prop = new HashSet<Resource>();
		logger.info("Step: Listing data properties.");
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						Property p = st.getPredicate();
						RDFNode o = st.getObject();
						return p.equals(RDF.type) && o.equals(OWL.DatatypeProperty);
					}
				});
		
		for(;iter.hasNext();){
			Statement st = iter.next();
			data_prop.add(st.getSubject());
		}
		logger.info("Data properties count: "+data_prop.size());
		return data_prop;
	}
	
	private Set<Resource> listObjectProperties(Model model) {
		Set<Resource> object_prop = new HashSet<Resource>();
		logger.info("Step: Listing object properties.");
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						Property p = st.getPredicate();
						RDFNode o = st.getObject();
						return p.equals(RDF.type) && o.equals(OWL.ObjectProperty);
					}
				});
		
		for(;iter.hasNext();){
			Statement st = iter.next();
			object_prop.add(st.getSubject());
		}
		logger.info("Object properties count: "+object_prop.size());
		return object_prop;
	}
}
