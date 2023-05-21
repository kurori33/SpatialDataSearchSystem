package com.example.demo.dataGenerator;
import java.util.Random;

/**
 * Created by Viechle on 18.12.2016.
 */
public class RandVector{
	
    public static final int EQUAL = 0;
    public static final int GREATER = 1;
    public static final int LESS = -1;
    public static final int SUBSTITUTABLE = 2;
    @Deprecated
    public static final int UNRANKED = -2;

    int id;
    int dim;
    String name;
    double[] values;
    String padding;

    public RandVector(int d){

        this.id = RandomDataGenerator.getNextVectorId();
        this.dim = d;
        values = new double[dim];

    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public RandVector(double[] values){
    	this(values.length);
    	this.values = values;
    }

    void generate_indep(int dim){

        for(int d = 0 ; d < dim ; d++){

            values[d] = random_equal(0,1);
        }

    }

    void generate_corr(int dim){

        do {

            double v = random_peak(0, 1, dim);
            double l = v <= 0.5 ? v : 1.0 - v;

            for (int d = 0; d < dim; d++) {

                values[d] = v;
            }

            for (int d = 0; d < dim; d++) {

                double h = random_normal(0, 1);
                values[d] += h;
                values[(d + 1) % dim] -= h;
            }
        } while (!is_vector_ok(values));
    }

    void generate_anti(int dim){

        do {

            double v = random_normal(0.5, 0.25);
            double l = v <= 0.5 ? v : 1.0 - v;

            for (int d = 0; d < dim; d++) {

                values[d] = v;
            }

            for (int d = 0; d < dim; d++) {

                double h = random_equal(-1, 1);
                values[d] += h;
                values[(d + 1) % dim] -= h;
            }
        } while (!is_vector_ok(values));
    }

    static boolean is_vector_ok(double[] values){

        for(int i = 0 ; i < values.length ; i++){

            if(values[i] < 0.0 || values[i] > 1.0){

                return false;
            }
        }
        return true;
    }

    static double random_equal(double min, double max){

        Random randomNr = new Random();
        return randomNr.nextDouble()*(max-min) + min;
    }

    static double random_peak(double min, double max, int dim){

        double sum = 0.0;

        for(int d =0 ; d < dim ; d++){

            sum += random_equal(0, 1);
        }
        sum /= dim;
        return sum *(max-min) + min;
    }

    static double random_normal(double med, double var){

        return random_peak(med-var, med+var, 12);
    }

    public void generate_padding(String padInit, int padLen){

        int offset = this.id % padInit.length();
        if((offset + padLen) > padInit.length()){  //check if the padding exceedes the length of padInit

            int rest = (offset + padLen) - padInit.length();
            padding = padInit.substring(offset, padInit.length());
            padding += padInit.substring(0,rest);
        }else{

            padding = padInit.substring(offset, offset + padLen);
        }
    }
    
    public double[] getValues(){
    	return values;
    }


	public int compare(RandVector o) {
		 int result = SUBSTITUTABLE;
		 double[] vals1 = this.getValues();
		 double[] vals2 = o.getValues();
	        for (int i = 0; i < vals1.length; i++) {
				if (Double.compare(vals1[i],vals2[i]) < 0) {
	                // this is better in the current base preference
	                if (result == LESS) {
	                    // at least once worse and now better: unranked
	                    return UNRANKED;
	                }
	                result = GREATER;
	            } else if (Double.compare(vals1[i], vals2[i]) > 0) {
	                // this is worse in the current base preference
	                if (result == GREATER) {
	                    // at least once better and now worse: unranked
	                    return UNRANKED;
	                }
	                result = LESS;
	            }
	        }
	        return result;
	}
	
	@Override
	public String toString(){
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for(int i=0; i < values.length; i++){
			buffer.append(values[i]);
			if(i < values.length-1)
				buffer.append(", ");
		}
		buffer.append("]");
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object obj){
		
		 if (obj == null || !(obj instanceof RandVector))
		        return false;
		   
		    final RandVector rnd = (RandVector) obj;
		    
		    if(rnd.values.length != this.values.length)
		    	return false;
		    
		   	boolean isEquals = false;
		    for(int i=0; i < rnd.values.length; i++){
		    	if(Double.compare(this.values[i], rnd.values[i]) != 0){
		    		isEquals = false;
		    		break;
		    	}else{
		    		isEquals = true;
		    	}
		    }
		return isEquals;
	}
}
