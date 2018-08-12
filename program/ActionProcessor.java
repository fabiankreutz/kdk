package kdk.program;

import java.awt.Point;
import java.util.Vector;

/** The class that executes the actions.<br />
  Each robot receives an instance of this class in its turn routine, and can
  influence the game through it.<br />
  After the end of the turn this class will become unusable. */

public class ActionProcessor
{

	static final int AKTION_SCHUSS = 310;
	static final int AKTION_SENSOR = 311;
	static final int AKTION_EXPLOD = 312;
	static final int AKTION_PING   = 313;
	static final int AKTION_KOMM   = 314;

	private KdKFeld kf;
	private Robot1 r;
	private Werte w;
	private boolean inaktion = false;


/** Creates a new instance of the ActionProcessor */

	public ActionProcessor(KdKFeld kf, Robot1 r)
	{
		this.kf = kf;
		this.r = r;
		this.w = r.getWerte();
	}


/** Shoots into a target field. Return value is the success of the action.
 The shot is considered a success, if there is enough energy available
 considering the distance and intensity. */

	public boolean processSchuss(Point ziel, int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if ((w.getTyp() == Robot1.TYP_PARASIT) ||
		    (w.getTyp() == Robot1.TYP_MINE))
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		int p = r.getAbstand(ziel);
		if (p>9)
			 return false;
		if ((!w.schuss(ensitaet,p)) || (r2 == null))
			return false;
		Werte w2 = r2.getWerte();
		w2.deltaEnergie(-ensitaet);
		if (w2.getEnergie() <= 0)
			kf.removeRobot(r2);
		else
			r2.addLog(AKTION_SCHUSS,w.getPosition(),ensitaet);
		return true;
	}


/** Executes a sensor scan. The return value is success if a robot has been seen
  on the target field. A failure to see it can be due to insufficient enery (for
  distance and intensity), insufficient intensity to pierce the cloaking field or
  simply an actually empty target field. */

	public boolean processSensor(Point ziel, int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		int p = r.getAbstand(ziel);
		if (p>9)
			 return false;
		if (p==0)
			return true;
		int kosten = (ensitaet*w.sensorkosten[w.getTyp()][p-1]);
		if ((!w.sensor(ensitaet,p)) || (r2 == null))
			return false;
		if (p>1)
			r2.addLog(AKTION_SENSOR,w.getPosition(),ensitaet);
		if (r2.getWerte().getVisibilitaet()<ensitaet)
			return true;
		else
			return false;
	}


/** Creates a new copy of the robot.  Success depends on the fertility and the
  content of the target field. A parasite can procreated into an occupied
  field.  It will eat the other robot immediately.  If the parasite is infertile,
  an infection of the enemy robot happens. */

	public boolean processVermehren(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (r.getAbstand(ziel)>1)
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (w.getTyp() == Robot1.TYP_PARASIT)
		{
			if (w.isFertil())
			{
				int frass = 0;
				if (r2 != null)
				{
					kf.removeRobot(r2);
					frass = r2.getWerte().getEnergie();
				}
				else
					w.setUnFertil();
				inaktion = true;
				kf.addRobot(ziel.x, ziel.y, r.instantiiere());
				inaktion = false;
				kf.getFeld(ziel.x, ziel.y).getWerte().deltaEnergie(frass);
				return true;
			}
			else
				if (r2 == null)
					return false;
				else
				{
					inaktion = true;
					r2.getWerte().infekt(r.instantiiere());
					inaktion = false;
					return true;
				}
		}
		if (!w.isFertil())
			return false;
		if (r2 != null)
		{
			if (r2.getWerte().getTyp() == Robot1.TYP_PARASIT)
			{
				w.setUnFertil();
				r2.getWerte().deltaEnergie(2);
			}
			return false;
		}
		inaktion = true;
		kf.addRobot(ziel.x, ziel.y, r.instantiiere());
		inaktion = false;
		w.setUnFertil();
		return true;
	}


/** Explodes the mine. This causes a damage of twice the remaining energy
  to the mine to each enemy roboter in a field around.  Allied robots do
  not receive damage.<br />
  This action destroys the mine, but if it is fertile at the moment of
  explosion, a new mine is created in the ashes. */

	public void processExplosion()
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() != Robot1.TYP_MINE)
			return;
		int schaden = w.getEnergie()*2;
		Point[] umkreis = r.getUmgebung(1);
