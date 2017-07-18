package mygame;

import com.jme3.bullet.joints.motors.TranslationalLimitMotor;
import com.jme3.app.SimpleApplication;
import com.jme3.asset.TextureKey;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.joints.ConeJoint;
import com.jme3.bullet.joints.HingeJoint;
import com.jme3.bullet.joints.PhysicsJoint;
import com.jme3.bullet.joints.SixDofJoint;
import com.jme3.bullet.joints.motors.RotationalLimitMotor;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Matrix3f;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import com.jme3.shadow.BasicShadowRenderer;
import com.jme3.shadow.DirectionalLightShadowRenderer;
import com.jme3.system.JmeContext;
import com.jme3.texture.Texture;
import com.jme3.util.TangentBinormalGenerator;
import java.io.BufferedWriter;
import network.Network;
import network.NetworkBuilder;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author normenhansen
 */
public class CreaturesMain extends SimpleApplication implements ActionListener {

	private BulletAppState bulletAppState = new BulletAppState();
	private Node agent = new Node();
	private Node shoulders;
	private Vector3f upforce = new Vector3f(0, 200, 0);
	private boolean applyForce = false;
	//private HingeJoint leftShoulderJoint;
	private SixDofJoint lShoulderJoint;
	private SixDofJoint rShoulderJoint;
	//private TranslationalLimitMotor motor1;
	protected RotationalLimitMotor pitchMotor1;
	protected RotationalLimitMotor yawMotor1;
	protected RotationalLimitMotor rollMotor1;
	protected float desiredPitchVelocity1 = 0;
	protected float desiredYawVelocity1 = 0;
	protected float desiredRollVelocity1 = 0;
	protected RotationalLimitMotor pitchMotor2;
	protected RotationalLimitMotor yawMotor2;
	protected RotationalLimitMotor rollMotor2;
	protected float desiredPitchVelocity2 = 0;
	protected float desiredYawVelocity2 = 0;
	protected float desiredRollVelocity2 = 0;
	private Network nn;
	private double[] singleLineNetworkInput;
	private int popSize = 20;
	private int genCounter = 1;
	private int numGens = 100;
	private int numEvalSteps = 15000;
	//Either in evolve mode or evaluate mode
	//If in evaluate mode, a network id to be evaluated needs to be specified
	private boolean evolveMode = true;
	private int networkIdToEval = 1;

	private int runningNetworkId = 1;
	private int evalCounter = 0;
	private HashMap<Integer,Integer> fitnessVals = new HashMap<Integer,Integer>();
	private TreeMap<Integer,Integer> sortedFitnessVals;
	private Integer[] mostFit;
	private Integer[] leastFit;
	private double survivalPercentage = 0.30;
	private Vector3f startLoc;

	public static void main(String[] args) {
		CreaturesMain app = new CreaturesMain();
		app.setPauseOnLostFocus(false);
		app.start();
		//This alternate run line runs the code without graphics
		//app.start(JmeContext.Type.Headless);
	}

	@Override
	public void simpleInitApp() {
		Logger.getLogger("").setLevel(Level.SEVERE);
		setUpLight();
		setupKeys();
		flyCam.setMoveSpeed(10);
		bulletAppState = new BulletAppState();
		stateManager.attach(bulletAppState);
		//Makes wireframe visible
//		bulletAppState.getPhysicsSpace().enableDebug(assetManager);

		PhysicsTestHelper.createPhysicsTestWorld(rootNode, assetManager, bulletAppState.getPhysicsSpace());
		createRagDoll();

		//Create initial population of neural network controllers
		NetworkCreator nc = new NetworkCreator();


		if (evolveMode) {
			for (int i = 0; i < popSize; i++) {
				String networkXMLString = nc.createNetwork(runningNetworkId, 7, 12);
				try {
					File saveFile = new File("/home/jack/projects/evodevo/Creatures/networks/network" + runningNetworkId + ".xml");
					BufferedWriter out = new BufferedWriter(new FileWriter(saveFile));
					out.write(networkXMLString);
					out.close();
				} catch (IOException exception) {
					System.out.println (exception.getMessage ());
					System.exit (0);
				}
				runningNetworkId++;
			}
			runningNetworkId = 1;
		} else {
			runningNetworkId = networkIdToEval;
		}

		loadNetwork(runningNetworkId);

		singleLineNetworkInput = new double[7];
		singleLineNetworkInput[6] = 1.0f;
		System.out.println("generation: " + genCounter);
		System.out.println("evaluating network: " + nn.getId());
	}

