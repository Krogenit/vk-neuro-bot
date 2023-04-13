package org.deeplearning4j.examples.recurrent.encdec;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.ArrayUtils;
import org.deeplearning4j.nn.api.Layer;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.BackpropType;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration.GraphBuilder;
import org.deeplearning4j.nn.conf.GradientNormalization;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.Updater;
import org.deeplearning4j.nn.conf.graph.MergeVertex;
import org.deeplearning4j.nn.conf.graph.rnn.DuplicateToTimeSeriesVertex;
import org.deeplearning4j.nn.conf.graph.rnn.LastTimeStepVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.EmbeddingLayer;
import org.deeplearning4j.nn.conf.layers.GravesLSTM;
import org.deeplearning4j.nn.conf.layers.RnnOutputLayer;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.graph.vertex.GraphVertex;
import org.deeplearning4j.nn.transferlearning.FineTuneConfiguration;
import org.deeplearning4j.nn.transferlearning.TransferLearning;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.deeplearning4j.util.ModelSerializer;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.lossfunctions.LossFunctions;

/**
 * <p>
 * This is a seq2seq encoder-decoder LSTM model made according to Google's paper
 * <a href="https://arxiv.org/abs/1506.05869">A Neural Conversational Model</a>.
 * </p>
 * <p>
 * The model tries to predict the next dialog line using the provided one. It
 * learns on the <a href=
 * "https://www.cs.cornell.edu/~cristian/Cornell_Movie-Dialogs_Corpus.html">Cornell
 * Movie Dialogs corpus</a>. Unlike simple char RNNs this model is more
 * sophisticated and theoretically, given enough time and data, can deduce facts
 * from raw text. Your mileage may vary. This particular network architecture is
 * based on AdditionRNN but changed to be used with a huge amount of possible
 * tokens (10-40k) instead of just digits.
 * </p>
 * <p>
 * Use the get_data.sh script to download, extract and optimize the train data.
 * It's been only tested on Linux, it could work on OS X or even on Windows 10
 * in the Ubuntu shell.
 * </p>
 * <p>
 * Special tokens used:
 * </p>
 * <ul>
 * <li><code>&lt;unk&gt;</code> - replaces any word or other token that's not in
 * the dictionary (too rare to be included or completely unknown)</li>
 * <li><code>&lt;eos&gt;</code> - end of sentence, used only in the output to
 * stop the processing; the model input and output length is limited by the
 * ROW_SIZE constant.</li>
 * <li><code>&lt;go&gt;</code> - used only in the decoder input as the first
 * token before the model produced anything
 * </ul>
 * <p>
 * The architecture is like this:
 *
 * <pre>
 * Input =&gt; Embedding Layer =&gt; Encoder =&gt; Decoder =&gt; Output (softmax)
 * </pre>
 * <p>
 * The encoder layer produces a so called "thought vector" that contains a
 * neurally-compressed representation of the input. Depending on that vector the
 * model produces different sentences even if they start with the same token.
 * There's one more input, connected directly to the decoder layer, it's used to
 * provide the previous token of the output. For the very first output token we
 * send a special <code>&gt;go&lt;</code> token there, on the next iteration we
 * use the token that the model produced the last time. On the training stage
 * everything is simple, we apriori know the desired output so the decoder input
 * would be the same token set prepended with the <code>&gt;go&lt;</code> token
 * and without the last <code>&lt;eos&gt;</code> token. Example:
 * </p>
 * <p>
 * Input: "how" "do" "you" "do" "?"<br>
 * Output: "I'm" "fine" "," "thanks" "!" "<code>&lt;eos&gt;</code>"<br>
 * Decoder: "<code>&lt;go&gt;</code>" "I'm" "fine" "," "thanks" "!"
 * </p>
 * <p>
 * Actually, the input is reversed as per <a href=
 * "https://papers.nips.cc/paper/5346-sequence-to-sequence-learning-with-neural-networks.pdf">Sequence
 * to Sequence Learning with Neural Networks</a>, the most important words are
 * usually in the beginning of the phrase and they would get more weight if
 * supplied last (the model "forgets" tokens that were supplied "long ago", i.e.
 * they have lesser weight than the recent ones). The output and decoder input
 * sequence lengths are always equal. The input and output could be of any
 * length (less than {@link #ROW_SIZE}) so for purpose of batching we mask the
 * unused part of the row. The encoder and decoder layers work sequentially.
 * First the encoder creates the thought vector, that is the last activations of
 * the layer. Those activations are then duplicated for as many time steps as
 * there are elements in the output so that every output element can have its
 * own copy of the thought vector. Then the decoder starts working. It receives
 * two inputs, the thought vector made by the encoder and the token that it
 * _should have produced_ (but usually it outputs something else so we have our
 * loss metric and can compute gradients for the backward pass) on the previous
 * step (or <code>&lt;go&gt;</code> for the very first step). These two vectors are simply
 * concatenated by the merge vertex. The decoder's output goes to the softmax
 * layer and that's it.
 * </p>
 * <p>
 * The test phase is much more tricky. We don't know the decoder input because
 * we don't know the output yet (unlike in the train phase), it could be
 * anything. So we can't use methods like outputSingle() and have to do some
 * manual work. Actually, we can but it would require full restarts of the
 * entire process, it's super slow and ineffective.
 * </p>
 * <p>
 * First, we do a single feed forward pass for the input with a single decoder
 * element, <code>&lt;go&gt;</code>. We don't need the actual activations except
 * the "thought vector". It resides in the second merge vertex input (named
 * "dup"). So we get it and store for the entire response generation time. Then
 * we put the decoder input (<code>&lt;go&gt;</code> for the first iteration) and the thought
 * vector to the merge vertex inputs and feed it forward. The result goes to the
 * decoder layer, now with rnnTimeStep() method so that the internal layer state
 * is updated for the next iteration. The result is fed to the output softmax
 * layer and then we sample it randomly (not with argMax(), it tends to give a
 * lot of same tokens in a row). The resulting token is looked up in the
 * dictionary, printed to the {@link System#out} and then it goes to the next
 * iteration as the decoder input and so on until we get
 * <code>&lt;eos&gt;</code>.
 * </p>
 * <p>
 * To continue the training process from a specific batch number, enter it when
 * prompted; batch numbers are printed after each processed macrobatch. If
 * you've changed the minibatch size after the last launch, recalculate the
 * number accordingly, i.e. if you doubled the minibatch size, specify half of
 * the value and so on.
 * </p>
 */
