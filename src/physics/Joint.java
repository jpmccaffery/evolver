package physics;

public class Joint implements AbstractJoint, CompositeObject
{
	public Joint ()
	{
		m_joint = new AetherJoint ();
	}

	public Joint (AbstractJoint joint_)
	{
		m_joint = joint_;
	}

	public Vector3f angularSpeed ()
	{
		return m_joint.angularSpeed ();
	}

	public void setAngularSpeed (Vector3f speed_)
	{
		m_joint.setAngularSpeed (speed_);
	}

	public Limb leftJoin ()
	{
		return m_joint.leftJoin ();
	}

	public Limb rightJoin ()
	{
		return m_joint.rightJoin ();
	}

	public void unregister ()
	{
		m_joint.unregister ();
	}

	public Vector3f position ()
	{
		return m_joint.position ();
	}

	public void registerWithBullet (DummyBulletSpace space_)
	{
		m_joint.unregister ();
		m_joint = new BulletJoint (m_joint);
		m_joint.registerWithBullet (space_);
	}

	public void registerWithJMonkey (PhysicsSpace space_, Node rootNode_)
	{
		m_joint.unregister ();
		m_joint = new MonkeyJoint (m_joint);
		m_joint.registerWithJMonkey (space_, node_);
	}

	private AbstractJoint m_joint;
}
