package frc.robot.subsystems;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.units.Time;
import edu.wpi.first.wpilibj.Timer;
import frc.Ports;

public class Intake {

    private CANSparkMax roller;

    private double power = 0.5;

    private double threshold = 17.5;
    private double start = 0;

    private static Intake instance;

    public Intake(){
        roller = new CANSparkMax(Ports.Motor1, MotorType.kBrushless);
        roller.setSmartCurrentLimit(15);
        roller.burnFlash();
    }

    public void periodic(){

      if(roller.getOutputCurrent() > threshold){
        if(start == 0){
          start = Timer.getFPGATimestamp();
        }

        if( Timer.getFPGATimestamp() > start + 4){
          turnOff();
        }


          turnOff();
      }
    }

    public void intake(){
        roller.set(power);
    }

    public void outtake(){
        roller.set(-power);
    }

    public void increasePower() {
        power += .05;
        if (power > 1)
          power = 1;
      }
    
      public void decreasePower() {
        power -= .05;
        if (power < 0)
          power = 0;
      }

      public void turnOff(){
        power = 0.0;
        roller.set(power);
      }

      public double getCurrent(){
        return roller.getOutputCurrent();
      }
    
      public double getPower() {
        return power;
      }    

      public static Intake getInstance() {
        if (instance == null) {
          instance = new Intake();
        }
    
        return instance;
      }
}