UMKREISFOR:	for (int i=0; i<umkreis.length; i++)
		{
			Robot1 r2 = kf.getFeld(umkreis[i].x, umkreis[i].y);
			if (r2 == null)
				continue UMKREISFOR;
			if (!kf.isVerbuendet(r,r2))
			{
				Werte w2 = r2.getWerte();
				w2.deltaEnergie(-schaden);
				if (w2.getEnergie() < 1)
					kf.removeRobot(r2);
				else
					r2.addLog(AKTION_EXPLOD,w.getPosition(),schaden);
			}
		}
		Point pos = w.getPosition();
		inaktion = true;
		if (w.isFertil())
			kf.addRobot(pos.x, pos.y, r.instantiiere());
		w.deltaEnergie(w.getEnergie());
		dispose();
		throw new KdKException("Für die Rasse !");
	}


/** Moves the robot. Success depends on available movement points and the
  content of the target field.  An enemy parasite immediately eats the
  robot; any other enemy type prevents the movement. */

	public boolean processBewegung(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		int p = r.getAbstand(ziel);
		if (p > w.getMobilitaet())
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (r2 != null)
		{
			Werte w2 = r2.getWerte();
			if (w.getTyp() == Robot1.TYP_PARASIT)
			{
				w.deltaEnergie(w2.getEnergie());
				kf.removeRobot(r2);
			}
			else if (w2.getTyp() == Robot1.TYP_PARASIT)
			{
				w2.deltaEnergie(w.getEnergie());
				inaktion = true;
				w.deltaEnergie(-w.getEnergie());
				dispose();
				throw new KdKException("In den Schlund");
			}
			else
				return false;
		}
		w.deltaMobilitaet(p);
		kf.move(w.getPosition(),ziel);
		w.setPosition(ziel);
		return true;
	}


/** Sends a ping to all robots within a distance defined by the given intensity.
  Maximum intensity is 9. All robots in this circumference receive an entry in
  their log containing position and signature of this robot. */

	public void processPing(int ensitaet)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		Robot1 r2;
		if (ensitaet > 9)
			ensitaet = 9;
		Vector robs = kf.getNachbarn(r,ensitaet);
		for (int i=0; i<robs.size(); i++)
		{
			r2 = (Robot1)robs.elementAt(i);
			r2.addLog(AKTION_PING,w.getPosition(),w.getSendSignatur());
		}
	}


/** This free scan reports a succes if an allied robot occupies the target field. */

	public boolean processSigScan(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() == Robot1.TYP_PARASIT)
			return false;
		Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
		if (r2 == null)
			return false;
		return (kf.isVerbuendet(r,r2));
	}


/** Enters a log entry into another robot's log.  This action is unhygenic
  and causes parasitic infections to spread. */

	public boolean processKommunikation(Point ziel, int data)
	{
                if (inaktion)
                        throw new KdKException("Nicht Schummeln !");
                Robot1 r2 = kf.getFeld(ziel.x, ziel.y);
                int p = r.getAbstand(ziel);
		if ((r2 != null) && (p==1)) {
			r2.addLog(AKTION_KOMM,w.getPosition(),data);
			Werte w2 = r2.getWerte();
			boolean rueck = w2.isInfiziert();
			if (w.isInfiziert())
			{
				inaktion = true;
				w2.infekt(w.getParasit().instantiiere());
				inaktion = false;
			}
			else if (rueck)
			{
				inaktion = true;
				w.infekt(w2.getParasit().instantiiere());
				inaktion = false;
			}
		}
		return true;
	}


/** Special action for hunters.  A robot on the target field will be marked
  until the death of the hunter and can be tracked via <i>getMarked</i>. */

	public void processMark(Point ziel)
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		if (w.getTyp() != Robot1.TYP_JAEGER)
			return;
		Robot1 a = kf.getFeld(w.getMarked().x,w.getMarked().y);
		if (a != null)
			a.getWerte().removeMarker(r);
		Robot1 z = kf.getFeld(ziel.x, ziel.y);
		if (z != null)
			z.getWerte().setMarker(r);
	}


/** Special action for hunters.  Returns the current position of a previously
  marked robot. */

	public Point getMarked()
	{
		if (inaktion)
			throw new KdKException("Nicht Schummeln !");
		return w.getMarked();
	}

/** Removes all connections to the game field.  Method processAction()
  becomes unusable and will throw a NullPointerExceptions. */

	public void dispose()
	{
		inaktion = true;
		kf = null;
		r = null;
		w = null;
	}

}
