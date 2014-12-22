package pt.tecnico.rgi;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Document {

	private Integer classType;
    private Map<Integer, Integer> features;
	private Map<Integer, Double> features_tf;
	private Map<Integer, Double> features_idf;
    private Map<Integer, Double> features_tfidf;
	
	
	public Document(Integer classType){ // sci.med a class é sci
		this.classType = classType;
        features = new HashMap();
		features_tf = new HashMap();
		features_idf = new HashMap();
        features_tfidf = new HashMap();
	}
	
	// Adicionar Features com o TF do documento
	public void addFeature(Integer feature){
	    if(features.containsKey(feature))
            features.put(feature, features.get(feature) + 1);
        else features.put(feature, 1);
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
	public void addIDFFeature(Integer feature, Double occurrency){
		features_idf.put(feature, occurrency);
	}
	//Aceder ao IDF de uma feature
	public Double getFeatureIDF(Integer feature){
		return features_idf.get(feature);
	}
	//Aceder a todos os IDF's de todas as features
	public Map<Integer, Double> getFeaturesIDF(){
		return features_idf;
	}

    public Double getFeatureTFIDF(Integer feature){
        return features_tfidf.get(feature);
    }
    //Aceder a todos os IDF's de todas as features
    public Map<Integer, Double> getFeaturesTFIDF(){
        return features_tfidf;
    }

    public Integer getClassType() {
        return classType;
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

    public void calculateIDF(List<Document> docs) {
        features.forEach((k,v)->{
            Double idf = null;
            int count = 0;
            for(Document doc : docs) {
                if ((idf = doc.getFeatureIDF(k)) != null)
                    break;
                else if(doc.getFeatureTF(k) != 0)
                    count++;
            }
            if(idf != null) {
                addIDFFeature(k, idf);
            }
            else {
                addIDFFeature(k, Math.log(docs.size()/count));
            }
        });
    }

    public void calculateTFIDF() {
        features_tf.forEach((k,v) -> {
            features_tfidf.put(k, v * getFeatureIDF(k));
        });
    }
}
