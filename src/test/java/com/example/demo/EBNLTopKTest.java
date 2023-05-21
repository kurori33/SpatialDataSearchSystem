package com.example.demo;

import org.junit.Test;
import com.example.demo.topkskyline.EBNLTopK;

import java.util.ArrayList;

public class EBNLTopKTest extends TopKTest{
	
	public EBNLTopKTest(int topK, ArrayList<Object> input, ArrayList<ArrayList<Object>> S_ml) {
		super(topK, input, S_ml);
	}
	

	@Test
	public void testResult() {
		EBNLTopK ebnl = new EBNLTopK(input, topK);
		ArrayList<Object> result = ebnl.getResult();
		assertResultEqual(result);
	}
}
