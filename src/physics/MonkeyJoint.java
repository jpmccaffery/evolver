package physics;

import com.jme3.scene.Node;


public class MonkeyJoint implements AbstractJoint, MonkeyObject
{
	public MonkeyJoint (AbstractJoint joint_)
	{
		m_leftJoin = joint_.leftJoin ();
		m_rightJoin = joint_.rightJoin ();

		Node leftNode = m_leftJoin.monkeyNode ();
		Node rightNode = m_rightJoin.monkeyNode ();

		Vector3f connectionPoint = joint_.connectionPoint ();

		Vector3f leftPivot = leftNode.worldToLocal (connectionPoint, new Vector3f());
		Vector3f rightPivot = rightNode.worldToLocal (connectionPoint, new Vector3f());

		m_joint = new SixDofJoint (leftNode.getControl (RigidBodyControl.class),
		                           rightNode.getControl (RigidBodyControl.class),
		                           leftPivot, rightPivot, true);

		m_joint.getRotationalLimitMotor (0).setEnableMotor (true);
		m_joint.getRotationalLimitMotor (1).setEnableMotor (true);
		m_joint.getRotationalLimitMotor (2).setEnableMotor (true);

		m_joint.getRotationalLimitMotor (0).setMaxMotorForce (1);
		m_joint.getRotationalLimitMotor (1).setMaxMotorForce (1);
		m_joint.getRotationalLimitMotor (2).setMaxMotorForce (1);
	}

	public Vector3f angularSpeed ()
	{
		return new Vector3f (m_joint.getRotationalLimitMotor (0).getTargetVelocity ()
		                     m_joint.getRotationalLimitMotor (1).getTargetVelocity ()
		                     m_joint.getRotationalLimitMotor (2).getTargetVelocity ());
	}

	public void setAngularSpeed (Vector3f speed_)
	{
		m_joint.getRotationalLimitMotor (0).setTargetVelocity (speed_.getX ());
		m_joint.getRotationalLimitMotor (1).setTargetVelocity (speed_.getY ());
		m_joint.getRotationalLimitMotor (2).setTargetVelocity (speed_.getZ ());
	}

	public Limb leftJoin ()
	{
		return m_leftJoin;
	}

	public Limb rightJoin ()
	{
		return m_rightJoin;
	}

	public void unregister ()
	{
		// Nothing to do?
	}

	public Vector3f position ()
	{
		return m_joint.pivotA ();
	}

	public Node registerWithJMonkey (PhysicsSpace space_, Node rootNode_)
	{
		m_joint.unregister ();
		m_joint = new MonkeyJoint (m_joint);

		return m_joint.registerWithJMonkey (space_);
	}

	private SixDofJoint m_joint;
	private Limb m_leftJoin;
	private Limb m_rightJoin;
}
