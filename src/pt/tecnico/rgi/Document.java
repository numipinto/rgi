package pt.tecnico.rgi;

import java.util.Map;

public class Document {

	private String className;
	private Map<String, Integer> features_tf;
	private Map<String, Integer> features_idf;
	
	
	public Document(String path){ // sci.med a class é sci
		className = path.split(".")[0];
		features_tf = new Map<String, Integer>();
		features_idf = new Map<String, Integer>();
	}
	
	// Adicionar Features com o TF do documento
	public void addNewFeature(String feature, Integer occurrency){
		features_tf.put(feature, occurrency);
	}
	//Aceder a uma feature em específico
	public Integer getFeatureTF(String feature){
		return features_tf.get(feature);
	}
	//Aceder a todas as TF das features
	public Map<String, Integer> getFeaturesTF(){
		return features_tf;
	}
	//Acrescentar IDF de uma Feature especifica
	public void addIDFFeature(String feature, Integer occurrency){
		features_idf.put(feature, occurrency);
	}
	//Aceder ao IDF de uma feature
	public Integer getFeatureIDF(String feature){
		return features_idf.get(feature);
	}
	//Aceder a todos os IDF's de todas as features
	public Map<String, Integer> getFeaturesIDF(){
		return features_idf;
	}
}
