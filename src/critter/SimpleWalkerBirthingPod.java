package critter;

import physics.Joint;

import java.util.List;
import java.util.ArrayList;


public class SimpleWalkerBirthingPod extends WalkerBirthingPod
{
	public SimpleWalkerBirthingPod (BrainVat vat_)
	{
		super (vat_, new SimpleBody ());
	}
}
