package frc.robot.subsystems.launcher;

import com.revrobotics.CANSparkMax;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.SparkMaxPIDController;

import edu.wpi.first.math.controller.ArmFeedforward;
import edu.wpi.first.math.trajectory.TrapezoidProfile;
import edu.wpi.first.math.trajectory.TrapezoidProfile.Constraints;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import com.revrobotics.CANSparkMax.IdleMode;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import frc.robot.Constants.LauncherConstants;
import frc.robot.subsystems.intake.Intake;
import frc.robot.Ports;

public class Launcher {

    public enum LauncherState {
        AMP(-65, 0.25),
        STOP(-1.809524536132812, 0.0),
        HOLD(-18.714231491088867, 0.0),
        TRAP(-74.04991149902344, 0.6),
        HANDOFF(14.92857551574707, 0.25),
        SPEAKER(-67.0, 1.0);

        public double position;
        public double launchSpeed;

        private LauncherState(double position, double launchSpeed) {
            this.position = position;
            this.launchSpeed = launchSpeed;
        }
    }

    double pivotPower = 0.25;

    private CANSparkMax shootMotor1;
    private CANSparkMax shootMotor2;

    private CANSparkMax flicker;

    private CANSparkMax pivotMotor1;
    private CANSparkMax pivotMotor2;

    private ArmFeedforward feedForward;
    private SparkMaxPIDController pivotController1;
    private SparkMaxPIDController pivotController2;

    private TrapezoidProfile motionProfile;
    private TrapezoidProfile.State goal = new TrapezoidProfile.State();
    private TrapezoidProfile.State setPoint = new TrapezoidProfile.State(LauncherState.STOP.position, 0);

    private double veloSP = 4000;
    private double maxAccel = 3000;

    private static RelativeEncoder encoder1;
    private static RelativeEncoder encoder2;

    private boolean[] connections = new boolean[10];

    private static LauncherState launchState = LauncherState.STOP;

    public static Launcher instance;
    public static Intake intake;

    public Launcher() {
        shootMotor1 = new CANSparkMax(Ports.shootMotor1, MotorType.kBrushless);
        shootMotor1.restoreFactoryDefaults();

        shootMotor1.setSmartCurrentLimit(60);
        shootMotor1.setIdleMode(IdleMode.kCoast);
        shootMotor1.setInverted(false);
        shootMotor1.burnFlash();

        shootMotor2 = new CANSparkMax(Ports.shootMotor2, MotorType.kBrushless);
        shootMotor2.restoreFactoryDefaults();

        shootMotor2.setSmartCurrentLimit(60);
        shootMotor2.setIdleMode(IdleMode.kCoast);
        shootMotor2.setInverted(false);
        shootMotor2.burnFlash();

        flicker = new CANSparkMax(Ports.flicker, MotorType.kBrushless);
        flicker.restoreFactoryDefaults();

        flicker.setSmartCurrentLimit(20);
        flicker.setIdleMode(IdleMode.kBrake);
        flicker.setInverted(false);
        flicker.burnFlash();

        pivotMotor1 = new CANSparkMax(Ports.pivotMotor1, MotorType.kBrushless);
        pivotMotor1.restoreFactoryDefaults();

        pivotMotor1.setSmartCurrentLimit(60);
        pivotMotor1.setIdleMode(IdleMode.kBrake);
        pivotMotor1.setInverted(true);
        pivotMotor1.setOpenLoopRampRate(1);

        pivotMotor2 = new CANSparkMax(Ports.pivotMotor2, MotorType.kBrushless);
        pivotMotor2.restoreFactoryDefaults();

        pivotMotor2.setIdleMode(IdleMode.kBrake);
        pivotMotor2.setSmartCurrentLimit(60);
        pivotMotor2.setInverted(false);
        pivotMotor2.setOpenLoopRampRate(1);
        pivotMotor2.burnFlash();

        feedForward = new ArmFeedforward(0.085, 0.037, 0.0, 0.0);
        motionProfile = new TrapezoidProfile(new Constraints(veloSP, maxAccel));

        // Prototype numbers
        // upper: .045 lower: .0285 ks:.0085 kg:.037

        encoder1 = pivotMotor1.getEncoder();
        encoder2 = pivotMotor2.getEncoder();

        pivotController1 = pivotMotor1.getPIDController();
        pivotController2 = pivotMotor2.getPIDController();

        pivotController1.setP(LauncherConstants.pivotPCoefficient);
        pivotController1.setI(LauncherConstants.pivotICoefficient);
        pivotController1.setD(LauncherConstants.pivotDCoefficient);

        pivotController2.setP(LauncherConstants.pivotPCoefficient);
        pivotController2.setI(LauncherConstants.pivotICoefficient);
        pivotController2.setD(LauncherConstants.pivotDCoefficient);

        pivotController1.setFeedbackDevice(encoder1);
        pivotController2.setFeedbackDevice(encoder2);

        pivotController1.setOutputRange(-1, 1);
        pivotController2.setOutputRange(-1, 1);

        pivotMotor1.burnFlash();
        pivotMotor2.burnFlash();

        intake = Intake.getInstance();
    }

