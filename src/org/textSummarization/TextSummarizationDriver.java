
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.RemoteIterator;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.input.SequenceFileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.LocatedFileStatus;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.CharSequenceLowercase;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.CsvIterator;
import cc.mallet.topics.ParallelTopicModel;
import cc.mallet.topics.TopicInferencer;
import cc.mallet.types.Alphabet;
import cc.mallet.types.FeatureSequence;
import cc.mallet.types.IDSorter;
import cc.mallet.types.Instance;
import cc.mallet.types.InstanceList;
import cc.mallet.types.LabelSequence;

public class TextSummarizationDriver extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(TextSummarizationDriver.class);

	/*
	 * The main method invokes the PageRankDriver ToolRunner, which creates and
	 * runs a new instance of PageRankDriver job.
	 */

	public static void main(String[] args) throws Exception {
		System.out.println("hereeeee");
		Configuration configuration = new Configuration();

		
		System.exit(ToolRunner.run(configuration, new TextSummarizationDriver(), args));
	}

	@Override
	public int run(String[] args) throws Exception {
		FileSystem fs = null;
		Configuration config = new Configuration();
		// TODO Auto-generated method stub
		try {
			System.out.println("*************PROCESS START****************");
			/*
			 * args[0] = "/home/gowtham/Desktop/DataSet"; //input Data set
			 * args[1] = "/home/gowtham/Desktop/processed"; //prepocessed file
			 * output args[2] = "/home/gowtham/Desktop/TermFrequency"; // Term
			 * Frequency output args[3] = "/home/gowtham/Desktop/TFIDF"; //
			 * TFIDF Output args[4] = "/home/gowtham/Desktop/InputVector";
			 * //input vector args[5] = "/home/gowtham/Desktop/KMeans"; //K
			 * Means output args[6] = "/home/gowtham/Desktop/TopicTerms"; //LDA
			 * Output args[7] = "/home/gowtham/Desktop/SimilarTerms"; //Semantic
			 * terms output args[8] = "/home/gowtham/Desktop/Summarization";
			 * //Summarization output
			 */ 
			fs = FileSystem.get(config);

			FileStatus[] status = fs.listStatus(new Path(args[0])); // input
																	// path
																	// containing
																	// file
																	// collection
			for (int i = 0; i < status.length; i++) {
				System.out.println("status" + status.length);
				BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status[i].getPath())));
				String lineRead;
				lineRead = br.readLine();
				String doc = "";

				boolean value = false;
				while (lineRead != null) {
					System.out.println("line" + lineRead);
					if (value) {
						doc += lineRead;

					}
					value = true;
					lineRead = br.readLine();
				}

				String[] linesArray = doc.split("\\.");

				Path pt2 = new Path(args[1] + "/file" + i + ".txt"); // output
																		// file
																		// path
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fs.create(pt2, true)));
				// TO append data to a file, use fs.append(Path f)

				for (String line : linesArray) {
					bw.write(line + ".");
					bw.newLine();
				}
				bw.close();
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("*************PROCESS ENDS****************");
		System.out.println("*************VECTOR CREATION STARTS****************");

		boolean isClustered = false;
		int res_termfrequency = ToolRunner.run(new TermFrequency(), args);
		if (res_termfrequency != 0) {
			return 1;
		}
		int res_tfidf = ToolRunner.run(new TFIDF(), args);
		if (res_tfidf != 0)
			return 1;

		System.out.println("*************VECTOR CREATION ENDS****************");
		System.out.println("************* K-MEANS STARTS****************");
		isClustered = runKMeans(args,fs, config);
		System.out.println("RESULT OF KMESAAAAAAAAAA"+isClustered);
		if (!isClustered)
			return 1;
		System.out.println("************* K-MEANS ENDS****************");

		System.out.println("************* LDA STARTS****************");

		// call LDA
		boolean performLDA = performLDA(args[5]);
		if (!performLDA) {
			return 1;
		}
		// Combine all topics from LDA Phase
		boolean combineTopics = combineTopics("/user/vpcl/project/output/topics", args[6]);
		if (!combineTopics) {
			return 1;
		}
		System.out.println("************* LDA ENDS ****************");

		System.out.println("************* SEMANTIC TERM STARTS****************");
		// push this directory to Hadoop
		System.setProperty("wordnet.database.dir", args[9]);
		boolean createdSemanticTerms = createSemanticTerms(args[6], "/user/vpcl/project/output/semanticTerms");
		if (!createdSemanticTerms) {
			return 1;
		}
		boolean isSorted = cleanAndSorting("/user/vpcl/project/output/semanticTerms", args[7]);
		if (!isSorted) {
			return 1;
		}
		System.out.println("************* SEMANTIC TERM ENDS****************");
		System.out.println("************* SUMMARIZATION STARTS****************");
		int res_summarizer = ToolRunner.run(new Summarizer(), args);
		if (res_summarizer != 0) {
			return 1;
		}
		System.out.println("************* SUMMARIZATION ENDS****************");
		return 0;
	}

	// Creating Link Graph
	private boolean createVectors(String input, String output)
			throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Job job = Job.getInstance(getConf(), "createVectors");
		job.setJarByClass(this.getClass());
		FileInputFormat.addInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, new Path(output));
		// job.setInputFormatClass(XmlInputFormat.class); //setting the input
		// format of the link graph generator as Xml format.
		// job.setMapperClass(createVectorMapper.class);
		job.setMapOutputKeyClass(Text.class);
		// job.setOutputFormatClass(SequenceFileOutputFormat.class); //setting
		// the output format of the link graph generator to Sequence file
		// format.
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		// job.setNumReduceTasks(3); //setting the number of reduce tasks to be
		// 2
		// job.setReducerClass(CreateVectorReducer.class);
		return job.waitForCompletion(true);
	}

	private boolean runKMeans(String[] args,FileSystem fs, Configuration conf) throws IOException, ClassNotFoundException, InterruptedException {
		int iteration = 1;
		//Configuration conf = getConf();
		conf.set("num.iteration", iteration + "");
		Path in = new Path(args[4]);
		Path center = new Path("/user/vpcl/project/output/Center/inputCenter.txt"); // change
																				// it
																				// to
																				// hadoop
																				// path
		conf.set("centroid.path", center.toString());
		Path out = new Path("/user/vpcl/project/output/Iterations/depth_1");
		Job job = Job.getInstance(conf);
		job.setJobName("KMeans Clustering");
		job.setMapperClass(KMeansMapper.class);
		job.setReducerClass(KMeansReducer.class);
		job.setJarByClass(KMeansMapper.class);
		FileInputFormat.addInputPath(job, in);
		//FileSystem fs = FileSystem.get(conf);
		System.out.println("****** sampathhh*********");
		System.out.println("****** WRITING sampath START*********");
		writeCenters(conf, center, fs, args);
		System.out.println("****** FINISHED WRITING CENTROIDS*********");
		FileOutputFormat.setOutputPath(job, out);
		job.setOutputKeyClass(ClusterCenter.class);
		job.setOutputValueClass(Text.class);
		job.waitForCompletion(true);
		long counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
		iteration++;
		int iter = 0;
		while (iter < 2) {
			conf = new Configuration();
			conf.set("centroid.path", center.toString());
			conf.set("num.iteration", iteration + "");
			job = Job.getInstance(conf);
			job.setJobName("KMeans Clustering " + iteration);

			job.setMapperClass(KMeansMapper.class);
			job.setReducerClass(KMeansReducer.class);
			job.setJarByClass(KMeansMapper.class);

			in = new Path("/user/vpcl/project/output/Iterations/depth_" + (iteration - 1) + "/");
			out = new Path("/user/vpcl/project/output/Iterations/depth_" + iteration);

			FileInputFormat.addInputPath(job, in);
			//if (fs.exists(out))
			//	fs.delete(out, true);

			FileOutputFormat.setOutputPath(job, out);
			// job.setInputFormatClass(TextInputFormat.class);
			// job.setOutputFormatClass(TextOutputFormat.class);
			job.setOutputKeyClass(ClusterCenter.class);
			job.setOutputValueClass(Text.class);

			job.waitForCompletion(true);
			iteration++;
			counter = job.getCounters().findCounter(KMeansReducer.Counter.CONVERGED).getValue();
			iter++;

		}

		Path result = new Path("/user/vpcl/project/output/Iterations/depth_" + (iteration - 1) + "/");

		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (!status.isDirectory()) {
				Path path = status.getPath();
				if (!path.getName().equals("_SUCCESS")) {

					conf = new Configuration();
					job = Job.getInstance(conf);
					job.setJobName("KMeans Clustering Final");
					job.setMapperClass(KMeansMapperFinal.class);
					job.setReducerClass(KMeansReducerFinal.class);
					job.setJarByClass(KMeansMapper.class);
					out = new Path("/user/vpcl/project/output/KMeansOutput"); // intermediate
					FileInputFormat.addInputPath(job, result);
					FileOutputFormat.setOutputPath(job, out);
					job.setOutputKeyClass(Text.class);
					job.setOutputValueClass(Text.class);
					if (job.waitForCompletion(true)) {
						try {
							Path kMeanFiles = new Path("/user/vpcl/project/output/KMeansOutput/part-r-00000");
							BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(kMeanFiles)));
							String line;
							line = br.readLine();
							int i = 0;
							while (line != null) {
								String[] files = line.split("\t");
								String[] fileNames = files[1].split(",");
		//						Path homeDir = fs.getHomeDirectory();
								// System.out.println("Home folder -" +homeDir);
		//						Path workingDir = fs.getWorkingDirectory();
								// System.out.println("working folder -"
								// +workingDir);
								String clus = "Cluster" + i;
								// File newFolderPath= new
								// File(args[5]+"/"+clus);
								Path newFolderPath = new Path("/user/vpcl/project/output/kmeans/" + clus);
			//					newFolderPath = Path.mergePaths(workingDir, newFolderPath);
							//	if (fs.exists(newFolderPath)) {
							//		fs.delete(newFolderPath, true); // Delete
																	// existing
																	// Directory
							//	}
								// newFolderPath.mkdir();
								fs.mkdirs(newFolderPath); // Create new
															// Directory
								for (String fileName : fileNames) {
									 File localFilePath = new File(args[1]+"/"+fileName);
									 File hdfsFilePath= new File(newFolderPath+"/"+fileName);
									System.out.println("*****FILENAMES"+fileName);
									//Path localFilePath = new Path(args[1]+"/" + fileName);
									//Path hdfsFilePath = new Path(newFolderPath+"/"+ fileName);
									System.out.println("local path**"+args[1]+"/"+fileName);
									System.out.println("hdfs path**"+newFolderPath+"/");
									FileUtil.copy(fs, new Path(args[1]+"/"+fileName), fs, new Path(newFolderPath+"/"), false, conf);
									 //FileUtils.copyFile(localFilePath,hdfsFilePath);
				
									//fs.copyFromLocalFile(localFilePath, hdfsFilePath);
								}
								line = br.readLine();
								i++;
							}
						} catch (Exception e) {
							System.out.println("exception********"+e);
						}
					}

				}
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void writeCenters(Configuration conf, Path center, FileSystem fs, String[] args) throws IOException {
		try (SequenceFile.Writer centerWriter = SequenceFile.createWriter(fs, conf, center, ClusterCenter.class,
				IntWritable.class)) {
			final IntWritable value = new IntWritable(0);
			System.out.println("Enter the number of clusters");
			Scanner sc = new Scanner(System.in);
			//BufferedReader console = new BufferedReader(new java.io.InputStreamReader(System.in));
			int noOfClusters = sc.nextInt();
			System.out.println("K"+noOfClusters);
			//System.out.println("Enter the path of the data file to select the clusters from");
			//String dataFilePath = console.readLine();
			String dataFilePath = args[4];
			Path dataPath = new Path(dataFilePath+"/part-r-00000");
            BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(dataPath)));
            String line;
            line=br.readLine();
            Pattern p = Pattern.compile("\\[(.*?)\\]");
            List<String> lines = new ArrayList<String>();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
           } 
			for(int i=0;i<noOfClusters;i++){
				Random r = new Random();
		       String randomLine = lines.get(r.nextInt(lines.size()));
		       String[] fileNameVectors = randomLine.split("=");
		       Matcher m = p.matcher(fileNameVectors[1]);
				String v=null;
				while(m.find()) {
				   v = m.group(1);
				}
				String[] vec = v.split(",");
				double[] vecArray = new double[vec.length];		
				for(int j=0;j<vec.length;j++){
					String trim = vec[j].replaceAll("\\s+","");
					vecArray[j] = Double.parseDouble(trim);
					//System.out.println("vec array"+vecArray[j]);
				}
				VectorWritable vw = new VectorWritable(vecArray);
		       centerWriter.append(new ClusterCenter(vw), value);
		     	}
	}	}

	// Performing LDA
	private boolean performLDA(String input) throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub

		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Boolean jobStatus = true;
		Path result = new Path(input);

		int i = 0;
		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (status.isDirectory() && jobStatus) {
				Job job = Job.getInstance(getConf(), "performLDA");
				job.setJarByClass(this.getClass());

				Path path = status.getPath();
				FileInputFormat.addInputPath(job, path);
				FileOutputFormat.setOutputPath(job, new Path("/user/vpcl/project/output/topics/topic" + i)); // intermediate
				job.setMapOutputKeyClass(Text.class);
				job.setMapOutputValueClass(Text.class);
				job.setMapperClass(LDAMapper.class);
				// job.setOutputFormatClass(SequenceFileOutputFormat.class);
				// //setting the output format of the link graph generator to
				// Sequence file format.
				job.setOutputKeyClass(Text.class);
				job.setOutputValueClass(IntWritable.class);
				// job.setNumReduceTasks(3); //setting the number of reduce
				// tasks to be 2
				job.setReducerClass(LDAReducer.class);
				jobStatus = job.waitForCompletion(true);
				i = i + 1;

			}
		}

		return jobStatus;
	}

	public boolean combineTopics(String input, String output)
			throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Configuration conf = new Configuration();
		FileSystem fs = FileSystem.get(conf);
		Path result = new Path(input);

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fs.create(new Path(output), true)));
		FileStatus[] stati = fs.listStatus(result);
		for (FileStatus status : stati) {
			if (status.isDirectory()) {
				System.out.println("pathhhhh" + status.getPath());
				FileStatus[] stati1 = fs.listStatus(status.getPath());
				for (FileStatus status1 : stati1) {
					if (!status1.getPath().getName().equals("_SUCCESS")) {
						System.out.println("pathhhhhffffffffffff" + status1.getPath());
						BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(status1.getPath())));
						String line;
						line = br.readLine();
						System.out.println("readinggggg" + line);
						while (line != null) {

							writer.write(line.trim() + "\n");

							line = br.readLine();
						}

					}
				}

			}
		}
		writer.close();
		return true;
	}

	private boolean createSemanticTerms(String input, String output)
			throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Job job = Job.getInstance(getConf(), "createSemanticTerms");
		job.setJarByClass(this.getClass());
		FileInputFormat.addInputPaths(job, input);
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setMapperClass(SemanticTermsMapper.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(IntWritable.class);
		// job.setOutputFormatClass(SequenceFileOutputFormat.class); //setting
		// the output format of the link graph generator to Sequence file
		// format.
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(IntWritable.class);
		// job.setNumReduceTasks(3); //setting the number of reduce tasks to be
		// 2
		job.setReducerClass(SemanticTermsReducer.class);
		return job.waitForCompletion(true);
	}

	private boolean cleanAndSorting(String input, String output)
			throws IOException, ClassNotFoundException, InterruptedException {
		// TODO Auto-generated method stub
		Job job = Job.getInstance(getConf(), "cleanAndSorting");
		job.setJarByClass(this.getClass());
		FileInputFormat.setInputPaths(job, new Path(input));
		FileOutputFormat.setOutputPath(job, new Path(output));
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapperClass(RankSortMapper.class);
		job.setMapOutputKeyClass(IntWritable.class);
		job.setMapOutputValueClass(Text.class);
		// job.setInputFormatClass(SequenceFileInputFormat.class); //setting the
		// input format of the clean and sorting job as Sequence file format.
		job.setOutputFormatClass(TextOutputFormat.class); // setting the output
															// format of the
															// clean and sorting
															// job back to Text
															// format.
		job.setSortComparatorClass(SortComparator.class); // Sort comparator
															// class to sort the
															// page rank results
															// in the descending
															// order.
		job.setNumReduceTasks(1); // setting the number of reduce tasks to be 1
		job.setReducerClass(RankSortReducer.class);
		return job.waitForCompletion(true);
	}

	public List<String> runLDA(String input) {

		// Begin by importing documents from text to feature sequences

		ArrayList<String> topicList = new ArrayList<String>();
		try {
			ArrayList<Pipe> pipeList = new ArrayList<Pipe>();

			// Pipes: lowercase, tokenize, remove stopwords, map to features
			pipeList.add(new CharSequenceLowercase());
			pipeList.add(new CharSequence2TokenSequence(Pattern.compile("\\p{L}[\\p{L}\\p{P}]+\\p{L}")));
			// push stop words to HaDOOP
			//pipeList.add(new TokenSequenceRemoveStopwords(new File("/user/vpcl/project/stopword/" + "stopword.txt"),
			//		"UTF-8", false, false, false));
			pipeList.add(new TokenSequence2FeatureSequence());

			InstanceList instances = new InstanceList(new SerialPipes(pipeList));

			/*
			 * File folder = new File(input); ArrayList<File> files = new
			 * ArrayList<File>();
			 * 
			 * files = listf(input, files); File[] listOfFiles =
			 * files.toArray(new File[files.size()]); for (int i = 0; i <
			 * listOfFiles.length; i++) { File file = listOfFiles[i]; if
			 * (file.isFile() && file.getName().endsWith(".txt")) { content =
			 * FileUtils.readFileToString(file); writer.println(content); do
			 * somthing with content } }
			 */

			// System.out.println("Content"+content);
			// Reader fileReader = new InputStreamReader((input)), "UTF-8");
			instances.addThruPipe(new CsvIterator(new StringReader(input),
					Pattern.compile("^(\\S*)[\\s,]*(\\S*)[\\s,]*(.*)$"), 3, 2, 1)); // data,
																					// label,
																					// name
																					// fields

			// Create a model with 100 topics, alpha_t = 0.01, beta_w = 0.01
			// Note that the first parameter is passed as the sum over topics,
			// while
			// the second is the parameter for a single dimension of the
			// Dirichlet prior.
			int numTopics = 5;
			ParallelTopicModel model = new ParallelTopicModel(numTopics, 1.0, 0.01);

			model.addInstances(instances);

			// Use two parallel samplers, which each look at one half the corpus
			// and combine
			// statistics after every iteration.
			model.setNumThreads(2);

			// Run the model for 50 iterations and stop (this is for testing
			// only,
			// for real applications, use 1000 to 2000 iterations)
			model.setNumIterations(50);
			model.estimate();

			// Show the words and topics in the first instance

			// The data alphabet maps word IDs to strings
			Alphabet dataAlphabet = instances.getDataAlphabet();

			FeatureSequence tokens = (FeatureSequence) model.getData().get(0).instance.getData();
			LabelSequence topics = model.getData().get(0).topicSequence;

			Formatter out = new Formatter(new StringBuilder(), Locale.US);
			for (int position = 0; position < tokens.getLength(); position++) {
				out.format("%s-%d ", dataAlphabet.lookupObject(tokens.getIndexAtPosition(position)),
						topics.getIndexAtPosition(position));
			}
			// System.out.println(out);

			// Estimate the topic distribution of the first instance,
			// given the current Gibbs state.
			double[] topicDistribution = model.getTopicProbabilities(0);

			// Get an array of sorted sets of word ID/count pairs
			ArrayList<TreeSet<IDSorter>> topicSortedWords = model.getSortedWords();

			// Show top 5 words in topics with proportions for the first
			// document
			System.out.println("OUTSIDE FORRRRRRRRRR");
			for (int topic = 0; topic < numTopics; topic++) {
				Iterator<IDSorter> iterator = topicSortedWords.get(topic).iterator();
				System.out.println("INSIDE FORRRRRRRR");
				out = new Formatter(new StringBuilder(), Locale.US);
				out.format("%d\t%.3f\t", topic, topicDistribution[topic]);
				int rank = 0;
				while (iterator.hasNext() && rank < 5) {
					IDSorter idCountPair = iterator.next();
					out.format("%s (%.0f) ", dataAlphabet.lookupObject(idCountPair.getID()), idCountPair.getWeight());
					rank++;
					System.out.println("topiccccccc" + (String) dataAlphabet.lookupObject(idCountPair.getID()));
					topicList.add((String) dataAlphabet.lookupObject(idCountPair.getID()));
				}

			}
			// writer.close();
			// Create a new instance with high probability of topic 0
			StringBuilder topicZeroText = new StringBuilder();
			Iterator<IDSorter> iterator = topicSortedWords.get(0).iterator();

			int rank = 0;
			while (iterator.hasNext() && rank < 5) {
				IDSorter idCountPair = iterator.next();
				topicZeroText.append(dataAlphabet.lookupObject(idCountPair.getID()) + " ");
				rank++;
			}

			// Create a new instance named "test instance" with empty target and
			// source fields.
			InstanceList testing = new InstanceList(instances.getPipe());
			testing.addThruPipe(new Instance(topicZeroText.toString(), null, "test instance", null));

			TopicInferencer inferencer = model.getInferencer();
			double[] testProbabilities = inferencer.getSampledDistribution(testing.get(0), 10, 1, 5);

			// return topicList;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return topicList;

	}

	public static ArrayList<File> listf(String directoryName, ArrayList<File> files) {
		File directory = new File(directoryName);

		// get all the files from a directory
		File[] fList = directory.listFiles();
		for (File file : fList) {
			if (file.isFile()) {
				files.add(file);
			} else if (file.isDirectory()) {
				listf(file.getAbsolutePath(), files);
			}
		}

		return files;
	}

}
