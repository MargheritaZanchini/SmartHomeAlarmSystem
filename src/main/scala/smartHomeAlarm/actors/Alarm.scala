package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import scala.concurrent.duration.*

object Alarm:

  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case Off()
    case TurnOn()
    case EntryTimeout()
  export Command.*

  private val IntervalDelaySeconds = 15.seconds
  private case object SensorTimerKey

  def apply(replyTo: ActorRef[AlarmStarting]): Behavior[Command] =
    Behaviors.withTimers: timers =>
      active(timers, replyTo)

  private def active(timers: TimerScheduler[Command], replyTo: ActorRef[AlarmStarting]): Behavior[Command] =
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
          replyTo ! AlarmStarting() //invia la notifica all'esterno
          Behaviors.same