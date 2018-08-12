package kdk.program;

import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/** Grafische und Datenkomponente.<BR>
  Das Spielfeld enth&auml;lt ein Feld sowie einen Vector mit allen Robotern.
  Als grafische Komponente malt sie sich selbst als Raster und ruft darauf
  die paint()-Methode aller Roboter auf. */

public class KdKFeld extends JComponent {

/** Anzahl der Felder in der Horizontalen */
	public static int xgroesse = 20;
/** Anzahl der Felder in der Vertikalen */
	public static int ygroesse = 25;
/** Infektionen / Marker anzeigen */
	public static boolean verbose = true;

	private JTextArea log = null;
	private Vector roboter;
	private Robot1[][] feld;
	private Werte kw;
	private Robot1 dran;
	private KdKFeldSteuerung kfs;
	private Buendnisse buendnisse;


/** Erstellt eine neue Instanz des KdKFeldes.<BR>
  Ben&ouml;tigt wird eine Instanz der FeldSteuerung in ein JTextArea, in das
  die Meldungen geschrieben werden. */

	public KdKFeld(KdKFeldSteuerung kfs, JTextArea log)
	{
		setPreferredSize(new Dimension(420,530));
		enableEvents(AWTEvent.MOUSE_EVENT_MASK);
		roboter = new Vector();
		this.log = log;
		this.kfs = kfs;
		feld = new Robot1[xgroesse][ygroesse];
		buendnisse = new Buendnisse();
	}


/** F&uuml;gt eine Zeile an das Meldungsfeld an. */

	void addLogText(String s)
	{
		log.append(s+"\n");
	}


/** F&uuml;gt einen Roboter in das Feld ein.
  Im Vector wird er an die letzte Stelle gepackt und ist ensprechend zuletzt 
  am Zug. */

	boolean addRobot(int x, int y, Robot1 r)
	{
		if ((x<0) || (y<0) ||
		    (x>=xgroesse) || (y>=ygroesse))
			return false;
		Werte w = r.getWerte();
		if (!buendnisse.addRobot(w.getRealSignatur(),
					w.getVerbuendete()))
			return false;
		w.setPosition(new Point(x,y));
		roboter.add(r);
		if (feld[x][y] != null)
			removeRobot(feld[x][y]);
		feld[x][y] = r;
		r.start(this);
		repaintRobot(x,y);
		return true;
	}


/** Entfernt einen Roboter aus Feld und Vector.
  Zuletzt werden noch die "letzten Worte" ausgegeben. */

	void removeRobot(Robot1 r)
	 {
		roboter.removeElement(r);
		Point p = r.getWerte().getMarked();
		if (p != null)
		{
			Robot1 z = getFeld(p.x, p.y);
			if (z != null)
				z.getWerte().removeMarker(r);
		}
		p = r.getWerte().getPosition();
		feld[p.x][p.y] = null;
		String s = r.letzteWorte();
		if ((s != null) && (!s.trim().equals("")))
			addLogText(s);
		repaintRobot(p.x,p.y);
		buendnisse.removeRobot(r.getWerte().getRealSignatur());
	}


/** Gibt den Inhalt eines Feldplatzes zur&uuml;ck. */

	Robot1 getFeld(int x, int y)
	{
		return feld[x][y];
	}

/** Fragt die B&uuml;ndnisse der Roboter untereinander ab. */

	boolean isVerbuendet(Robot1 r1, Robot1 r2)
	{
		int s1 = r1.getWerte().getRealSignatur();
		int s2 = r2.getWerte().getRealSignatur();
		return buendnisse.isVerbuendet(s1,s2);
	}


/** Gibt alle Roboter in einem Radius um den angegebenen Roboter zur&uuml;ck. */

	Vector getNachbarn(Robot1 r, int d)
	{
		Vector result = new Vector();
		for (int i=0; i<roboter.size(); i++)
		{
			Robot1 r2 = (Robot1)roboter.elementAt(i);
			if (r.getAbstand(r2.getWerte().getPosition())
								<= d)
				result.add(r2);
		}
		return result;
	}


/** Leert das Feld und den Vector. */

	void clearAll()
	{
		roboter.removeAllElements();
		feld = new Robot1[xgroesse][ygroesse];
		buendnisse = new Buendnisse();
		log.setText("");
		repaint();
	}


/** Bewegt den Inhalt eines Feldplatzes auf einen anderen. */

