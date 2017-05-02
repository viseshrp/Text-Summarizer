import java.io.IOException;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class SemanticTermsReducer extends Reducer<Text ,  IntWritable ,  Text ,  IntWritable > {
    @Override 
    public void reduce( Text word,  Iterable<IntWritable > counts,  Context context)
       throws IOException,  InterruptedException {
       int termFreq = 0;
       
       for ( IntWritable count  : counts) {
          termFreq  += count.get();
       }
       context.write(word,  new IntWritable(termFreq));
    }
 }