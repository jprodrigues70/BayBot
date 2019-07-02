package war;
import robocode.*;
import robocode.util.Utils;
import java.util.*;
import java.awt.Color;

public class BayBot extends AdvancedRobot {

  int numofvariables = 5;

  // A quantidade de poderes precisa ser numofvariables - 2
  static int[] powers = new int[]{
    1, 4, 8
  };
    public ScannedRobotEvent Enemy;

  int isNear = 0;
  double lastPower = 0;

  static double nearShotPower = 0;
  static double farShotPower = 0;
  static int missingWhenNear = 0;
  static int missingWhenFar = 0;

  boolean go = true;

  List<ScannedRobotEvent> sre = new ArrayList<ScannedRobotEvent>();

  static int round = 0;

  static ArrayList<int[]> hitShots = new ArrayList<int[]>();
  static ArrayList<int[]> missShots = new ArrayList<int[]>();

  int moveDirection = 1;
  double lastTurnEnergy;

  public void run() {
    setColors(Color.pink, Color.black, Color.red);

    setAdjustRadarForGunTurn(true);
    setAdjustRadarForRobotTurn(true);
    setAdjustGunForRobotTurn(true);
    setTurnRadarRightRadians(Double.POSITIVE_INFINITY);

    while (true) {
      if (Enemy != null) {
        setTurnGunRight((Enemy.getBearing() + getHeading() + 360)%360 - getGunHeading());
      } else {
        setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
      }
      if (getDistanceRemaining() == 0 && 0 < getTime()) {
        double x = 0, y = 0;

        x = Math.random() * getBattleFieldWidth();
        y = Math.random() * getBattleFieldHeight();

        double radarAngle =
          (
            Math.toDegrees(
              Math.atan((x - getX()) / (y - getY()))
            ) + (
              y < getY() ? 180 : 360
            )
          ) % 360;

        if (Double.isNaN(radarAngle)) {
          radarAngle = x > getX() ? 90 : 270;
        }

        double turn = radarAngle - getHeading();
        if (Math.abs(turn) > 180) {
          turn = (360 - Math.abs(turn))*Math.signum(turn)*-1;
        }

        setTurnRight(turn);

        if (Math.abs(turn) > 90) {
          turn = Math.signum(turn)*-1*(180-Math.abs(turn));
          go = false;
        } else {
          go = true;
        }


        do{
          scan();
        } while(getTurnRemaining() != 0);

        double distance = Math.sqrt(Math.pow(x - getX(), 2) + Math.pow(y - getY(), 2));

        if (go) {
          setAhead(distance);
        } else {
          setBack(distance);
        }
      }

      Enemy = null;
      scan();
    }
  }

  public void onScannedRobot(ScannedRobotEvent e) {
    Enemy = e;
    gunf (e);
    radarf (e);
    int idx = new Random().nextInt(powers.length);
    lastPower = Math.min(powers[idx], (int)getEnergy());

    if (aim(e)) {
      fired(e);
    } else {
          gunf (e);
        double energy = lastTurnEnergy - e.getEnergy();
        if (energy <= 3) {
          // zigzag
          moveDirection = -moveDirection;
          setTurnRight(e.getBearing() - 30 + 90 * moveDirection);
          setAhead(120 * moveDirection);
        } else{
          // go to enemy
          setTurnRight(e.getBearing());
          setAhead(100);
        }
        setTurnGunRight(reposition(e));
        fired(e);
        lastTurnEnergy = e.getEnergy();
    }

  }

  private boolean aim(ScannedRobotEvent scannedRobot) {
    // 36 é o tamanho do robô
    return Math.abs(
        (scannedRobot.getBearing() + getHeading() + 360) % 360 - getGunHeading())
          <=
        Math.abs(2*180*Math.atan(36*0.5d/scannedRobot.getDistance()/Math.PI));
  }

