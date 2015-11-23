package edu.cornell.vivo.ontologyparser;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
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

public class RdfOwlRdfdTripleExtractor {

	private final static Logger logger = LoggerFactory.getLogger(RdfOwlRdfdTripleExtractor.class);

	public Map<Resource, Set<Statement>> listOWLEntitiesData(Model model) {
		Map<Resource, Set<Statement>> owl = new HashMap<Resource, Set<Statement>>();
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						if(s.isAnon()) return false;
						return s.getNameSpace().equals(OWL.getURI());
					}
				});
		if(iter != null){
			for(;iter.hasNext();){
				Statement st = iter.next();
				Set<Statement>  value = owl.get(st.getSubject());
				if(value == null){
					value = new HashSet<Statement>();
					value.add(st);
					owl.put(st.getSubject(), value);
				}else{
					value.add(st);
					owl.put(st.getSubject(), value);
				}	
			}
		}
		return owl;
	}

	public Map<Resource, Set<Statement>>  listRDFEntitiesData(Model model) {
		Map<Resource, Set<Statement>> rdf = new HashMap<Resource, Set<Statement>>();
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						if(s.isAnon()) return false;
						return s.getNameSpace().equals(RDF.getURI());
					}
				});
		if(iter != null){
			for(;iter.hasNext();){
				Statement st = iter.next();
				Set<Statement>  value = rdf.get(st.getSubject());
				if(value == null){
					value = new HashSet<Statement>();
					value.add(st);
					rdf.put(st.getSubject(), value);
				}else{
					value.add(st);
					rdf.put(st.getSubject(), value);
				}	
			}
		}
		return rdf;
	}

	public Map<Resource, Set<Statement>>  listRDFSEntitiesData(Model model) {
		Map<Resource, Set<Statement>> rdfs = new HashMap<Resource, Set<Statement>>();
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						if(s.isAnon()) return false;
						return s.getNameSpace().equals(RDFS.getURI());
					}
				});
		if(iter != null){
			for(;iter.hasNext();){
				Statement st = iter.next();
				Set<Statement>  value = rdfs.get(st.getSubject());
				if(value == null){
					value = new HashSet<Statement>();
					value.add(st);
					rdfs.put(st.getSubject(), value);
				}else{
					value.add(st);
					rdfs.put(st.getSubject(), value);
				}	
			}
		}
		return rdfs;
	}

	public Map<Resource, Set<Statement>> listXSDEntitiesData(Model model) {
		Map<Resource, Set<Statement>> xsd = new HashMap<Resource, Set<Statement>>();
		StmtIterator iter = model.listStatements(
				new SimpleSelector(null, null, (RDFNode) null){
					public boolean selects(Statement st){
						Resource s = st.getSubject();
						if(!s.isURIResource()) return false;
						return s.asResource().getNameSpace().equals(XSD.getURI());
					}
				});
		if(iter != null){
			for(;iter.hasNext();){
				Statement st = iter.next();
				Set<Statement>  value = xsd.get(st.getSubject());
				if(value == null){
					value = new HashSet<Statement>();
					value.add(st);
					xsd.put(st.getSubject(), value);
				}else{
					value.add(st);
					xsd.put(st.getSubject(), value);
				}	
			}
		}
		return xsd;
	}

}
