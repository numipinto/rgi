package pt.tecnico.rgi;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Doc2Vector {

    private static String FEATURE_FILE = "feature.list";

    private Map<String, Integer> features;
    private Map<String, Integer> ignoreSet;
    private List<Map<Integer, Integer>> documents;

    public Doc2Vector() {
        this.features = new HashMap<>();
        this.ignoreSet = new HashMap<>();
        this.documents = new ArrayList<>();
    }

    private void readFeatureList(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        BufferedReader br = Files.newBufferedReader(path);
        // animal 5
        String line;
        while ((line = br.readLine()) != null) {
            String[] feature = line.split(" ");
            features.put(feature[0], Integer.parseInt(feature[1]));
        }

        br.close();
    }

    private void writeFeatureList(String fileName) throws IOException {
        Path new_path = Paths.get(fileName + ".tmp");
        Path old_path = Paths.get(fileName);
        BufferedWriter bw = Files.newBufferedWriter(new_path,
                StandardOpenOption.CREATE_NEW,
                StandardOpenOption.APPEND,
                StandardOpenOption.WRITE);

        Set<Map.Entry<String, Integer>> pairs = features.entrySet();
        pairs.forEach(entry -> {
            try {
                // animal 5
                bw.append(String.format("%s %d\n", entry.getKey(), entry.getValue()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        bw.flush();
        bw.close();

        Files.copy(new_path, old_path, StandardCopyOption.REPLACE_EXISTING);
        new_path.toFile().delete();
    }

    private void readIgnoredWords(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        BufferedReader br = Files.newBufferedReader(path);
        // animal 5
        String line;

        while ((line = br.readLine()) != null) {
            String str = line.replaceAll("\n", "");
            if(str.compareTo("") != 0) {
                ignoreSet.put(str, 1);
                System.out.println(str);
            }
        }
        br.close();
    }

    private boolean isIgnore(String feature) {
        return ignoreSet.containsKey(feature);
    }

    private int getFeature(String word) {
        int feature;
        if (features.containsKey(word))
            feature = features.get(word);
        else {
            feature = features.keySet().size() + 1;
            features.put(word, feature);
        }
        return feature;
    }

    /*
    * Add a feature to a document
    * */
    private void addDocFeature(Map<Integer, Integer> docTerms, int feature) {
        if (docTerms.containsKey(feature)) {
            docTerms.put(feature, docTerms.get(feature) + 1);
        } else docTerms.put(feature, 1);
    }

    private void storeDoc(Map<Integer, Integer> doc) {
        documents.add(doc);
    }
    
    private int docWTerm;
    private int docTotal;
    
    private int tf(String term, BufferedReader fileReader) throws IOException {
    	String line;
    	int retValue = 0;    	
    	while ((line = fileReader.readLine()) != null) {
    		String[] words = line.split(" ");
            List<String> wordsList = Arrays.asList(words);
            for (String word : wordsList) {
                word.replaceAll("\n\t\r.,:;?!<>«»()/#$%&=\"´`", "");
                if(word.matches(term)){
                    retValue = 1;
                }
            }
        }
    	return retValue;
    }
    
    private double idf(String term,Path documents) throws IOException {
    	docWTerm = 0;
    	docTotal = 0;
    	Files.walk(Paths.get(documents.toString())).forEach(filePath -> {
    		docTotal+=1;
    		if (Files.isRegularFile(filePath)) {
    	    	BufferedReader br;
				try {
					
					br = Files.newBufferedReader(filePath);
					docWTerm += tf(term,br); //vamos ver o nmr de docs onde ocorre o termo
    	          
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	    }
    	});
    	return Math.log(docTotal/docWTerm);
    }
    
    private double tf_idf(String term,Path doc, Path documents) throws IOException {
    	BufferedReader br  = Files.newBufferedReader(doc);
    	int termFrequency = tf(term,br);
    	double inverseDocumentFrequency = idf(term,documents);
    	return  termFrequency * inverseDocumentFrequency ;
    }

    private Map<Integer, String> readClassDirectory(String fileName) throws IOException {
        Path path = Paths.get(fileName);
        BufferedReader br = Files.newBufferedReader(path);
        Map<Integer, String> classPath = new HashMap();
        String line;

        while((line = br.readLine()) != null) {
            String[] splitLine = line.split(" ");
            classPath.put(Integer.parseInt(splitLine[0]), splitLine[1]);
        }

        br.close();
        return classPath;
    }
    
    private void parseDoc(Path file, int classe) throws IOException {
        BufferedReader br = Files.newBufferedReader(file);
        final Map<Integer, Integer> docTerms = new HashMap<Integer, Integer>();
        // animal 5
        String line;
        while ((line = br.readLine()) != null) {
            String[] words = line.split(" ");
            List<String> wordsList = Arrays.asList(words);
            wordsList.forEach(word -> {
                int feature;
                word.replaceAll("\n\t\r.,:;?!<>«»()/#$%&=\"´`", "");
                if(word.matches("[a-zA-Z]+-?[a-zA-Z]+")){
                    if(!isIgnore(word)) {
                        feature = getFeature(word);
                        addDocFeature(docTerms, feature);
                    }
                }
                else System.out.println("ERROR parsing word: " + word);
            });
        }
        br.close();
        storeDoc(docTerms);
    }


    // java Doc2Vector -Dinput TF|IDF|TF-IDF D:\\ignore.set D:\\feature.set D:\\docs\trainclasses.set
    public static void main(String[] args) {
        Doc2Vector doc2Vector = new Doc2Vector();

        try {
            doc2Vector.readIgnoredWords(args[0]);
            doc2Vector.readFeatureList(args[1]);

            //Parse Documents
            Map<Integer, String> paths = doc2Vector.readClassDirectory(args[2]);
            paths.forEach((k,v) -> {
                Path path = Paths.get(v);
                File[] files = path.toFile().listFiles();
                List<File> list = Arrays.asList(files);
                list.forEach(file -> {
                    try {
                        doc2Vector.parseDoc(file.toPath(), k);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            });

            doc2Vector.writeFeatureList(args[1]);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

