// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.Launcher;

public class Robot extends TimedRobot {

  private XboxController operator = new XboxController(0);

  private Launcher launcher;

  @Override
  public void robotInit() {
    launcher = Launcher.getInstance();
  }

  @Override
  public void robotPeriodic() {}

  @Override
  public void autonomousInit() {}

  @Override
  public void autonomousPeriodic() {}

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() {

    SmartDashboard.putNumber("Power", launcher.getPower());

    if(operator.getRightBumperPressed()){
      launcher.increasePower();
      launcher.out();
    }

     if(operator.getLeftBumperPressed()){
      launcher.decreasePower();
      launcher.out();
    }

    if(operator.getAButton()) {
      launcher.setPower(0);
      launcher.out();
    }

    // if (operator.getRightTriggerAxis() > .1){
    //     launcher.launch();
    // }

    // if (operator.getLeftTriggerAxis() > .1){
    //     launcher.out();
    // }
  }

  @Override
  public void disabledInit() {}

  @Override
  public void disabledPeriodic() {}

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}

  @Override
  public void simulationInit() {}

  @Override
  public void simulationPeriodic() {}
}
