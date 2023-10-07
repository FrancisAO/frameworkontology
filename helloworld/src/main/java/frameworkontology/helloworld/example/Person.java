package frameworkontology.helloworld.example;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.sym.Name;

import frameworkontology.helloworld.ontology.concept.Concept;

@Component
@Concept("HelloWorld")
public class Person {

	private String motto;
	
	public void setMotto(String motto) {
		this.motto = motto;
	}
	
	public String getMotto() {
		return motto;
	}
	
}
