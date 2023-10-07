package com.frameworkontology.sparql.repo;

import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRepoFactory {

	public SailRepository createRepo() {
		return new SailRepository(new SchemaCachingRDFSInferencer(new MemoryStore()));
	}
}
