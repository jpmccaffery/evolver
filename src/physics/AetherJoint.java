package physics;

public class AetherJoint implements AbstractJoint, AetherObject
{
	public AetherJoint ()
	{
		m_speed = 0;
		m_position = new Vector3f (0, 0, 0);

		m_leftJoin = new Limb ();
		m_rightJoin = new Limb ();
	}

	public AetherJoint (AbstractJoint joint_)
	{
		m_speed = joint_.angularSpeed ();
		m_position = joint_.position ();

		m_leftJoin = joint_.leftJoin ();
		m_rightJoin = joint_.rightJoin ();
	}

	public Vector3f angularSpeed ();
	{
		return m_speed;
	}

	public void setAngularSpeed (Vector3f speed_)
	{
		m_speed = speed_;
	}

	public Limb leftJoin ()
	{
		return m_leftJoin;
	}

	public Limb rightJoin ()
	{
		return m_rightJoin;
	}

	public Vector3f position ()
	{
		return m_position;
	}

	///////////////////////////////////////////////////////////////////////////
	private Vector3f m_speed;
	private Vector3f m_position;

	private Limb m_leftJoin;
	private Limb m_rightJoin;
}
