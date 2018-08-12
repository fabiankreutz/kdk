package kdk.program;

import java.util.Vector;
import java.awt.*;


/** Robot1 is a superclass of all participating robots.
 It implements all properties and game logic. */

public abstract class Robot1
{

	public static final int TYP_CUSTOM	= 0;
	public static final int TYP_PARASIT	= 1;
	public static final int TYP_MINE	= 2;
	public static final int TYP_JAEGER	= 3;
/** For the meaning of the types, see <A HREF="Anleitung.html#typen">Anleitung</A> */
	public static final int TYP_BRUETER	= 4;

	private Werte werte = null;
	private KdKFeld kf;
	private Vector actionLog = new Vector();
/** The default paint() meathod paints a rectangle of this color. */
	protected Color farbe = Color.blue;


/** Creates an instance of the robot.
  The values given here cannot be changed by the robot itself.
  This is the only legal way to create a new robot instance. */

	public Robot1(int energie, int signatur, int fertilitaet, int typ,
			int[] verbuendete)
	{
		werte = new Werte(energie, signatur, fertilitaet, typ, verbuendete);
	}


/** This constructor is called when the user manually adds a robot to the field.
  In the current implementation it as a useless robot that has no allies. */

	public Robot1()
	{
		this(2,0,0,0,new int[0]);
	}


/** Stores the instance of the field in the robot, in order to access the textfield. */

	final void start(KdKFeld kf)
	{
		this.kf = kf;
	}


/** Adds an external event into the log. */

	final void addLog(int typ, Point p, int ensitaet)
	{
		actionLog.add(new KdKAction(typ,p,ensitaet));
	}

/** Clears the log. */

	final void clearLog()
	{
		 actionLog.removeAllElements();
	}


/** Returns the internal instance of all properties. */

	public final Werte getWerte()
	{
		return werte;
	}


/** Calculates the distance of this instance to another point.
  Considering the torus form of the field. */

	public final int getAbstand(Point p)
	{
		int b = KdKFeld.xgroesse;
		int h = KdKFeld.ygroesse;
		Point pos = getWerte().getPosition();
		int dx = Math.abs(p.x-pos.x);
		int dy = Math.abs(p.y-pos.y);
		int abstand = Math.max(
			dx<=(b/2)?dx:b-dx,
			dy<=(h/2)?dy:h-dy);

			//Math.abs(Math.abs(p.x-pos.x))>(b/2)?-
				 //Math.abs(b-p.x)),
			//Math.abs(Math.abs(h-pos.y)-
				 //Math.abs(h-p.y)));
		return abstand;
	}


/** Returns a field of 8*r points, which are of the given distance to this instance. */

	public Point[] getUmgebung(int entf)
	{
		Point pos = getWerte().getPosition();
		if ((entf < 1) || (entf > 9))
			 return null;
		Point[] result = new Point[8*entf];
		int i = 0;
		for (int a=0; a<=(2*entf); a++)
		{
			result[i++] = getFeld(pos.x+entf,pos.y-entf+a);
			result[i++] = getFeld(pos.x-entf,pos.y+entf-a);
		}
		for (int a=1; a<=(2*entf)-1; a++)
		{
			result[i++] = getFeld(pos.x+entf-a,pos.y+entf);
			result[i++] = getFeld(pos.x-entf+a,pos.y-entf);
		}
		return result;
	}

/** Considers the torus property of the field. */

	public final Point getFeld(int xp, int yp)
	{
		int b = KdKFeld.xgroesse;
		int h = KdKFeld.ygroesse;
		if (xp<0) xp += b;
		if (xp>=b) xp -= b;
		if (yp<0) yp += h;
		if (yp>=h) yp -= h;
		return new Point(xp,yp);
	}


/** Returns a list of event received since the last turn.
  At the end of the turn, the log will be automatically cleared, so that at all times there
  are only "new" events in it. */

	public KdKAction[] getLog()
	{
		KdKAction[] result = new KdKAction[actionLog.size()];
		return (KdKAction[])actionLog.toArray(result);
	}


/** Puts a message into the message field.
  This method should not be used excessively, as already the "last words" fill up the text area.
  Use this only for debug messages */

	public void meldung(String s)
	{
		kf.addLogText(s);
	}


/** This is the game logic / intelligence of the robot.
  This method is called once for each turn of the robot
  If the robot dies during that turn, an exception is thrown.
  The field can only be influenced via the given ActionProcessor, which will be disabled
  on death or at the end of the turn.  Another attempt to access the field despite these
  states will result in a NullPointerException.
  It is useful to first check the list of received events.
 * @see Robot1#getLog() */

	public abstract void zug(ActionProcessor kap);


/** Returns the new instance upon procreation.
  The default implementation instanciates this same class. */

	public Robot1 instantiiere()
	{
		try
		{
			return (Robot1)getClass().newInstance();
		} catch(Exception e) { }
		return null;
	}


/** Returns a short message whenever a robot of this class dies. */

	public String letzteWorte()
	{
		return "Es starb ein "+getClass().getName();
	}


/** Returns the info to be shown to the user after left mouse click. */
	
	public String info()
	{
		return "Energie	       : "+werte.getEnergie()+"\n"+
			"Fertil in      : "+werte.getFertilitaet()+"\n"+
			"Unsichtbarkeit : "+werte.getVisibilitaet()+"\n"+
			"Typ            : "+werte.getTyp()+"\n";
	}


/** Paints the robot. The default implementation paints a rectangle of a certain color.
 * @see Robot1#farbe */

	public void paint(Graphics g, int b, int h)
	{
		g.setColor(farbe);
		g.fillRect(0,0,b,h);
	}

/** Properties of a single action.
  Actions with effect on other robots (shot, scan) are stored in their log.
 * @see Robot1#getLog() */

protected class KdKAction {

	private int typ = -1;
	private Point punkt;
	private int ensitaet = 0;


/** Saves the type of action, source position and intensity. */

	public KdKAction(int t, Point p, int e)
	{
		typ = t;
		punkt = p;
		ensitaet = e;
	}

	public int getTyp()
	{
		return typ;
	}

	public Point getPunkt()
	{
		return new Point(punkt.x,punkt.y);
	}

	public int getIntensitaet()
	{
		return ensitaet;
	}

}

}
