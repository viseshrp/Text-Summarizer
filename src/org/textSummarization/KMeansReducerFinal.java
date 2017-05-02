 


import java.io.IOException;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.io.Text;


public class KMeansReducerFinal extends Reducer<Text, Text, Text, Text> {

	@Override
	protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException,
			InterruptedException {
		
		boolean first = true;
		String fileName = "";
        for (Text value : values) {
            if(!first){
            	fileName += ",";
            }
            fileName += value.toString();
            first = false;
        }
        context.write(new Text("Cluster"),  new Text(fileName));
	}
}
