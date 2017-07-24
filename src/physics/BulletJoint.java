package physics;

public class BulletJoint implements AbstractJoint, BulletObject
{
	public BulletJoint (AbstractJoint joint_)
	{
		System.out.println ("Bullet physics not supported.");
		System.exit (0);
	}

	public Vector3f angularSpeed ()
	{
		return new Vector3f (0, 0, 0);
	}

	public void setAngularSpeed (Vector3f speed_)
	{
	}

	public Limb leftJoin ()
	{
		return new Limb ();
	}

	public Limb rightJoin ()
	{
		return new Limb ();
	}

	public void unregister ()
	{
	}

	public Vector3f position ()
	{
		return new Vector3f (0, 0, 0);
	}

	public DummyNode registerWithBullet (DummyBulletSpace space_)
	{
		return new DummyNode ();
	}
}
