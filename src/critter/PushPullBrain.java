package critter;

import java.util.List;


public interface PushPullBrain
{
	public PushPullBrain (Network network_, int numInputs_, int numOutputs_)
	{
		m_network = network_;
		m_numInputs = numInputs;
		m_numOutputs = numOutputs;
	}

	public List<Double> readOutput (float tpf_)
	{
		List<Double> output = new ArrayList<Double> ();
		int totalNeurons = m_numInputs + 2 * m_numOutputs;

		for (int i = m_inputNeurons + 1; i < totalNeurons + 1; i += 2)
		{
			output.add (
				m_nn.getNeuronByID(i).getMembranePotential() -
				m_nn.getNeuronByID(i + 1).getMembranePotential());
		}

		return output;
	}

	public void activate (List<Double> input_, float tpf_)
	{
		if (input_.size () != m_numInputs)
		{
			System.out.println ("PushPullBrain::activate: Size mismatch");
			System.exit (0);
		}

		m_network.activate (input_);
	}

	private final Network m_network;
	private final int m_numInputs;
	private final int m_numOutputs;
}
