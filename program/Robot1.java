package kdk.program;

import java.util.Vector;
import java.awt.*;


/** Robot1 ist der Ahne jeden mitspielenden Roboters. Hier sind alle Eigenschaft und auch die Spielelogik wird hier implementiert. Auf das Feld hat diese Klasse */

public abstract class Robot1
{

	public static final int TYP_CUSTOM	= 0;
	public static final int TYP_PARASIT	= 1;
	public static final int TYP_MINE	= 2;
	public static final int TYP_JAEGER	= 3;
/** Zur Bedeutung der Typen siehe die <A HREF="Anleitung.html#typen">Anleitung</A> */
	public static final int TYP_BRUETER	= 4;

	private Werte werte = null;
	private KdKFeld kf;
	private Vector actionLog = new Vector();
/** Die Standard-paint-Routine malt ein Rechteck mit dieser Farbe. */
	protected Color farbe = Color.blue;


/** Erstellt eine Instanz des Roboters. Die hier festgelegten Werte k&ouml;nnen zur Lebenszeit des Roboters nicht mehr von ihm selber ver&auml;ndert werden. Dies ist die einzige echte M&ouml;glichkeit f&uuml;r den Programmierer des Roboters eine legale Instanz zu bilden. */

	public Robot1(int energie, int signatur, int fertilitaet, int typ,
			int[] verbuendete)
	{
		werte = new Werte(energie, signatur, fertilitaet, typ, verbuendete);
	}


/** Dieser Konstruktor wird beim manuellen Einf&uuml;gen des Roboters in das Feld aufgerufen. In seiner derzeitigen Implementierung kann der Roboter allerings nichts und hat auch keine Verb&uuml;ndete. */

	public Robot1()
	{
		this(2,0,0,0,new int[0]);
	}


/** Platziert eine Feld-Instanz im Roboter, wegen des Zugriffs auf das Textfeld. */

	final void start(KdKFeld kf)
	{
		this.kf = kf;
	}


/** F&uuml;gt ein externes Ereignis in das Logbuch des Roboters ein. */

	final void addLog(int typ, Point p, int ensitaet)
	{
		actionLog.add(new KdKAction(typ,p,ensitaet));
	}

/** L&ouml;scht alle Eintr&auml;ge aus dem Logbuch. */

	final void clearLog()
	{
		 actionLog.removeAllElements();
	}


/** Gibt die interne Instanz der Eigenschaften zur&uuml;ck. */

	public final Werte getWerte()
	{
		return werte;
	}


/** Errechnet den Abstand dieser Instanz zu einem anderen Punkt. dabei wird die Torus-Eigenschaft des Feldes ber&uuml;cksichtigt. */

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


/** Gibt ein Feld von den 8*r Punkten wieder, die den angegebenen Abstand von dieser Instanz haben. */

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

/** Ber&uuml;cksichtigt die Torus-Eigenschaft des Feldes. */

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


/** Gibt die Liste der in der letzten Runde empfangenen Ereignisse zur&uuml;ck. 
Nach dem Ende der Runde wird das Logbuch automatisch gel&ouml;scht, es befinden sich also nur "neue" Ereignisse darin. */

	public KdKAction[] getLog()
	{
		KdKAction[] result = new KdKAction[actionLog.size()];
		return (KdKAction[])actionLog.toArray(result);
	}


/** Gibt eine Meldung im Textfeld aus. Diese Methode sollte nicht exzessiv verwendet werden, da alleine schon die Letzte-Worte Meldungen das Textfeld f&uuml;llen. Verwenden Sie es besser nur f&uuml;r Debug-Meldungen. */

	public void meldung(String s)
	{
		kf.addLogText(s);
	}


/** Hier wird die Spielintelligenz des Roboters implementiert. Die Methode wird zu jedem Zug des Roboters einmal aufgerufen. Falls w&auml;hrend dieses Zuges der Tod eintritt, wird die Methode mit einer Exception beendet. Einflu&szlig; auf das Feld kann &uuml;ber den gegebenen ActionProcessor genommen werden. Dieser wird am Ende der Runde oder durch den Tod des Roboters ung&uuml;ltig gemacht und w&uuml;rde dann bei weiterem Aufruf zu einer Null-Pointer-Exception f&uuml;hren.
Es empfiehlt sich zu Beginn des Zuges die Liste der in der letzten Runde empfangenen Ereignisse abzufragen.
 * @see Robot1#getLog() */

	public abstract void zug(ActionProcessor kap);


/** Gibt beim Aufruf der Vermehre-Aktion durch den Roboter die neue einzuf&uuml;gende Instanz zur&uuml;ck. Die Standardversion versucht die eigene Klasse neu zu instantiierten. */

	public Robot1 instantiiere()
	{
		try
		{
			return (Robot1)getClass().newInstance();
		} catch(Exception e) { }
		return null;
	}


/** Gibt einen Spruch zur&uuml;ck, der beim Tod einer Instanz im Textfeld erscheint. */

	public String letzteWorte()
	{
		return "Es starb ein "+getClass().getName();
	}


/** Gibt die Information zur&uuml;ck, die bei einem Links-Click auf einen Roboter im Textfeld erscheint. */
	
	public String info()
	{
		return "Energie	       : "+werte.getEnergie()+"\n"+
			"Fertil in      : "+werte.getFertilitaet()+"\n"+
			"Unsichtbarkeit : "+werte.getVisibilitaet()+"\n"+
			"Typ            : "+werte.getTyp()+"\n";
	}


/** Malt den Roboter. Die Standardversion malt ein Rechteck der gespeicherten Farbe.
 * @see Robot1#farbe */

	public void paint(Graphics g, int b, int h)
	{
		g.setColor(farbe);
		g.fillRect(0,0,b,h);
	}

/** Speichert die Werte einer Aktion ab. Aktionen die Einflu&szlig; auf andere Roboter haben (Schuss, Sensorscan) werden von diesem empfangen und im Logbuch abgelegt.
 * @see Robot1#getLog() */

protected class KdKAction {

	private int typ = -1;
	private Point punkt;
	private int ensitaet = 0;


/** Speichert den Typen der Aktion, den Ausgangspunkt und die Intensit&auml;t ab. */

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
