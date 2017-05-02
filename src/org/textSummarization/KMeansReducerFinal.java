
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
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;

/*
 * Writes corresponding cluster and respective filenames in each cluster.
 * */
public class KMeansReducerFinal extends Reducer<Text, Text, Text, Text> {

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {

		boolean first = true;
		String fileName = "";
		for (Text value : values) {
			if (!first) {
				fileName += ",";
			}
			fileName += value.toString();
			first = false;
		}
		context.write(new Text("Cluster"), new Text(fileName));
	}
}
