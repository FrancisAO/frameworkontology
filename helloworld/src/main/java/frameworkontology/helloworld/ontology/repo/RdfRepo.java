package frameworkontology.helloworld.ontology.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.eclipse.rdf4j.sail.inferencer.fc.SchemaCachingRDFSInferencer;

@Component
public class RdfRepo {

	@Autowired
	private ResourceLoader resourceLoader;
	private Repository repo;

	public RdfRepo() {
		Resource resource = resourceLoader.getResource("classpath:filename.txt");
		try {
			File file = resource.getFile();
			repo = new SailRepository(
					new SchemaCachingRDFSInferencer(
							new MemoryStore(file)));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void getMetadata(String concept) {
		String query = "SELECT ?name ?label where {?x rdf:about}";
	}
	
}
