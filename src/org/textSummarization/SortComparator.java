
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

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.io.WritableComparator;

/*
 * Sorts the results of top semantic terms in descending
 * order.
 * */
public class SortComparator extends WritableComparator {

	protected SortComparator() {
		super(IntWritable.class, true);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public int compare(WritableComparable o1, WritableComparable o2) {
		IntWritable k1 = (IntWritable) o1;
		IntWritable k2 = (IntWritable) o2;
		int cmp = k1.compareTo(k2);
		return -1 * cmp;
	}
}
