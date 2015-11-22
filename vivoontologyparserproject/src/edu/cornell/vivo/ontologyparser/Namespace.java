package edu.cornell.vivo.ontologyparser;

public enum Namespace {

	SW_STATUS("http://www.w3.org/2003/06/sw-vocab-status/ns#", "BIBO"), //BIBO
	BIBO("http://purl.org/ontology/bibo/", "BIBO"), // BIBO
	SKOS_05("http://www.w3.org/2008/05/skos#", "BIBO"),  // BIBO (#scopeNote, #editorialNote)
	
	C4O("http://purl.org/spar/c4o/", "C4O"), // C4O
	CITO("http://purl.org/spar/cito/", "CITO"), // CITO
	DCTERMS("http://purl.org/dc/terms/", "DC"), // DC
	
	EVENT("http://purl.org/NET/c4dm/event.owl#", "EVENT"), // EVENT
	C4DM ("http://purl.org/NET/c4dm/", "EVENT"),  // EVENT (TYPE ONTOLOGY)
	
	FABIO("http://purl.org/spar/fabio/", "FABIO"), // FABIO
	FOAF("http://xmlns.com/foaf/0.1/", "FOAF"), // FAOF
	
	GEO("http://aims.fao.org/aos/geopolitical.owl#", "GEO"), // GEO
	
	OBO("http://purl.obolibrary.org/obo/", "OBO"), // OBO
	OBO_IN_OWL("http://www.geneontology.org/formats/oboInOwl#", "OBO"), // OBO (4 subclasses of OBO:IAO_0000102)
	
	RO("http://www.obofoundry.org/ro/ro.owl#", "RO"),  // RO (has agent)
		
	OCRE_RSCH("http://purl.org/net/OCRe/research.owl#", "OCRE_RSCH"), // OCRE_RSCH
	OCRE_SD("http://purl.org/net/OCRe/study_design.owl#", "OCRE_SD"),  // OCRE_SD
	OCRE_STAT("http://purl.org/net/OCRe/statistics.owl#", "OCRE_STAT"), // OCRE_STAT
	OCRE_SP("http://purl.org/net/OCRe/study_protocol.owl#", "OCRE_SP"), // OCRE_SP
	
	SKOS_02_CORE("http://www.w3.org/2004/02/skos/core#", "SKOS"), // SKOS
	SKOS_02("http://www.w3.org/2004/02/skos/", "SKOS"),  // SKOS  (label "SKOS")
	
	SOFTWARE("http://www.ebi.ac.uk/efo/swo/", "SOFTWARE"), // SOFTWARE
	
	VCARD_NS("http://www.w3.org/2006/vcard/ns#", "VCARD"),  // VCARD
	VCARD("http://www.w3.org/2006/vcard/", "VCARD"),	// VCARD (label "VCARD")
	
	VIVO_CORE("http://vivoweb.org/ontology/core#", "VIVO"),  // VIVO
	VIVO("http://vivoweb.org/ontology/", "VIVO"), // VIVO
	SCIRES("http://vivoweb.org/ontology/scientific-research#", "VIVO") //VIVO
	;
	
//VANN("http://purl.org/vocab/vann/"), // NOT_OK (/preferredNamespaceUri)
//OCRE("http://purl.org/net/OCRe/"),  // (RESEARCH, STUDY_DESGIN ONTOLOGY)

//http://isf/   deprecated_op
//http://aims.fao.org/aos/   <http://aims.fao.org/aos/geopolitical.owl>
//http://vitro.mannlib.cornell.edu/ns/vitro/0.7 (label "Vitro internals")
//http://vitro.mannlib.cornell.edu/ns/vitro/0.7#   (#descriptionAnnot, #exampleAnnot, #moniker)
//http://vitro.mannlib.cornell.edu/ns/vitro/public#  (#File, #FileByStream)
//http://vitro.mannlib.cornell.edu/ns/vitro/  (/public "Vitro Public Ontology")
	
	private String namespace;
	private String ontologyName;

	private Namespace(String ns, String ontName) {
		namespace = ns;
		ontologyName = ontName;
	}

	public String getNamespace() {
		return namespace;
	}
	
	public String getOntologyName() {
		return ontologyName;
	}
	
}
