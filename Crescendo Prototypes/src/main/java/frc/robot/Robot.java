// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import frc.robot.subsystems.Launcher;
import frc.robot.subsystems.Intake;

public class Robot extends TimedRobot {

 
  private XboxController intakController = new XboxController(1);

  // private Launcher launcher;
  private Intake intake;

  @Override
  public void robotInit() {

    intake = Intake.getInstance();
  }

  @Override
  public void robotPeriodic() {

    SmartDashboard.putNumber("Intake Current", intake.getCurrent());
  }

  @Override
  public void autonomousInit() {
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
  }

  @Override
  public void teleopPeriodic() {

    intake.periodic();

    // SmartDashboard.putNumber("Launcher Power", launcher.getPower());
    SmartDashboard.putNumber("Intake Power", intake.getPower());
    SmartDashboard.putNumber("Intake Graph", intake.getPower());



    // *INTAKE CONTROLS* //
    if(intakController.getRightBumperPressed()){
      intake.intake();
    }

    if(intakController.getLeftBumperPressed()){
      intake.decreasePower();
      intake.intake();
    }

    if(intakController.getBButtonPressed()){
      intake.increasePower();
      intake.outtake();

    }

    if(intakController.getXButtonPressed()){
      intake.decreasePower();
      intake.outtake();
      
    }

    if (intakController.getAButton()) {
      intake.turnOff();
    }

    //* LAUNCHER CONTROLS */
    // if (launcherController.getRightBumperPressed()) {
    //   launcher.increasePower();
    //   launcher.launch();
    // }

    // if (launcherController.getLeftBumperPressed()) {
    //   launcher.decreasePower();
    //   launcher.launch();
    // }

    // if(launcherController.getBButtonPressed()){
    //   launcher.increasePower();
    //   launcher.out();
    // }

    // if(launcherController.getYButtonPressed()){
    //   launcher.increasePower();
    //   launcher.angle();
    // }

    // if(launcherController.getXButtonPressed()){
    //   launcher.decreasePower();
    //   launcher.out();
    // }

    // if (launcherController.getAButton()) {
    //   launcher.setPower(0);
    //   launcher.out();
    // }

    // if (operator.getRightTriggerAxis() > .1){
    // launcher.launch();
    // }

    // if (operator.getLeftTriggerAxis() > .1){
    // launcher.out();
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
