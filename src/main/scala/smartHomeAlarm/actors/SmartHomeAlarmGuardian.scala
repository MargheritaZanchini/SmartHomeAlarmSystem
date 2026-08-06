package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.{Behavior, *}
import smartHomeAlarm.smartHomeAlarmProtocol.*
import smartHomeAlarm.smartHomeAlarmProtocol.zones.{Garden, GroundFloor, UpperFloor}

import java.util.UUID
import scala.concurrent.duration.DurationInt

object SmartHomeAlarmGuardian:

  //spawn actors e set up

  enum Command:
    case DetectedMovement(event: MotionDetected)
    case InputEntered(event: TryInput)
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

  private val sensorForZone: Map [UUID, zones] = Map(UUIDLivingRoom -> GroundFloor,
    UUIDWindow1 -> GroundFloor,
    UUIDWindow2 -> UpperFloor,
    UUIDDoor -> Garden)



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

      val inputAdapter = context.messageAdapter[TryInput](InputEntered.apply)
      val userInteraction = context.spawn(UserInteraction(inputAdapter), "keyboardPin")

      disarmed(context, alarm)

  private def disarmed(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                      ): Behavior[Command] =
    Behaviors.receive: (contexta, message) =>
      message match

        //se il pin giusto viene inserito l'utente seleziona la modalità
        case InputEntered(TryInput(triedPin)) =>
          if triedPin.equals(pin) then
            context.log.info("Pin Correct. \nChoose modality: \n1: FullMode \n2: NighMode \n3: DayMode")
            chooseModality(context, alarm)
          else {
            context.log.warn("Incorrect PIN. Retry.")
            Behaviors.same
          }
        case _ => Behaviors.same

  private def chooseModality(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                            ): Behavior[Command] = {
    Behaviors.receive: (contexta, message) =>
      //l'utente sceglie una modalità
      message match
        case InputEntered(TryInput(triedInput)) =>
          triedInput match
            case "1" =>
              context.log.info("FullMode selcted. Going to Exit Delay.")
              exitDelay(context, alarm, modes.FullMode.activeZones)
            case "2" =>
              context.log.info("NightMode selcted. Going to Exit Delay.")
              exitDelay(context, alarm, modes.NightMode.activeZones)
            case "3" =>
              context.log.info("DayMode selcted. Going to Exit Delay.")
              exitDelay(context, alarm, modes.DayMode.activeZones)
        case _ =>
          context.log.warn("Incorrect Number. " +
            "\nChoose modality: \n1: FullMode \n2: NighMode \n3: DayMode")
          Behaviors.same
  }

  private def exitDelay(context: ActorContext[Command], alarm: ActorRef[Alarm.Command],
                        activeMode: List[zones]
                       ): Behavior[Command] = {
    //viene iniziato un timer in cui i sensori non triggherano nulla per permettere all'utente di uscire di casa
    Behaviors.withTimers { timers =>
      timers.startSingleTimer(exitTimerKey, FinishTimer(), varExitDelay)

      Behaviors.receiveMessagePartial:
        case FinishTimer() =>
          context.log.info("Exit delay finished. Going to armed.")
          armed(context, alarm, activeMode)
        case InputEntered(_) => Behaviors.same
        case Command.DetectedMovement(_) => Behaviors.same
        case _ => Behaviors.same
    }
  }


  private def armed(context: ActorContext[Command], alarm: ActorRef[Alarm.Command],
                    activeMode: List[zones]
                   ): Behavior[Command] =
    Behaviors.receiveMessagePartial:
      //se vengono rilevati movimenti nella zona attiva selezionati andiamo in entry delay
      case Command.DetectedMovement(MotionDetected(sensorID)) =>
        if activeMode.contains(sensorForZone(sensorID)) then
          context.log.info("Going to entry delay.")
          alarm ! Alarm.TurnOn()
          entryDelay(context, alarm)
        else
          Behaviors.same
      case InputEntered(_) => Behaviors.same
      case _ => Behaviors.same


  private def entryDelay(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                        ): Behavior[Command] = {

    //se l'utente inserisce il pin entro il timer l'allarme non suona
    Behaviors.receiveMessagePartial:
      case InputEntered(TryInput(triedPin)) =>
        if triedPin.equals(pin) then {
          context.log.info("PIN correct. Turning off the alarm and going to disarmed.")
          alarm ! Alarm.Off() //diciamo all'allarme di fermarsi
          disarmed(context, alarm)
        } else {
          context.log.warn("incorrect PIN. Retry.")
          Behaviors.same
        }

      case AlarmStarted(_) =>
        context.log.error("going to emergency.")
        emergency(context, alarm)

      case Command.DetectedMovement(_) =>
        Behaviors.same

      case _ =>
        Behaviors.same
  }


  private def emergency(context: ActorContext[Command], alarm: ActorRef[Alarm.Command]
                       ): Behavior[Command] =
    context.log.warn("RING RING RING!")
    Behaviors.receiveMessagePartial:

      case InputEntered(TryInput(triedPin))  =>
        if triedPin.equals(pin) then {
          context.log.info("PIN correct. Emergency stopped. Going to disarmed.")
          alarm ! Alarm.Off() //diciamo all'allarme di fermarsi
          disarmed(context, alarm)
        } else {
          context.log.warn("incorrect PIN. RING RING RING! Retry.")
          Behaviors.same
        }

      case _ => Behaviors.same