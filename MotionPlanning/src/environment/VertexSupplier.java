package environment;

import java.awt.geom.Point2D;
import java.util.function.Supplier;

public class VertexSupplier implements Supplier<Vertex> {
	
	Environment env;
	Label labelling;
	
	public VertexSupplier(Environment env) {
		this.env=env;

	}
	
	@Override
	public Vertex get() {
		Point2D x = env.sample();
		try {
			return new Vertex(x);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
