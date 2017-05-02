
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
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

/*
 * Reducer class to sort topic terms based on term frequency.
 * */
public class RankSortReducer extends Reducer<IntWritable, Text, Text, Text> {

	private static final long topResults = 15; // To retrieve the top 15
												// semantic terms

	@Override
	public void reduce(IntWritable key, Iterable<Text> values, Context context)
			throws IOException, InterruptedException {
		long count = 1;
		// Counter to keep track of the top semantic terms.
		if (context.getCounter("topTopicTerms", "topTopicTerms") != null) {
			count = context.getCounter("topTopicTerms", "topTopicTerms").getValue();
		}
		for (Text val : values) {
			if (count < topResults) {
				count = count + 1;
				context.getCounter("topTopicTerms", "topTopicTerms").setValue(count);
				context.write(val, new Text(""));
			}
		}
	}
}