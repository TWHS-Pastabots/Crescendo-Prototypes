// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;

import frc.robot.commands.BreakBeamHandoff;
import frc.robot.commands.HandoffCommand;
import frc.robot.commands.ShootCommand;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.intake.Intake;

import frc.robot.subsystems.launcher.Launcher;
import frc.robot.subsystems.launcher.Launcher.LauncherState;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.subsystems.swerve.Drivebase;
import frc.robot.subsystems.vision.AutoAlign;
import frc.robot.subsystems.vision.Cam1Align;
import frc.robot.subsystems.vision.VisionTablesListener;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.geometry.Translation3d;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */

  private Drivebase drivebase;
  private Climber climber;
  private Intake intake;
  private Launcher launcher;
  // private DigitalInputs digitalInputs;
  private AutoAlign autoAlign;
  // private LED litty;
  private VisionTablesListener visTables;
  private Cam1Align cam1;
  private Translation3d bestTagPos;
  private Translation3d currentPosTag;

  private static XboxController driver;
  private static XboxController operator;

  private Command m_autoSelected;

  private BreakBeamHandoff handoffCommand;
  private ShootCommand shootCommand;
  private HandoffCommand currentSpikeHandoff;

  private boolean useCurrentSpike;

  @Override
  public void robotInit() {
    drivebase = Drivebase.getInstance();
    launcher = Launcher.getInstance();
    intake = Intake.getInstance();
    climber = Climber.getInstance();
    autoAlign = AutoAlign.getInstance();
    visTables = VisionTablesListener.getInstance();
    cam1 = Cam1Align.getInstance();

    driver = new XboxController(0);
    operator = new XboxController(1);
    // drivebase.resetOdometry(new Pose2d(1, 1, new Rotation2d(0)));

    handoffCommand = new BreakBeamHandoff();
    shootCommand = new ShootCommand();

    currentSpikeHandoff = new HandoffCommand();

    // CameraServer.startAutomaticCapture(0);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    drivebase.periodic();

    visTables.putInfoOnDashboard();

    // launcher.launcherConnections();
    // intake.intakeConnections();
    // climber.climberConnections();

    // launcher.printConnections();
    // intake.printConnections();
    // climber.printConnections();

    SmartDashboard.putNumber("Gyro Angle:", drivebase.getHeading() + 90);
    SmartDashboard.putNumber("X-coordinate", drivebase.getPose().getX());
    SmartDashboard.putNumber("Y-coordinate", drivebase.getPose().getY());

    SmartDashboard.putString("Alliance", DriverStation.getAlliance().toString());

    SmartDashboard.putNumber("Flipper Current", intake.getFlipperCurrent());
    SmartDashboard.putNumber("Pivot Current", launcher.getPivotCurrent());
    SmartDashboard.putNumber("Roller Current", intake.getRollerCurrent());

    SmartDashboard.putNumber("Flipper Position", intake.getFlipperPosition());
    // SmartDashboard.putNumber("Launcher Position", launcher.getPosition());

    SmartDashboard.putString("Intake State", intake.getIntakeState().toString());
    SmartDashboard.putString("Launcher State", launcher.getLaunchState().toString());

    SmartDashboard.putBoolean("Launcher Breakbeam", launcher.getBreakBeam());
    // SmartDashboard.putBoolean("Intake Breakbeam", intake.getBreakBeam());

    // SmartDashboard.putBoolean("Shoot Done", autoSpeaker.isFinished());

    // visTables.putInfoOnDashboard();

    // SmartDashboard.putNumber("Frontleft Rotation",
    // drivebase.getModuleRotations()[0]);
    // SmartDashboard.putNumber("Frontright Rotation",
    // drivebase.getModuleRotations()[1]);
    // SmartDashboard.putNumber("Backleft Rotation",
    // drivebase.getModuleRotations()[2]);
    // SmartDashboard.putNumber("Backright Rotation",
    // drivebase.getModuleRotations()[3]);

    SmartDashboard.putNumber("Translational Velocity", drivebase.getTranslationalVelocity());
    SmartDashboard.putNumber("Angular Velocity", drivebase.getTurnRate());

    

    // bestTagPos = cam1.getBestTagAbsPos();
    // currentPosTag = visTables.getBestPos().plus(bestTagPos);
    // SmartDashboard.putNumber("X Pos from Tag", currentPosTag.getX());
    // SmartDashboard.putNumber("Y Pos from Tag", currentPosTag.getY());
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    if (m_autoSelected != null) {
      m_autoSelected.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
    // intake.updatePose();

    /* DRIVE CONTROLS */
    double ySpeed;
    double xSpeed;
    double rot;

    // ySpeed = -driver.getLeftX();
    // xSpeed = driver.getLeftY();
    // rot = -driver.getRightX();
    ySpeed = drivebase.inputDeadband(-driver.getLeftX());
    xSpeed = drivebase.inputDeadband(driver.getLeftY());
    rot = drivebase.inputDeadband(-driver.getRightX());

    if (driver.getXButton()) {
      drivebase.lockWheels();
    } else {
      drivebase.drive(xSpeed, ySpeed, rot, true);
    }

    // if (driver.getYButton()) {
    //   // launcher.setLauncherState(LauncherState.AMP);
    //   launcher.setReverseLauncherOn();
    //   launcher.setFlickerOn();
    // } else{
    //   launcher.setFlickOff();
    //   launcher.setLauncherOff();
    // }

    /* INTAKE CONTROLS */

    if (operator.getRightBumper() && !useCurrentSpike) {
      handoffCommand.schedule();
    } else if (operator.getRightBumper()) {
      currentSpikeHandoff.schedule();
    }

    if (operator.getYButton()) {
      // intake.setIntakeState(IntakeState.GROUND);
      // launcher.setLauncherState(LauncherState.LONG);
      launcher.updatePose();
    }

    if (operator.getBButton()) {
      intake.setReverseRollerPower();
    }

    if (operator.getLeftBumper()) {
      // intake.setIntakeState(IntakeState.STOP);
      // launcher.setLauncherState(LauncherState.HOVER);
      launcher.updatePose();
      launcher.setLauncherOff();
      launcher.setFlickOff();

    }

    // *CLIMBER CONTROLS */

    if (driver.getRightBumper()) {
      climber.setClimbingPower();
    } else if (driver.getLeftBumper()) {
      climber.setReverseClimberPower();
    } else {
      climber.setClimberOff();
    }

    /* LAUNCHER CONTROLS */

    // if (-operator.getRightY() > 0) {
    // launcher.setPivotPower();
    // } else if (-operator.getRightY() < 0) {
    // launcher.setReversePivotPower();
    // } else {
    // launcher.setPivotOff();
    // }

    // if (operator.getPOV() == 0) {
    //   launcher.setLauncherState(LauncherState.SPEAKER);
    // }
    // if (operator.getPOV() == 90) {
    //   launcher.setLauncherState(LauncherState.AMP);
    // }
    // if (operator.getPOV() == 180) {
    //   launcher.setLauncherState(LauncherState.TRAP);
    // }
    // if (operator.getPOV() == 270) {
    //   launcher.setLauncherState(LauncherState.LONG);
    // }

    if (operator.getXButton()) {
      intake.setReverseRollerPower();
      launcher.setFlickerReverse();

      launcher.setReverseLauncherOn();
    }

    if (operator.getRightTriggerAxis() > 0) {
      shootCommand.initialize();
      shootCommand.schedule();
    } else if (operator.getLeftTriggerAxis() > 0) {
      launcher.setLauncherOff();
      launcher.setFlickOff();
      intake.setRollerOff();
      shootCommand.cancel();
      handoffCommand.cancel();
      // litty.setBlue();
    }

    
   

  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void testInit() {
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }
}