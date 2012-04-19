package forkJoin;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinSlow {
	static Random rand;
	private final static int numberOfPoints = 1000000;
	
	//http://www.roseindia.net/java/beginners/arrayexamples/QuickSort.shtml
	public static void quick_srt(double array[], double array2[], int low, int n){
		int lo = low;
		int hi = n;
		if (lo >= n) return;

		double mid = array[(lo + hi) / 2];
		while (lo < hi) {
			while (lo<hi && array[lo] < mid) {
				lo++;
			}
			while (lo<hi && array[hi] > mid) {
				hi--;
			}
			if (lo < hi) {
				double T = array[lo];
				array[lo] = array[hi];
				array[hi] = T;
				double T2 = array2[lo];
				array2[lo] = array2[hi];
				array2[hi] = T2;
			}
		}
		if (hi < lo) {
			int T = hi;
			hi = lo;
			lo = T;
		}
		quick_srt(array, array2, low, lo);
		quick_srt(array, array2, lo == low ? lo+1 : lo, n);
	}
		
	public static void main(String[] arg){
		double[] xCoordinate = new double[numberOfPoints];
		double[] yCoordinate = new double[numberOfPoints];
		System.out.println("Please provide the seed");
		Scanner scan = new Scanner(System.in);
		int seed = scan.nextInt();
		
		 rand = new Random(seed);
		 for(int i = 0; i < numberOfPoints; i++){
			 xCoordinate[i] = rand.nextDouble();
			 yCoordinate[i] = rand.nextDouble();
		 }
		 long startTime = System.nanoTime();
		 quick_srt(xCoordinate, yCoordinate, 0, numberOfPoints - 1);
		 long elapsedTime = System.nanoTime() - startTime;
		 elapsedTime /= 1000000;
		 System.out.println("Quick sort " + elapsedTime);
		 startTime = System.nanoTime();
		 ArrayList<Double> answers = CalculateClosestPoints.fjPool.invoke(new 
				 CalculateClosestPoints(xCoordinate, yCoordinate, 0, 
						 numberOfPoints, xCoordinate.length, numberOfPoints));
		 elapsedTime = System.nanoTime() - startTime;
		 elapsedTime /= 1000000;
		 System.out.println("Time took was " + elapsedTime);
		 for(Double d: answers){
			 System.out.println(d);
		 }
	}
}	

	class CalculateClosestPoints extends RecursiveTask<ArrayList<Double>> {
		private static final long serialVersionUID = 1L;
		final int SEQUENTIAL_THRESHOLD = 1000;
		public static ForkJoinPool fjPool = new ForkJoinPool();
	    int numberToIterate, initial, end, totalPoints;
	    double[] xCoordinate;
	    double[] yCoordinate;
	    double distance;
	    
	    CalculateClosestPoints(double[] xCoordinate, double[] yCoordinate, 
	    		int initial, int end, int length, int total) {
	    	numberToIterate = length;
	        this.xCoordinate = xCoordinate;
	        this.yCoordinate = yCoordinate;
	        distance = 2.0;
	        this.initial = initial;
	        this.end = end;
	        totalPoints = total;
	    }

		@Override
		protected ArrayList<Double> compute() {
			double xPoint = 0, yPoint = 0, xPoint2 = 0, yPoint2 = 0;
			ArrayList<Double> returnArray = new ArrayList<Double>();
			if(numberToIterate <= SEQUENTIAL_THRESHOLD){
				for(int i = initial; i < end; i++){
					
					for(int j = 0; j < totalPoints; j++){ 
						double xDistance = xCoordinate[i] - xCoordinate[j]; 
						double yDistance = yCoordinate[i] - yCoordinate[j];
						double tempDistance =  Math.sqrt(Math.pow(xDistance, 2) + 
								Math.pow(yDistance, 2));
						if(tempDistance < distance && i != j) {
							xPoint = xCoordinate[i];
							yPoint = yCoordinate[i];
							xPoint2 = xCoordinate[j];
							yPoint2 = yCoordinate[j];
							distance = tempDistance;
						}
					}//inner loop
				}//outer loop
				returnArray.add(distance);
				returnArray.add(xPoint);
				returnArray.add(yPoint);
				returnArray.add(xPoint2);
				returnArray.add(yPoint2);
				return returnArray;
			}
			else{
				int mid = initial + (end - initial) / 2;
				int length = end - mid;
				CalculateClosestPoints left = new CalculateClosestPoints(xCoordinate,
						yCoordinate, initial, mid, length, totalPoints);
				CalculateClosestPoints right = new CalculateClosestPoints(xCoordinate,
						yCoordinate, mid, end, length, totalPoints);
				left.fork();
				ArrayList<Double> rightAnswer = right.compute();
				ArrayList<Double> leftAnswer  = left.join();
				if(rightAnswer.get(0) < leftAnswer.get(0)) return rightAnswer;
				else return leftAnswer;
			}
		}
	}
