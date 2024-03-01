// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.commands.BreakBeamHandoff;
import frc.robot.commands.ShootCommand;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.launcher.Launcher;
import frc.robot.subsystems.launcher.Launcher.LauncherState;
import edu.wpi.first.wpilibj.XboxController;
import frc.robot.subsystems.swerve.Drivebase;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.commands.PathPlannerAuto;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;

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

  private static XboxController driver;
  private static XboxController operator;

  private Command m_autoSelected;

  private BreakBeamHandoff handoff;
  private ShootCommand shootCommand;

  private SendableChooser<Command> m_chooser;

  @Override
  public void robotInit() {
    drivebase = Drivebase.getInstance();
    launcher = Launcher.getInstance();
    intake = Intake.getInstance();
    // climber = Climber.getInstance();

    driver = new XboxController(0);
    operator = new XboxController(1);
    drivebase.resetOdometry(new Pose2d(0.0, 0.0, new Rotation2d(0)));

    m_chooser = AutoBuilder.buildAutoChooser();

    SmartDashboard.putData("Auto choices", m_chooser);

    shootCommand = new ShootCommand();
    handoff = new BreakBeamHandoff();
  
    // CameraServer.startAutomaticCapture(0);
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    drivebase.periodic();

    // launcher.launcherConnections();
    // intake.intakeConnections();
    // climber.climberConnections();

    // launcher.printConnections();
    // intake.printConnections();
    // climber.printConnections();

    SmartDashboard.putNumber("Flipper Current", intake.getFlipperCurrent());
    SmartDashboard.putNumber("Pivot Current", launcher.getPivotCurrent());
    SmartDashboard.putNumber("Roller Current", intake.getRollerCurrent());

    SmartDashboard.putNumber("Flipper Position", intake.getFlipperPosition());
    SmartDashboard.putNumber("Launcher Position1", launcher.getPosition()[0]);
    SmartDashboard.putNumber("Launcher Position2", launcher.getPosition()[1]);


    SmartDashboard.putString("Intake State", intake.getIntakeState());
    SmartDashboard.putString("Launcher State", launcher.getLaunchState().toString());
    SmartDashboard.putBoolean("Done? ", handoff.isFinished());
    SmartDashboard.putBoolean("BreakBeam", launcher.getBreakBeam());

    SmartDashboard.putNumber("X-Coordinate", drivebase.getPose().getX());
    SmartDashboard.putNumber("Y-Coordinate", drivebase.getPose().getY());
    SmartDashboard.putNumber("Op Right Stick", -operator.getRightY());

  }

  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();

    // drivebase.resetPose(PathPlannerAuto.getStaringPoseFromAutoFile(m_chooser.getSelected().getName()));

    if (m_autoSelected != null) {
      m_autoSelected.schedule();
    }
  }

  @Override
  public void autonomousPeriodic() {
    intake.periodic();
    launcher.updatePose();
  }

  @Override
  public void teleopInit() {
    if (m_autoSelected != null) {
      m_autoSelected.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
    intake.periodic();
    launcher.updatePose();

    boolean fieldRelative = true;

    /* DRIVE CONTROLS */

    double ySpeed = driver.getLeftX();
    double xSpeed = -driver.getLeftY();
    double rot = driver.getRightX();

    if (driver.getYButton()) {
      fieldRelative = !fieldRelative;
    }
    if (driver.getAButton()) {
      drivebase.lockWheels();
    } else {
      drivebase.drive(xSpeed, ySpeed, rot, fieldRelative);
    }

    /* INTAKE CONTROLS */

    // if (operator.getRightBumper()) {
    //   handoff.schedule();
    // }

    // if(operator.getXButton()){
    // intake.setFlipperPower();
    // } else if(operator.getYButton()){
    // intake.setReverseFlipperPower();
    // } else {
    // intake.setFlipperOff();
    // }

    // *CLIMBER CONTROLS */

    // if (driver.getRightBumper()) {
    //   climber.setClimbingPower();
    // } else if (driver.getLeftBumper()) {
    //   climber.setReverseClimberPower();
    // } else {
    //   climber.setClimberOff();
    // }

    /* LAUNCHER CONTROLS */

    // if (operator.getRightY() > 0.1) {
    // launcher.setPivotPower();
    // } else if (operator.getRightY() < -0.1) {
    // launcher.setReversePivotPower();
    // } else {
    // launcher.setPivotOff();
    // }

    if (operator.getRightTriggerAxis() > 0) {
      launcher.setLauncherOn();
    }else if(operator.getRightBumper()){
      launcher.setFlickerOn();
    } else if (operator.getLeftTriggerAxis() > 0) {
      launcher.setReverseLauncherOn();
      launcher.setFlickerReverse();
    } else {
      launcher.setLauncherOff();
      launcher.setFlickOff();
    }

    if(operator.getAButton()){
      intake.setRollerPower();
    }
    else{
      intake.setRollerOff();
    }

    // if (operator.getAButton()) {
    //   launcher.setPivotState(LauncherState.HANDOFF);
    // } else if (operator.getBButton()) {
    //   launcher.setPivotState(LauncherState.SPEAKER);
    // }
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
