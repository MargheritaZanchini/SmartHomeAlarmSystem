package smartHomeAlarm.actors

import org.apache.pekko.actor.typed.scaladsl.*
import org.apache.pekko.actor.typed.*
import scala.concurrent.duration.*

object Alarm:

  import smartHomeAlarm.smartHomeAlarmProtocol.*

  enum Command:
    case Off(replyTo: ActorRef[AlarmStarting])
    case TurnOn(replyTo: ActorRef[AlarmStarting])
    case EntryTimeout(replyTo: ActorRef[AlarmStarting])
  export Command.*

  private val IntervalDelaySeconds = 15.seconds
  private case object SensorTimerKey

  def apply(): Behavior[Command] =
    Behaviors.withTimers: timers =>
      active(timers)

  private def active(timers: TimerScheduler[Command]): Behavior[Command] =
    Behaviors.receive: (context, message) =>
      message match
        // Caso 1: Riceve il comando di disattivazione
        case Off(replyTo) =>
          context.log.info("Alarm has been stopped")
          timers.cancel(SensorTimerKey) // Annulla il countdown se ancora attivo
          Behaviors.same

        // Caso 2: Riceve il comando di attivazione
        case TurnOn(replyTo) =>
          context.log.info("Starting alarm countdown: {} seconds", IntervalDelaySeconds.toSeconds)

          // Programma il timer mandando a SE STESSO il messaggio privato EntryTimeout(replyTo)
          timers.startSingleTimer(
            SensorTimerKey,
            EntryTimeout(replyTo), // <--- Messaggio interno per il timer
            IntervalDelaySeconds
          )
          Behaviors.same

        // Caso 3: Il timer è scaduto! L'attore riceve il messaggio da se stesso
        case EntryTimeout(replyTo) =>
          context.log.warn("Entry delay expired! Triggering alarm signal...")
          replyTo ! AlarmStarting() // Ora invia la notifica all'esterno
          Behaviors.same