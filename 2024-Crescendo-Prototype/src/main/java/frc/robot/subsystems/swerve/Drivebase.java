package frc.robot.subsystems.swerve;

import java.util.function.BooleanSupplier;

import com.kauailabs.navx.frc.AHRS;
import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.util.*;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.Vector;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.math.numbers.N3;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Ports;
import frc.robot.Constants.DriveConstants;
import frc.robot.subsystems.vision.Cam1Align;
import frc.robot.subsystems.vision.Localization;
import frc.robot.subsystems.vision.VisionTablesListener;

public class Drivebase extends SubsystemBase {
  private static Drivebase instance;

  private Localization localization;
  private Cam1Align cam1Align;
  private VisionTablesListener visTable;

  public SwerveModule frontLeft;
  public SwerveModule backLeft;
  public SwerveModule frontRight;
  public SwerveModule backRight;

  private HolonomicPathFollowerConfig config;

  private static AHRS gyro;
  private double currentRotation = 0.0;

  SwerveDriveOdometry odometry;
  SwerveDrivePoseEstimator poseEstimator;

  private static final Vector<N3> stateStdDevs = VecBuilder.fill(0.05, 0.05, Units.degreesToRadians(5));
  private static final Vector<N3> visionMeasurementStdDevs = VecBuilder.fill(0.5, 0.5, Units.degreesToRadians(10));

  Field2d fieldmap = new Field2d();

  public Drivebase() {

    localization = Localization.getInstance();
    cam1Align = Cam1Align.getInstance();
    visTable = VisionTablesListener.getInstance();

    // Swerve modules

    frontLeft = new SwerveModule(Ports.frontLeftDrive, Ports.frontLeftSteer,
        DriveConstants.kFrontLeftChassisAngularOffset, true);
    backLeft = new SwerveModule(Ports.backLeftDrive, Ports.backLeftSteer, DriveConstants.kBackLeftChassisAngularOffset,
        false);
    frontRight = new SwerveModule(Ports.frontRightDrive, Ports.frontRightSteer,
        DriveConstants.kFrontRightChassisAngularOffset, false);
    backRight = new SwerveModule(Ports.backRightDrive, Ports.backRightSteer,
        DriveConstants.kBackRightChassisAngularOffset, true);

    gyro = new AHRS(SPI.Port.kMXP);

    gyro.setAngleAdjustment(90);
    gyro.zeroYaw();

    odometry = new SwerveDriveOdometry(
        DriveConstants.kDriveKinematics,
        Rotation2d.fromDegrees(-gyro.getAngle()), new SwerveModulePosition[] {
            frontLeft.getPosition(),
            frontRight.getPosition(),
            backRight.getPosition(),
            backLeft.getPosition(),

        });

    poseEstimator = new SwerveDrivePoseEstimator(DriveConstants.kDriveKinematics, gyro.getRotation2d(),
        getPositions(), new Pose2d(), stateStdDevs, visionMeasurementStdDevs);

    config = new HolonomicPathFollowerConfig(new PIDConstants(1.2, 0, 0),
        new PIDConstants(0.048, 0, 0.0005),
        // .00012
        5, Math.sqrt(Math.pow(DriveConstants.kTrackWidth / 2, 2) +
            Math.pow(DriveConstants.kWheelBase / 2, 2)),
        new ReplanningConfig());

    SmartDashboard.putData("FIELD", fieldmap);

    AutoBuilder.configureHolonomic(this::getPose, this::resetOdometry, this::getSpeeds, this::setChassisSpeed, config,
        shouldFlipPath(), this);

  }

  public void setFieldPose(final Pose2d pose) {
    this.resetOdometry(pose);
  }

  public void periodic() {

    if(visTable.getTagVisible()){
      var tagPose = cam1Align.getBestTagAbsPos();

     Pose3d visPose = tagPose.transformBy(localization.getTransform3d().inverse());

      poseEstimator.addVisionMeasurement(visPose.toPose2d(), visTable.getTimeStamp());
    }
    poseEstimator.update(gyro.getRotation2d(), getPositions());

    // Update the odometry in the periodic block
    // odometry.update(Rotation2d.fromDegrees(-gyro.getAngle()),
    // new SwerveModulePosition[] {
    // frontLeft.getPosition(),
    // frontRight.getPosition(),
    // backRight.getPosition(),
    // backLeft.getPosition()
    // });

    fieldmap.setRobotPose(odometry.getPoseMeters().getX(), odometry.getPoseMeters().getY(),
        odometry.getPoseMeters().getRotation());
  }

  // Returns the currently-estimated pose of the robot
  public Pose2d getPose() {

    return poseEstimator.getEstimatedPosition();
    // return odometry.getPoseMeters();
  }

  // Resets the odometry to the specified pose
  public void resetOdometry(Pose2d pose) {
    odometry.resetPosition(
        Rotation2d.fromDegrees(-gyro.getAngle()),
        new SwerveModulePosition[] {
            frontLeft.getPosition(), frontRight.getPosition(), backLeft.getPosition(),
            backRight.getPosition()
        },
        pose);
  }