        void move(Point p, Point q)
        {
                feld[q.x][q.y] = feld [p.x][p.y];
                feld[p.x][p.y] = null;
                repaintRobot(q.x,q.y);
                repaintRobot(p.x,p.y);
        }


/** Organisiert den Zug des im Vector ersten Roboters.
 * @returns false, wenn sich keine zwei gegnerischen Roboter mehr auf dem Feld
  befinden. */

	boolean schritt()
	{
		if (roboter.isEmpty())
		{
			addLogText("Feld ist leer");
			return false;
		}
		boolean result = true;
		if (!buendnisse.gegnerda())
		{
			addLogText("Nur Verbündete auf dem Feld");
			result = false;
		}
		dran = (Robot1)(roboter.elementAt(0));
		roboter.removeElementAt(0);
		kw = dran.getWerte();
		if (kw.validate() > 0)
		{
			addLogText("Roboter überläd sich !");
			removeRobot(dran);
		} 
		else
		{
			ActionProcessor kap = new ActionProcessor(this,dran);
			try
			{
				dran.zug(kap);
				repaintRobot(dran.getWerte().getPosition().x,
					     dran.getWerte().getPosition().y);
				kap.dispose();
				kap = null;
				kw.regeneriere();
				roboter.add(dran);
			}
			catch(KdKException ke) {
				removeRobot(dran);
			}
		}
//		repaint();
		return result;
	}


/** Interne Abfrage der Mausereignisse.<BR>
  Diese Methode ruft processClick() mit Feldkoordinaten auf. */

	public void processMouseEvent(MouseEvent me)
	{
		if (me.getID() == MouseEvent.MOUSE_RELEASED)
			processClick((me.getX() / (getWidth()/xgroesse)),
			             (me.getY() / (getHeight()/ygroesse)),
			             (me.getModifiers()));
	}

/** Reagiert auf einen Mausclick im Spielfeld.<BR>
  Genauer : MouseReleased-Ereignis. Bei einem Linksclick wird die Info des
  entsprechenden Roboters angezeigt. Rechtsclick &ouml;fnet entweder den
  addRobotDialog oder den killRobotDialog in der Feldsteuerung.
 * @see kdk.program.KdKFeldSteuerung */

	public void processClick(int x, int y, int button)
	{
		switch (button)
		{
			case MouseEvent.BUTTON1_MASK :
			{
				if (feld[x][y] != null) 
					addLogText(feld[x][y].info());
				break;
			}
			case MouseEvent.BUTTON3_MASK :
			{
				if (feld[x][y] == null) 
					kfs.openAddRobotDialog(x,y);
				else
					if (kfs.openKillRobotDialog())
						removeRobot(feld[x][y]);
				break;
			}
		}
	}

	private int rund(int w, double fw)
	{
		return (int)Math.round(w*fw);
	}


/** Malt das Feld als Raster und ruft die paint()-Methoden der Roboter auf.
 * @see kdk.program.Robot1#paint(Graphics, int, int) */

	public void paint(Graphics g)
	{
		double xf = (double)getWidth() / (double)xgroesse;
		double yf = (double)getHeight() / (double)ygroesse;
		g.setColor(Color.black);
		g.drawRect(0,0,getWidth()-1,getHeight()-1);
		for (int a = 1; a < xgroesse; a++)
			g.drawLine(rund(a,xf),0,
				   rund(a,xf),getHeight());
		for (int b = 1; b < ygroesse; b++)
			g.drawLine(0,rund(b,yf),
				   getWidth(),rund(b,yf));
		for (int a = 0; a < xgroesse; a++)
			for (int b = 0; b < ygroesse; b++)
			{
				if (feld[a][b] != null)
					feld[a][b].paint(
						g.create(
						  rund(a,xf)+1,
						  rund(b,yf)+1,
						  rund(a+1,xf)-1,
						  rund(b+1,yf)-1),
						rund(1,xf)-1,
						rund(1,yf)-1);
			}
	}


/** Berechnet die Position anhand der gegebenen Feldkoordinaten und ruft die
  <I>paint()</I> Rountine der Roboter auf.
 * @see kdk.program.Robot1#paint(Graphics, int, int) */

