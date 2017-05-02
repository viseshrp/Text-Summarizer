 

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class CombineTopicsReducer extends Reducer<Text ,  Text ,  Text ,  Text > {
    @Override 
    public void reduce( Text key,  Iterable<Text> values,  Context context)
       throws IOException,  InterruptedException {
       int termFreq = 0;
       StringBuffer sb=new StringBuffer(""); 
       TextSummarizationDriver driverClass = new TextSummarizationDriver();
       
       for(Text value: values){
    	   
    	   context.write(new Text(""), value);
    	   
       }
       }
             
 }
