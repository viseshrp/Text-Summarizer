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

/*
 * Computes Manhattan distance between two vectors
 * */
public final class ManhattanDistance implements DistanceMeasurer {

	@Override
	public double measureDistance(double[] set1, double[] set2) {
		double sum = 0;
		int length = set1.length;
		for (int i = 0; i < length; i++) {
			sum += Math.abs(set1[i] - set2[i]);
		}
		return sum;
	}

	@Override
	public double measureDistance(DoubleVector vec1, DoubleVector vec2) {
		return vec1.subtract(vec2).abs().sum();
	}

}
