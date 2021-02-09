package environment;

import java.util.function.Supplier;

import org.jgrapht.graph.DefaultEdge;

public class EdgeSupplier implements Supplier<DefaultEdge> {

	@Override
	public DefaultEdge get() {
		return new DefaultEdge();
	}

}