  public void resetOdometry() {
    odometry.resetPosition(
        Rotation2d.fromDegrees(-gyro.getAngle()),
        new SwerveModulePosition[] {
            frontLeft.getPosition(), frontRight.getPosition(), backLeft.getPosition(),
            backRight.getPosition()
        },
        getPose());
  }

  public void drive(double forward, double side, double rot, boolean fieldRelative) {

    double xSpeedCommanded;
    double ySpeedCommanded;

    xSpeedCommanded = side;
    ySpeedCommanded = forward;
    currentRotation = rot;

    // Convert the commanded speeds into the correct units for the drivetrain
    double xSpeedDelivered = xSpeedCommanded * DriveConstants.kMaxSpeedMetersPerSecond;
    double ySpeedDelivered = ySpeedCommanded * DriveConstants.kMaxSpeedMetersPerSecond;
    double rotDelivered = currentRotation * DriveConstants.kMaxAngularSpeed;

    var chassisspeeds = ChassisSpeeds.fromRobotRelativeSpeeds(xSpeedDelivered, ySpeedDelivered, rotDelivered,
        Rotation2d.fromDegrees(gyro.getAngle()));
    new ChassisSpeeds();

    setChassisSpeed(chassisspeeds);
  }

  public void setChassisSpeed(ChassisSpeeds input) {
    var speeds = ChassisSpeeds.discretize(input, 0.02);

    var swerveModuleStates = DriveConstants.kDriveKinematics.toSwerveModuleStates(speeds);

    SwerveDriveKinematics.desaturateWheelSpeeds(
        swerveModuleStates, DriveConstants.kMaxSpeedMetersPerSecond);

    frontLeft.setDesiredState(swerveModuleStates[0]);
    frontRight.setDesiredState(swerveModuleStates[1]);
    backLeft.setDesiredState(swerveModuleStates[2]);
    backRight.setDesiredState(swerveModuleStates[3]);
  }

  public SwerveModulePosition[] getPositions() {
    return new SwerveModulePosition[] {
        frontLeft.getPosition(),
        frontRight.getPosition(),
        backLeft.getPosition(),
        backRight.getPosition()
    };
  }

  public double getTranslationalVelocity() {
    return frontLeft.getTranslationalVelocity();
  }

  public SwerveModuleState[] getModuleStates() {
    return new SwerveModuleState[] {
        frontLeft.getState(),
        frontRight.getState(),
        backLeft.getState(),
        backRight.getState()
    };
  }

  public ChassisSpeeds getSpeeds() {
    return DriveConstants.kDriveKinematics.toChassisSpeeds(getModuleStates());
  }

  public void resetPose(final Pose2d pose) {
    odometry.resetPosition(gyro.getRotation2d(), getPositions(), pose);
  }

  public void lockWheels() {
    frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
    frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
    backLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(45)));
    backRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-45)));
  }

  public void wheelsTo90() {
    frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-90)));
    frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(90)));
    backLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(90)));
    backRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(-90)));
  }

  public void wheelsTo0() {
    frontLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(0)));
    frontRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(0)));
    backLeft.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(0)));
    backRight.setDesiredState(new SwerveModuleState(0, Rotation2d.fromDegrees(0)));
  }

  // sets state for all modules
  public void setModuleStates(SwerveModuleState[] desiredStates) {
    SwerveDriveKinematics.desaturateWheelSpeeds(
        desiredStates, DriveConstants.kMaxSpeedMetersPerSecond);
    frontLeft.setDesiredState(desiredStates[0]);
    frontRight.setDesiredState(desiredStates[1]);
    backLeft.setDesiredState(desiredStates[2]);
    backRight.setDesiredState(desiredStates[3]);
  }

  // sets drive encoders to 0
  public void resetEncoders() {
    frontLeft.resetEncoders();
    backLeft.resetEncoders();
    frontRight.resetEncoders();
    backRight.resetEncoders();
  }

  /** Zeroes the heading of the robot. */
  public void zeroHeading() {
    gyro.reset();
  }

  // Returns the heading of the robot(=180 to 180)
  public double getHeading() {
    return Rotation2d.fromDegrees(-gyro.getAngle()).getDegrees();
  }

  public BooleanSupplier shouldFlipPath() {
    return new BooleanSupplier() {
      @Override
      public boolean getAsBoolean() {
        var alliance = DriverStation.getAlliance();
        if (alliance.isPresent()) {
          return alliance.get() == DriverStation.Alliance.Red;
        }
        return false;
      }
    };
  }

  // Returns the turn rate of the robot
  public double getTurnRate() {
    return gyro.getRate();
  }

  public double inputDeadband(double input) {

    return Math.abs(input) > .05 ? input : 0;
  }

  public double[] getModuleRotations() {
    return new double[] {
        frontLeft.getModuleAngle(),
        frontRight.getModuleAngle(),
        backLeft.getModuleAngle(),
        backRight.getModuleAngle()
    };
  }

  public static Drivebase getInstance() {
    if (instance == null) {
      instance = new Drivebase();
    }
    return instance;
  }

}