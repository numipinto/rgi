package pt.tecnico.rgi;

import java.util.Map;

public class Document {

	private Integer classType;
	private Map<Integer, Integer> features_tf;
	private Map<Integer, Integer> features_idf;
	
	
	public Document(Integer classType){ // sci.med a class é sci
		this.classType = classType;
		features_tf = new Map<Integer, Integer>();
		features_idf = new Map<Integer, Integer>();
	}
	
	// Adicionar Features com o TF do documento
	public void addNewFeature(Integer feature, Integer occurrency){
		features_tf.put(feature, occurrency);
	}
	//Aceder a uma feature em específico
	public Integer getFeatureTF(Integer feature){
		return features_tf.get(feature);
	}
	//Aceder a todas as TF das features
	public Map<Integer, Integer> getFeaturesTF(){
		return features_tf;
	}
	//Acrescentar IDF de uma Feature especifica
	public void addIDFFeature(Integer feature, Integer occurrency){
		features_idf.put(feature, occurrency);
	}
	//Aceder ao IDF de uma feature
	public Integer getFeatureIDF(Integer feature){
		return features_idf.get(feature);
	}
	//Aceder a todos os IDF's de todas as features
	public Map<Integer, Integer> getFeaturesIDF(){
		return features_idf;
	}
}
