// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.Ports;

public class Launcher extends SubsystemBase {

  private static Launcher instance;

  private CANSparkMax leftMotor;
  private CANSparkMax rightMotor;


  private double power = .7;

  public Launcher() {
    leftMotor = new CANSparkMax(Ports.LeftMotor, MotorType.kBrushless);
    rightMotor = new CANSparkMax(Ports.RightMotor, MotorType.kBrushless);

    leftMotor.setSmartCurrentLimit(25);
    leftMotor.setInverted(true);
    leftMotor.burnFlash();

    rightMotor.setSmartCurrentLimit(25);
    rightMotor.burnFlash();

  }

  @Override
  public void periodic() {
  }

  public void launch(){
    leftMotor.set(power);
    rightMotor.set(power);
  }

  public void out(){
    leftMotor.set(-power);
    rightMotor.set(-power);
  }

  public void increasePower(){
    power += .05;
  }

    public void decreasePower(){
    power -= .05;
  }

  public double getPower(){
    return power;
  }

  public static Launcher getInstance(){
    if (instance == null){
      instance = new Launcher();
    }

    return instance;
  }
}