    public void periodic() {

        setPoint = motionProfile.calculate(0.02, setPoint, goal);

        pivotController1.setReference(launchState.position, CANSparkMax.ControlType.kPosition, 0,
                feedForward.calculate(encoder1.getPosition(), 0));

          pivotController2.setReference(launchState.position, CANSparkMax.ControlType.kPosition, 0,
                feedForward.calculate(encoder2.getPosition(), 0));

    }

    public double[] getConstants() {
        return new double[] { pivotController1.getP(), pivotController1.getI(), pivotController1.getD() };
    }

    public void setPivotPower() {
        pivotMotor1.set(pivotPower + feedForward.calculate(encoder1.getPosition(), veloSP));
        pivotMotor2.set(pivotPower + feedForward.calculate(encoder2.getPosition(), veloSP));
    }

    public void setReversePivotPower() {
        pivotMotor1.set(-pivotPower + feedForward.calculate(encoder1.getPosition(), veloSP));
        pivotMotor2.set(pivotPower + feedForward.calculate(encoder2.getPosition(), veloSP));

    }

    public void setPivotOff() {
        pivotMotor1.set(0.0);
        pivotMotor2.set(0.0);
    }

    public void setLauncherOn() {
        shootMotor1.set(launchState.launchSpeed);
        shootMotor2.set(launchState.launchSpeed);
    }

    public void setReverseLauncherOn() {
        shootMotor1.set(-launchState.launchSpeed);
        shootMotor2.set(-launchState.launchSpeed);
    }

    public void setLauncherOff() {
        shootMotor1.set(0.0);
        shootMotor2.set(0.0);
    }

    public void setFlickerOn() {
        flicker.set(.5);
    }

    public void setFlickerReverse() {
        flicker.set(-.5);
    }

    public void setFlickOff() {
        flicker.set(0);
    }

    public double[] getPosition() {
        return new double[]{encoder1.getPosition(), encoder2.getPosition()};
    }

    public LauncherState getLaunchState() {
        return launchState;
    }

    public double getPivotCurrent() {
        return pivotMotor1.getOutputCurrent();
    }

    public boolean hasReachedPose(double tolerance) {
        if (Math.abs(getPosition()[0] - launchState.position) > tolerance) {
            return true;
        }
        return false;
    }

    public void setPivotState(LauncherState state) {
        launchState = state;
        goal = new TrapezoidProfile.State(launchState.position, 0);
    }

    public double getVelocity() {
        return encoder1.getVelocity();
    }

    public double getVelocityGoal() {
        return setPoint.velocity;
    }

    public boolean[] launcherConnections() {

        if (shootMotor1.getBusVoltage() != 0) {
            connections[0] = true;
        } else {
            connections[0] = false;
        }

        if (shootMotor1.getOutputCurrent() != 0) {
            connections[1] = true;
        } else {
            connections[1] = false;
        }

        if (shootMotor2.getBusVoltage() != 0) {
            connections[2] = true;
        } else {
            connections[2] = false;
        }

        if (shootMotor2.getOutputCurrent() != 0) {
            connections[3] = true;
        } else {
            connections[3] = false;
        }

        if (pivotMotor1.getBusVoltage() != 0) {
            connections[4] = true;
        } else {
            connections[4] = false;
        }

        if (pivotMotor1.getOutputCurrent() != 0) {
            connections[5] = true;
        } else {
            connections[5] = false;
        }

        if (pivotMotor2.getBusVoltage() != 0) {
            connections[6] = true;
        } else {
            connections[6] = false;
        }


        if (pivotMotor2.getOutputCurrent() != 0) {
            connections[7] = true;
        } else {
            connections[7] = false;
        }

        if (flicker.getBusVoltage() != 0) {
            connections[8] = true;
        } else {
            connections[8] = false;
        }

        if (flicker.getOutputCurrent() != 0) {
            connections[9] = true;
        } else {
            connections[9] = false;
        }

        return connections;
    }

    public void printConnections() {
        SmartDashboard.putBoolean("shootMotor1 Voltage", connections[0]);
        SmartDashboard.putBoolean("shootMotor1 Current", connections[1]);

        SmartDashboard.putBoolean("shootMotor2 Voltage", connections[2]);
        SmartDashboard.putBoolean("shootMotor2 Current", connections[3]);

        SmartDashboard.putBoolean("Pivot Voltage", connections[4]);
        SmartDashboard.putBoolean("Pivot Current", connections[5]);

        SmartDashboard.putBoolean("Flicker Voltage", connections[6]);
        SmartDashboard.putBoolean("Flicker Current", connections[7]);
    }

    public static Launcher getInstance() {
        if (instance == null)
            instance = new Launcher();
        return instance;
    }
}
