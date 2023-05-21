package com.example.demo;

import com.example.demo.dataGenerator.RandVector;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertTrue;

@RunWith(value = Parameterized.class)
public abstract class TopKTest {

	protected int topK;
	protected ArrayList<Object> input;
	protected ArrayList<ArrayList<Object>> S_ml;
	
	public TopKTest(int topK, ArrayList<Object> input, ArrayList<ArrayList<Object>> S_ml) {
		this.topK = topK;
		this.input = input;
		this.S_ml = S_ml;
	}


	@Parameters(name = "{index}: testResult(k: {0}, input: {1}) = result: {2}")
	public static Collection<Object[]> data() {

		List<Object[]> input = new ArrayList<>();
		input.addAll(randData());
		input.addAll(corrData());
		input.addAll(antiData());

		return input;
	}

	protected void assertResultEqual(ArrayList<Object> result) {
		int k = 0;
		for (ArrayList<Object> expectedResult : S_ml) {
			int count = 0;
			for (Object resultObj : result) {
				if (expectedResult.contains(resultObj))
					count++;
			}
			k += count;
			if (expectedResult.size() > count)
				break;
		}

		boolean isSameOutput = false;
		if (k == topK)
			isSameOutput = true;
		assertTrue(isSameOutput);
	}

	private static Collection<Object[]> randData() {

		List<Object[]> input = new ArrayList<>();

		/*
		 * Entropy: [0.25127311367437281, 0.3419881690481886, 0.3148779915358124, 0.1774787825423224] 
		 * Sum: [0.68, 1.09, 0.94, 0.46]
		 */
		ArrayList<RandVector> randInput = new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.45, 0.23 }), new RandVector(new double[] { 0.98, 0.11 }),
						new RandVector(new double[] { 0.16, 0.78 }), new RandVector(new double[] { 0.14, 0.32 })));
		
		ArrayList<ArrayList<Object>> output1 = new ArrayList<>();
		output1.add(new ArrayList<Object>(Arrays.asList(new RandVector(new double[] { 0.14, 0.32 }))));
		input.add(new Object[] { 1, randInput.clone(), output1});

		ArrayList<ArrayList<Object>> output2 = new ArrayList<>();
		output2.add(new ArrayList<Object>(Arrays.asList(new RandVector(new double[] { 0.14, 0.32 }), new RandVector(new double[] { 0.45, 0.23 }))));
		input.add(new Object[] { 2, randInput.clone(), output2});

		ArrayList<ArrayList<Object>> output3 = new ArrayList<>();
		output3.add(new ArrayList<Object>(Arrays.asList(new RandVector(new double[] { 0.14, 0.32 }),
				new RandVector(new double[] { 0.45, 0.23 }), new RandVector(new double[] { 0.98, 0.11 }))));
		input.add(new Object[] { 3, randInput.clone(), output3 });
		return input;
	}

	private static Collection<Object[]> corrData() {

		ArrayList<RandVector> corrInput = new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.27965500992690867, 0.23283541684296566 }),
						new RandVector(new double[] { 0.7462466986814347, 0.8297705918181446 }),
						new RandVector(new double[] { 0.08756684234164319, 0.5039803730341649 }),
						new RandVector(new double[] { 0.5851183282557083, 0.45796361383713313 }),
						new RandVector(new double[] { 0.5719495102689698, 0.0849608941191407 }),
						new RandVector(new double[] { 0.3137708398116368, 0.17915749710379258 }),
						new RandVector(new double[] { 0.49225609399115866, 0.2572643211127219 }),
						new RandVector(new double[] { 0.3851415055021312, 0.693608265525496 }),
						new RandVector(new double[] { 0.5065358728586449, 0.7112674471654965 }),
						new RandVector(new double[] { 0.635289047793018, 0.34003660414792436 }),
						new RandVector(new double[] { 0.7629169492421959, 0.8815765594124547 }),
						new RandVector(new double[] { 0.4843145980717754, 0.38268335706839496 }),
						new RandVector(new double[] { 0.5013661904440406, 0.4319727836648669 }),
						new RandVector(new double[] { 0.47333117996115925, 0.5259793413529459 }),
						new RandVector(new double[] { 0.48219268136709514, 0.922423607501337 }),
						new RandVector(new double[] { 0.17054517003893793, 0.7249234428510463 }),
						new RandVector(new double[] { 0.10551427913228589, 0.2398497512431506 }),
						new RandVector(new double[] { 0.44473414332976446, 0.7520035686266464 }),
						new RandVector(new double[] { 0.22894984509417682, 0.2957674586815261 }),
						new RandVector(new double[] { 0.3755509135167587, 0.2586844541559205 })));

		ArrayList<ArrayList<RandVector>> S_ml = new ArrayList<>();
		// S_ml[0]
		S_ml.add(
				new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.27965500992690867, 0.23283541684296566 }),
						new RandVector(new double[] { 0.08756684234164319, 0.5039803730341649 }),
						new RandVector(new double[] { 0.5719495102689698, 0.0849608941191407 }),
						new RandVector(new double[] { 0.3137708398116368, 0.17915749710379258 }),
						new RandVector(new double[] { 0.10551427913228589, 0.2398497512431506 }))));
		// S_ml[1]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.49225609399115866, 0.2572643211127219 }),
				new RandVector(new double[] { 0.17054517003893793, 0.7249234428510463 }),
				new RandVector(new double[] { 0.22894984509417682, 0.2957674586815261 }),
				new RandVector(new double[] { 0.3755509135167587, 0.2586844541559205 }))));
		// S_ml[2]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.3851415055021312, 0.693608265525496 }),
				new RandVector(new double[] { 0.4843145980717754, 0.38268335706839496 }),
				new RandVector(new double[] { 0.47333117996115925, 0.5259793413529459 }),
				new RandVector(new double[] { 0.635289047793018, 0.34003660414792436 }))));
		// S_ml[3]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.5013661904440406, 0.4319727836648669 }),
				new RandVector(new double[] { 0.44473414332976446, 0.7520035686266464 }))));
		// S_ml[4]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.5851183282557083, 0.45796361383713313 }),
				new RandVector(new double[] { 0.48219268136709514, 0.922423607501337 }),
				new RandVector(new double[] { 0.5065358728586449, 0.7112674471654965 }))));
		// S_ml[5]
		S_ml.add(new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.7462466986814347, 0.8297705918181446 }))));
		// S_ml[6]
		S_ml.add(new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.7629169492421959, 0.8815765594124547 }))));

		List<Object[]> input = new ArrayList<>();
		input.add(new Object[] { 5, corrInput.clone(), S_ml });
		input.add(new Object[] { 6, corrInput.clone(), S_ml });
		input.add(new Object[] { 9, corrInput.clone(), S_ml });
		input.add(new Object[] { 10, corrInput.clone(), S_ml });
		input.add(new Object[] { 13, corrInput.clone(), S_ml });
		input.add(new Object[] { 14, corrInput.clone(), S_ml });
		input.add(new Object[] { 15, corrInput.clone(), S_ml });
		input.add(new Object[] { 16, corrInput.clone(), S_ml });
		input.add(new Object[] { 18, corrInput.clone(), S_ml });
		input.add(new Object[] { 19, corrInput.clone(), S_ml });
		input.add(new Object[] { 20, corrInput.clone(), S_ml });

		return input;
	}

	private static Collection<Object[]> antiData() {

		ArrayList<RandVector> antiInput = new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.9546467066987754, 0.014280836562333477 }),
						new RandVector(new double[] { 0.44598849816593966, 0.4013207026202956 }),
						new RandVector(new double[] { 0.9661525356962756, 0.13081494999096288 }),
						new RandVector(new double[] { 0.4797546364342542, 0.6611110804274059 }),
						new RandVector(new double[] { 0.7064390056067597, 0.25432287914371376 }),
						new RandVector(new double[] { 0.3152160664236874, 0.7162843860094275 }),
						new RandVector(new double[] { 0.48021963782936083, 0.6226031186454807 }),
						new RandVector(new double[] { 0.5605638193528972, 0.6758156569715212 }),
						new RandVector(new double[] { 0.5124389237655047, 0.42604267452333866 }),
						new RandVector(new double[] { 0.6347268930063603, 0.4104640779256197 }),
						new RandVector(new double[] { 0.7089857896866181, 0.22610309659306171 }),
						new RandVector(new double[] { 0.3941540169734077, 0.7017854459566435 }),
						new RandVector(new double[] { 0.3868092447196132, 0.715390597201482 }),
						new RandVector(new double[] { 0.951093823304141, 0.005775067052891436 }),
						new RandVector(new double[] { 0.7086709033929468, 0.35641894266571805 }),
						new RandVector(new double[] { 0.24246847278357309, 0.6391652085811466 }),
						new RandVector(new double[] { 0.6363740484871241, 0.3838361325493187 }),
						new RandVector(new double[] { 0.46831686902084746, 0.5428608082423603 }),
						new RandVector(new double[] { 0.2626309427920057, 0.7351039129314285 }),
						new RandVector(new double[] { 0.387372931978208, 0.5917940599425093 })));

		ArrayList<ArrayList<RandVector>> S_ml = new ArrayList<>();
		// S_ml[0]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.44598849816593966, 0.4013207026202956 }),
				new RandVector(new double[] { 0.7064390056067597, 0.25432287914371376 }),
				new RandVector(new double[] { 0.7089857896866181, 0.22610309659306171 }),
				new RandVector(new double[] { 0.951093823304141, 0.005775067052891436 }),
				new RandVector(new double[] { 0.24246847278357309, 0.6391652085811466 }),
				new RandVector(new double[] { 0.6363740484871241, 0.3838361325493187 }),
				new RandVector(new double[] { 0.387372931978208, 0.5917940599425093 }))));
		// S_ml[1]
		S_ml.add(
				new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.9546467066987754, 0.014280836562333477 }),
						new RandVector(new double[] { 0.2626309427920057, 0.7351039129314285 }),
						new RandVector(new double[] { 0.5124389237655047, 0.42604267452333866 }),
						new RandVector(new double[] { 0.6347268930063603, 0.4104640779256197 }),
						new RandVector(new double[] { 0.7086709033929468, 0.35641894266571805 }),
						new RandVector(new double[] { 0.46831686902084746, 0.5428608082423603 }),
						new RandVector(new double[] { 0.3152160664236874, 0.7162843860094275 }),
						new RandVector(new double[] { 0.3868092447196132, 0.715390597201482 }),
						new RandVector(new double[] { 0.3941540169734077, 0.7017854459566435 }))));
		// S_ml[2]
		S_ml.add(new ArrayList<>(Arrays.asList(new RandVector(new double[] { 0.9661525356962756, 0.13081494999096288 }),
				new RandVector(new double[] { 0.4797546364342542, 0.6611110804274059 }),
				new RandVector(new double[] { 0.48021963782936083, 0.6226031186454807 }))));
		// S_ml[3]
		S_ml.add(new ArrayList<>(
				Arrays.asList(new RandVector(new double[] { 0.5605638193528972, 0.6758156569715212 }))));

		List<Object[]> input = new ArrayList<>();
		input.add(new Object[] { 6, antiInput.clone(), S_ml });
		input.add(new Object[] { 7, antiInput.clone(), S_ml });
		input.add(new Object[] { 8, antiInput.clone(), S_ml });
		input.add(new Object[] { 16, antiInput.clone(), S_ml });
		input.add(new Object[] { 17, antiInput.clone(), S_ml });
		input.add(new Object[] { 19, antiInput.clone(), S_ml });
		input.add(new Object[] { 20, antiInput.clone(), S_ml });

		return input;
	}
}