	public void repaintRobot(int a, int b)
	{
		double xf = (double)getWidth() / (double)xgroesse;
		double yf = (double)getHeight() / (double)ygroesse;
		Graphics g = getGraphics().create(
					rund(a,xf)+1,   rund(b,yf)+1,
					rund(a+1,xf)-1, rund(b+1,yf)-1);
		if (feld[a][b] != null)
		{
			Robot1 r = feld[a][b];
			r.paint(g, rund(1,xf)-1, rund(1,yf)-1);
			if (verbose)
			{
				if (r.getWerte().getTyp() == Robot1.TYP_JAEGER)
				{
					Point p = r.getWerte().getMarked();
					if (p != null)
						getGraphics().drawLine(
							rund(a,xf),rund(b,yf),
							rund(p.x,xf),rund(p.y,yf));
				}
				if (r.getWerte().isInfiziert())
				{
					g.setColor(Color.green);
					g.fillOval(4,4,2,2);
				}
			}
				
		}
		else
		{
			g.setColor(getBackground());
			g.fillRect(0,0, rund(1,xf)-1, rund(1,yf)-1);
		}
			
	}

//	public void reshape(int x, int y, int b, int h)
//	{
//		int g = Math.min(b,h);
//		super.reshape(x,y,g-4,g);
//	}


/** Verwaltung der B&uuml;ndnisse unter den Robotern. */

 class Buendnisse
 {

	private int[][] klassen = new int[0][0];
	private int[] klaanz = new int[0];


/** Debug-Methode zur Visualisierung der derzeitig gespeicherten B&uuml;ndnisse auf der Standardausgabe. */

	public void ausgabe()
	{
		for (int i=0; i<klassen.length; i++)
		{
			System.out.print("Klasse "+i+" ("+klaanz[i]+"):");
			for (int j=0; j<klassen[i].length; j++)
				System.out.print(" "+klassen[i][j]);
			System.out.println();
		}
	}


/** F&uuml;gt einen Roboter ins Feld ein und errechnet sich die B&uuml;ndnisklasse, zu dieser er geh&ouml;rt. */

	public boolean addRobot(int s, int[] v)
	{
		s = Math.abs(s);
		int i = getKlasse(s);
		if (i > -1)
		{
			klaanz[i]++;
			return true;
		}
KLASSENFOR:	for (int j=0; j<v.length; j++)
		{
			int k = getKlasse(v[j]);
			if (k == -1)
				continue KLASSENFOR;
			v[j] = -1;
			if (i == -1)
				i = k;
			else if (i != k)
				return false;
		}
		if (i > -1)
		{
			klaanz[i]++;
			klassen[i] = compose(klassen[i],s,v);
		}
		else
		{
			klassen = compose(klassen,compose(v,s,new int[0]));
			klaanz = compose(klaanz,1,new int[0]);
		}
		return true;
	}


/** Z&auml;hlt die Anzahl der  Roboter dieser B&uuml;ndnisklasse auf dem Feld um eins herab. */
	public void removeRobot(int s)
	{
		klaanz[getKlasse(s)]--;
	}


/** Pr&uuml;ft, ob sich die Roboter gegenseitig als Verb&uuml;ndete angeben. */

	public boolean isVerbuendet(int s1, int s2)
	{
		if (s1 == s2)
			return true;
		return (getKlasse(s1) == getKlasse(s2));
	}

/** Pr&uuml;ft, ob sich mehr als eine B&uuml;ndnisklasse auf dem Spielfeld befindet. */

	public boolean gegnerda()
	{
		int result = 0;
		for (int i=0; i<klaanz.length; i++)
			if (klaanz[i] > 0)
				result++;
		return (result > 1);
	}

	private int getKlasse(int s)
	{
		for (int i=0; i<klassen.length; i++)
			for (int j=0; j<klassen[i].length; j++)
				if (klassen[i][j] == s)
					return i;
		return -1;
	}

	private int[] compose(int[] e, int z, int[] d)
	{
		int dlength = 0;
		for (int i=0; i<d.length; i++)
			if (d[i] > 0)
				dlength++;
		int[] result = new int[e.length+1+dlength];
		int i = 0;
		for (int j=0; j<e.length; j++)
			result[i++] = e[j];
		result[i++] = z;
		for (int j=0; j<d.length; j++)
			if (d[j] > 0)
				result[i++] = d[j];
		return result;
	}

	private int[][] compose(int[][] e, int[] z)
	{
		int[][] result = new int[e.length+1][0];
		int i = 0;
		for (int j=0; j<e.length; j++)
			result[i++] = e[j];
		result[i] = z;
		return result;
	}

 }

}
