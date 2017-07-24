package physics;

public interface AbstractJoint
{
	public Vector3f angularSpeed ();
	public void setAngularSpeed (Vector3f speed_);

	public Limb leftJoin ();
	public Limb rightJoin ();
}