  private void fired(ScannedRobotEvent e) {
    if (getEnergy() > 5 ) {
      isNear = (e.getDistance() < 200) ? 1 : 0;
      if (isNear == 1 && missingWhenNear == 0) {
        lastPower = (nearShotPower > 0)? nearShotPower : lastPower;
        setFire(lastPower);
      } else if (isNear == 1) {
        lastPower = Math.min(lastPower, nearShotPower);
        setFire(lastPower);
      }

      if (isNear == 0 && missingWhenFar == 0) {
        lastPower = (farShotPower > 0)? farShotPower : lastPower;
        setFire(lastPower);
      } else if (isNear == 0) {
        lastPower = Math.min(lastPower, farShotPower);
        setFire(lastPower);
      }

    } else {
      lastPower = powers[0];
      isNear = (e.getDistance() < 200) ? 1 : 0;

      if (isNear == 1 && missingWhenNear == 0) {
        while(getEnergy() > 0) {
          setFire(lastPower);
        }
      } else {
        setFire(lastPower);
      }
    }

  }


  /**
   * Gun Movement
   * @param e
   */
  public void gunf (ScannedRobotEvent e) {
    // Recovers enemy bearing
    double degree = e.getBearing();
    // Recovers my info
    double heading = getHeading();
    double gunHeading = getGunHeading();

    // Aim gun to the enemy
    double turn = heading - gunHeading + degree;
    if (Math.abs(turn) > 180) {
      turn = (360 - Math.abs(turn))*Math.signum(turn)*-1;
    }
    setTurnGunRight(turn);
  }

  /**
   * Radar Movement
   *
   * @param e
   */
  public void radarf (ScannedRobotEvent e) {
    // Radar turns (this movement is a widely used standard)
    double radarTurn = getHeadingRadians() + e.getBearingRadians() - getRadarHeadingRadians();
    setTurnRadarRightRadians(Utils.normalRelativeAngle(radarTurn));
  }

  private double reposition(ScannedRobotEvent e) {
    double degree = e.getBearing();
    double heading = getHeading();
    double gunHeading = getGunHeading();

    double turn = heading - gunHeading + degree;
    double gunTurnDegrees =
      Utils.normalRelativeAngleDegrees(turn) + e.getVelocity() * Math.sin(e.getHeading());

    return gunTurnDegrees;
  }

  /**
   * Quando colidir com um robô
   * @param e
   */
  public void onHitRobot(HitRobotEvent e) {
    if (e.isMyFault()) {
      reverseDirection();
    } else {
      lastPower=powers[powers.length - 1];
      fire(lastPower);
    }
  }

  /**
   * Quando sou atingido
   */
  public void onHitByBullet(HitByBulletEvent e) {
    double ang = 90 - e.getBearing();
    turnLeft(ang);
    turnRadarRight(360);
  }


  /**  Colisão com parede */
  public void onHitWall(HitWallEvent e) {
    reverseDirection();
    setAhead(-200);
  }

  /**
   * Mapeia intensidade do tiro
   *
   * @param acertou: 1 indica que acertou, 0 indica que errou
   * @return
   */
  public int[] mapearTiro(int acertou) {
    int a[] = new int[numofvariables];

    for (int i = 0; i < powers.length; i++) {
      if (lastPower == powers[i]) {
        a[i] = 1;
      } else {
        a[i] = 0;
      }
    }

    a[numofvariables - 2] = isNear;
    a[numofvariables - 1] = acertou;
    return a;
  }

  public void onBulletMissed(BulletMissedEvent event) {
    missShots.add(mapearTiro(0));
  }

  public void onBulletHit(BulletHitEvent event) {
    hitShots.add(mapearTiro(1));
  }

  public void onWin(WinEvent e) {
    turnRight(15);
    while(true) {
      turnLeft(180);
      turnLeft(30);
      turnRight(30);
    }
  }

