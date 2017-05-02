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

import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.log4j.Logger;

/*
 * Mapper to emit term frequency of the semantic terms list as key and topic
 * terms as value.
 * */
public class RankSortMapper extends Mapper<LongWritable, Text, IntWritable, Text> {

	private static final Logger LOG = Logger.getLogger(RankSortMapper.class);

	@Override
	public void map(LongWritable offset, Text value, Context context) throws IOException, InterruptedException {
		String line = value.toString();
		String[] splits = line.split("\\t");
		String topicTerms = splits[0];
		int termFreq = Integer.parseInt(splits[1]);
		context.write(new IntWritable(termFreq), new Text(topicTerms));
	}
}