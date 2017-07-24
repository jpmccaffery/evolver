package physics;

import com.jme3.bullet.PhysicsSpace;

import com.jme3.scene.Node;


/// \brief Basic JMonkey physics object
interface MonkeyObject extends PhysicsObject
{
	public void registerWithJMonkey (PhysicsSpace space_, Node rootNode_);
}
