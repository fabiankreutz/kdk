package kdk.program;

import java.util.Vector;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

/** Component for graphic and data.<br />
  This class has the field and a vector of all robots.
  As graphical component it can paint itself and calls each robot's 
  paint() method. */

public class KdKFeld extends JComponent {

/** Number of horizontal fields */
	public static int xgroesse = 20;
/** Number of vertical fields */
	public static int ygroesse = 25;
/** Show infection / marker */
	public static boolean verbose = true;

	private JTextArea log = null;
	private Vector roboter;
	private Robot1[][] feld;
	private Werte kw;
	private Robot1 dran;
	private KdKFeldSteuerung kfs;
	private Buendnisse buendnisse;


/** Creates a new instance of a KdkField.<br />
  Requires an instance of a controlling FeldSteuerung and a JTextArea for
  messages. */

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


/** Appents a line into the message field. */

	void addLogText(String s)
	{
		log.append(s+"\n");
	}


/** Adds a robot into the field.
  It will be appended to the vector so that its turn will be last. */

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


/** Removes a robot from field and vector.
  Finally, the "last words" will be posted on the message field. */

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


/** Returns the content of a field. */

	Robot1 getFeld(int x, int y)
	{
		return feld[x][y];
	}

/** Determines if robots ar allied. */

	boolean isVerbuendet(Robot1 r1, Robot1 r2)
	{
		int s1 = r1.getWerte().getRealSignatur();
		int s2 = r2.getWerte().getRealSignatur();
		return buendnisse.isVerbuendet(s1,s2);
	}


/** Returns all robots an a given radius. */

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


/** Empties field and vector. */

	void clearAll()
	{
		roboter.removeAllElements();
		feld = new Robot1[xgroesse][ygroesse];
		buendnisse = new Buendnisse();
		log.setText("");
		repaint();
	}


/** Moves the content of a field to another field. */

        void move(Point p, Point q)
        {
                feld[q.x][q.y] = feld [p.x][p.y];
                feld[p.x][p.y] = null;
                repaintRobot(q.x,q.y);
                repaintRobot(p.x,p.y);
        }


/** Handles the turn of the first robot of the vector.
 * @return false if no two enemy robots are on a field. */

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


/** Internal query of mouse events.<br />
  This method calls processClick() with coordinates. */

	public void processMouseEvent(MouseEvent me)
	{
		if (me.getID() == MouseEvent.MOUSE_RELEASED)
			processClick((me.getX() / (getWidth()/xgroesse)),
			             (me.getY() / (getHeight()/ygroesse)),
			             (me.getModifiers()));
	}

/** Reacts to a mouseclick on a field.<br />
  More exact: MouseRelease-Event. A left click shows robot information
  in the message bot.  A right click either shows the addRobotDialog or
  the killRobotDialog.
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


/** Paints the field as a table and calls the paint() methods of the robots.
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


/** Calculates the position according to the given coordinates and calls the
  <i>paint</i> routine of the robots.
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


/** Management of alliances between robots. */

 class Buendnisse
 {

	private int[][] klassen = new int[0][0];
	private int[] klaanz = new int[0];


/** Debug method to visualize the current allies on the standard output. */

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


/** Adds a robot to the field and calculates the Buendnis class to which it belongs. */

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


/** Decreases the number of robots in the alliance class of the robot on this field by one. */
	public void removeRobot(int s)
	{
		klaanz[getKlasse(s)]--;
	}


/** Checks if the robots accept each others as allies. */

	public boolean isVerbuendet(int s1, int s2)
	{
		if (s1 == s2)
			return true;
		return (getKlasse(s1) == getKlasse(s2));
	}

/** Checks if there is more than one alliance left on the field. */

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
