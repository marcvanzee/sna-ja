package main;

import java.util.ArrayList;

public class Matrix {

    private int nrows;
    private int ncols;
    public int[][] data;
    public ArrayList<ArrayList<Integer>> node_value = new ArrayList<ArrayList<Integer>>();

    public Matrix(int[][] dat) {
        this.data = dat;
        this.nrows = dat.length;
        this.ncols = dat[0].length;
        initNodeValues();
    }

    public Matrix(int nrow, int ncol) {
        this.nrows = nrow;
        this.ncols = ncol;
        data = new int[nrow][ncol];
        initNodeValues(); 
    }
    
    private void initNodeValues() {
    	for (int i=0; i<nrows; i++) {
        	node_value.add(new ArrayList<Integer>());
        	node_value.get(i).add(i);
        }
    }
    
    public int getNcols() {
    	return ncols;
    }
    
    public int getNrows() {
    	return nrows;
    }
    
    public int getValueAt(int i, int j) {
    	return data[i][j];
    }
    
    public void setValueAt(int i, int j, int v) {
    	if ((0 <= i)&&(i<nrows)&&(j<=0)&&(j<ncols)) {
    		data[i][j] = v;
    	}
    }
    
    public int[] getRow(int i) {
    	return data[i];
    }
    
    public double[] getRowAsDouble(int i) {
    	double ret[] = new double[ncols];
    	
    	for (int j=0;j<ncols;j++) {
    		ret[j] = ((double) data[i][j]);
    	}
    	
    	return ret;
    }
    
    public String toString() {
    	String ret = "";
    	
    	for (int i=0;i<nrows;i++) {
    		for (int j=0;j<ncols;j++) {
    			ret += data[i][j] + " ";
    		}
    		
    		ret += "\n";
    	}
    	
    	return ret;
    }
    
    public int sumRow(int i) {
    	int ret = 0;
    	
    	for (int j=0; j<ncols; j++) {
    		ret += data[i][j];
    	}
    	
    	return ret;
    }
    
    public static Matrix transpose(Matrix m) {
	    int[][] o = new int[m.getNcols()][m.getNrows()];
	    
	    for (int i=0;i<m.getNrows();i++) {
	        for (int j=0;j<m.getNcols();j++) {
	            o[j][i] = m.getValueAt(i, j);
	        }
	    }
	    return new Matrix(o);
	}
    
    public static Matrix multiply(Matrix a, Matrix b) {
        int rowsInA = a.getNrows();
        int columnsInA = a.getNcols(); // same as rows in B
        int columnsInB = b.getNcols();
        
        int[][] c = new int[rowsInA][columnsInB];
        
        for (int i = 0; i < rowsInA; i++) {
            for (int j = 0; j < columnsInB; j++) {
                for (int k = 0; k < columnsInA; k++) {
                    c[i][j] = c[i][j] + a.getValueAt(i, k) * b.getValueAt(k, j);
                }
            }
        }
        
        return new Matrix(c);
    }
    
    public static Matrix normalize(Matrix m, int n) {
    	int [][] ret = new int[m.getNrows()][m.getNcols()];
    	
    	for (int i=0;i<m.getNrows();i++) {
    		for (int j=0;j<m.getNcols();j++) {
    			ret[i][j] = (m.getValueAt(i, j)+n)/2;
    		}
    	}
    	
    	return new Matrix(ret);
    }
    
    public static Matrix toSimilarity(Matrix m) {
    	int [][] ret = new int[m.getNrows()][m.getNcols()];
    	
    	for (int i=0;i<m.getNrows();i++) {
    		for (int j=0;j<m.getNcols();j++) {
    			ret[i][j] = (m.getValueAt(i, j)==0?-1:1);
    		}
    	}
    	
    	return new Matrix(ret);
    }

	public static Matrix adjacency(Matrix m) {
		int [][] ret = new int[m.getNrows()][m.getNcols()];
    	
    	for (int i=0;i<m.getNrows();i++) {
    		for (int j=0;j<m.getNcols();j++) {
    			ret[i][j] = (m.getValueAt(i, j)!=0?1:0);
    		}
    	}
    	
    	return new Matrix(ret);
	}

	public static Matrix specialMultiply(Matrix a, Matrix b) 
	{ 
		int rowsInA = a.getNrows();
	    int columnsInA = a.getNcols(); // same as rows in B
	    int columnsInB = b.getNcols();
	    
	    Matrix c = new Matrix(new int[rowsInA][columnsInB]);
	    
	    for (int i = 0; i < rowsInA; i++) {
	    	if (c.node_value.get(i).isEmpty()) continue;
	        for (int j = 0; j < columnsInB; j++) {
	        	if (c.node_value.get(j).isEmpty()) continue;
	            for (int k = 0; k < columnsInA; k++) {
	                c.data[i][j] = c.data[i][j] + a.getValueAt(i, k) * b.getValueAt(k, j);
	            }
	            
	            if ((c.data[i][j] == columnsInA) && (i!=j)) { 
	            	c.node_value.get(i).add(j);
	            	c.node_value.get(j).clear();
	            }
	            
	        }
	    }
	    
	    return c;
	}

	public static Matrix simplify(Matrix d) {
		int count = 0;
		for (int i=0; i<d.node_value.size(); i++) count += (d.node_value.get(i).isEmpty()?0:1);
		
		int data[][] = new int[count][count];
		Matrix ret = new Matrix(data);
		ret.node_value.clear();
		
		int x=0,y;
		
		for (int i=0;i<d.nrows; i++) {
			if (d.node_value.get(i).isEmpty()) continue;
			ret.node_value.add(d.node_value.get(i));
			y=0;
			for (int j=0; j<d.ncols; j++) {
				if (d.node_value.get(j).isEmpty()) continue;

				ret.data[x][y] = d.data[i][j];
				y++;					
			}
			x++;
		}
		
		return ret;
	}
}