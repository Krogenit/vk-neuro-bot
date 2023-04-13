package org.deeplearning4j.examples.recurrent.encdec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class CorpusProcessor {
    public static final String SPECIALS = "!\"#$;%^:?*()[]{}<>«»,.–—-=+…/\\&~_0123456789";
    private Set<String> dictSet = new HashSet<>();
    private Map<String, Double> freq = new HashMap<>();
    private Map<String, Double> dict = new HashMap<>();
    private boolean countFreq;
    //private InputStream is;
    private int rowSize;
    public String outputString = "";
    //private BufferedReader br;
    String fileName;
    List<String> lines = new ArrayList<String>();
    List<String> fileLines = new ArrayList<String>();

    public CorpusProcessor(String filename, int rowSize, boolean countFreq) {
        this.rowSize = rowSize;
        this.countFreq = countFreq;
        this.fileName = filename;
    }
    
    String lastName = "";
    String lastLine = "";
    
	public void add(String speakerName, String text) {
		if (speakerName.equals(lastName)) {
			if (!lastLine.isEmpty()) {
				if (!SPECIALS.contains(lastLine.substring(lastLine.length() - 1))) {
					lastLine += ",";
				}
				lastLine += " " + text;
			} else {
				lastLine = text;
			}
		} else {
			if (lastLine.isEmpty()) {
				lastLine = text;
			} else {
				lines.add(lastLine);
				processLine(lastLine);
				lastLine = text;
				//lineAdded = true;
			}
			lastName = speakerName;
		}
	}
	
	public void saveLines() {
		try {
			FileWriter writer = new FileWriter(new File("messages", "saved_lines.txt"));
			for(String line : lines) {
				writer.write(line + "\n");
			}
			
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void loadLines() {
		lines.clear();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("messages", "saved_lines.txt")), StandardCharsets.UTF_8));
			String line;
			while ((line = br.readLine()) != null) {
				lines.add(line);
				processLine(line);
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
    public void createDic() {
    	fileLines.clear();
    	String line;
        String lastName = "";
        String lastLine = "";
        try {
        	int lineNumber = 1;
        	BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), StandardCharsets.UTF_8));
			while ((line = br.readLine()) != null) {
				String lowerCaseString = line.toLowerCase();
				int firstBecketIndex = lowerCaseString.indexOf(']');
				String date = lowerCaseString.substring(1, firstBecketIndex);
				String stringAfterDate = lowerCaseString.substring(firstBecketIndex+1);
				firstBecketIndex = stringAfterDate.indexOf(']');
				String id = stringAfterDate.substring(1, firstBecketIndex);
				String stringAfterId = stringAfterDate.substring(firstBecketIndex+1);
				int pointsIndex = stringAfterId.indexOf(':');
				String speakerName = stringAfterId.substring(0, pointsIndex);
				
				String text = stringAfterId.substring(pointsIndex+2);
			    if (!text.contains("http")) 
			    {
			        if (speakerName.equals(lastName)) {
			            if (!lastLine.isEmpty()) {
			                if (!SPECIALS.contains(lastLine.substring(lastLine.length() - 1))) {
			                    lastLine += ",";
			                }
			                lastLine += " " + text;
			            } else {
			                lastLine = text;
			            }
			        } else {
			            if (lastLine.isEmpty()) {
			                lastLine = text;
			            } else {
			            	fileLines.add(lastLine);
			                processLine(lastLine);
			                lastLine = text;
			            }
			            lastName = speakerName;
			        }
			    }
			    lineNumber++;
			}
			br.close();
	        processLine(lastLine);
	        fileLines.add(lastLine);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public String getOutput(String input) {
    	return "";
    }

    protected void processLine(String lastLine) {
        tokenizeLine(lastLine, dictSet, false);
    }
    
    private void processLineForDataSet(String lastLine, List<List<Double>> corpus) {
        List<String> words = new ArrayList<>();
        tokenizeLine(lastLine, words, true);
        corpus.add(wordsToIndexes(words));
    }

    // here we not only split the words but also store punctuation marks
    protected void tokenizeLine(String lastLine, Collection<String> resultCollection, boolean addSpecials) {
        String[] words = lastLine.split("[ \t]");
        for (String word : words) {
            if (!word.isEmpty()) {
                boolean specialFound = true;
                while (specialFound && !word.isEmpty()) {
                    for (int i = 0; i < word.length(); ++i) {
                        int idx = SPECIALS.indexOf(word.charAt(i));
                        specialFound = false;
                        if (idx >= 0) {
                            String word1 = word.substring(0, i);
                            if (!word1.isEmpty()) {
                                addWord(resultCollection, word1);
                            }
                            if (addSpecials) {
                                addWord(resultCollection, String.valueOf(word.charAt(i)));
                            }
                            word = word.substring(i + 1);
                            specialFound = true;
                            break;
                        }
                    }
                }
                if (!word.isEmpty()) {
                    addWord(resultCollection, word);
                }
            }
        }
    }

    private void addWord(Collection<String> coll, String word) {
        if (coll != null) {
            coll.add(word);
        }
        if (countFreq) {
            Double count = freq.get(word);
            if (count == null) {
                freq.put(word, 1.0);
            } else {
                freq.put(word, count + 1);
            }
        }
    }

    public Set<String> getDictSet() {
        return dictSet;
    }

    public Map<String, Double> getFreq() {
        return freq;
    }

    public void setDict(Map<String, Double> dict) {
        this.dict = dict;
    }

    /**
     * Converts an iterable sequence of words to a list of indices. This will
     * never return {@code null} but may return an empty {@link java.util.List}.
     *
     * @param words
     *            sequence of words
     * @return list of indices.
     */
    protected final List<Double> wordsToIndexes(final Iterable<String> words) {
        int i = rowSize;
        final List<Double> wordIdxs = new LinkedList<>();
        for (final String word : words) {
            if (--i == 0) {
                break;
            }
            final Double wordIdx = dict.get(word);
            if (wordIdx != null) {
                wordIdxs.add(wordIdx);
            } else {
                wordIdxs.add(0.0);
            }
        }
        
        return wordIdxs;
    }

	public void createDataSet(List<List<Double>> corpus) {
		for(String line : lines) {
			processLineForDataSet(line, corpus);
		}
		
		for(String line : fileLines) {
			processLineForDataSet(line, corpus);
		}
	}

}
