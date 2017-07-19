package critter;

import java.util.List;


public interface Actuator
{
	public void act (List<Double> input_, float tpf_);
	public int size ();
}
