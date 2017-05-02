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
import edu.smu.tspell.wordnet.*;
import java.text.ParseException;

/*
 * Mapper to emit LDA key as key and current line read from each document
 * in the cluster. 
 * */
public class LDAMapper extends Mapper<LongWritable, Text, Text, Text> {
	private final static IntWritable one = new IntWritable(1);

	public void map(LongWritable offset, Text lineText, Context context) throws IOException, InterruptedException {
		try {
			String term = lineText.toString();
			Text currlines = new Text(term);
			context.write(new Text("LDAKey"), currlines);
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}