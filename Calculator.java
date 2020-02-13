package Indy;

import java.util.ArrayList;

/*
 * This class is a matrix calculator, with generic algorithms
 * for calculating various kinds of matrix operations.
 * The methods are divided into matrix operations and
 * machine learning related operations.
 * 
 */
public class Calculator {
	
	/*
	 * Matrix operation methods
	 */
	
	/*
	 * Transpose: switch columns and row
	 */
    public double[][] transpose(double [][] a){
        double[][] aT = new double[a[0].length][a.length];
        for (int i = 0; i < a.length; i++)
            for (int j = 0; j < a[0].length; j++)
                aT[j][i] = a[i][j];
        return aT;
    }
    
    /*
     * subtract matrices: A-B
     */
    public double[][] subtract(double[][] a, double[][] b) {
    	int rowA = a.length;
    	int colA = a[0].length;
    	int rowB = b.length;
    	int colB = b[0].length;
    	if (rowA == rowB && colA == colB) {
        	double[][] aMinusB = new double[rowA][colB];
    		for (int i = 0; i < rowA; i++) {
                for (int j = 0; j < colA; j++) {
                    aMinusB[i][j] += a[i][j] - b[i][j];
                }
            }
    		return aMinusB;
    	} else {
    		return null;
    	}
    }
    
    /*
     * multiply matrices: A*B
     */
    public double[][] multiply(double[][] a, double[][] b) {
    	int rowA = a.length;
    	int colA = a[0].length;
    	int rowB = b.length;
    	int colB = b[0].length;
    	if (colA == rowB) {
        	double[][] ab = new double[rowA][colB];
        	for (int i = 0; i < rowA; i++) {
                for (int j = 0; j < colB; j++) {
                    for (int k = 0; k < colA; k++) {
                        ab[i][j] += a[i][k] * b[k][j];
                    }
                }
            }
    		return ab;
    	} else {
    		return null;
    	}
    }
    
    /*
     * Gauss-Jordan Elimination process to obtain row reduced echelon form.
     * Used for getting the inverse of a matrix, as any invertible
     * matrix becomes an identity matrix.
     */
    public double[][] rowReduce(double[][] a) {
		int rowA = a.length;
		int colA = a[0].length;
		// for each row
	    for (int i = 0; i < rowA; ++i) {
	        // pivot value has the same row and col num.
	        double pivot = a[i][i];
	        // divide the row by pivot value. 
	        if (pivot != 0){
	            for (int j = 0; j < colA; ++j){
	                a[i][j] /= pivot;
	            }
	        }
	        // for other rows:
	        for (int k = 0; k < rowA; ++k){
	        	if (k == i) {
	        		continue;
	        	} else {
	        		// the value of this row on the pivot column
	        		double pVal = a[k][i];
	        		// subtract row with the pivot row * pVal
	                for (int l = 0; l < colA; ++l){
	                    a[k][l] -= a[i][l] * pVal;
	                }
	        	}
	        }
	    }
	    return a;
	}
    
    /*
     * Create an identity matrix of any size of n. I
     */
    public double[][] identity(int n) {
    	double[][] eye = new double[n][n];
    	for (int i = 0; i < n; i++) {
    		for (int j = 0; j < n; j++) {
    			if (i == j) {
    				eye[i][j] = 1;
    			}
    			else {
    				eye[i][j] = 0;
    			}
    		}
    	}
    	return eye;
    }
    
    /*
     * Find the inverse of a matrix using rowReduce and identity
     */
    public double[][] inverse(double[][] a) {
    	int n = a.length;
    	double[][] eye = this.identity(n);
    	double[][] aI = new double[n][2*n];
    	for (int i = 0; i < n; i++) {
    		for (int j = 0; j < n; j++) {
        		aI[i][j] = a[i][j];
        	}
    		for (int j = n; j < 2*n; j++) {
    			aI[i][j] = eye[i][j-n];
        	}
    	}
    	double[][] iA = this.rowReduce(aI);
    	double[][] inv = new double[n][n];
    	for (int i = 0; i < n; i++) {
    		for (int j = 0; j < n; j++) {
        		inv[i][j] = iA[i][j+n];
        	}
    	}
    	return inv;
    }
    
    /*
     * Below are calculations for the autoregression techniques, 
     * using the above matrix operations
     */
    
    /*
     * Matrix of x-values
     */
	public double[][] xMatrix(ArrayList<Double> input, int p) {
		double[][] xMatrix = new double[input.size()-p][p+1];
		for (int i = 0; i < input.size()-p; i++) {
			xMatrix[i][0] = 1;
			for (int j = 1; j < p+1; j++) {
				xMatrix[i][j] = input.get(i+p-j);
			}
		}
		return xMatrix;
	}
	
	/*
	 * Vector of y-values
	 */
	public double[][] yVector(ArrayList<Double> input, int p) {
		double[][] yVector = new double[input.size()-p][1];
		for (int i = 0; i < input.size()-p; i++) {
			yVector[i][0] = input.get(i+p);
		}
		return yVector;
	}
	
	/*
	 * Find coefficients based on x and y values
	 */
	public double[][] leastSquares(double[][] x, double[][] y) {
		double[][] xT = this.transpose(x);
		return this.multiply(this.inverse(this.multiply(xT, x)),this.multiply(xT, y));
	}
	
	/*
	 * Calculate Bayesian Information Criteria of the determined model
	 */
	public double infoCriteria(double[][] x, double[][]y, double[][] b, int n) {
		double[][] xb = this.multiply(x,b);
		double[][] yMinusXb = this.subtract(y,xb);
		double rss = this.multiply(this.transpose(yMinusXb),yMinusXb)[0][0];
		int p = b.length - 1;
		return Math.log(rss/n) + p*Math.log(n)/n;
	}
	
}
