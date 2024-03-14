package frc.robot.subsystems.vision;
import edu.wpi.first.apriltag.AprilTagDetection;
import edu.wpi.first.apriltag.AprilTagPoseEstimator;
import edu.wpi.first.apriltag.AprilTagPoseEstimator.Config;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.vision.Cam1Align;

public class Localization {
    private double previousX;
    private double previousY;
    private double previousTheta;
    private Cam1Align cam1Align;
    private VisionTablesListener visTables;

    private AprilTagPoseEstimator poseEstimator;

    private static Localization instance;

    public Localization() {
        previousX = 0.0;    //need to figure out how to transfer starting coordinates from pathplanner from here
        previousY = 0.0;    //need to figure out how to transfer starting coordinates from pathplanner from here
        previousTheta = 0.0;    //need to figure out how to transfer starting coordinates from pathplanner from here
        cam1Align = Cam1Align.getInstance();
        visTables = VisionTablesListener.getInstance();

        Config config = new Config(0, 699.378, 677.716, 345.606,  207.127);

        poseEstimator = new AprilTagPoseEstimator(config);
    }

    public Transform3d getTransform3d() {
        // double[] tagAbsPos = cam1Align.getBestTagAbsPos();
        // double x = tagAbsPos[0];
        // double y = tagAbsPos[1];

        // double deltaX = x - previousX;
        // double deltaY = y - previousY;

        // Update previous position
        // previousX = x;
        // previousY = y;

        // Update heading (theta)
        // double theta = Math.atan2(deltaY, deltaX);

        // if (!Double.isNaN(theta) && !Double.isInfinite(theta)) {
        //     previousTheta = theta;
        // }

        // SmartDashboard.putNumber("X Coordinate", x);
        // SmartDashboard.putNumber("Y Coordinate", y);
        SmartDashboard.putNumber("Theta", Math.toDegrees(previousTheta));

        int id = visTables.getBestID();

        double centerX = visTables.getX();
        double centerY = visTables.getY();


        //.1 meters for comp
        //.216 in lab

        double length = .216;


        double[] corners = new double[]{
            centerX - length, centerY - length,
            centerX + length, centerY - length,
            centerX + length, centerY + length,
            centerX - length, centerY + length 
        };

        return poseEstimator.estimate(new AprilTagDetection("36h11", id, visTables.getHamming(), 70, 
        visTables.getHomography(), centerX, centerY, corners));
    }

    public double[] getCurrentPosition() {
        double[] currentPosition = new double[3];
        currentPosition[0] = previousX;
        currentPosition[1] = previousY;
        currentPosition[2] = previousTheta;
        return currentPosition;
    }

    public static Localization getInstance() {
        if (instance == null)
            instance = new Localization();
        return instance;
    }
}