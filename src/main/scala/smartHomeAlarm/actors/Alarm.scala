package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import org.apache.pekko.actor.typed.receptionist.{Receptionist, ServiceKey}

import scala.concurrent.duration.*

object Alarm:

  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case Off()
    case TurnOn()
    case EntryTimeout()
  export Command.*

  val alarmServiceKey = ServiceKey[Command]("alarm")
  private val IntervalDelaySeconds = 15.seconds
  private case object SensorTimerKey

  def apply(): Behavior[Command] =
    Behaviors.withTimers: timers =>
      Behaviors.setup: context =>
        val router = context.spawn(Routers.group(SmartHomeAlarmGuardian.guardianAlarmServiceKey), "guardian")
        context.system.receptionist ! Receptionist.Register(alarmServiceKey, context.self)
        active(timers, router)
  

  private def active(timers: TimerScheduler[Command], router: ActorRef[AlarmStarting]): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        //disattivazione
        case Off() =>
          context.log.info("Alarm has been stopped")
          timers.cancel(SensorTimerKey) //annulla il countdown se ancora attivo
          Behaviors.same

        //attivazione
        case TurnOn() =>
          context.log.info("Starting alarm countdown: {} seconds", IntervalDelaySeconds.toSeconds)

          //programma il timer mandando a se stesso il messaggio privato EntryTimeout(replyTo)
          timers.startSingleTimer(
            SensorTimerKey,
            EntryTimeout(), //messaggio interno per il timer
            IntervalDelaySeconds
          )
          Behaviors.same

        //timer scaduto, l'attore riceve il messaggio da se stesso
        case EntryTimeout() =>
          context.log.warn("Entry delay expired! Triggering alarm signal...")
          router ! AlarmStarting() //invia la notifica all'esterno
          Behaviors.same