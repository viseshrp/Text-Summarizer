
/*=============================================================================
|   Assignment:  Final Project - Multiple Document Summarization
|       Author:  Group7 - (Sampath, Ajay, Visesh)
|       Grader:  Walid Shalaby
|
|       Course:  ITCS 6190
|   Instructor:  Srinivas Akella
|
|     Language:  Java 
|     Version :  1.8.0_101
|                
| Deficiencies:  No logical errors.
*===========================================================================*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;

/*
 * Performs extractive summarization where sentences are filtered out
 * based on semantically similar terms in the final list.
 * */
public class Summarizer extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(Summarizer.class);

	public int run(String[] args) throws Exception {
		Job job = Job.getInstance(getConf(), " summarizer ");

		Configuration configuration = job.getConfiguration(); // create a
																// configuration
																// reference
		String termList = "";

		try {
			FileSystem fs = FileSystem.get(configuration);
			Path path = new Path(args[6]);
			BufferedReader br = new BufferedReader(new InputStreamReader(fs.open(path)));
			String term;
			term = br.readLine();
			int i = 0;
			while (i < 5) {
				termList += term.trim() + ",";
				term = br.readLine();
				i++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		configuration.set("termList", termList); // use
													// configuration
													// object to
													// pass file
													// count

		job.setJarByClass(this.getClass());
		job.setMapperClass(Map.class);
		job.setReducerClass(Reduce.class);

		FileInputFormat.addInputPath(job, new Path(args[1]));
		FileOutputFormat.setOutputPath(job, new Path(args[8]));

		// Explicitly set key and value types of map and reduce output
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		job.setMapOutputKeyClass(Text.class);
		job.setMapOutputValueClass(Text.class);
		// job.setNumReduceTasks(1);

		return job.waitForCompletion(true) ? 0 : 1;
	}

	public static class Map extends Mapper<LongWritable, Text, Text, Text> {

		public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {

			String termList = context.getConfiguration().get("termList");
			String[] termListArray = termList.split(",");
			for (String term : termListArray) {
				if (lineText.toString().toLowerCase().contains(" " + term.toLowerCase() + " ")) {
					context.write(new Text("key"), lineText);
					break;
				}
			}
		}
	}

	public static class Reduce extends Reducer<Text, Text, Text, Text> {
		@Override
		public void reduce(Text word, Iterable<Text> iterable, Context context)
				throws IOException, InterruptedException {

			for (Text text : iterable) {
				// write to output
				context.write(new Text(""), text);
			}
		}
	}
}