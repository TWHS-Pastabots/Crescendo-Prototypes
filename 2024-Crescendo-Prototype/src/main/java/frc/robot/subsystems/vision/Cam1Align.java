package frc.robot.subsystems.vision;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation3d;

public class Cam1Align {
    public static Cam1Align instance;
    private VisionTablesListener vListener;

    private double xPose;
    private double yPose;
    private double zRot;
    private Pose3d bestTagAbsPos;

    private PIDController[] controllers = {
        new PIDController(3.0, 0, 0),
        new PIDController(3.0, 0, 0),
        new PIDController(1.2, 0, 0)
    };

    private double[] speakerXYRotSP = {-0.90, 0.67, 0};
    private double[] ampXYRotSP = {-0.90, 0.9, 0};
    private double[] sourceXYRotSP = {-0.90, 0.8, 0};
    private double[] stageXYRotSP = {-0.90, 0.7, 0};

    public Cam1Align() {
        vListener = VisionTablesListener.getInstance();
    }

    public static Cam1Align getInstance() {
        if(instance == null) {
            instance = new Cam1Align();
        }
        return instance;
    }

    public double[] getAlignmentPos() {
        int id = (int)vListener.getBestID();
        
        if(id == 3 || id == 4 || id == 7 || id == 8) {
            return speakerXYRotSP;
        } else if (id == 5 || id == 6) {
            return ampXYRotSP;
        } else if(id == 1 || id == 2 || id == 9 || id == 10) {
            return sourceXYRotSP;
        } else {
            return stageXYRotSP;
        }
    }
    
    

    public Pose3d getBestTagAbsPos() {
        int id = (int)vListener.getBestID();
        //return format: [x, y, z, rot (degrees)]
        // ID X     Y    Z     Rotation
        // 1 593.68 9.68 53.38 120°
        // 2 637.21 34.79 53.38 120°
        // 3 652.73 196.17 57.13 180°
        // 4 652.73 218.42 57.13 180°
        // 5 578.77 323.00 53.38 270°
        // 6 72.5 323.00 53.38 270°
        // 7 -1.50 218.42 57.13 0°
        // 8 -1.50 196.17 57.13 0°
        // 9 14.02 34.79 53.38 60°
        // 10 57.54 9.68 53.38 60°
        // 11 468.69 146.19 52.00 300°
        // 12 468.69 177.10 52.00 60°
        // 13 441.74 161.62 52.00 180°
        // 14 209.48 161.62 52.00 0°
        // 15 182.73 177.10 52.00 120°
        // 16 182.73 146.19 52.00 240°
        switch(id) {
            case 1:
                bestTagAbsPos = new Pose3d(new Translation3d(593.68, 9.68, 53.38), new Rotation3d());
                break;
            case 2:
                bestTagAbsPos = new Pose3d(new Translation3d(637.21, 34.79, 53.38), new Rotation3d());
                break;
            case 3:
                bestTagAbsPos= new Pose3d(new Translation3d(652.73, 196.17, 57.13), new Rotation3d());
                break;
            case 4:
                bestTagAbsPos= new Pose3d(new Translation3d(652.73, 218.42, 57.13), new Rotation3d());
                break;
            case 5:
                bestTagAbsPos= new Pose3d(new Translation3d(578.77, 323.00, 53.38), new Rotation3d());
                break;
            case 6:
                bestTagAbsPos= new Pose3d(new Translation3d(72.5, 323.00, 53.38), new Rotation3d(0,0, -90));
                break;
            case 7:
                bestTagAbsPos= new Pose3d(new Translation3d(-1.50, 218.42, 57.13), new Rotation3d());
                break;
            case 8:
                bestTagAbsPos= new Pose3d(new Translation3d(-1.50, 196.17, 57.13), new Rotation3d());
                break;
            case 9:
                bestTagAbsPos= new Pose3d(new Translation3d(14.02, 34.79, 53.38), new Rotation3d());
                break;
            case 10:
                bestTagAbsPos= new Pose3d(new Translation3d(57.54, 9.68, 53.38), new Rotation3d());
                break;
            case 11:
                bestTagAbsPos= new Pose3d(new Translation3d(468.69, 146.19, 52.00), new Rotation3d());
                break;
            case 12:
                bestTagAbsPos= new Pose3d(new Translation3d(468.69, 177.10, 52.00), new Rotation3d());
                break;
            case 13:
                bestTagAbsPos= new Pose3d(new Translation3d(441.74, 161.62, 52.00), new Rotation3d());
                break;
            case 14:
                bestTagAbsPos= new Pose3d(new Translation3d(209.48, 161.62, 52.00), new Rotation3d());
                break;
            case 15:
                bestTagAbsPos= new Pose3d(new Translation3d(182.73, 177.10, 52.00), new Rotation3d());
                break;
            case 16:
                bestTagAbsPos= new Pose3d(new Translation3d(182.73, 146.19, 52.00), new Rotation3d());
                break;
            default:
                bestTagAbsPos = null;
        }

        return bestTagAbsPos;
    }

    public double getYSpeed(PIDController controller){
        yPose = vListener.getY();
        double[] SP = getAlignmentPos();

        if(!reachYPose(0.05, yPose)) {
            return controllers[1].calculate(yPose, SP[1]);
        } 
        return 0;
    }

    public double getXSpeed(PIDController controller){
        xPose = vListener.getX();
        double[] SP = getAlignmentPos();

        if(!reachYPose(0.05, xPose)) {
            return controllers[0].calculate(xPose, SP[0]);
        }
        return 0;

    }
    public double getRotSpeed(PIDController controller){
        zRot = vListener.getRot();
        double[] SP = getAlignmentPos();

        if(!reachYPose(0.05, zRot)) {
            return controllers[2].calculate(zRot, SP[2]);
        }
        return 0;
    }

    public boolean reachXPose(double tolerance, double measurment){
        double[] SP = getAlignmentPos();
        if (Math.abs(measurment - SP[0]) < tolerance){
            return true;
        }
        return false;
    }

    public boolean reachYPose(double tolerance, double measurment){
        double[] SP = getAlignmentPos();
        if (Math.abs(measurment - SP[1]) < tolerance){
            return true;
        }
        return false;
    }

    public boolean reachRot(double tolerance, double measurment){
        double[] SP = getAlignmentPos();
        if (Math.abs(measurment - SP[2]) < tolerance){
            return true;
        }
        return false;
    }
}