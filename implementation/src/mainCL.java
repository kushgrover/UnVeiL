import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BuDDyFactory;
import settings.PlanningSettings;

import java.io.BufferedWriter;
import java.io.FileWriter;

public final class mainCL {

	private mainCL() {
	}

	public static void main(String[] args) throws Exception {
		// set parameters
		new PlanningSettings(args);
		if((boolean) PlanningSettings.get("debug")) {
			// output parameters if debug mode
			PlanningSettings.outputParameters();
		}

		// default:1 (might not work for value >1)
		int numOfRuns = (Integer) PlanningSettings.get("numberOfRuns");
		Object[][] output = new Object[numOfRuns][7];
		Object[] sum = {
				0, // num of iterations
				0, // Total sampled points
				0, // RRG size
				(float) 0, // movement length
				(float) 0, // path length
				(float) 0, // total length
				(double) 0, // total time
		};

		// one iteration for each run
		for(int i=0; i < numOfRuns; i++) {
			// Initialize Everything
			BDDFactory factory = BuDDyFactory.init(1000000, (int) PlanningSettings.get("bddFactoryCacheSize"));
			System.out.println("Starting initialization ... ");
			Planning plan = new Planning(factory);
			System.out.println("Initialization complete\n\n");

			Object[] data;
			// which algorithm to use
			if ((boolean) PlanningSettings.get("firstExplThenPlan")) {
				data = plan.firstExplThenPlan();
			} else {
				data = plan.explAndPlanTogether();
			}
			System.out.println("\n\n");
			factory.done(); // close factory

			//Output results
			String outputDir = (String) PlanningSettings.get("outputDirectory");
			BufferedWriter writer = new BufferedWriter(new FileWriter(outputDir + "output.csv", true));
			for(int j=0; j<7; j++){
				output[i][j] = data[j];
				if(j<3) {
					writer.write(data[j] + ",");
					sum[j] = (Integer) sum[j] + (Integer) data[j];
				}
				else if(j<6) {
					writer.write(data[j] + ",");
					sum[j] = (Float) sum[j] + (Float) data[j];
				}
				else {
					BufferedWriter w = new BufferedWriter(new FileWriter("temp/timeout.txt"));
					w.write(String.valueOf((int) (((Double) data[j]) / 1000000)));
					w.close();
					writer.write(((Double) data[j])/1000000 + ",");
					sum[j] = (Double) sum[j] + (Double) data[j];
				}
			}
			writer.newLine();
			writer.close();
		}
	}

}