public class EncoderDecoderLSTM {

    /**
     * Dictionary that maps words into numbers.
     */
    private final Map<String, Double> dict = new HashMap<>();

    /**
     * Reverse map of {@link #dict}.
     */
    private final Map<Double, String> revDict = new HashMap<>();

    private final String CHARS = CorpusProcessor.SPECIALS;

    /**
     * The contents of the corpus. This is a list of sentences (each word of the
     * sentence is denoted by a {@link java.lang.Double}).
     */
    private final List<List<Double>> corpus = new ArrayList<>();

    private static final int HIDDEN_LAYER_WIDTH = 512; // this is purely empirical, affects performance and VRAM requirement
    private static final int EMBEDDING_WIDTH = 128; // one-hot vectors will be embedded to more dense vectors with this width
    private static final String CORPUS_FILENAME = "neural/data/learning_data.txt"; // filename of data corpus to learn
    private static final String LAST_CORPUS_FILENAME = "learning_data.txt";
    private String modelFilename;
    private String lastModelFilename;
    private static final String BACKUP_MODEL_FILENAME = "neural/models/rnn_train.bak.zip"; // filename of the previous version of the model (backup)
    private static final int MINIBATCH_SIZE = 32;
    private static final Random rnd = new Random(new Date().getTime());
    private static final long SAVE_EACH_MS = TimeUnit.MINUTES.toMillis(15); // save the model with this period
    private static final long TEST_EACH_MS = TimeUnit.MINUTES.toMillis(1); // test the model with this period
    private static final int MAX_DICT = 50000; // this number of most frequent words will be used, unknown words (that are not in the
                                               // dictionary) are replaced with <unk> token
    private static final int TBPTT_SIZE = 25;
    private static final double LEARNING_RATE = 1e-1;
    private static final double RMS_DECAY = 0.95;
    private static final int ROW_SIZE = 64; // maximum line length in tokens

    /**
     * The delay between invocations of {@link java.lang.System#gc()} in
     * milliseconds. If VRAM is being exhausted, reduce this value. Increase
     * this value to yield better performance.
     */
    private static final int GC_WINDOW = 2000;

    private static final int MACROBATCH_SIZE = 1; // see CorpusIterator

    private FileWriter testWriter;
    
