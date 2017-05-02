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
 * 
 * Interface for measuring distance between two vectors
 * */

public interface DistanceMeasurer {
	public double measureDistance(double[] set1, double[] set2);
	public double measureDistance(DoubleVector vec1, DoubleVector vec2);
}
