package kdk.roboter;

import kdk.program.*;
import java.awt.*;

/** Robot - I AM SO STUPID<br />
Does nothing more than running around a bit and shoot a little, if something happens to be right in front of it.<br />
Example robot version 1.0 by Fabian Kreutz */

public class Dummi extends Robot1 {

  private static int tote = 0;
  private static int lebende = 0;
  private static int[] bewegung = {6, 8, 1, 3, 1, 8, 6, 4};
  int posi = 0;
  Werte werte;

  public Dummi() {
    super(1,8556,2,0,new int[0]);
    lebende++;
    werte = getWerte();
    werte.setMobilitaet(1);
    werte.setVisibilitaet(0);
    werte.setRundenEnergie(11);
    farbe = Color.green.brighter();
//    System.out.println(werte.validate());
  }

  public void zug(ActionProcessor ap) {
    Point[] u = getUmgebung(1);
    for (int b=0; b<u.length; b++)
      if ((smartscan(ap,u[b],4)) && (werte.getRundenEnergie()>0))
        ap.processSchuss(u[b--],1);
    for (int b = 0; b < u.length; b++)
      ap.processVermehren(u[b]);
    boolean eingesperrt = true;
    for (int b = 0; b < u.length; b++)
    if (!ap.processSigScan(u[b]))
      eingesperrt = false;
    if (eingesperrt)
    {
      farbe = Color.green.darker();
      bewegung[posi] = 5;
    }

    Point p = werte.getPosition();
    int x = p.x-2;
    int y = p.y-2;
    if (smartscan(ap, getFeld(x,y),2))
      ap.processSchuss(getFeld(x,y),4);
    x = p.x+2;
    y = p.y+2;
    if (smartscan(ap, getFeld(x,y),2))
      ap.processSchuss(getFeld(x,y),4);

    if (werte.getRundenEnergie()>0)
    {
      u = getUmgebung(5);
      for (int i=0; i<u.length; i++)
        if (!ap.processSigScan(u[i]))
        {
	  int st = werte.getRundenEnergie() / 2;
          if (st>4)
            st = (st+4) / 2;
          ap.processSchuss(u[i],st);
          i = u.length;
        }
    }

    p = werte.getPosition();
    Point p2 = p;
    switch (bewegung[posi]) {
    case 1: {
      p2 = getFeld(p.x-1,p.y+1);
      break; }
    case 2: {
      p2 = getFeld(p.x,p.y+1);
      break; }
    case 3: {
      p2 = getFeld(p.x+1,p.y+1);
      break; }
    case 4: {
      p2 = getFeld(p.x-1,p.y);
      break; }
    case 5: {
      p2 = getFeld(p.x,p.y);
      break; }
    case 6: {
      p2 = getFeld(p.x+1,p.y);
      break; }
    case 7: {
      p2 = getFeld(p.x-1,p.y+1);
      break; }
    case 8: {
      p2 = getFeld(p.x,p.y+1);
      break; }
    case 9: {
      p2 = getFeld(p.x+1,p.y+1);
      break; }
/*    case 1: {
      p2 = getFeld(p.x+1,p.y-1);
      break; }
    case 2: {
      p2 = getFeld(p.x-1,p.y+1);
      break; }
    case 3: {
      p2 = getFeld(p.x+1,p.y);
      break; }
    case 4: {
      p2 = getFeld(p.x-1,p.y);
      break; }
    case 5: {
      p2 = getFeld(p.x+1,p.y+1);
      break; }
    case 6: {
      p2 = getFeld(p.x-1,p.y-1);
      break; }
    case 7: {
      p2 = getFeld(p.x,p.y+1);
      break; }
    case 8: {
      p2 = getFeld(p.x,p.y-1);
      break; } */
    }
    ap.processBewegung(p2);
    if (!eingesperrt) {
      if (++bewegung[posi] == 10) bewegung[posi] = 1;
      if (++posi == 8) posi = 1;
    }
  }

  public Robot1 instantiiere() { return new Dummi(); }
  
  public String letzteWorte() { return "RIP "+(++tote)+"/"+(--lebende); }

  public String info()
  {
    Werte w = getWerte();
    tote = 0;
    lebende = 1;
    return "Dummi\nEnergie: "+w.getEnergie()+"\nFertil: "+w.getFertilitaet();
  }

  private boolean smartscan(ActionProcessor ap, Point p, int i) {
    boolean result = !ap.processSigScan(p);
    if (result) result = ap.processSensor(p,i);
    return result; }

  public void paint(Graphics g, int b, int h)
  {
    super.paint(g,b,h);
    int mb = b/2;
    int mh = h/2;
    g.setColor(Color.black);
    switch (bewegung[posi])
    {
      case 1: { g.drawLine(2,h-5,3,h-6);
                g.drawLine(4,h-2,5,h-3); break; }
      case 2: { g.drawLine(mb-2,h-2,mb-2,h-4);
                g.drawLine(mb+2,h-2,mb+2,h-4); break; }
      case 3: { g.drawLine(b-2,h-5,b-3,h-6);
                g.drawLine(b-4,h-2,b-5,h-3); break; }
      case 4: { g.drawLine(2,mh-2,4,mh-2);
                g.drawLine(2,mh+2,4,mh+2); break; }
      case 5: { g.drawLine(mb-2,mh-1,mb-2,mh+1);
                g.drawLine(mb+2,mh-1,mb+2,mh+1); break; }
      case 6: { g.drawLine(b-2,mh-2,b-4,mh-2);
                g.drawLine(b-2,mh+2,b-4,mh+2); break; }
      case 7: { g.drawLine(2,5,3,6);
                g.drawLine(4,2,5,3); break; }
      case 8: { g.drawLine(mb-2,2,mb-2,4);
                g.drawLine(mb+2,2,mb+2,4); break; }
      case 9: { g.drawLine(b-2,5,b-3,6);
                g.drawLine(b-4,2,b-5,3); break; }
    }
  }

}
