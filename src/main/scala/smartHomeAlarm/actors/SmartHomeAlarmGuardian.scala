package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.{Behavior, *}
import smartHomeAlarm.smartHomeAlarmProtocol.cuStates.Disarmed
import smartHomeAlarm.smartHomeAlarmProtocol.sensorsType.{PIRDoor, PIRLivingRoom, WindowSensor}
import smartHomeAlarm.smartHomeAlarmProtocol.{AlarmStarting, MotionDetected, TryPin, cuStates}

import java.util.UUID
import scala.concurrent.duration.DurationInt

object SmartHomeAlarmGuardian:

  //spawn actors e set up

  enum Command:
    case DetectedMovement(event: MotionDetected)
    case PinEntered(event: TryPin)
    case AlarmStarted(event: AlarmStarting)
    case FinishTimer()

  export Command.*

  val pin: String = "1234"

  private case object exitTimerKey

  private val UUIDDoor = UUID.randomUUID()
  private val UUIDLivingRoom = UUID.randomUUID()
  private val UUIDWindow1 = UUID.randomUUID()
  private val UUIDWindow2 = UUID.randomUUID()
  private val varExitDelay = 15.seconds
  private val varEntryDelay = 20.seconds

  //finite state machine

  def apply():
  Behavior[Command] =
    Behaviors.setup: context =>
      val motionAdapter = context.messageAdapter[MotionDetected](DetectedMovement.apply)

      val sensorDoor = context.spawn(Sensor(PIRDoor, UUIDDoor, motionAdapter), "SensorDoor")
      val sensorLivingRoom = context.spawn(Sensor(PIRLivingRoom, UUIDLivingRoom, motionAdapter), "SensorLivingRoom")
      val sensorWindow1 = context.spawn(Sensor(WindowSensor, UUIDWindow1, motionAdapter), "SensorWindow1")
      val sensorWindow2 = context.spawn(Sensor(WindowSensor, UUIDWindow2, motionAdapter), "SensorWindow2")

      val alarmAdapter = context.messageAdapter[AlarmStarting](AlarmStarted.apply)
      val alarm = context.spawn(Alarm(alarmAdapter), "Alarm")

      val pinAdapter = context.messageAdapter[TryPin](PinEntered.apply)
      val userInteraction = context.spawn(UserInteraction(pinAdapter), "keyboardPin")

      disarmed(context, alarm)


  private def disarmed(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                      ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(TryPin(triedPin)) =>
          if (triedPin.equals(pin)) then
            context.log.info("PIN correct. Going to exit delay.")
            exitDelay(context, alarm)
          else {
            context.log.warn("Incorrect PIN. Retry.")
            Behaviors.same
          }
        case _ => Behaviors.same


  private def exitDelay(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                       ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      Behaviors.withTimers { timers =>
        timers.startSingleTimer(exitTimerKey, FinishTimer(), varExitDelay)

        message match
          case FinishTimer() =>
            context.log.info("Exit delay finished. Going to armed.")
            armed(context, alarm)
          case PinEntered(_) => Behaviors.same
          case Command.DetectedMovement(_) => Behaviors.same
          case _ => Behaviors.same

      }


  private def armed(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                   ): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        case PinEntered(_) => Behaviors.same
        case Command.DetectedMovement(MotionDetected(sensorID)) =>
          context.log.info("Going to entry delay.")
          alarm ! Alarm.TurnOn()
          entryDelay(context, alarm)
        case _ => Behaviors.same


  private def entryDelay(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                        ): Behavior[Command] =
    
    Behaviors.receiveMessagePartial:

      case PinEntered(TryPin(triedPin)) =>
        if (triedPin.equals(pin)) then {
          context.log.info("PIN correct. Turning off the alarm and going to disarmed.")
          alarm ! Alarm.Off() //diciamo all'allarme di fermarsi
          disarmed(context, alarm)
          Behaviors.same
        } else {
          context.log.warn("incorrect PIN. Retry.")
          Behaviors.same
        }

      case AlarmStarted(_) =>
        context.log.error("going to emergency.")
        emergency(context, alarm)
        Behaviors.same

      case Command.DetectedMovement(_) => Behaviors.same

      case _ => Behaviors.same


    Behaviors.same

  private def emergency(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                       ): Behavior[Command] =
    context.log.warn("RING RING RING!")
    Behaviors.receiveMessagePartial:

      case PinEntered(TryPin(triedPin))  =>
        if (triedPin.equals(pin)) then {
          context.log.info("PIN correct. Emergency stopped. Going to disarmed.")
          alarm ! Alarm.Off() //diciamo all'allarme di fermarsi
          disarmed(context, alarm)
          Behaviors.same
        } else {
          context.log.warn("incorrect PIN. RING RING RING! Retry.")
          Behaviors.same
        }

      case _ => Behaviors.same