    /**
     * The computation graph model.
     */
    private ComputationGraph net;
    public Object look = new Object();
    
    private int epoh = 1;
    private int batch = 0;
    String neural_name;
    
    public boolean started = false;
    public boolean isRunning = true;
    private final File neuralDataFolder = new File("neural", "data");
    private final File neuralModelsFolder = new File("neural", "models");
    private final File neuralInfoFile = new File(neuralDataFolder, "neural_info.txt");

    public static void main(String[] args) throws IOException {
        new EncoderDecoderLSTM().runLearning();
    }
    
    public EncoderDecoderLSTM() {
		try {
            if(neuralInfoFile.exists()) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(neuralInfoFile)));
                String line = reader.readLine();
                epoh = Integer.parseInt(line);
                line = reader.readLine();
                batch = Integer.parseInt(line);
                neural_name = reader.readLine();
                reader.close();
            } else {
                neural_name = "first";
                saveNeuralInfo();
            }

            modelFilename = "neural/models/" +neural_name + "_epoh" + epoh + "_batch" + batch + ".zip";
            lastModelFilename = "neural/models/" +neural_name + "_epoh" + epoh + "_batch" + batch + ".zip";
            neuralDataFolder.mkdirs();
            neuralModelsFolder.mkdirs();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
    
    public void runDialogTest() throws IOException {
    	Nd4j.getMemoryManager().setAutoGcWindow(GC_WINDOW);

        createOrUpdateDictionary();

        File networkFile = new File(".", lastModelFilename);
        System.out.println("Loading the existing network...");
        net = ModelSerializer.restoreComputationGraph(networkFile);
        
        System.out.println("Dialog started.");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("In> ");
            
            // input line is appended to conform to the corpus format
            String line = scanner.nextLine();
//            String line = "1 +++$+++ u11 +++$+++ m0 +++$+++ WALTER +++$+++ " + scanner.nextLine() + "\n";
            CorpusProcessor dialogProcessor = new CorpusProcessor("", ROW_SIZE,
                    false) {
                @Override
                public String getOutput(String lastLine) {
                    List<String> words = new ArrayList<>();
                    tokenizeLine(lastLine, words, true);
                    final List<Double> wordIdxs = wordsToIndexes(words);
                    if (!wordIdxs.isEmpty()) {
                        System.out.print("Got words: ");
                        for (Double idx : wordIdxs) {
                            System.out.print(revDict.get(idx) + " ");
                        }
                        System.out.println();
                        String out = output(wordIdxs, true);
                        System.out.println("Out> " + out);
                        return out;
                    } return "";
                }
            };
            dialogProcessor.setDict(dict);
            dialogProcessor.getOutput(line);
        }
    }
    
    public void runDialog() throws IOException {
    	Nd4j.getMemoryManager().setAutoGcWindow(GC_WINDOW);

        createOrUpdateDictionary();

        File networkFile = new File(".", lastModelFilename);
        System.out.println("Loading the existing network...");
        net = ModelSerializer.restoreComputationGraph(networkFile);
        started = true;
        System.out.println("Dialog started.");
    }
    
    CorpusProcessor dialogProcessor;
    
    public String newMessage(String msg)
    {
        // input line is appended to conform to the corpus format
        String line = msg;
//        String line = "1 +++$+++ u11 +++$+++ m0 +++$+++ WALTER +++$+++ " + scanner.nextLine() + "\n";
        if(dialogProcessor == null) {
        	dialogProcessor = new CorpusProcessor("", ROW_SIZE, false) {
	            @Override
	            public String getOutput(String text) {
	                List<String> words = new ArrayList<>();
	                tokenizeLine(text, words, true);
	                final List<Double> wordIdxs = wordsToIndexes(words);
	                if (!wordIdxs.isEmpty()) {
	                    return output(wordIdxs, false);
	                } else {
	                	return "";
	                }
	            }
	        };
        	dialogProcessor.setDict(dict);
        }
        String output = dialogProcessor.getOutput(line);
        if(output.length() == 0) {
        	return this.newMessage(msg);
        }
        
        output = output.substring(0, output.length()-6);
        if(output.length() == 0) {
        	return this.newMessage(msg);
        }
        
        if(output.startsWith(", ")) 
        	output = output.substring(2, output.length());
        if(output.length() == 0) {
        	return this.newMessage(msg);
        }
        
        output = output.replace(" ,", ",");
        output = output.replace(" .", ".");
        output = output.replace(" !", "!");
        output = output.replace(" ?", "?");
        output = output.replace(" )", ")");
        output = output.replace(" (", "(");
        output = output.replace(" :", ":");
        output = output.replace(" ;", ";");
//        output = output.replace(" id", "id");
        output = output.replace(" ]", "]");
        output = output.replace(") 0", ")0");
        output = output.replace("0 )", "0)");
        output = output.replace(": d", ":D");
        output = output.replace("; d", ";D");
        output = output.replace(": 3", ":3");
//        output = output.replace("\" ", "\"");
        output = output.replace(" \"", "\"");
        
        String[] words = output.split(" ");
        output = "";
        for(int i=0;i<words.length;i++) {
        	String word = words[i];
        	if(word.startsWith("id")) {
        		word = word.replace("id", "[id");
            	if(!word.endsWith("]")) {
            		word += "]";
            	}
        	}
        	
        	output += word;
        	if(i < words.length - 1) {
        		output += " ";
        	}
        }
        
//        output = output.replace("id396564139|саси", " [id396564139|саси]");
//        output = output.replace("id396564139|дибил", " [id396564139|дибил]");
//        output = output.replace("id66311705|илья", " [id66311705|илья]");
//        output = output.replace("id396564139|immortal", " [id396564139|immortal]");
        if(output.length() == 0) {
        	return this.newMessage(msg);
        }
        return output;
    }

    public void runLearning() throws IOException {
    	Nd4j.getMemoryManager().setAutoGcWindow(GC_WINDOW);

        createOrUpdateDictionary();
//        testWriter = new FileWriter(new File("messages", "learning.txt"), true);
        File networkFile = new File(".", modelFilename);
        int offset = 0;
        if (networkFile.exists()) {
            System.out.println("Loading the existing network...");
            net = ModelSerializer.restoreComputationGraph(networkFile);
//            System.out.print("Enter d to start dialog or a number to continue training from that minibatch: ");
//            String input;
//            try (Scanner scanner = new Scanner(System.in)) {
//                input = scanner.nextLine();
//                offset = Integer.valueOf(input);
//                
//            }
            offset = batch;
            test();
        } else {
            System.out.println("Creating a new network...");
            createComputationGraph();
        }
        System.out.println("Number of parameters: " + net.numParams());
        net.setListeners(new ScoreIterationListener(1));
        train(networkFile, offset);
    }

    public void modifyComputationGraph() {
    	FineTuneConfiguration fineTuneConf = new FineTuneConfiguration.Builder()
                .learningRate(5e-5)
                .updater(Updater.NESTEROVS)
                .seed(12345)
                .build();
    	
    	ComputationGraph newNet = new TransferLearning.GraphBuilder(net)
    			.fineTuneConfiguration(fineTuneConf)
    			.setInputTypes(InputType.recurrent(dict.size()), InputType.recurrent(dict.size()))
    			.removeVertexKeepConnections("embeddingEncoder")
    			.removeVertexKeepConnections("decoder")
    			.removeVertexKeepConnections("output")
    			.addLayer("embeddingEncoder",
                new EmbeddingLayer.Builder()
                    .nIn(dict.size())
                    .nOut(EMBEDDING_WIDTH)
                    .build(),
                "inputLine")
    			.addLayer("decoder",
                new GravesLSTM.Builder()
                    .nIn(dict.size() + HIDDEN_LAYER_WIDTH)
                    .nOut(HIDDEN_LAYER_WIDTH)
                    .activation(Activation.TANH)
                    .build(),
                "merge")
    			.addLayer("output",
                new RnnOutputLayer.Builder()
                    .nIn(HIDDEN_LAYER_WIDTH)
                    .nOut(dict.size())
                    .activation(Activation.SOFTMAX)
                    .lossFunction(LossFunctions.LossFunction.MCXENT)
                    .build(),
                "decoder")
    			.setOutputs("output")
    			.build();
    			
    	this.net = newNet;
    }
    
    /**
     * Configure and initialize the computation graph. This is done once in the
     * beginning to prepare the {@link #net} for training.
     */
    private void createComputationGraph() {
        final NeuralNetConfiguration.Builder builder = new NeuralNetConfiguration.Builder()
            .iterations(1)
            .learningRate(LEARNING_RATE)
            .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
            .miniBatch(true)
            .updater(Updater.RMSPROP)
            .weightInit(WeightInit.XAVIER)
            .gradientNormalization(GradientNormalization.RenormalizeL2PerLayer);

        final GraphBuilder graphBuilder = builder.graphBuilder()
            .pretrain(false)
            .backprop(true)
            .backpropType(BackpropType.Standard)
            .tBPTTBackwardLength(TBPTT_SIZE)
            .tBPTTForwardLength(TBPTT_SIZE)
            .addInputs("inputLine", "decoderInput")
            .setInputTypes(InputType.recurrent(dict.size()), InputType.recurrent(dict.size()))
            .addLayer("embeddingEncoder",
                new EmbeddingLayer.Builder()
                    .nIn(dict.size())
                    .nOut(EMBEDDING_WIDTH)
                    .build(),
                "inputLine")
            .addLayer("encoder",
                new GravesLSTM.Builder()
                    .nIn(EMBEDDING_WIDTH)
                    .nOut(HIDDEN_LAYER_WIDTH)
                    .activation(Activation.TANH)
                    .build(),
                "embeddingEncoder")
            .addVertex("thoughtVector", new LastTimeStepVertex("inputLine"), "encoder")
            .addVertex("dup", new DuplicateToTimeSeriesVertex("decoderInput"), "thoughtVector")
            .addVertex("merge", new MergeVertex(), "decoderInput", "dup")
            .addLayer("decoder",
                new GravesLSTM.Builder()
                    .nIn(dict.size() + HIDDEN_LAYER_WIDTH)
                    .nOut(HIDDEN_LAYER_WIDTH)
                    .activation(Activation.TANH)
                    .build(),
                "merge")
            .addLayer("output",
                new RnnOutputLayer.Builder()
                    .nIn(HIDDEN_LAYER_WIDTH)
                    .nOut(dict.size())
                    .activation(Activation.SOFTMAX)
                    .lossFunction(LossFunctions.LossFunction.MCXENT)
                    .build(),
                "decoder")
            .setOutputs("output");

        net = new ComputationGraph(graphBuilder.build());
        net.init();
    }
    
    CorpusIterator logsIterator;

    private void train(File networkFile, int offset) throws IOException {
        long lastSaveTime = System.currentTimeMillis();
        long lastTestTime = System.currentTimeMillis();
        logsIterator = new CorpusIterator(corpus, MINIBATCH_SIZE, MACROBATCH_SIZE, dict.size(), ROW_SIZE);
        logsIterator.setCurrentBatch(offset);
        while(corpus.size() < 25) {
        	try {
				System.out.println("Low corpus " + corpus.size());
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
        
        for (int epoch = epoh; epoch < 10000; ++epoch) {
        	if(!isRunning) break;
            System.out.println("Epoch " + epoch);
            int lastPerc = 0;
            while (logsIterator.hasNextMacrobatch()) {
            	synchronized (look) {
                    net.fit(logsIterator);
                    started = true;
            	}
                logsIterator.nextMacroBatch();
                System.out.println("Batch = " + logsIterator.batch());
                int newPerc = (logsIterator.batch() * 100 / logsIterator.totalBatches());
                if (newPerc != lastPerc) {
                    System.out.println("Epoch complete: " + newPerc + "%");
                    lastPerc = newPerc;
                }
//                if (System.currentTimeMillis() - lastSaveTime > SAVE_EACH_MS) {
//                    saveModel(new File("neural_saves", neural_name + "_epoh" + epoch + "_batch" + logsIterator.batch() +".zip"));
//                    lastSaveTime = System.currentTimeMillis();
//                }
//                if (System.currentTimeMillis() - lastTestTime > TEST_EACH_MS) {
//                    test();
//                    lastTestTime = System.currentTimeMillis();
//                }
                batch = logsIterator.batch();
            }
//            saveModel(networkFile, epoch);
//            FileWriter writer = new FileWriter(new File("neural_saves", "neuro_info.txt"));
//            writer.write("" + epoch + "\n");
//            writer.write("" + 0 + "\n");
//            writer.close();
            epoh++;
            logsIterator.reset();
        }
//		saveModel();
//		epoh++;
    }

    private void startDialog(Scanner scanner) throws IOException {
        System.out.println("Dialog started.");
        while (true) {
            System.out.print("In> ");
            // input line is appended to conform to the corpus format
            String line = scanner.nextLine();
//            String line = "1 +++$+++ u11 +++$+++ m0 +++$+++ WALTER +++$+++ " + scanner.nextLine() + "\n";
            CorpusProcessor dialogProcessor = new CorpusProcessor("", ROW_SIZE,
                    false) {
                @Override
                public String getOutput(String lastLine) {
                    List<String> words = new ArrayList<>();
                    tokenizeLine(lastLine, words, true);
                    final List<Double> wordIdxs = wordsToIndexes(words);
                    if (!wordIdxs.isEmpty()) {
                        System.out.print("Got words: ");
                        for (Double idx : wordIdxs) {
                            System.out.print(revDict.get(idx) + " ");
                        }
                        System.out.println();
                        System.out.print("Out> ");
                        return output(wordIdxs, true);
                    } return "";
                }
            };
            dialogProcessor.setDict(dict);
            dialogProcessor.getOutput(line);
        }
    }

    public void loadDictionaryFromFile() {
    	corpusProcessor.createDic();
    }
   
    public void saveModel() throws IOException {
        System.out.println("Saving the model...");
//        File backup = new File(".", BACKUP_MODEL_FILENAME + "epoch");
//        if (networkFile.exists()) {
//            if (backup.exists()) {
//                backup.delete();
//            }
//            networkFile.renameTo(backup);
//        }
        ModelSerializer.writeModel(net, new File(neuralModelsFolder, neural_name + "_epoh" + epoh + "_batch" + batch +".zip"), true);
        saveNeuralInfo();
        System.out.println("Done.");
    }

    public void saveNeuralInfo() throws IOException {
        FileWriter writer = new FileWriter(neuralInfoFile);
        writer.write("" + epoh + "\n");
        writer.write("" + batch + "\n");
        writer.write("" + neural_name + "\n");
        writer.close();
    }
    
//    private void saveModel(File networkFile) throws IOException {
//        System.out.println("Saving the model...");
//        File backup = new File(".", BACKUP_MODEL_FILENAME);
//        if (networkFile.exists()) {
//            if (backup.exists()) {
//                backup.delete();
//            }
//            networkFile.renameTo(backup);
//        }
//        ModelSerializer.writeModel(net, networkFile, true);
//        System.out.println("Done.");
//    }

    private void test() {
        System.out.println("======================== TEST ========================");
        if(corpus.size() > 0) {
            int selected = rnd.nextInt(corpus.size());
            List<Double> rowIn = new ArrayList<>(corpus.get(selected));
            System.out.print("In: ");
            String in = "";
            for (Double idx : rowIn) {
            	in += (revDict.get(idx) + " ");
            }
            System.out.print(in);
            System.out.println();
            try {
                String out = output(rowIn, false);
                System.out.println("Out: " + out);   
            } catch(Exception e) {
            	e.printStackTrace();
                System.out.println("Out: error");   
            }


        }
        System.out.println("====================== TEST END ======================");
//        try{
//        testWriter.write("In: " + in + "\n");
//        testWriter.write("Out: " + out + "\n");
//        testWriter.flush();
//        }catch(Exception e) {e.printStackTrace();}
    }

    private String output(List<Double> rowIn, boolean printUnknowns) {
    	synchronized (look) {
        	String outputStr = "";
            net.rnnClearPreviousState();
            Collections.reverse(rowIn);
            INDArray in = Nd4j.create(ArrayUtils.toPrimitive(rowIn.toArray(new Double[0])), new int[] { 1, 1, rowIn.size() });
            double[] decodeArr = new double[dict.size()];
            decodeArr[2] = 1;
            INDArray decode = Nd4j.create(decodeArr, new int[] { 1, dict.size(), 1 });
            net.feedForward(new INDArray[] { in, decode }, false);
            org.deeplearning4j.nn.layers.recurrent.GravesLSTM decoder = (org.deeplearning4j.nn.layers.recurrent.GravesLSTM) net
                    .getLayer("decoder");
            Layer output = net.getLayer("output");
            GraphVertex mergeVertex = net.getVertex("merge");
            INDArray thoughtVector = mergeVertex.getInputs()[1];
            for (int row = 0; row < ROW_SIZE; ++row) {
                mergeVertex.setInputs(decode, thoughtVector);
                INDArray merged = mergeVertex.doForward(false);
                INDArray activateDec = decoder.rnnTimeStep(merged);
                INDArray out = output.activate(activateDec, false);
                double d = rnd.nextDouble();
                double sum = 0.0;
                int idx = -1;
                for (int s = 0; s < out.size(1); s++) {
                    sum += out.getDouble(0, s, 0);
                    if (d <= sum) {
                        idx = s;
                        if (printUnknowns || s != 0) {
                        	outputStr += revDict.get((double) s) + " ";
                        }
                        break;
                    }
                }
                if (idx == 1) {
                    break;
                }
                double[] newDecodeArr = new double[dict.size()];
                newDecodeArr[idx] = 1;
                decode = Nd4j.create(newDecodeArr, new int[] { 1, dict.size(), 1 });
            }
            
            return outputStr;
    	}
    }

    public void addToDictionary(String speakerName, String text) {
    	corpusProcessor.add(speakerName, text);
    }
    
    public void saveDictionaryLines() {
    	corpusProcessor.saveLines();
    }
    
    CorpusProcessor corpusProcessor;
    public int dictionarySize = 0;
    public int corpusSize = 0;
    
    public void createOrUpdateDictionary() throws IOException, FileNotFoundException {
    	dict.clear();
    	revDict.clear();
    	corpus.clear();
    	
        double idx = 3.0;
        dict.put("<unk>", 0.0);
        revDict.put(0.0, "<unk>");
        dict.put("<eos>", 1.0);
        revDict.put(1.0, "<eos>");
        dict.put("<go>", 2.0);
        revDict.put(2.0, "<go>");
        for (char c : CHARS.toCharArray()) {
            if (!dict.containsKey(c)) {
                dict.put(String.valueOf(c), idx);
                revDict.put(idx, String.valueOf(c));
                ++idx;
            }
        }
        System.out.println("Building the dictionary...");
        
        if(corpusProcessor == null) {
            corpusProcessor = new CorpusProcessor(CORPUS_FILENAME, ROW_SIZE, true);
            corpusProcessor.createDic();
//            corpusProcessor.loadLines();
        }

        Map<String, Double> freqs = corpusProcessor.getFreq();
        Set<String> dictSet = new TreeSet<>(); // the tokens order is preserved for TreeSet
        Map<Double, Set<String>> freqMap = new TreeMap<>(new Comparator<Double>() {

            @Override
            public int compare(Double o1, Double o2) {
                return (int) (o2 - o1);
            }
        }); // tokens of the same frequency fall under the same key, the order is reversed so the most frequent tokens go first
        for (Entry<String, Double> entry : freqs.entrySet()) {
            Set<String> set = freqMap.get(entry.getValue());
            if (set == null) {
                set = new TreeSet<>(); // tokens of the same frequency would be sorted alphabetically
                freqMap.put(entry.getValue(), set);
            }
            set.add(entry.getKey());
        }
        int cnt = 0;
        dictSet.addAll(dict.keySet());
        File file = new File(neuralDataFolder, "testdict.txt");
        FileWriter writer = new FileWriter(file);
//        // get most frequent tokens and put them to dictSet
        for (Entry<Double, Set<String>> entry : freqMap.entrySet()) {
            for (String val : entry.getValue()) {
                if (dictSet.add(val))
                {
                    writer.write(entry.getKey() + " : " + val + "\n");
                	if(++cnt >= MAX_DICT) {
	            	   		break;
                	}
                }
            }
            if (cnt >= MAX_DICT) {
                break;
            }
        }
        writer.close();
        // all of the above means that the dictionary with the same MAX_DICT constraint and made from the same source file will always be
        // the same, the tokens always correspond to the same number so we don't need to save/restore the dictionary
        System.out.println("Dictionary is ready, size is " + dictSet.size());
        dictionarySize = dictSet.size();
        // index the dictionary and build the reverse dictionary for lookups
        File file1 = new File(neuralDataFolder, "testdict1.txt");
        writer = new FileWriter(file1);
        for (String word : dictSet) {
            if (!dict.containsKey(word)) {
                dict.put(word, idx);
                revDict.put(idx, word);
                writer.write(word + "\n");
                ++idx;
            }
        }
        writer.close();
        System.out.println("Total dictionary size is " + dict.size() + ". Processing the dataset...");
        corpusProcessor.setDict(dict);
        corpusProcessor.createDataSet(corpus);
        System.out.println("Done. Corpus size is " + corpus.size());
        corpusSize = corpus.size();
        if(logsIterator != null) logsIterator.setCorpusAndDictSize(corpus, dict.size());
    }

    private String toTempPath(String path) {
        return System.getProperty("java.io.tmpdir") + "/" + path;
    }

}