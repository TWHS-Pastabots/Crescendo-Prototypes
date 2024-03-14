package frc.robot.subsystems.vision;

import java.util.ArrayList;

import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.networktables.DoubleArraySubscriber;
import edu.wpi.first.networktables.IntegerArraySubscriber;
import edu.wpi.first.networktables.IntegerSubscriber;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StringSubscriber;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class VisionTablesListener {
    private static VisionTablesListener instance;

    private NetworkTableInstance networkTable;
    private NetworkTable visionTable;
    final IntegerArraySubscriber tagIDSub;
    private IntegerArraySubscriber xCoordsSub;
    private IntegerArraySubscriber yCoordsSub;
    private IntegerArraySubscriber zRotsSub;
    private IntegerSubscriber bestIDSub;
    private IntegerSubscriber bestXSub;
    private IntegerSubscriber bestYSub;
    private IntegerSubscriber bestZSub;
    private IntegerArraySubscriber ringCenterXSub;
    private IntegerArraySubscriber ringCenterYSub;
    private IntegerArraySubscriber hommographyR1Sub;
    private IntegerArraySubscriber hommographyR2Sub;
    private IntegerArraySubscriber hommographyR3Sub;
    private IntegerSubscriber hammingSub;
    private IntegerSubscriber timeStampSub;

    private double yPose = 0;
    private double xPose = 0;
    private double zRot = 0;
    private double ringX = -1;
    private double ringY = -1;
    private int bestTagID = -1;
    private double bestTagX = -1;
    private double bestTagY = -1;
    private double bestTagZ = -1;
    private double hamming = -1;

    private String cam1Stream = null;

    private boolean tagVisible;

    private double timeStamp;

    private double[] homography;
    // private IntegerArraySubscriber xEulerSub;
    // private IntegerArraySubscriber yEulerSub;
    // private IntegerArraySubscriber zEulerSub;

    public VisionTablesListener() {
        networkTable = NetworkTableInstance.getDefault();
        visionTable = networkTable.getTable("Vision");
        tagIDSub = visionTable.getIntegerArrayTopic("IDs").subscribe(new long[] {});
        xCoordsSub = visionTable.getIntegerArrayTopic("X Coords").subscribe(new long[] {});
        yCoordsSub = visionTable.getIntegerArrayTopic("Y Coords").subscribe(new long[] {});
        zRotsSub = visionTable.getIntegerArrayTopic("Z Euler Angles").subscribe(new long[] {});
        ringCenterXSub = visionTable.getIntegerArrayTopic("Ring Center X Coords").subscribe(new long[] {});
        ringCenterYSub = visionTable.getIntegerArrayTopic("Ring Center Y Coords").subscribe(new long[] {});
        bestIDSub = visionTable.getIntegerTopic("Best Tag ID").subscribe(-1);
        bestXSub = visionTable.getIntegerTopic("Best Tag X").subscribe(-1);
        bestYSub = visionTable.getIntegerTopic("Best Tag Y").subscribe(-1);
        bestZSub = visionTable.getIntegerTopic("Best Tag Z").subscribe(-1);
        hommographyR1Sub = visionTable.getIntegerArrayTopic("Homography R1").subscribe(new long[] {});
        hommographyR2Sub = visionTable.getIntegerArrayTopic("Homography R2").subscribe(new long[] {});
        hommographyR3Sub = visionTable.getIntegerArrayTopic("Homography R3").subscribe(new long[] {});
        hammingSub = visionTable.getIntegerTopic("Best Ham").subscribe(-1);
        timeStampSub = visionTable.getIntegerTopic("Best Timestamp").subscribe(-1);

    }

    public void putInfoOnDashboard() {

        double ringCenterX[];
        double ringCenterY[];
        double[] xPoses;
        double[] yPoses;
        double[] zRots;
        double[] homographiesX;
        double[] homographiesY;
        double[] homographiesZ;

        double homogTest = -1;

        if (tagIDSub.get().length != 0) {
            yPoses = convertArray(yCoordsSub.get());
            xPoses = convertArray(xCoordsSub.get());
            zRots = convertArray(zRotsSub.get());
            homographiesX = convertArray(hommographyR1Sub.get());
            homographiesY = convertArray(hommographyR2Sub.get());
            homographiesZ = convertArray(hommographyR3Sub.get());
            homography = fuseHomographies(homographiesX, homographiesY);
            timeStamp = (double)timeStampSub.get() * 100000;

            // homogTest = (convertArray(hommographySub.get()))[0];
            // homographies = fillMatrix(hommographySub.get(), homo );

            tagVisible = true;
        } else {
            xPoses = new double[] { -.90 };
            yPoses = new double[] { -.67 };
            zRots = new double[] { .5 };
            tagVisible = false;
        }
        bestTagID = (int) bestIDSub.get();
        bestTagX = bestXSub.get();
        bestTagY = bestYSub.get();
        bestTagZ = bestZSub.get();
        hamming = hammingSub.get();
        SmartDashboard.putNumber("IDs", bestTagID);

        if (xPoses.length != 0) {
            xPose = xPoses[0];
            yPose = yPoses[0];
            zRot = zRots[0];

            // SmartDashboard.putNumberArray("IDs", convertArray(tagIDSub.get()));
            SmartDashboard.putNumber("X Coords", xPose);
            SmartDashboard.putNumber("Y Coords", yPose);
            SmartDashboard.putNumber("Z Rot", zRot);
            SmartDashboard.putBoolean("Tag in Sight", tagVisible);
            SmartDashboard.putNumber("Best Tag ID", bestTagID);
            if (homogTest != -1) {
                SmartDashboard.putNumber("Homography", homogTest);
            }
        }

        if (ringCenterXSub.get().length != 0) {
            ringCenterX = convertArray(ringCenterXSub.get());
            ringCenterY = convertArray(ringCenterYSub.get());
        } else {
            ringCenterX = new double[] { -1 };
            ringCenterY = new double[] { -1 };
        }

        ringX = ringCenterX[0];
        ringY = ringCenterY[0];
        SmartDashboard.putNumber("Ring X Coord", ringX);
        SmartDashboard.putNumber("Ring Y Coords", ringY);

    }

    public double[] fuseHomographies(double[] x, double[] y) {
        double[] fused = new double[x.length + y.length];
        for (int i = 0; i < x.length * 2; i++) {
            fused[i] = i % 2 == 0 ? x[i] : y[i];
        }

        return fused;
    }

    // need to convert each value to double individually, can't typecast entire
    // array
    private double[] convertArray(long[] arr) {
        double[] newArr = new double[arr.length];

        for (int i = 0; i < arr.length; i++)
            newArr[i] = (double) (arr[i]) / 1000.0;

        return newArr;
    }

    // private double[][][] convertHomography(long[][][] hom) {
    // for(int i = 0; i < hom.length; i++) {
    // for(int j = 0; j < hom[i].length; j++) {
    // hom[i][j] = convertArray(hom[i][j]);
    // }
    // }
    // return hom;
    // }

    public static VisionTablesListener getInstance() {
        if (instance == null)
            instance = new VisionTablesListener();
        return instance;
    }

    public double[] getHomography(){
        return homography != null ? homography : new double[9];
    }

    public boolean getTagVisible(){
        return tagVisible;
    }

    public int getHamming(){
        return (int)hamming;
    }

    public double getTimeStamp(){
        return timeStamp;
    }

    public double getRot() {
        return zRot;
    }

    public double getY() {
        return yPose;
    }

    public double getX() {
        return xPose;
    }

    public double getRingX() {
        return ringX;
    }

    public double getRingY() {
        return ringY;
    }

    public int getBestID() {
        return bestTagID;
    }

    public Translation3d getBestPos() {
        return new Translation3d(bestTagX, bestTagY, bestTagZ);
    }
}