  public double[] bayes() {

    int totalHit = hitShots.size();
    int totalMiss = missShots.size();

    double hitClassProbability = probability(totalHit, totalHit + totalMiss);
    double missClassProbability = probability(totalMiss, totalHit + totalMiss);

    double [] hits = countShots(hitShots);
    double [] misses = countShots(missShots);

    double [] probabilityPerHit = probability(hits, totalHit);
    double [] probabilityPerMiss = probability(misses, totalMiss);

    // Probabilidade de estar perto e acertar para cada poder de tiro
    double [] nearAndHitProbability =
        totalProbability(
            Arrays.copyOfRange(probabilityPerHit, 0, numofvariables - 2),
            probabilityPerHit[numofvariables - 2] * hitClassProbability
            );

    // Probabilidade de estar perto e errar para cada poder de tiro
    double [] nearAndMissProbability =
        totalProbability(
            Arrays.copyOfRange(probabilityPerMiss, 0, numofvariables - 2),
            probabilityPerMiss[numofvariables - 2] * missClassProbability
            );
    // Probabilidade de estar longe e acertar para cada poder de tiro
    double [] farAndHitProbability =
        totalProbability(
            Arrays.copyOfRange(probabilityPerHit, 0, numofvariables - 2),
            probabilityPerHit[numofvariables - 1] * hitClassProbability
            );
    // Probabilidade de estar longe e errar para cada poder de tiro
    double [] farAndMissProbability =
        totalProbability(
            Arrays.copyOfRange(probabilityPerMiss, 0, numofvariables - 2),
            probabilityPerMiss[numofvariables - 1] * missClassProbability
            );


    // Cada força tem mais chance de errar ou de acertar quando  está perto?
    double[] maxBetweenHitAndMissnear =
      maxPerIndex(nearAndHitProbability, nearAndMissProbability);
    int [] nearAndHitChances =
      comparePerIndex(maxBetweenHitAndMissnear, nearAndHitProbability);

    // Cada força tem mais chance de errar ou de acertar quando  está longe?
    double[] maxBetweenHitAndMissFar =
      maxPerIndex(farAndHitProbability, farAndMissProbability);
    int [] farAndHitChances =
      comparePerIndex(maxBetweenHitAndMissFar, farAndHitProbability);

    // Se não houver probabilidade de acerto, informar
    if (sumPositions(nearAndHitChances) == 0) { missingWhenNear = 1; }
    if (sumPositions(farAndHitChances) == 0) { missingWhenFar = 1; }

    // Soma das probabilidades de acerto e erro de cada poder de tiro
    double[] sumnearHitsAndMissProbabilities =
      sumPerIndex(nearAndHitProbability, nearAndMissProbability, 1.0);
    double[] sumFarHitsAndMissProbabilities =
      sumPerIndex(farAndHitProbability, farAndMissProbability, 1.0);

    // Probabilidade final de acertos para cada poder de tiro
    double[] finalnearProbabilities =
      probability(nearAndHitProbability, sumnearHitsAndMissProbabilities);
    int bestnearProbabilityIndex = largestItemIndex(finalnearProbabilities);

    System.err.println("\nProbabilidade de estar perto e acertar para cada força:");
    for (int i = 0; i < finalnearProbabilities.length; i++) {
      System.err.print("F" + (i + 1) + ": " + finalnearProbabilities[i] + " ");
    }
    System.err.println();

    double[] finalFarProbabilities =
      probability(farAndHitProbability, sumFarHitsAndMissProbabilities);
    int bestFarProbabilityIndex = largestItemIndex(finalFarProbabilities);

    System.err.println("\nProbabilidade de estar longe e acertar para cada força:");
    for (int i = 0; i < finalFarProbabilities.length; i++) {
      System.err.print("F" + (i + 1) + ": " + finalFarProbabilities[i] + " ");
    }
    System.err.println();

    System.err.println("\nMelhor de Perto: " + (bestnearProbabilityIndex + 1));
    System.err.println("Melhor de Longe: " + (bestFarProbabilityIndex + 1));


    // Define poder de tiro para perto.
    return new double[] {
      powers[bestnearProbabilityIndex],
      powers[bestFarProbabilityIndex]
    };
  }

  public void onRoundEnded(RoundEndedEvent e) {
    round = getRoundNum();
    double [] bayesResult = bayes();

    if (round >= 1) {
      nearShotPower = bayesResult[0];
      farShotPower = bayesResult[1];
      System.err.print("\nForça do tiro Perto: " + nearShotPower + "\n");
      System.err.print("Força do tiro Longe: " + farShotPower + "\n");
    } else {
      System.err.println("\nA força do tiro será definidad no próximo round");
    }
  }

