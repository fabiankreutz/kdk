package kdk.roboter;

import kdk.program.*;
import java.awt.*;

/** Robot - THE MINE</br>
The mine does nothing but slowly procreating and expolding whenever a stranger comes close. It has a good cloaking field and tries to keep one field distance to other mines.<br />
Example robot Version 1.2 by Fabian Kreutz */

public class Mine extends Robot1
{

	public ActionProcessor ap;

	public Mine()
	{
		super(8,10302,6,2,new int[0]);
		Werte w = getWerte();
		w.setMobilitaet(1);
		w.setVisibilitaet(4);
		w.setRundenEnergie(0);
	}

	public void zug(ActionProcessor ap)
	{
		this.ap = ap;
		Point[] u = getUmgebung(1);
		if (getWerte().isFertil())
			for (int b=0; b<u.length; b++)
				if (!smartScan(u[b])) {
					ap.processVermehren(u[b]);
					b = u.length; }

		explosion(u);
		int anz = 0;
		for (int b=0; b<u.length; b++)
			if (ap.processSigScan(u[b])) anz++;

		int[] feld = new int[8];
		int np = 0;
		if ((anz>0) && (anz<6)) {
			for (int a=1; a<9; a++) {
				if (!ap.processSigScan(u[a-1]))
					if (!ap.processSigScan(u[naechst(a)-1]))
						if (!ap.processSigScan(u[naechst(naechst(a))-1]))
							feld[np++] = naechst(a);
			}
		 if (np-->0)
			 ap.processBewegung(u[feld[(int)(Math.random()*np)]-1]);
		}
		explosion(u);
	}

	public Robot1 instantiiere()
	{
		return new Mine();
	}

	public String letzteWorte()
	{
		return "Da war 'ne Mine !";
	}

	private int naechst(int i)
	{
		int result = i + 2;
		if (result > 8) result -= 7;
		return result;
	}

	private boolean smartScan(Point p)
	{
		boolean result = ap.processSigScan(p);
		if (!result)
			result = ap.processSensor(p,4);
		return result;
	}

	private void explosion(Point[] u)
	{
		for (int b=0; b<u.length; b++)
			if ((ap.processSensor(u[b],4)) && 
			    (!ap.processSigScan(u[b])))
			{
//				int i = gegnerCount();
//  Maybe run into a greater amassment of enemies.
				Point d;
				if (b>4)
					d = u[b-2];
				else
					d = u[b+2];
				if (!ap.processSensor(d,4))
					ap.processBewegung(d);
				
				ap.processExplosion();
			}
	}

	public void paint(Graphics g, int b, int h)
	{
		g.setColor(Color.gray);
		g.fillOval(1,1,b-1,h-1);
		g.setColor(Color.darkGray);
		g.drawLine(b/8,h/8,     3*b/8,3*h/8);
		g.drawLine(7*b/8,7*h/8, 5*b/8,5*h/8);
		g.drawLine(b/8,7*h/8,   3*b/8,5*h/8);
		g.drawLine(7*b/8,h/8,   5*b/8,3*h/8);
	}

}
