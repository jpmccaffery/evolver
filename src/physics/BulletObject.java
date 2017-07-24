package physics;


/// \brief Basic Bullet physics object
interface BulletObject extends PhysicsObject
{
	public void registerWithBullet (DummyBulletSpace space_);
}
