package pt.tecnico.rgi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class Document {

	private Integer classType;
    private Map<Integer, Integer> features;
	private Map<Integer, Double> features_tf;
	private Map<Integer, Integer> features_idf;
	
	
	public Document(Integer classType){ // sci.med a class é sci
		this.classType = classType;
		features_tf = new HashMap<>();
		features_idf = new HashMap<>();
	}
	
	// Adicionar Features com o TF do documento
	public void addNewFeature(Integer feature){
		features.put(feature, features.size() + 1);
	}
	//Aceder a uma feature em específico
	public Double getFeatureTF(Integer feature){
		return features_tf.get(feature);
	}
	//Aceder a todas as TF das features
	public Map<Integer, Double> getFeaturesTF(){
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

    public void calculateTF() {
        Iterator iter = features.values().iterator();
        int max = 0;
        while(iter.hasNext()) {
            int v = (Integer)iter.next();
            if(v > max)
                max = v;
        }

        final int final_max = max;
        features.forEach((k, v) -> {
            double tf = 0.5 + (0.5 * v) / final_max;
            features_tf.put(k, tf);
        });
    }
}
