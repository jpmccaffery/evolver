/*
 * Copyright (C) 2007 Derek James
 *
 * This file is part of SIPHON (Simulating the Phylogeny and Ontogeny of the Neocortex).
 *
 * SIPHON is free software; you can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program; if
 * not, write to the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
 * 02111-1307 USA
 *
 * created by Derek James on November 4th, 2007
 */
package network;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Iterator;

/**
 * Neuron component of an artificial neural network.
 * A leaky integrator neuron integrates input over time, stored
 * as a membrane potential which leaks. Its output is the same as its
 * current state.
 *
 * @author Derek James
 */

public class LeakyIntegratorNeuron implements Neuron {

private long id; //unique id for each neuron
private int layerId; //layer within a column (1-6)
private long columnId; //id of the column the neuron is in
private long sheetId; //id of the sheet the neuron is in
private String type; //input, output, hidden, or delay
private long parentId = 0; //if delay unit, ID of parent neuron
private int delay = 0; //number of time steps for delay neurons

private float xLoc;
private float yLoc;
private float zLoc;

//Neurons follow Dale's Principle. All connections from a given neuron
//are either excitatory (weight > 0) or inhibitory (weight < 0)
private boolean excitatory = true;

private static double membranePotential = 0.0d;
private double newMembranePotential;
private double synapticOutput = 0.0d;
private List<Double> previousMembranePotentials = new ArrayList<Double> ();
private List<Double> previousActivationLevels = new ArrayList<Double> ();

//value above which the neuron spikes and the membrane potential resets to zero
private static double threshold = 0.632;
private int exceededThreshold = 0;
private int justFired = 0;
private List<Double> previousFiringTimes = new ArrayList<Double> ();
private int inhibitedDuration = 100;
private int stepsUntilNotInhibited = 0;
private double currentTime = 0.0;
private double input = 0.0;
private double deltaT = 0.01;
private double timeConstant = 10.0;
private boolean activeDuringLearningWindow = false;
private double recentLTPMax = 0.0;
private double neighborActivation = 0.0;
private double exp = 2.0;
private double max = 1.0;
private double midpointToMax = 0.5;
private double inhibitionWeight = 5.0;


private List<Connection> afferentConnections =
	new ArrayList<Connection> (); //inbound connections
private List<Connection> efferentConnections =
	new ArrayList<Connection> (); //outbound connections

public List<Connection> getAfferentConnections ()
{
	return afferentConnections;
}

//update list of afferent (inbound) connections
public void updateAfferentConnections(List<Connection> connections)
{
	Iterator<Connection> iter = connections.iterator();

	while (iter.hasNext ())
	{
		Connection conn = iter.next ();

		if (afferentConnections.contains (conn))
		{
			return;
		}
		else if (conn.getPostsynapticId () == id)
		{
			afferentConnections.add (conn);
		}
	}
}

//update list of efferent (outbound) connections
public void updateEfferentConnections (List<Connection> connections)
{
	Iterator<Connection> iter = connections.iterator();

	while (iter.hasNext ())
	{
		Connection conn = iter.next();

		if (efferentConnections.contains (conn))
		{
			return;
		}
		else if (conn.getPresynapticId () == id)
		{
			efferentConnections.add (conn);
		}
	}
}

public void setExcitatory(boolean newExcitatorySetting) {
	excitatory = newExcitatorySetting;
}

public void setId (long newId) {
	id = newId;
}

public void setLocation(float newX, float newY, float newZ) {
	xLoc = newX;
	yLoc = newY;
	zLoc = newZ;
}

public float[] getLocation() {
	float dims[] = new float[3];
	dims[0] = xLoc;
	dims[1] = yLoc;
	dims[2] = zLoc;
	return dims;
}

public void setType(String newType) {
	type = newType;
}

public String getType() {
	return type;
}

public double getThreshold() {
	return threshold;
}

public double getMembranePotential() {
	return newMembranePotential;
}

public void setCurrentTime(double newCurrentTime) {
	currentTime = newCurrentTime;
}

public void setMembranePotential(double membranePotentialToSet) {
	newMembranePotential = membranePotentialToSet;
}

public List<Double> getPreviousFiringTimes ()
{
	return previousFiringTimes;
}

public void setPreviousFiringTimes (List<Double> newPreviousFiringTimes)
{
	previousFiringTimes = newPreviousFiringTimes;
}

public void inhibitNeuron () {
	stepsUntilNotInhibited = inhibitedDuration;
}

public void setNeighborActivation(double newNeighborActivation) {
	neighborActivation = newNeighborActivation;
}

//This method is called to update the neuron's membrane potential. Synchronous updating
//is used, so the new membrane potential is not made permanent until all neurons have
//been updated.
public void updateMembranePotential (double newInput, double newCurrentTime) {
	currentTime = newCurrentTime;	 
	newMembranePotential = newMembranePotential + (deltaT * (-newMembranePotential - (inhibitionWeight * neighborActivation) + newInput));
	previousMembranePotentials.add(newMembranePotential);
	if (newMembranePotential > 0) {
		activeDuringLearningWindow = true;
	}
}

//This method is called after all the membrane potentials for all neurons have been
//calculated, in order to insure synchronous updating
public void synchronizeMembranePotential () {
	membranePotential = 0.0;
	membranePotential += newMembranePotential;
	updateSynapticOutput();
}

private void updateSynapticOutput() {
	if (membranePotential >= 0) {
		synapticOutput = (max * Math.pow(membranePotential, exp)) / (Math.pow(midpointToMax, exp) + Math.pow(membranePotential, exp));	
	} else {
		synapticOutput = 0;
	}
	//previousActivationLevels.add(newMembranePotential);
	previousActivationLevels.add(synapticOutput);
}

public double getSynapticOutput() {
	return synapticOutput;
}

public void setSynapticOutput(double newSynapticOutput) {
	synapticOutput = newSynapticOutput;
}

public double getPreviousMembranePotential (int delay) {
	return previousMembranePotentials.get(delay);
}


public double getPreviousActivationLevel (int delay) {
	return previousActivationLevels.get(delay);
}


public int getJustFired () {
	return justFired;
}

public int getExceededThreshold () {
	return exceededThreshold;
}

public void setJustFired(int newJustFired) {
	justFired = newJustFired;
}

public void updatePreviousFiringTimes (double timeFired) {
	//Only last ten firing times are kept in memory for use in updating
	//the synaptic activity
	if (previousFiringTimes.size() < 10) {
		previousFiringTimes.add(timeFired);
	} else {
		previousFiringTimes.remove(Collections.min(previousFiringTimes));
		previousFiringTimes.add(timeFired);
	}
}

public void removeMostRecentFiringTime() {
	if (!previousFiringTimes.isEmpty()) {
		previousFiringTimes.remove(Collections.max(previousFiringTimes));  
	}
}

public double getMostRecentFiringTime() {
	double mostRecentFiringTime = 0.0;
	if (!previousFiringTimes.isEmpty()) {
		mostRecentFiringTime = Collections.max(previousFiringTimes);		
	}
	return mostRecentFiringTime;
}

public long getId () {
	return id;
}

public boolean getExcitatory() {
	return excitatory;
}

public void reset() {
	membranePotential = 0.0;
	newMembranePotential = 0.0;
	synapticOutput = 0.0;
	previousFiringTimes.clear();
	justFired = 0;
}

//only used for delay neurons
public void setParentId (long newParentId) {
	parentId = newParentId;
}

public long getParentId () {
	return parentId;
}
public void setDelay (int newDelay) {
	delay = newDelay;
}

public int getDelay () {
	return delay;
}

public void setTimeConstant (double newTimeConstant) {
	timeConstant = newTimeConstant;
}

public void setDeltaT (double newDeltaT) {
	deltaT = newDeltaT;
}

public boolean getActiveDuringLearningWindow() {
	return activeDuringLearningWindow;
}

public void setActiveDuringLearningWindow(boolean newActiveDuringLearningWindow) {
	activeDuringLearningWindow = newActiveDuringLearningWindow;
}

public void setRecentLTPMax(double newRecentLTPMax) {
	recentLTPMax = newRecentLTPMax;
}

public double getRecentLTPMax() {
	return recentLTPMax;
}

}
