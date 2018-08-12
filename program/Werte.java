package kdk.program;

import java.awt.Point;
import java.util.Vector;


/** Class to store the properties of a robot.
  An instance of this class will be permanently attached to a robot,
  so that some values are given on construction while others change
  at runtime. */

public final class Werte
{


/** Costs of properties by robot type. */
	static final int[][] kosten = {
/* no type */	{2, 3, 2, 2, 2},
/* parasite */	{3, 9, 1, 3, 2},
/* mine */	{2, 6, 5, 1, 2},
/* hunter */	{4, 5, 1, 4, 1},
/* breeder */	{1, 2, 5, 4, 3}};
/** Maximal points that robot types can pay. */
	static final int[] maxkosten = {50, 50, 50, 50, 50};
	static final int[] regeneration = {2,-1,1,1,3};
	static final int[][] schusskosten = {
		{1, 1, 2, 2, 2, 3, 3, 3, 4},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{0, 0, 0, 0, 0, 0, 0, 0, 0},
		{4, 2, 1, 1, 2, 2, 2, 2, 3},
		{2, 3, 4, 5, 5, 6, 6, 7, 8}};
	static final int[][] sensorkosten = {
		{0, 1, 1, 1, 2, 2, 2, 3, 3},
		{1, 1, 2, 2, 3, 3, 3, 4, 4},
		{0, 1, 1, 3, 5, 5, 5, 3, 3},
		{0, 1, 2, 3, 4, 5, 4, 3, 2},
		{1, 2, 3, 4, 5, 6, 7, 8, 9}};

	private int akt_signatur, signatur;
	private int akt_energie, maxenergie;
	private int akt_fertilitaet, fertilitaet;
	private int typ;
	private int[] verbuendete;
	private Point pos = null;
	private int akt_mobilitaet, mobilitaet;
	private int akt_ssenergie, ssenergie;
	private int visibilitaet;
	private Robot1 parasit = null;
	private Point marked = null;
	private Vector marker = new Vector();
	private int infektionsdauer = 0;

/** Creates a new instance of KdKWerte.  The parameters will be fixed
  and cannot be changed by the robot.
 * @param energie energy of the robot
 * @param signatur signature
 * @param fertilitaet number of rounds until fertile
 * @param typ type of the robot
 * @param verbuendete field of signatures of other robots. */

	public Werte(int energie, int signatur, int fertilitaet,
				int typ, int[] verbuendete)
	{
		if ((energie > 8) || (energie < 1))
			energie = 5;
		akt_energie = energie;
		maxenergie = 2*energie;
		if ((fertilitaet < 2) || (fertilitaet > 10))
			fertilitaet = 6;
		akt_fertilitaet = fertilitaet;
		this.fertilitaet = fertilitaet;
		this.signatur = signatur;
		akt_signatur = signatur;
		this.typ = typ;
		this.verbuendete = verbuendete;
	}


/** Calculates if energy of robot is within tolerance. */

	public final int validate()
	{
		int fert;
		if (fertilitaet==0)
			fert=10;
		else
			fert=10-fertilitaet;
		int[] werte = {maxenergie/2, fert,
			mobilitaet, visibilitaet,
			ssenergie};
		int result = 0;
		for (int i=0; i<werte.length; i++)
			result += werte[i]*kosten[typ][i];
		return (result - maxkosten[typ]);
	}


/** Checks if a robot is defunct or otherwise regenerates remaining movement points into energy,
  or reduces a point from the parasite. */

	void regeneriere()
	{
		if (akt_energie <= 0)
			throw new KdKException("Roboter ist Schrott");
		if (akt_fertilitaet > 0)
			akt_fertilitaet--;
		if (isInfiziert())
		{
			if (getInfektionsDauer()<5)
				infektionsdauer++;
			else
				throw new KdKException("Ausbruch des Parasiten");
		}
		if (typ == Robot1.TYP_PARASIT)
			if (akt_energie - 1 <= 0)
				throw new KdKException("Parasit verhungert");
		if (!isInfiziert())
			akt_energie = Math.min(
				maxenergie,
				akt_energie + regeneration[typ]);
		akt_ssenergie = ssenergie;
		akt_mobilitaet = mobilitaet;
	}


/** Infects this instance. */

	void infekt(Robot1 p)
	{
		parasit = p;
		infektionsdauer = 0;
	}


/** Returns the parasite. */

	Robot1 getParasit()
	{
		return parasit;
	}


/** Shows, if the instance is infected by a parasite */

	boolean isInfiziert()
	{
		return (parasit != null);
	}


/** Shows how long ago the infection happened. */

	int getInfektionsDauer()
	{
		return infektionsdauer;
	}


/** Returns the current position of the robot. */

	public Point getPosition()
	{
		return new Point(pos.x,pos.y);
	}

	public int getEnergie()
	{
		return akt_energie;
	}


/** Returns the field of allied signatures.
  A robot will be rejected, if there are alliance conflicts during initiation. */
	int[] getVerbuendete()
	{
		return verbuendete;
	}

	public int getVisibilitaet()
	{
		return visibilitaet;
	}

	public int getFertilitaet()
	{
		return akt_fertilitaet;
	}


/** The signature that the robot sends and which another robot receives upon query. */

	public int getSendSignatur()
	{
		return akt_signatur;
	}


/** The real signature which is used to determine alliances. */

	public int getRealSignatur()
	{
		return signatur;
	}
		
	public boolean isFertil()
	{
		return (akt_fertilitaet == 0);
	}

	public int getTyp()
	{
		return typ;
	}

	public int getMobilitaet()
	{
		return akt_mobilitaet;
	}

	public int getRundenEnergie()
	{
		return ssenergie;
	}

	void setPosition(Point p)
	{
		pos = new Point(p.x,p.y);
		for (int i=0; i<marker.size(); i++)	
		{
			Robot1 m = (Robot1)marker.elementAt(i);
			m.getWerte().setMarked(p);
		}
	}


/** Adds to the current energy. */

	void deltaEnergie(int wert)
	{
		akt_energie += wert;
	}


/** Subtracts from current mobility (movement points). */
	void deltaMobilitaet(int wert)
	{
		akt_mobilitaet -= wert;
	}


/** Decreases number of shots for this turn. */

	boolean schuss(int wert, int abstand)
	{
		int kosten = wert*(schusskosten[getTyp()][abstand-1]);
		if (wert>4)
			kosten += (wert-5);
		akt_ssenergie-=kosten;
		return (akt_ssenergie>=0);
	}


/** Decreases number of sensor scans for this turn. */

	boolean sensor(int wert, int abstand)
	{
		int kosten = wert*(sensorkosten[getTyp()][abstand-1]);
		if (wert>4)
			kosten += (wert-5);
		akt_ssenergie-=kosten;
		return (akt_ssenergie>=0);
	}

	void setUnFertil()
	{
		akt_fertilitaet = fertilitaet;
	}

	public void setMobilitaet(int wert)
	{
		if ((wert <= 9) && (wert >= 0))
			mobilitaet = wert;
	}

	public void setRundenEnergie(int wert)
	{
		if (wert >= 0)
			ssenergie = wert;
	}

	public void setVisibilitaet(int wert)
	{
		if ((wert <= 5) && (wert >= 0))
			visibilitaet = wert;
	}

	void setMarker(Robot1 r)
	{
		marker.add(r);
		r.getWerte().setMarked(getPosition());
	}

	void removeMarker(Robot1 r)
	{
		marker.remove(r);
	}

	void setMarked(Point p)
	{
		marked = p;
	}

	Point getMarked()
	{
		return marked;
	}

}
