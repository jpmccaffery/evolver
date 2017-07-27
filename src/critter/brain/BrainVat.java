package critter.brain;

import network.Network;


public abstract class BrainVat
{
	protected BrainVat (Class type_)
	{
		m_type = type_;
	}

	public Brain grow (int numInputs_, int numOutputs_)
	{
		int in = 0;
		int out = 0;

		// Switch over brain types
		if (m_type == StraightBrain.class)
		{
			in = StraightBrain.nnInputs (numInputs_);
			out = StraightBrain.nnOutputs (numOutputs_);

			return new StraightBrain (getNetwork (in, out));
		}
		else if (m_type == PushPullBrain.class)
		{
			in = PushPullBrain.nnInputs (numInputs_);
			out = PushPullBrain.nnOutputs (numOutputs_);

			return new PushPullBrain (getNetwork (in, out));
		}

		System.err.println ("BrainVat::grow: Unknown brain type.");
		System.exit (0);

		return null;
	}

	protected abstract Network getNetwork (int numInputs_, int numOutputs_);

	private final Class m_type;
}