	private void loadNetwork(int networkID) {
//		try {
			File file = new File("/home/jack/projects/evodevo/Creatures/networks/network" + networkID + ".xml");
			NetworkBuilder networkBuilder = new NetworkBuilder();
			nn = networkBuilder.buildNetworkFromFile( file );
			nn.setWinnerTakeAll( true );
//		} catch (IOException exception) {
//			System.out.println (exception.getMessage ());
//			System.exit (0);
//		}
	}


	private void setUpLight() {
		viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
		// We add light so we see the scene
		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.Gray);
		rootNode.addLight(al);

		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(1, -2.5f, -2).normalizeLocal());
		sun.setColor(ColorRGBA.Gray.mult(1.7f));
		rootNode.addLight(sun);
	}

	private void createRagDoll() {
		shoulders = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 1.5f, 0), true, false);
		Node uArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, 0.8f, 0), false, false);
		Node uArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, 0.8f, 0), false, false);
		//Node lArmL = createLimb(0.2f, 0.5f, new Vector3f(-0.75f, -0.2f, 0), false, false);
		//Node lArmR = createLimb(0.2f, 0.5f, new Vector3f(0.75f, -0.2f, 0), false, false);
		//Node body = createLimb(0.2f, 1.0f, new Vector3f(0.00f, 0.5f, 0), false, true);
		//Node hips = createLimb(0.2f, 0.5f, new Vector3f(0.00f, -0.5f, 0), true, false);
		//Node uLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -1.2f, 0), false, false);
		//Node uLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -1.2f, 0), false, false);
		//Node lLegL = createLimb(0.2f, 0.5f, new Vector3f(-0.25f, -2.2f, 0), false, false);
		//Node lLegR = createLimb(0.2f, 0.5f, new Vector3f(0.25f, -2.2f, 0), false, false);

		//join(body, shoulders, new Vector3f(0f, 1.4f, 0));
		//join(body, hips, new Vector3f(0f, -0.5f, 0));

		lShoulderJoint = join(uArmL, shoulders, new Vector3f(-0.75f, 1.4f, 0));
		rShoulderJoint = join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, 0));

		//motor1 = leftShoulderJoint.getTranslationalLimitMotor();
		pitchMotor1 = lShoulderJoint.getRotationalLimitMotor(0);
		yawMotor1 = lShoulderJoint.getRotationalLimitMotor(1);
		rollMotor1 = lShoulderJoint.getRotationalLimitMotor(2);
		pitchMotor1.setEnableMotor(true);
		yawMotor1.setEnableMotor(true);
		rollMotor1.setEnableMotor(true);
		pitchMotor1.setMaxMotorForce(1);
		yawMotor1.setMaxMotorForce(1);
		rollMotor1.setMaxMotorForce(1);

		pitchMotor2 = rShoulderJoint.getRotationalLimitMotor(0);
		yawMotor2 = rShoulderJoint.getRotationalLimitMotor(1);
		rollMotor2 = rShoulderJoint.getRotationalLimitMotor(2);
		pitchMotor2.setEnableMotor(true);
		yawMotor2.setEnableMotor(true);
		rollMotor2.setEnableMotor(true);
		pitchMotor2.setMaxMotorForce(1);
		yawMotor2.setMaxMotorForce(1);
		rollMotor2.setMaxMotorForce(1);


		join(uArmR, shoulders, new Vector3f(0.75f, 1.4f, 0));
		//join(uArmL, lArmL, new Vector3f(-0.75f, .4f, 0));
		//join(uArmR, lArmR, new Vector3f(0.75f, .4f, 0));

		//join(uLegL, hips, new Vector3f(-.25f, -0.5f, 0));
		//join(uLegR, hips, new Vector3f(.25f, -0.5f, 0));
		//join(uLegL, lLegL, new Vector3f(-.25f, -1.7f, 0));
		//join(uLegR, lLegR, new Vector3f(.25f, -1.7f, 0));

		agent.attachChild(shoulders);
		//ragDoll.attachChild(body);
		//ragDoll.attachChild(hips);
		agent.attachChild(uArmL);
		agent.attachChild(uArmR);
		//ragDoll.attachChild(lArmL);
		//ragDoll.attachChild(lArmR);
		//ragDoll.attachChild(uLegL);
		//ragDoll.attachChild(uLegR);
		//ragDoll.attachChild(lLegL);
		//ragDoll.attachChild(lLegR);

		rootNode.attachChild(agent);
		bulletAppState.getPhysicsSpace().addAll(agent);
		startLoc = agent.getLocalTranslation();

	}

	private Node createLimb(float width, float height, Vector3f location, boolean rotate, boolean kinematic) {
		int axis = rotate ? PhysicsSpace.AXIS_X : PhysicsSpace.AXIS_Y;
		CapsuleCollisionShape shape = new CapsuleCollisionShape(width, height, axis);

		Node node = new Node("Limb");
		RigidBodyControl rigidBodyControl = new RigidBodyControl(shape, 1);
		rigidBodyControl.setFriction(0.05f);

		if (kinematic) {
			//rigidBodyControl.setKinematic(true);
		}

		//rigidBodyControl.setAngularSleepingThreshold(0);
		//rigidBodyControl.setGravity(Vector3f.ZERO);

		node.setLocalTranslation(location);
		node.addControl(rigidBodyControl);

		node.setShadowMode(ShadowMode.Cast);

		Cylinder limbBox = new Cylinder(32, 32, width, height, true);
		Geometry limbGeometry = new Geometry("Limb", limbBox);


		//Geometry limbGeometry = (Geometry) assetManager.loadModel("Models/Teapot/Teapot.obj");

		Material mat2 = new Material(assetManager,  "Common/MatDefs/Light/Lighting.j3md");
		mat2.setBoolean("UseMaterialColors",true); 
		mat2.setColor("Ambient",  ColorRGBA.Red);
		mat2.setColor("Diffuse",  ColorRGBA.Red);
		mat2.setColor("Specular", ColorRGBA.White);
		mat2.setFloat("Shininess", 12);	

		limbGeometry.setMaterial(mat2);
		if (!rotate) {
			Quaternion newRotation = new Quaternion();
			newRotation.fromAngleAxis( FastMath.PI / 2 , new Vector3f(1,0,0) );
			limbGeometry.setLocalRotation(newRotation);
		}  else {
			Quaternion newRotation = new Quaternion();
			newRotation.fromAngleAxis( FastMath.PI / 2 , new Vector3f(0,1,0) );
			limbGeometry.setLocalRotation(newRotation);			 
		}
		node.attachChild(limbGeometry);

		return node;
	}


	private SixDofJoint join(Node A, Node B, Vector3f connectionPoint) {	
		Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
		Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());

		//ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
		//joint.setLimit(1f, 1f, 0);

		SixDofJoint joint=new SixDofJoint(A.getControl(RigidBodyControl.class),
					 B.getControl(RigidBodyControl.class),
					 pivotA, 
					 pivotB, 
					 true  );	  
		return joint;
	}

	/*
	private HingeJoint join(Node A, Node B, Vector3f connectionPoint) {

		Vector3f pivotA = A.worldToLocal(connectionPoint, new Vector3f());
		Vector3f pivotB = B.worldToLocal(connectionPoint, new Vector3f());

		//ConeJoint joint = new ConeJoint(A.getControl(RigidBodyControl.class), B.getControl(RigidBodyControl.class), pivotA, pivotB);
		//joint.setLimit(1f, 1f, 0);

		HingeJoint joint=new HingeJoint(A.getControl(RigidBodyControl.class),
					 B.getControl(RigidBodyControl.class),
					 pivotA,  // pivot point local to A
					 pivotB,  // pivot point local to B
					 Vector3f.UNIT_Z,		   // DoF Axis of A (Z axis)
					 Vector3f.UNIT_Z  );		// DoF Axis of B (Z axis)
		return joint;
	}

*/
	private void setupKeys() {
		inputManager.addMapping("PitchPlus", new KeyTrigger(KeyInput.KEY_I));
		inputManager.addMapping("PitchMinus", new KeyTrigger(KeyInput.KEY_K));
		inputManager.addMapping("YawPlus", new KeyTrigger(KeyInput.KEY_O));
		inputManager.addMapping("YawMinus", new KeyTrigger(KeyInput.KEY_U));
		inputManager.addMapping("RollPlus", new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping("RollMinus", new KeyTrigger(KeyInput.KEY_J));
		inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_R));
		inputManager.addMapping("toggleEnableMotors", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "RollMinus", "RollPlus", "PitchMinus", "PitchPlus", "YawMinus", "YawPlus", "Reset", "toggleEnableMotors");
	}

   @Override
	public void onAction(String name, boolean isPressed, float tpf) {
		if (name.equals("PitchPlus") && isPressed) {
			//desiredPitchVelocity1 += 1;
		   //System.out.println("PITCH +1: " + desiredPitchVelocity1);
		   //leftShoulderJoint.enableMotor(true, 1, .1f);
		}
		if (name.equals("PitchMinus") && isPressed) {
			//desiredPitchVelocity1 -= 1;
			//System.out.println("PITCH -1: " + desiredPitchVelocity1);
			//leftShoulderJoint.enableMotor(true, -1, .1f);
		}
		if (name.equals("YawPlus") && isPressed) {
			//desiredYawVelocity1 += 1;
			//System.out.println("YAW +1");
			//leftShoulderJoint.enableMotor(false, 0.0f, 0.1f);
		}
		if (name.equals("YawMinus") && isPressed) {
			//desiredYawVelocity1 -= 1;
			//System.out.println("YAW -1");
		}
		if (name.equals("RollPlus") && isPressed) {
			//desiredRollVelocity1 += 1;
			//System.out.println("ROLL +1");
		}
		if (name.equals("RollMinus") && isPressed) {
			//desiredRollVelocity1 -= 1;
			//System.out.println("ROLL -1");
		}
		if (name.equals("Reset") && isPressed) {
			desiredPitchVelocity1 = 0;
			desiredYawVelocity1 = 0;
			desiredRollVelocity1 = 0;
			System.out.println("RESET");
			//leftShoulderJoint.getBodyB().clearForces();
			//leftShoulderJoint.getBodyB().setPhysicsLocation(Vector3f.UNIT_Y);
			//leftShoulderJoint.getBodyB().setPhysicsRotation(new Quaternion());
		}
	}

	@Override
	public void simpleUpdate(float tpf) {

		if (genCounter > numGens) {
			return;
		}

		if (evalCounter > numEvalSteps) {
			//calculate fitness
			int tempFitness = 0;
			Vector3f endLoc = shoulders.getLocalTranslation();
			float distanceTraveled = startLoc.distance(endLoc);
			distanceTraveled = Math.round(distanceTraveled*100);
			tempFitness = (int) distanceTraveled;			 
			fitnessVals.put(runningNetworkId, tempFitness);
			evalCounter = 0;
			runningNetworkId++;
			if (runningNetworkId > popSize) {
				System.out.println("unsorted map: "+fitnessVals);
				FitnessComparator fc = new FitnessComparator(fitnessVals);
				sortedFitnessVals = new TreeMap<Integer,Integer>(fc);
				sortedFitnessVals.putAll(fitnessVals);
				System.out.println("sorted map: "+sortedFitnessVals);
				System.out.println(sortedFitnessVals.firstKey());
				Set sortedKeys = sortedFitnessVals.keySet();
				System.out.println(sortedKeys);
				int numSurviving = (int) (popSize * survivalPercentage);
				int numDying = popSize - numSurviving;
				Object[] sortedArray = sortedKeys.toArray();
				mostFit = Arrays.copyOfRange(sortedArray, 0, numSurviving, Integer[].class);
				leastFit = Arrays.copyOfRange(sortedArray, numSurviving, sortedArray.length, Integer[].class);
				for (int i = 0; i < mostFit.length; i++) {
					//System.out.println("mostFit: " + mostFit[i]);			
				}
				for (int i = 0; i < leastFit.length; i++) {
					//System.out.println("leastFit: " + leastFit[i]);					  
				}

				Repopulator rp = new Repopulator();
				rp.repopulate(mostFit, leastFit);
				fitnessVals.clear();		
				runningNetworkId = 1;
				genCounter++;
				if (genCounter > numGens) {
					System.out.println("Finished!");			
				} else {
					System.out.println("generation: " + genCounter); 
					System.out.println("evaluating network: " + runningNetworkId);
				}
			} else {
				loadNetwork(runningNetworkId);
				System.out.println("evaluating network: " + runningNetworkId);
				bulletAppState.getPhysicsSpace().removeAll(agent.getChild(0));
				bulletAppState.getPhysicsSpace().removeAll(agent.getChild(1));
				bulletAppState.getPhysicsSpace().removeAll(agent.getChild(2));
				agent.removeFromParent();
				System.gc();
				//create the next agent
				agent = new Node();
				createRagDoll();
			}
		} else {
			nn.activate( singleLineNetworkInput );
			singleLineNetworkInput[0] = desiredYawVelocity1;
			singleLineNetworkInput[1] = desiredRollVelocity1;
			singleLineNetworkInput[2] = desiredPitchVelocity1;
			singleLineNetworkInput[3] = desiredYawVelocity2;
			singleLineNetworkInput[4] = desiredRollVelocity2;
			singleLineNetworkInput[5] = desiredPitchVelocity2;
			singleLineNetworkInput[6] = 1.0f;

			//Set desired velocities as difference of two output neurons, using a push/pull representation
			desiredYawVelocity1 = (float) nn.getNeuronByID(7).getMembranePotential() - (float) nn.getNeuronByID(8).getMembranePotential();
			desiredRollVelocity1 = (float) nn.getNeuronByID(9).getMembranePotential() - (float) nn.getNeuronByID(10).getMembranePotential();
			desiredPitchVelocity1 = (float) nn.getNeuronByID(11).getMembranePotential() - (float) nn.getNeuronByID(12).getMembranePotential();
			desiredYawVelocity2 = (float) nn.getNeuronByID(13).getMembranePotential() - (float) nn.getNeuronByID(14).getMembranePotential();
			desiredRollVelocity2 = (float) nn.getNeuronByID(15).getMembranePotential() - (float) nn.getNeuronByID(16).getMembranePotential();
			desiredPitchVelocity2 = (float) nn.getNeuronByID(17).getMembranePotential() - (float) nn.getNeuronByID(18).getMembranePotential();

			yawMotor1.setTargetVelocity(desiredYawVelocity1);
			rollMotor1.setTargetVelocity(desiredRollVelocity1);
			pitchMotor1.setTargetVelocity(desiredPitchVelocity1);
			yawMotor2.setTargetVelocity(desiredYawVelocity2);
			rollMotor2.setTargetVelocity(desiredRollVelocity2);
			pitchMotor2.setTargetVelocity(desiredPitchVelocity2);
			evalCounter++;
		}
	/*
	if (genCounter < numGens) {

		if (evalCounter > numEvalSteps) {
			//Evaluate fitness here

			int tempFitness = 0;
			runningNetworkId++;		

			//Assign random fitness values
			//Random rand = new Random();
			//tempFitness = rand.nextInt(5000);

			Vector3f endLoc = shoulders.getLocalTranslation();
			//System.out.println("startLoc: " + startLoc);
			//System.out.println("endLoc: " + endLoc);
			float distanceTraveled = startLoc.distance(endLoc);

			distanceTraveled = Math.round(distanceTraveled*100);
			tempFitness = (int) distanceTraveled;
			//System.out.println("distanceTraveled: " + distanceTraveled);
						  
			fitnessVals.put(runningNetworkId, tempFitness);
			evalCounter = 0;

			if (runningNetworkId < popSize+1) {
				loadNetwork(runningNetworkId);
				System.out.println("evaluating network: " + runningNetworkId);
			} else {
				System.out.println("unsorted map: "+fitnessVals);
				FitnessComparator fc = new FitnessComparator(fitnessVals);
				sortedFitnessVals = new TreeMap<Integer,Integer>(fc);
				sortedFitnessVals.putAll(fitnessVals);
				System.out.println("sorted map: "+sortedFitnessVals);
				System.out.println(sortedFitnessVals.firstKey());
				Set sortedKeys = sortedFitnessVals.keySet();
				System.out.println(sortedKeys);
				int numSurviving = (int) (popSize * survivalPercentage);
				int numDying = popSize - numSurviving;
				Object[] sortedArray = sortedKeys.toArray();
				mostFit = Arrays.copyOfRange(sortedArray, 0, numSurviving, Integer[].class);
				leastFit = Arrays.copyOfRange(sortedArray, numSurviving, sortedArray.length, Integer[].class);
				for (int i = 0; i < mostFit.length; i++) {
					//System.out.println("mostFit: " + mostFit[i]);			
				}
				for (int i = 0; i < leastFit.length; i++) {
					//System.out.println("leastFit: " + leastFit[i]);					  
				}
	
				Repopulator rp = new Repopulator();
				rp.repopulate(mostFit, leastFit);
	
				runningNetworkId = 1;
				genCounter++;
				System.out.println("generation: " + genCounter);
				fitnessVals.clear();
			}
			//remove current agent from the physics space
			bulletAppState.getPhysicsSpace().removeAll(agent.getChild(0));
			bulletAppState.getPhysicsSpace().removeAll(agent.getChild(1));
			bulletAppState.getPhysicsSpace().removeAll(agent.getChild(2));
			agent.removeFromParent();
			System.gc();
			//create the next agent
			agent = new Node();
			createRagDoll();
		}
	}
*/
	}

	class FitnessComparator implements Comparator<Integer> {

		Map<Integer, Integer> base;
		public FitnessComparator(Map<Integer, Integer> base) {
			this.base = base;
		}

		// Note: this comparator imposes orderings that are inconsistent with equals.
		public int compare(Integer a, Integer b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}

	}
}
