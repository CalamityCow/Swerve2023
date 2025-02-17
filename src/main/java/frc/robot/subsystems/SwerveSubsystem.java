// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.subsystems;

import com.kauailabs.navx.frc.AHRS;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDriveOdometry;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import frc.robot.Constants.MotorConstants;
import frc.robot.Constants.SwerveConstants;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class SwerveSubsystem extends SubsystemBase {
  // private SwerveModule frontLeft;
  // private SwerveModule frontRight;
  // private SwerveModule backLeft;
  // private SwerveModule backRight;
  private SwerveModule[] modules;
  private SwerveDriveOdometry odometry;

  private final AHRS navX = new AHRS(SPI.Port.kMXP);

  /** Creates a new DriveTrain. */
  public SwerveSubsystem() {
    navX.reset();

    // frontLeft = new SwerveModule(Constants.MotorConstants.frontLeftDriveId,
    // Constants.MotorConstants.frontLeftSteerId);
    // frontRight = new SwerveModule(Constants.MotorConstants.frontRightDriveId,
    // Constants.MotorConstants.frontRightSteerId);
    // backLeft = new SwerveModule(Constants.MotorConstants.backLeftDriveId,
    // Constants.MotorConstants.backLeftSteerId);
    // backRight = new SwerveModule(Constants.MotorConstants.backRightDriveId,
    // Constants.MotorConstants.backRightSteerId);

    modules = new SwerveModule[] {
        new SwerveModule(
            MotorConstants.FRONT_LEFT_DRIVE_ID,
            MotorConstants.FRONT_LEFT_STEER_ID,
            MotorConstants.FRONT_LEFT_CAN_CODER_ID,
            Constants.SwerveConstants.CANCoderValue9),
        new SwerveModule(
            MotorConstants.FRONT_RIGHT_DRIVE_ID,
            MotorConstants.FRONT_RIGHT_STEER_ID,
            MotorConstants.FRONT_RIGHT_CAN_CODER_ID,
            Constants.SwerveConstants.CANCoderValue10),
        new SwerveModule(
            MotorConstants.BACK_LEFT_DRIVE_ID,
            MotorConstants.BACK_LEFT_STEER_ID,
            MotorConstants.BACK_LEFT_CAN_CODER_ID,
            Constants.SwerveConstants.CANCoderValue11),
        new SwerveModule(
            MotorConstants.BACK_RIGHT_DRIVE_ID,
            MotorConstants.BACK_RIGHT_STEER_ID,
            MotorConstants.BACK_RIGHT_CAN_CODER_ID,
            Constants.SwerveConstants.CANCoderValue12)
    };

    // Creating my odometry object from the kinematics object and the initial wheel
    // positions.
    // Here, our starting pose is 5 meters along the long end of the field and in
    // the
    // center of the field along the short end, facing the opposing alliance wall.
    odometry = new SwerveDriveOdometry(
        SwerveConstants.kinematics,
        getRotation2d(),
        getModulePositions(),
        Constants.SwerveConstants.STARTING_POSE);

    // new Thread(() -> {
    // try {
    // Thread.sleep(1000);
    // zeroHeading();
    // } catch (Exception e) {
    // }
    // }).start();
  }

  public void drive(double forwardSpeed,
      double leftSpeed, double rotationSpeed, boolean isFieldOriented) {
    ChassisSpeeds speeds;

    // System.out.println("NavX Angle " + navX.getAngle());

    if (isFieldOriented) {
      speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
          forwardSpeed,
          leftSpeed,
          rotationSpeed,
          Rotation2d.fromDegrees(navX.getAngle()));
    } else {
      speeds = new ChassisSpeeds(
          forwardSpeed,
          leftSpeed,
          rotationSpeed);
    }

    // System.out.println(speeds);

    SwerveModuleState[] states = SwerveConstants.kinematics.toSwerveModuleStates(speeds);
    // System.out.println(states);
    SwerveDriveKinematics.desaturateWheelSpeeds(
        states, MotorConstants.MAX_SPEED);

    for (int i = 0; i < modules.length; i++) {
      modules[i].setState(states[i]);

      // System.out.println(states[i]);
      // System.out.println(i);
    }
    
    updateAllSteerPositionSmartDashboard();
  }

  public SwerveModulePosition[] getModulePositions() {
    SwerveModulePosition[] positions = new SwerveModulePosition[modules.length];
    for (int i = 0; i < modules.length; i++) {
      positions[i] = modules[i].getPosition();
    }
    return positions;
  }

  public void resetPose(Rotation2d gyroAngle, SwerveModulePosition[] modulePositions, Pose2d newPose) {
    odometry.resetPosition(gyroAngle, modulePositions, newPose);
  }

  public void zeroHeading() {
    navX.reset();
  }

  public double getHeading() {
    return navX.getAngle() % 360;
  }

  /** Gets the NavX angle as a Rotation2d. */
  public Rotation2d getRotation2d() {
    return Rotation2d.fromDegrees(getHeading());
  }

  @Override
  public void periodic() {
    // This method will be called once per scheduler run
    SmartDashboard.putNumber("Robot Heading", getHeading());
    Rotation2d gyroAngle = getRotation2d();
    odometry.update(gyroAngle, getModulePositions());
  }

  public void stopModules() {
    for (SwerveModule module : modules) {
      module.stop();
    }
  }

  public void updateAllSteerPositionSmartDashboard() {
    for (SwerveModule currModule : modules) {
      currModule.updateSteerPositionSmartDashboard();
    }
  }

  public void test(int moduleNum, double driveSpeed, double rotationSpeed) {
    SwerveModule module = modules[moduleNum];

    module.setDriveSpeed(driveSpeed);
    module.setSteerSpeed(rotationSpeed);
  }

  public void steer(){
    modules[1].setSteerPosition(2);
  }
}
