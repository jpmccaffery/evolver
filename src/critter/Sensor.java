package critter;

import java.util.List;


public interface Sensor
{
	public List<Double> read (float tpf_);
	public int size ();
}
