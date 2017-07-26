package critter;

import network.Network;
import network.NetworkBuilder;

import java.io.File;


public class XMLBrainVat extends BrainVat
{
	public XMLBrainVat (String fileName_, Class type_)
	{
		super (type_);

		m_fileName = fileName_;
	}

	public XMLBrainVat (String fileName_)
	{
		this (fileName_, StraightBrain.class);
	}

	public Network getNetwork (int numInputs_, int numOutputs_)
	{
		File file = new File (m_fileName);
		NetworkBuilder nnBuilder = new NetworkBuilder ();
		Network nn = nnBuilder.buildNetworkFromFile (file);

		nn.setWinnerTakeAll (true);

		return nn;
	}

	private final String m_fileName;
}