  /**
   * Recuar
   */
  public void reverseDirection() {
    if (go) {
      go = false;
      setBack(200);
    } else {
      go = true;
      setAhead(200);
    }
  }

  /**
   * Contabiliza os tiros, sendo que as duas útlimas posições indicam, respectivamente
   * o número de tiros de perto e o número de tiros de longe
   * @param shots
   * @return
   */
  public double[] countShots(ArrayList<int[]>shots) {
    double result[] = new double[numofvariables];

    for (int i = 0; i < shots.size(); i++) {
      int aux[] = shots.get(i);

      for (int j = 0; j < aux.length - 1; j++) {
        if (j == numofvariables - 2) {
          if (aux[j] == 1) {
            result[j] = result[j] + 1;
          } else {
            result[j + 1] = result[j + 1] + 1;
          }
        } else if (aux[j] == 1 && j != numofvariables - 1) {
          result[j] = result[j] + 1;
        }
      }
    }
    return result;
  }

  public double [] probability(double[] counts, double [] totals) {
    double [] probabilities = new double[counts.length];
    for (int i = 0; i < probabilities.length; i++) {
      probabilities[i] = probability(counts[i], totals[i]);
    }
    return probabilities;
  }

  public double [] probability(double[] counts, int total) {
    System.err.println(counts.length);
    double [] probabilities = new double[counts.length];
    for(int i = 0; i < probabilities.length; i++) {
      probabilities[i] = probability(counts[i], total);
    }
    return probabilities;
  }

  public double probability(double count, double total) {
    if (total == 0) {
      total = 1;
    }
    return count / total;
  }

  public double [] totalProbability(double[] a, double b) {
    double [] probabilities = new double[a.length];
    for(int i = 0; i < a.length; i++) {
      probabilities[i] = a[i] * b;
    }
    return probabilities;
  }

  public double[] maxPerIndex(double[] a, double[] b) {
    if (a.length != b.length) {
      throw new NullPointerException("a e b precisam ter o mesmo tamanho");
    }
    double [] max = new double[a.length];

    for (int i = 0; i < max.length; i++) {
      max[i] = Math.max(a[i], b[i]);
    }

    return max;
  }
  public double sumPositions(Object[] a) {
    double sum = 0;

    for (int i = 0; i < a.length; i++) {
      sum = sum + (double)((Integer)a[i]).intValue();
    }
    return sum;
  }


  public int sumPositions(int[] a) {
    Integer[] b = new Integer[a.length];
    for(int i = 0; i < a.length; i++) {
        b[i] = Integer.valueOf (a[i]); // returns Integer value
    }

    return (int) sumPositions((Object[])b);
  }

  public double [] sumPerIndex(double[] a, double[] b) {
    return sumPerIndex(a, b, null);
  }

  public double[] sumPerIndex(double[] a, double[] b, Double minimum) {
    if (a.length != b.length) {
      throw new NullPointerException("a e b precisam ter o mesmo tamanho");
    }
    double result[] = new double[a.length];
    for (int i = 0; i < result.length; i++) {
      if (minimum != null) {
        result[i] = Math.max(a[i] + b[i], minimum);
      } else {
        result[i] = a[i] + b[i];
      }
    }
    return result;
  }

  public int[] comparePerIndex(double[] a, double[] b) {
    if (a.length != b.length) {
      throw new NullPointerException("a e b precisam ter o mesmo tamanho");
    }

    int [] results = new int[a.length];

    for (int i = 0; i < results.length; i++) {
      results[i] = a[i] == b[i] ? 1 : 0;
    }
    return results;
  }

  public int largestItemIndex(double[] a) {
    if (a.length == 0) {
      throw new NullPointerException("a não pode ser vazio");
    }
    int index = 0;
    double max = a[0];
    for (int i = 0; i < a.length; i++) {
      if (a[i] > max) {
        max = a[i];
        index = i;
      }
    }
    return index;
  }
}
