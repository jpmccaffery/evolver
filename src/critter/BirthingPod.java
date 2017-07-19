package critter;

public class BirthingPod
{
	public Critter birth ()
	{
		_birth ();

		Brain newBrain = m_brainVat.grow (inputSize (), outputSize ());

		return new Critter (newBrain, sensors (), actuators (),
				    body ());
	}

	protected BirthingPod (BrainVat brainVat_)
	{
		m_brainVat = brainVat_;
	}

	private int inputSize ()
	{
		int total = 0;

		for (Sensor s : sensors ())
			total += s.size ();

		return total;
	}

	private int outputSize ()
	{
		int total = 0;

		for (Actuator s : actuators ())
			total += s.size ();

		return total;
	}

	protected virtual void _birth ();

	protected virtual List<Sensor> sensors ();
	protected virtual List<Actuator> actuators ();
	protected virtual Body body ();
}
