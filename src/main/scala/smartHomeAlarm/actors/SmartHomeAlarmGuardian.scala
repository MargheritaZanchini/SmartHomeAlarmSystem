package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.{Behavior, *}
import smartHomeAlarm.smartHomeAlarmProtocol.cuStates.Disarmed
import smartHomeAlarm.smartHomeAlarmProtocol.sensorsType.{PIRDoor, PIRLivingRoom, WindowSensor}
import smartHomeAlarm.smartHomeAlarmProtocol.{AlarmStarting, MotionDetected, TryPin, cuStates}

import java.util.UUID

object SmartHomeAlarmGuardian:
  //spawn actors e set up

  enum Command:
    case DetectedMovement(event: MotionDetected)
    case PinEntered(event: TryPin)
    case AlarmStarted(event: AlarmStarting)


  export Command.*
  val pin: String = "1234"
  private val UUIDDoor = UUID.randomUUID()
  private val UUIDLivingRoom = UUID.randomUUID()
  private val UUIDWindow1 = UUID.randomUUID()
  private val UUIDWindow2 = UUID.randomUUID()
  
  //finite state machine

  def apply() :
    Behavior[Command] =
    Behaviors.setup: context =>
      val motionAdapter = context.messageAdapter[MotionDetected](DetectedMovement.apply)
      
      val sensorDoor = context.spawn(Sensor(PIRDoor, UUIDDoor, motionAdapter), "SensorDoor")
      val sensorLivingRoom = context.spawn(Sensor(PIRLivingRoom, UUIDLivingRoom, motionAdapter), "SensorLivingRoom")
      val sensorWindow1 = context.spawn(Sensor(WindowSensor, UUIDWindow1, motionAdapter), "SensorWindow1")
      val sensorWindow2 = context.spawn(Sensor(WindowSensor, UUIDWindow2, motionAdapter), "SensorWindow2")

      //val AlarmAdapter = context.messageAdapter()
      val alarm = context.spawn(Alarm(), "Alarm")

      val pinAdapter = context.messageAdapter[TryPin](PinEntered.apply)
      val userInteraction = context.spawn(UserInteraction(pinAdapter), "keyboardPin")

      disarmed(context)



  private def disarmed(context: ActorContext[Command],

                      ): Behavior[Command] =
    Behaviors.receive:(context, message) =>
      message match
        case PinEntered(TryPin(triedPin)) =>
          if( triedPin.equals(pin)) then
            exitDelay()
        case Command.DetectedMovement(_) => Behaviors.same
        case Command.AlarmStarted(_) => Behaviors.same

      Behaviors.same


  private def exitDelay() = ???
  private def entryDelay() = ???