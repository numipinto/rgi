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
    private List<Document> documents;

    public Doc2Vector() {
        this.features = new HashMap();
        this.ignoreSet = new HashMap();
        this.documents = new ArrayList();
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

    private void storeDoc(Document doc) {
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
        final Document doc = new Document(classe);
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
                        doc.addFeature(feature);
                    }
                }
                else System.out.println("ERROR parsing word: " + word);
            });
        }
        br.close();
        doc.calculateTF();
        storeDoc(doc);
    }

    private void outputTF(String fileName) {
        Path path = Paths.get(fileName);
        try {
            BufferedWriter bw = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);

            documents.forEach(doc -> {
                try {
                    bw.append(String.format("%d ", doc.getClassType()));
                    doc.getFeaturesTF().forEach((k,v)->{
                        try {
                            bw.append(String.format("%d:%f ", k, v));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputIDF(String fileName) {
        Path path = Paths.get(fileName);
        try {
            BufferedWriter bw = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);

            documents.forEach(doc -> {
                try {
                    bw.append(String.format("%d ", doc.getClassType()));
                    doc.getFeaturesIDF().forEach((k,v)->{
                        try {
                            bw.append(String.format("%d:%f ", k, v));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void outputTFIDF(String fileName) {
        Path path = Paths.get(fileName);
        try {
            BufferedWriter bw = Files.newBufferedWriter(path,
                    StandardOpenOption.CREATE_NEW,
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE);

            documents.forEach(doc -> {
                try {
                    bw.append(String.format("%d ", doc.getClassType()));
                    doc.getFeaturesTFIDF().forEach((k,v)->{
                        try {
                            bw.append(String.format("%d:%f ", k, v));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                    bw.append("\n");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // java Doc2Vector -Dinput TF|IDF|TF-IDF D:\\ignore.set D:\\feature.set D:\\docs\trainclasses.set
    public static void main(String[] args) {
        Doc2Vector doc2Vector = new Doc2Vector();
        String statistic = null, ignorePath = null, featurePath = null, docPath = null, output = null;

        try {
            for(int i=0; i < args.length; i+=2) {

                switch(args[i]) {
                    case "-s":
                        statistic = args[i+1];
                        break;
                    case "-i":
                        ignorePath = args[i+1];
                        break;
                    case "-f":
                        featurePath = args[i+1];
                        break;
                    case "-d":
                        docPath = args[i+1];
                        break;
                    case "-o":
                        output = args[i+1];
                        break;
                    case "-h":
                        System.out.println("This help text!");
                        return;

                }
            }

            if(ignorePath != null)
                doc2Vector.readIgnoredWords(ignorePath);
            if(featurePath!=null)
                doc2Vector.readFeatureList(featurePath);

            if(docPath != null) {
                //Parse Documents
                Map<Integer, String> paths = doc2Vector.readClassDirectory(docPath);
                for (Map.Entry<Integer, String> entry : paths.entrySet()){
                	
                    Path path = Paths.get(entry.getValue());
                    File dir = new File(entry.getValue());// + entry.getValue() );
                    File[] files = dir.listFiles();
                    List<File> list = Arrays.asList(files);
                    list.forEach(file -> {
                        try {
                            doc2Vector.parseDoc(file.toPath(), entry.getKey());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                };

                switch (statistic) {
                    case "TF":
                        //Output TF -> já calculado
                        doc2Vector.outputTF(output);
                        break;
                    case "IDF":
                    	for (Document doc : doc2Vector.documents) {
                            doc.calculateIDF(doc2Vector.documents);
                        };
                        //Output
                        doc2Vector.outputIDF(output);
                        break;
                    case "TFIDF":
                    	for (Document doc : doc2Vector.documents) {
                            doc.calculateIDF(doc2Vector.documents);
                        };
                        for (Document doc : doc2Vector.documents) {
                            doc.calculateTFIDF();
                        };
                        //Output
                        doc2Vector.outputTFIDF(output);
                        break;
                }
            }
            else {
                System.out.println("ERROR: NO DOCS FILE.");
                return;
            }

            doc2Vector.writeFeatureList(featurePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
    }
}

